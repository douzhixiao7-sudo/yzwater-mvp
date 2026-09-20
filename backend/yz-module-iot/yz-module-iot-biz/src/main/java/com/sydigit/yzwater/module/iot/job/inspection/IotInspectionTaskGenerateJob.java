package com.sydigit.yzwater.module.iot.job.inspection;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.framework.tenant.core.job.TenantJob;
import com.sydigit.yzwater.module.iot.service.inspectionplan.IotInspectionPlanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Inspection plan auto task generation job.
 */
@Component("iotInspectionTaskGenerateJob")
@Slf4j
public class IotInspectionTaskGenerateJob implements JobHandler {

    private static final int DEFAULT_LIMIT = 100;

    @Resource
    private IotInspectionPlanService inspectionPlanService;

    @Override
    @TenantJob
    public String execute(String param) {
        int limit = parseLimit(param);
        int successCount = inspectionPlanService.generateDuePlanTasks(limit);
        log.info("[execute][inspection plan auto task generation done][limit={}, successCount={}]", limit, successCount);
        return StrUtil.format("inspection plan auto task generation done, success plans: {}", successCount);
    }

    private int parseLimit(String param) {
        if (StrUtil.isBlank(param)) {
            return DEFAULT_LIMIT;
        }
        try {
            int value = Integer.parseInt(param.trim());
            return value > 0 ? value : DEFAULT_LIMIT;
        } catch (NumberFormatException ex) {
            log.warn("[parseLimit][invalid param, fallback to default][param={}]", param);
            return DEFAULT_LIMIT;
        }
    }
}
