package com.sydigit.yzwater.module.system.framework.sms.core.client.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sydigit.yzwater.framework.common.core.KeyValue;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import com.sydigit.yzwater.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import com.sydigit.yzwater.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import com.sydigit.yzwater.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import com.sydigit.yzwater.module.system.framework.sms.core.property.SmsChannelProperties;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 飞鸽短信客户端实现
 *
 * 说明：签名规则依赖飞鸽官方文档，如有差异需按官方规范调整。
 */
@Slf4j
public class FeigeSmsClient extends AbstractSmsClient {

    private static final String SEND_TEMPLATE_URL = "https://api.feige.cn/sendsms/template/send";
    private static final String TEMPLATE_INFO_URL = "https://api.feige.cn/openapi/template/info";

    private static final String RESPONSE_CODE_SUCCESS = "0";

    public FeigeSmsClient(SmsChannelProperties properties) {
        super(properties);
        Assert.notEmpty(properties.getApiKey(), "apiKey 不能为空");
        Assert.notEmpty(properties.getApiSecret(), "apiSecret 不能为空");
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId,
                                  List<KeyValue<String, Object>> templateParams) throws Throwable {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("template_id", parseTemplateId(apiTemplateId));
        body.put("messages", buildMessages(mobile, templateParams));
        body.put("user_data", String.valueOf(sendLogId));
        JSONObject response = postJson(SEND_TEMPLATE_URL, body);

        String code = response.getStr("code");
        String msg = response.getStr("msg");
        String batchId = response.getStr("batch_id");
        boolean success = Objects.equals(RESPONSE_CODE_SUCCESS, code);
        return new SmsSendRespDTO()
                .setSuccess(success)
                .setSerialNo(batchId)
                .setApiCode(code)
                .setApiMsg(msg);
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        JSONObject root = JSONUtil.parseObj(text);
        JSONArray data = root.getJSONArray("data");
        if (CollUtil.isEmpty(data)) {
            return new ArrayList<>();
        }
        return convertList(data, item -> {
            JSONObject itemObj = (JSONObject) item;
            String status = itemObj.getStr("status");
            return new SmsReceiveRespDTO()
                    .setSuccess("DELIVRD".equals(status))
                    .setErrorCode(status)
                    .setErrorMsg(itemObj.getStr("status_desc"))
                    .setMobile(itemObj.getStr("phone_number"))
                    .setReceiveTime(parseReceiveTime(itemObj.get("time")))
                    .setSerialNo(String.valueOf(itemObj.get("batch_id")))
                    .setLogId(parseLogId(itemObj.get("user_data")));
        });
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) throws Throwable {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("id", parseTemplateId(apiTemplateId));
        body.put("page", 1);
        body.put("page_size", 1);
        JSONObject response = postJson(TEMPLATE_INFO_URL, body);

        String code = response.getStr("code");
        if (!Objects.equals(RESPONSE_CODE_SUCCESS, code)) {
            log.error("[getSmsTemplate][模板编号({}) 响应异常({})]", apiTemplateId, response);
            return null;
        }
        JSONObject data = response.getJSONObject("data");
        if (data == null) {
            return null;
        }
        JSONArray list = data.getJSONArray("list");
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        JSONObject item = list.getJSONObject(0);
        Integer auditStatus = item.getInt("audit_status");
        return new SmsTemplateRespDTO()
                .setId(String.valueOf(item.get("id")))
                .setContent(item.getStr("content"))
                .setAuditStatus(convertSmsTemplateAuditStatus(auditStatus))
                .setAuditReason(resolveAuditReason(item));
    }

    private JSONObject postJson(String url, LinkedHashMap<String, Object> body) {
        String bodyJson = JSONUtil.toJsonStr(body);
        Map<String, String> headers = buildHeaders();
        String responseBody = HttpUtils.post(url, headers, bodyJson);
        return JSONUtil.parseObj(responseBody);
    }

    private Map<String, String> buildHeaders() {
        String apiKey = properties.getApiKey();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = IdUtil.fastSimpleUUID();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Api-Key", apiKey);
        headers.put("X-Timestamp", timestamp);
        headers.put("X-Nonce", nonce);
        headers.put("X-Sign", sign(apiKey, timestamp, nonce));
        return headers;
    }

    /**
     * 签名计算：X-Api-Key + Secret + X-Timestamp + X-Nonce 进行 SHA256（小写）
     */
    private String sign(String apiKey, String timestamp, String nonce) {
        String signContent = apiKey + properties.getApiSecret() + timestamp + nonce;
        return DigestUtil.sha256Hex(signContent).toLowerCase();
    }

    private List<Map<String, Object>> buildMessages(String mobile, List<KeyValue<String, Object>> templateParams) {
        Map<String, Object> params = templateParams.stream()
                .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue, (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
        LinkedHashMap<String, Object> message = new LinkedHashMap<>();
        message.put("phone_number", mobile);
        message.put("params", params);
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);
        return messages;
    }

    private Object parseTemplateId(String apiTemplateId) {
        if (StrUtil.isNumeric(apiTemplateId)) {
            return Integer.parseInt(apiTemplateId);
        }
        return apiTemplateId;
    }

    private Long parseLogId(Object userData) {
        if (userData == null) {
            return null;
        }
        if (userData instanceof Number) {
            return ((Number) userData).longValue();
        }
        String value = String.valueOf(userData);
        if (StrUtil.isNumeric(value)) {
            return Long.valueOf(value);
        }
        return null;
    }

    private LocalDateTime parseReceiveTime(Object time) {
        if (time == null) {
            return null;
        }
        Long timestamp = null;
        if (time instanceof Number) {
            timestamp = ((Number) time).longValue();
        } else if (StrUtil.isNumeric(String.valueOf(time))) {
            timestamp = Long.parseLong(String.valueOf(time));
        }
        if (timestamp == null) {
            return null;
        }
        if (timestamp > 1_000_000_000_000L) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    @VisibleForTesting
    Integer convertSmsTemplateAuditStatus(Integer auditStatus) {
        if (auditStatus == null) {
            return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
        }
        switch (auditStatus) {
            case 0:
            case 1:
                return SmsTemplateAuditStatusEnum.CHECKING.getStatus();
            case 2:
                return SmsTemplateAuditStatusEnum.SUCCESS.getStatus();
            case 3:
                return SmsTemplateAuditStatusEnum.FAIL.getStatus();
            default:
                throw new IllegalArgumentException(String.format("未知审核状态(%d)", auditStatus));
        }
    }

    private String resolveAuditReason(JSONObject item) {
        String[] keys = new String[]{"audit_reason", "audit_msg", "reason", "remark"};
        for (String key : keys) {
            String value = item.getStr(key);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
