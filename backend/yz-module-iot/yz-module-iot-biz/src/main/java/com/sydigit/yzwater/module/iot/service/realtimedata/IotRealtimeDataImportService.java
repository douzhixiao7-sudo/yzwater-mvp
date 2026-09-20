package com.sydigit.yzwater.module.iot.service.realtimedata;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.tenant.core.context.TenantContextHolder;
import com.sydigit.yzwater.framework.tenant.core.util.TenantUtils;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.imports.IotRealtimeDataImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.framework.realtimedata.config.IotRealtimeDataProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 实时数据配置导入 Service
 */
@Service
@Validated
public class IotRealtimeDataImportService {

    private static final String DEFAULT_SOURCE_NAME = "潘家河";
    private static final String DEFAULT_SOURCE_CODE = "PANJIAHE";
    private static final String DEFAULT_CRON = "0 */5 * * * ?";

    @Resource
    private IotRealtimeDataProperties properties;
    @Resource
    private IotRealtimeDataSourceService sourceService;
    @Resource
    private IotRealtimeDataMappingService mappingService;

    @Transactional(rollbackFor = Exception.class)
    public IotRealtimeDataImportRespVO importFromProperties(String sourceName) {
        validateProperties();
        List<IotRealtimeDataProperties.PointMapping> mappings = properties.getMappings();
        if (CollUtil.isEmpty(mappings)) {
            throw exception(REALTIME_DATA_IMPORT_MAPPING_EMPTY);
        }

        Long tenantId = resolveTenantId(mappings);
        String finalSourceName = StrUtil.blankToDefault(sourceName, DEFAULT_SOURCE_NAME);

        AtomicReference<IotRealtimeDataImportRespVO> resultRef = new AtomicReference<>();
        TenantUtils.execute(tenantId, () -> {
            Long sourceId = createSource(finalSourceName);
            int mappingCount = createMappings(sourceId, mappings);
            IotRealtimeDataImportRespVO respVO = new IotRealtimeDataImportRespVO();
            respVO.setSourceId(sourceId);
            respVO.setSourceName(finalSourceName);
            respVO.setMappingCount(mappingCount);
            resultRef.set(respVO);
        });
        return resultRef.get();
    }

    private void validateProperties() {
        if (properties == null
                || StrUtil.isBlank(properties.getUrl())
                || StrUtil.isBlank(properties.getUsername())
                || StrUtil.isBlank(properties.getPassword())) {
            throw exception(REALTIME_DATA_IMPORT_CONFIG_INVALID);
        }
    }

    private Long resolveTenantId(List<IotRealtimeDataProperties.PointMapping> mappings) {
        Set<Long> tenantIds = new HashSet<>();
        for (IotRealtimeDataProperties.PointMapping mapping : mappings) {
            if (mapping == null || mapping.getTenantId() == null) {
                continue;
            }
            tenantIds.add(mapping.getTenantId());
        }
        if (tenantIds.size() > 1) {
            throw exception(REALTIME_DATA_IMPORT_TENANT_MISMATCH);
        }
        if (tenantIds.isEmpty()) {
            return TenantContextHolder.getRequiredTenantId();
        }
        return tenantIds.iterator().next();
    }

    private Long createSource(String sourceName) {
        IotRealtimeDataSourceSaveReqVO sourceReqVO = new IotRealtimeDataSourceSaveReqVO();
        sourceReqVO.setName(sourceName);
        sourceReqVO.setCode(DEFAULT_SOURCE_CODE);
        sourceReqVO.setEnabled(Boolean.TRUE.equals(properties.getEnabled()));
        sourceReqVO.setUrl(StrUtil.trimToEmpty(properties.getUrl()));
        sourceReqVO.setUsername(StrUtil.trimToEmpty(properties.getUsername()));
        sourceReqVO.setPassword(StrUtil.trimToEmpty(properties.getPassword()));
        String requestBody = StrUtil.trimToEmpty(properties.getRequestBody());
        sourceReqVO.setRequestBody(StrUtil.isBlank(requestBody) ? null : requestBody);
        String cron = StrUtil.trimToEmpty(properties.getCron());
        sourceReqVO.setCron(StrUtil.isBlank(cron) ? DEFAULT_CRON : cron);
        sourceReqVO.setRemark("从配置导入");
        return sourceService.createSource(sourceReqVO);
    }

    private int createMappings(Long sourceId, List<IotRealtimeDataProperties.PointMapping> mappings) {
        int count = 0;
        int sort = 0;
        for (IotRealtimeDataProperties.PointMapping mapping : mappings) {
            if (mapping == null || StrUtil.isBlank(mapping.getPointName())
                    || mapping.getDeviceId() == null || StrUtil.isBlank(mapping.getIdentifier())) {
                throw exception(REALTIME_DATA_IMPORT_MAPPING_INVALID,
                        mapping == null ? null : mapping.getPointName());
            }
            IotRealtimeDataMappingSaveReqVO mappingReqVO = new IotRealtimeDataMappingSaveReqVO();
            mappingReqVO.setSourceId(sourceId);
            mappingReqVO.setPointName(mapping.getPointName());
            mappingReqVO.setDeviceId(mapping.getDeviceId());
            mappingReqVO.setIdentifier(mapping.getIdentifier());
            mappingReqVO.setEnabled(true);
            mappingReqVO.setSort(sort++);
            mappingService.createMapping(mappingReqVO);
            count++;
        }
        return count;
    }

}