package com.sydigit.yzwater.framework.operatelog.core.service;

import com.sydigit.yzwater.framework.common.biz.system.logger.OperateLogCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.sydigit.yzwater.framework.common.util.monitor.TracerUtils;
import com.sydigit.yzwater.framework.common.util.servlet.ServletUtils;
import com.sydigit.yzwater.framework.security.core.LoginUser;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 操作日志 ILogRecordService 实现类
 *
 * 基于 {@link OperateLogCommonApi} 实现，记录操作日志
 *
 * @author HUIHUI
 */
@Slf4j
public class LogRecordServiceImpl implements ILogRecordService {

    /**
     * 默认用户编号：用于未登录/匿名请求触发操作日志的场景，避免 user_id 为空导致数据库 NOT NULL 约束异常。
     */
    private static final Long DEFAULT_USER_ID = 0L;

    /**
     * 默认用户类型：0 表示未知/匿名（兼容 system_operate_log.user_type 默认值）。
     */
    private static final Integer DEFAULT_USER_TYPE = 0;

    @Resource
    private OperateLogCommonApi operateLogApi;

    @Override
    public void record(LogRecord logRecord) {
        OperateLogCreateReqDTO reqDTO = new OperateLogCreateReqDTO();
        try {
            reqDTO.setTraceId(TracerUtils.getTraceId());
            // 补充用户信息
            fillUserFields(reqDTO);
            // 补全模块信息
            fillModuleFields(reqDTO, logRecord);
            // 补全请求信息
            fillRequestFields(reqDTO);

            // 2. 异步记录日志
            operateLogApi.createOperateLogAsync(reqDTO);
        } catch (Throwable ex) {
            // 由于 @Async 异步调用，这里打印下日志，更容易跟进
            log.error("[record][url({}) log({}) 发生异常]", reqDTO.getRequestUrl(), reqDTO, ex);
        }
    }

    private static void fillUserFields(OperateLogCreateReqDTO reqDTO) {
        // 使用 SecurityFrameworkUtils。因为要考虑，rpc、mq、job，它其实不是 web；
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            // 登录、注册等匿名接口也可能触发 @LogRecord（例如短信登录自动创建用户）
            // 此时没有登录态，user_id 需要给一个稳定的兜底值，避免插入 system_operate_log 失败
            reqDTO.setUserId(DEFAULT_USER_ID);
            reqDTO.setUserType(DEFAULT_USER_TYPE);
            return;
        }
        reqDTO.setUserId(loginUser.getId());
        reqDTO.setUserType(loginUser.getUserType());
    }

    public static void fillModuleFields(OperateLogCreateReqDTO reqDTO, LogRecord logRecord) {
        reqDTO.setType(logRecord.getType()); // 大模块类型，例如：CRM 客户
        reqDTO.setSubType(logRecord.getSubType());// 操作名称，例如：转移客户
        reqDTO.setBizId(Long.parseLong(logRecord.getBizNo())); // 业务编号，例如：客户编号
        reqDTO.setAction(logRecord.getAction());// 操作内容，例如：修改编号为 1 的用户信息，将性别从男改成女，将姓名从lijun改成源码。
        reqDTO.setExtra(logRecord.getExtra()); // 拓展字段，有些复杂的业务，需要记录一些字段 ( JSON 格式 )，例如说，记录订单编号，{ orderId: "1"}
    }

    private static void fillRequestFields(OperateLogCreateReqDTO reqDTO) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        reqDTO.setRequestMethod(request.getMethod());
        reqDTO.setRequestUrl(request.getRequestURI());
        reqDTO.setUserIp(ServletUtils.getClientIP(request));
        reqDTO.setUserAgent(ServletUtils.getUserAgent(request));
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        throw new UnsupportedOperationException("使用 OperateLogApi 进行操作日志的查询");
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        throw new UnsupportedOperationException("使用 OperateLogApi 进行操作日志的查询");
    }

}
