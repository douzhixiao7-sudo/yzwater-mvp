package com.sydigit.yzwater.module.iot.service.device.message;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.sydigit.yzwater.framework.common.exception.ServiceException;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.date.LocalDateTimeUtils;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.sydigit.yzwater.module.iot.core.mq.message.IotDeviceMessage;
import com.sydigit.yzwater.module.iot.core.mq.producer.IotDeviceMessageProducer;
import com.sydigit.yzwater.module.iot.core.util.IotDeviceMessageUtils;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import com.sydigit.yzwater.module.iot.dal.tdengine.IotDeviceMessageMapper;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyService;
import com.sydigit.yzwater.module.iot.service.ota.IotOtaTaskRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Objects;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL;

/**
 * IoT 设备消息 Service 实现类
 *
 * @author lijun
 */
@Service
@Validated
@Slf4j
public class IotDeviceMessageServiceImpl implements IotDeviceMessageService {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotOtaTaskRecordService otaTaskRecordService;

    @Resource
    private IotDeviceMessageMapper deviceMessageMapper;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @Override
    public void defineDeviceMessageStable() {
        if (StrUtil.isNotEmpty(deviceMessageMapper.showSTable())) {
            log.info("[defineDeviceMessageStable][设备消息超级表已存在，创建跳过]");
            return;
        }
        log.info("[defineDeviceMessageStable][设备消息超级表不存在，创建开始...]");
        deviceMessageMapper.createSTable();
        log.info("[defineDeviceMessageStable][设备消息超级表不存在，创建成功]");
    }

    @Async
    @Override
    public void createDeviceLogAsync(IotDeviceMessage message) {
        IotDeviceMessageDO messageDO = BeanUtils.toBean(message, IotDeviceMessageDO.class)
                .setUpstream(IotDeviceMessageUtils.isUpstreamMessage(message))
                .setReply(IotDeviceMessageUtils.isReplyMessage(message))
                .setIdentifier(IotDeviceMessageUtils.getIdentifier(message));
        // 统一处理基础字段的二进制值，避免驱动强转异常
        messageDO.setId(normalizeNchar(messageDO.getId()));
        messageDO.setServerId(normalizeNchar(messageDO.getServerId()));
        messageDO.setIdentifier(normalizeNchar(messageDO.getIdentifier()));
        messageDO.setRequestId(normalizeNchar(messageDO.getRequestId()));
        messageDO.setMethod(normalizeNchar(messageDO.getMethod()));
        messageDO.setMsg(normalizeNchar(messageDO.getMsg()));
        if (message.getReportTime() != null) {
            messageDO.setReportTime(LocalDateTimeUtil.toEpochMilli(message.getReportTime()));
        }
        messageDO.setParams(buildJsonText(message.getParams()));
        messageDO.setData(buildJsonText(message.getData()));
        deviceMessageMapper.insert(messageDO);
    }

    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message) {
        IotDeviceDO device = deviceService.validateDeviceExists(message.getDeviceId());
        return sendDeviceMessage(message, device);
    }

    // TODO @芋艿：针对连接网关的设备，是不是 productKey、deviceName 需要调整下；
    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        return sendDeviceMessage(message, device, null);
    }

    private IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId) {
        // 1. 补充信息
        appendDeviceMessage(message, device);

        // 2.1 情况一：发送上行消息
        boolean upstream = IotDeviceMessageUtils.isUpstreamMessage(message);
        if (upstream) {
            deviceMessageProducer.sendDeviceMessage(message);
            return message;
        }

        // 2.2 情况二：发送下行消息
        // 如果是下行消息，需要校验 serverId 存在
        // TODO 芋艿：【设计】下行消息需要区分 PUSH 和 PULL 模型
        // 1. PUSH 模型：适用于 MQTT 等长连接协议。通过 serverId 将消息路由到指定网关，实时推送。
        // 2. PULL 模型：适用于 HTTP 等短连接协议。设备无固定 serverId，无法主动推送。
        // 解决方案：
        // 当 serverId 不存在时，将下行消息存入“待拉取消息表”（例如 iot_device_pull_message）。
        // 设备端通过定时轮询一个新增的 API（例如 /iot/message/pull）来拉取属于自己的消息。
        if (StrUtil.isEmpty(serverId)) {
            serverId = devicePropertyService.getDeviceServerId(device.getId());
            if (StrUtil.isEmpty(serverId)) {
                throw exception(DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL);
            }
        }
        deviceMessageProducer.sendDeviceMessageToGateway(serverId, message);
        // 特殊：记录消息日志。原因：上行消息，消费时，已经会记录；下行消息，因为消费在 Gateway 端，所以需要在这里记录
        getSelf().createDeviceLogAsync(message);
        return message;
    }

    /**
     * 补充消息的后端字段
     *
     * @param message 消息
     * @param device  设备信息
     */
    private void appendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now())
                .setDeviceId(device.getId()).setTenantId(device.getTenantId());
        // 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }
    }

    @Override
    public void handleUpstreamDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 1. 处理消息
        Object replyData = null;
        ServiceException serviceException = null;
        try {
            replyData = handleUpstreamDeviceMessage0(message, device);
        } catch (ServiceException ex) {
            serviceException = ex;
            log.warn("[handleUpstreamDeviceMessage][message({}) 业务异常]", message, serviceException);
        } catch (Exception ex) {
            log.error("[handleUpstreamDeviceMessage][message({}) 发生异常]", message, ex);
            throw ex;
        }

        // 2. 记录消息
        getSelf().createDeviceLogAsync(message);

        // 3. 回复消息。前提：非 _reply 消息，并且非禁用回复的消息
        if (IotDeviceMessageUtils.isReplyMessage(message)
                || IotDeviceMessageMethodEnum.isReplyDisabled(message.getMethod())
                || StrUtil.isEmpty(message.getServerId())) {
            return;
        }
        try {
            IotDeviceMessage replyMessage = IotDeviceMessage.replyOf(message.getRequestId(), message.getMethod(), replyData,
                    serviceException != null ? serviceException.getCode() : null,
                    serviceException != null ? serviceException.getMessage() : null);
            sendDeviceMessage(replyMessage, device, message.getServerId());
        } catch (Exception ex) {
            log.error("[handleUpstreamDeviceMessage][message({}) 回复消息失败]", message, ex);
        }
    }

    // TODO @芋艿：可优化：未来逻辑复杂后，可以独立拆除 Processor 处理器
    @SuppressWarnings("SameReturnValue")
    private Object handleUpstreamDeviceMessage0(IotDeviceMessage message, IotDeviceDO device) {
        // 设备上下线
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            String stateStr = IotDeviceMessageUtils.getIdentifier(message);
            assert stateStr != null;
            Assert.notEmpty(stateStr, "设备状态不能为空");
            deviceService.updateDeviceState(device, Integer.valueOf(stateStr));
            // TODO 芋艿：子设备的关联
            return null;
        }

        // 属性上报
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())) {
            devicePropertyService.saveDeviceProperty(device, message);
            return null;
        }

        // OTA 上报升级进度
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.OTA_PROGRESS.getMethod())) {
            otaTaskRecordService.updateOtaRecordProgress(device, message);
            return null;
        }

        // TODO @芋艿：这里可以按需，添加别的逻辑；
        return null;
    }

    @Override
    public PageResult<IotDeviceMessageDO> getDeviceMessagePage(IotDeviceMessagePageReqVO pageReqVO) {
        try {
            IPage<IotDeviceMessageDO> page = deviceMessageMapper.selectPage(
                    new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
            return new PageResult<>(page.getRecords(), page.getTotal());
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<IotDeviceMessageDO> getDeviceMessageListByRequestIdsAndReply(Long deviceId,
                                                                             List<String> requestIds,
                                                                             Boolean reply) {
        return deviceMessageMapper.selectListByRequestIdsAndReply(deviceId, requestIds, reply);
    }

    @Override
    public Long getDeviceMessageCount(LocalDateTime createTime) {
        return deviceMessageMapper.selectCountByCreateTime(
                createTime != null ? LocalDateTimeUtil.toEpochMilli(createTime) : null);
    }

    @Override
    public List<IotStatisticsDeviceMessageSummaryByDateRespVO> getDeviceMessageSummaryByDate(
            IotStatisticsDeviceMessageReqVO reqVO) {
        // 1. 按小时统计，获取分项统计数据
        List<Map<String, Object>> countList = deviceMessageMapper.selectDeviceMessageCountGroupByDate(
                LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[0]),
                LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[1]));

        // 2. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1],
                reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Integer upstreamCount = countList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], (Timestamp) vo.get("time")))
                    .mapToInt(value -> MapUtil.getInt(value, "upstream_count")).sum();
            Integer downstreamCount = countList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], (Timestamp) vo.get("time")))
                    .mapToInt(value -> MapUtil.getInt(value, "downstream_count")).sum();
            return new IotStatisticsDeviceMessageSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setUpstreamCount(upstreamCount).setDownstreamCount(downstreamCount);
        });
    }

  /*  private IotDeviceMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }*/

    private IotDeviceMessageService getSelf() {
        return SpringUtil.getBean(IotDeviceMessageService.class);
    }


    /**
     * 将二进制值转为 UTF-8 字符串，避免中文乱码
     */
    private String normalizeString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return StrUtil.removePrefix((String) value, "\uFEFF");
        }
        if (value instanceof byte[]) {
            String text = new String((byte[]) value, StandardCharsets.UTF_8);
            return StrUtil.removePrefix(text, "\uFEFF");
        }
        if (value instanceof Byte[]) {
            Byte[] bytes = (Byte[]) value;
            byte[] raw = new byte[bytes.length];
            for (int index = 0; index < bytes.length; index++) {
                raw[index] = bytes[index];
            }
            String text = new String(raw, StandardCharsets.UTF_8);
            return StrUtil.removePrefix(text, "\uFEFF");
        }
        return Convert.toStr(value);
    }

    /**
     * 统一返回非空字符串，避免 TDengine 驱动在 NCHAR 上处理 null 异常
     */
    private String normalizeNchar(Object value) {
        String text = normalizeString(value);
        return text == null ? "" : text;
    }

    /**
     * 仅处理顶层二进制对象，避免驱动将 byte[] 强转为 String
     */
    private Object normalizeBinaryValue(Object value) {
        if (value instanceof byte[] || value instanceof Byte[]) {
            return normalizeString(value);
        }
        return value;
    }

    /**
     * 统一返回 JSON 字符串，避免 TDengine 驱动对二进制值强转异常
     */
    private String buildJsonText(Object value) {
        if (value == null) {
            return "";
        }
        Object normalized = normalizeBinaryValue(value);
        if (normalized instanceof CharSequence) {
            return normalizeNchar(normalized.toString());
        }
        return JsonUtils.toJsonString(normalized);
    }

}
