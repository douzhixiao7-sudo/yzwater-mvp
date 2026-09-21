package com.sydigit.yzwater.module.system.framework.sms.core.client.impl;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.core.KeyValue;
import com.sydigit.yzwater.framework.common.util.collection.MapUtils;
import com.sydigit.yzwater.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import com.sydigit.yzwater.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import com.sydigit.yzwater.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import com.sydigit.yzwater.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import com.sydigit.yzwater.module.system.framework.sms.core.property.SmsChannelProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 控制台模拟短信：不调用任何外部短信平台，只把内容打到日志。
 * 交付环境使用，避免产生短信费用。
 */
@Slf4j
public class DebugConsoleSmsClient extends AbstractSmsClient {

    public DebugConsoleSmsClient(SmsChannelProperties properties) {
        super(properties);
    }

    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile,
                                  String apiTemplateId, List<KeyValue<String, Object>> templateParams) {
        Map<String, Object> params = MapUtils.convertMap(templateParams);
        String serialNo = StrUtil.uuid();
        log.info("[DEBUG_CONSOLE 模拟短信] mobile={}, sendLogId={}, apiTemplateId={}, params={}, serialNo={}",
                mobile, sendLogId, apiTemplateId, params, serialNo);
        return new SmsSendRespDTO()
                .setSuccess(true)
                .setSerialNo(serialNo)
                .setApiCode("0")
                .setApiMsg("DEBUG_CONSOLE");
    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        throw new UnsupportedOperationException("控制台模拟短信无需解析回调");
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) {
        return new SmsTemplateRespDTO().setId(apiTemplateId).setContent("")
                .setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason("");
    }

}
