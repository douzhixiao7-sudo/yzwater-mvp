package com.sydigit.yzwater.module.iot.service.inspectionstandard;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionCheckResultConfigVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 巡检标准检查结果配置编解码工具。
 */
public final class IotInspectionCheckResultConfigHelper {

    /**
     * 兼容历史版本：旧版本将检查结果配置拼接到标准备注中。
     */
    private static final String CHECK_RESULT_CONFIG_MARKER = "\n@@IOT_INSPECTION_CHECK_RESULTS@@\n";

    private IotInspectionCheckResultConfigHelper() {
    }

    /**
     * 兼容历史版本：提取纯备注文案（去除检查结果配置串）。
     */
    public static String extractRemark(String mergedRemark) {
        String normalizedRemark = StrUtil.blankToDefault(mergedRemark, "");
        int markerIndex = normalizedRemark.indexOf(CHECK_RESULT_CONFIG_MARKER);
        if (markerIndex < 0) {
            return normalizedRemark;
        }
        return StrUtil.blankToDefault(trimToNull(normalizedRemark.substring(0, markerIndex)), "");
    }

    /**
     * 兼容历史版本：从标准备注中提取检查结果配置。
     */
    public static List<IotInspectionCheckResultConfigVO> extractLegacyConfigsFromRemark(String mergedRemark) {
        String normalizedRemark = StrUtil.blankToDefault(mergedRemark, "");
        int markerIndex = normalizedRemark.indexOf(CHECK_RESULT_CONFIG_MARKER);
        if (markerIndex < 0) {
            return Collections.emptyList();
        }
        String configJson = normalizedRemark.substring(markerIndex + CHECK_RESULT_CONFIG_MARKER.length()).trim();
        return fromJson(configJson);
    }

    /**
     * 将检查结果配置序列化为 JSON 字符串。
     */
    public static String toJson(List<IotInspectionCheckResultConfigVO> checkResultConfigs) {
        List<IotInspectionCheckResultConfigVO> normalizedConfigs = normalizeConfigs(checkResultConfigs);
        if (normalizedConfigs.isEmpty()) {
            return null;
        }
        return JsonUtils.toJsonString(normalizedConfigs);
    }

    /**
     * 从 JSON 字符串中解析检查结果配置。
     */
    public static List<IotInspectionCheckResultConfigVO> fromJson(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            return Collections.emptyList();
        }
        try {
            List<IotInspectionCheckResultConfigVO> configs = JsonUtils.parseArray(configJson, IotInspectionCheckResultConfigVO.class);
            return normalizeConfigs(configs);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * 规范化检查结果配置，过滤空值并清理空白。
     */
    private static List<IotInspectionCheckResultConfigVO> normalizeConfigs(List<IotInspectionCheckResultConfigVO> checkResultConfigs) {
        if (checkResultConfigs == null || checkResultConfigs.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotInspectionCheckResultConfigVO> normalizedConfigs = new ArrayList<>();
        for (IotInspectionCheckResultConfigVO config : checkResultConfigs) {
            if (config == null || StrUtil.isBlank(config.getValue())) {
                continue;
            }
            IotInspectionCheckResultConfigVO normalizedConfig = new IotInspectionCheckResultConfigVO();
            normalizedConfig.setValue(config.getValue().trim());
            normalizedConfig.setLabel(StrUtil.blankToDefault(trimToNull(config.getLabel()), config.getValue().trim()));
            normalizedConfig.setRemark(StrUtil.blankToDefault(trimToNull(config.getRemark()), ""));
            normalizedConfigs.add(normalizedConfig);
        }
        return normalizedConfigs;
    }

    private static String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }
}
