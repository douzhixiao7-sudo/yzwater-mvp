package com.sydigit.yzwater.module.service.pump;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.pump.YzPumpStationMapper;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 泵站信息服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class PumpStationService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final BigDecimal KW_PER_MW = BigDecimal.valueOf(1000);
    private static final int GEOM_SRID = 4490;
    private static final String FACILITY_TYPE_PUMP_STATION = "pump_station";
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final YzPumpStationMapper pumpStationMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 分页查询泵站信息（字典字段转换为标签）
     */
    public PageResult<PumpStationPageRespVO> getPumpStationPage(PumpStationPageReqVO reqVO) {
        LambdaQueryWrapper<YzPumpStationDO> wrapper = pumpStationMapper.buildQueryWrapper(reqVO);
        PageResult<YzPumpStationDO> page = pumpStationMapper.selectPage(reqVO, wrapper);
        Map<String, String> typeMap = loadDictLabelMap("zd_bzlx");
        List<PumpStationPageRespVO> list = page.getList().stream()
                .map(item -> buildPageResp(item, typeMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 导出泵站数据（不分页）
     */
    public List<PumpStationExportExcelVO> getPumpStationExportList(PumpStationPageReqVO reqVO) {
        LambdaQueryWrapper<YzPumpStationDO> wrapper = pumpStationMapper.buildQueryWrapper(reqVO);
        List<YzPumpStationDO> list = pumpStationMapper.selectList(wrapper);
        Map<String, String> typeMap = loadDictLabelMap("zd_bzlx");
        Map<String, String> gradeMap = loadDictLabelMap("zd_gcdb");
        Map<String, String> deptMap = loadDictLabelMap("zd_gldw");
        return list.stream()
                .map(item -> buildExportExcelVO(item, typeMap, gradeMap, deptMap))
                .collect(Collectors.toList());
    }

    /**
     * 获取泵站详情（用于新增/编辑回显）
     */
    public PumpStationSaveReqVO getPumpStationDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("泵站ID不能为空");
        }
        YzPumpStationDO exists = pumpStationMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_070, "泵站不存在或已被删除");
        }
        PumpStationSaveReqVO vo = new PumpStationSaveReqVO();
        vo.setId(exists.getId());
        vo.setPumpStationCode(exists.getPumpStationCode());
        vo.setPumpStationName(exists.getPumpStationName());
        vo.setDivisionCode(toStringList(exists.getDivisionCode()));
        vo.setPumpStationType(exists.getPumpStationType());
        vo.setPumpStationPosition(exists.getPumpStationPosition());
        vo.setLongitude(exists.getLongitude());
        vo.setLatitude(exists.getLatitude());
        vo.setInstalledCapacityKw(toKw(exists.getInstalledCapacity()));
        vo.setCapacityFlow(exists.getCapacityFlow());
        vo.setUnitCount(exists.getUnitCount());
        vo.setNormalWaterLevel(exists.getNormalWaterLevel());
        vo.setPreDropWaterLevel(exists.getPreDropWaterLevel());
        vo.setMinimumOperatingWaterLevel(exists.getMinimumOperatingWaterLevel());
        vo.setSingleUnitPower(exists.getSingleUnitPower());
        vo.setSelfFlow(exists.getSelfFlow());
        vo.setInstalledFlow(exists.getInstalledFlow());
        vo.setPumpingFlow(exists.getPumpingFlow());
        vo.setEngineeringGrade(exists.getEngineeringGrade());
        vo.setEngineeringScale(exists.getEngineeringScale());
        vo.setFloodControlDesignStandard(exists.getFloodControlDesignStandard());
        vo.setConstructionTime(exists.getConstructionTime());
        vo.setManagementDepartment(toStringList(exists.getManagementDepartment()));
        vo.setPumpStationImages(toStringList(exists.getPumpStationImages()));
        vo.setPumpStationOverview(exists.getPumpStationOverview());
        return vo;
    }

    /**
     * 根据基础设施ID获取泵站详情
     */
    public PumpStationSaveReqVO getPumpStationDetailByFacilityId(Long facilityId) {
        if (facilityId == null) {
            throw ServiceExceptionUtil.invalidParamException("设施ID不能为空");
        }
        YzPumpStationDO exists = pumpStationMapper.selectOne(new LambdaQueryWrapper<YzPumpStationDO>()
                .eq(YzPumpStationDO::getFacilityId, facilityId)
                .last("limit 1"));
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_070, "泵站不存在或已被删除");
        }
        PumpStationSaveReqVO vo = new PumpStationSaveReqVO();
        vo.setId(exists.getId());
        vo.setPumpStationCode(exists.getPumpStationCode());
        vo.setPumpStationName(exists.getPumpStationName());
        vo.setDivisionCode(toStringList(exists.getDivisionCode()));
        vo.setPumpStationType(exists.getPumpStationType());
        vo.setPumpStationPosition(exists.getPumpStationPosition());
        vo.setLongitude(exists.getLongitude());
        vo.setLatitude(exists.getLatitude());
        vo.setInstalledCapacityKw(toKw(exists.getInstalledCapacity()));
        vo.setCapacityFlow(exists.getCapacityFlow());
        vo.setUnitCount(exists.getUnitCount());
        vo.setNormalWaterLevel(exists.getNormalWaterLevel());
        vo.setPreDropWaterLevel(exists.getPreDropWaterLevel());
        vo.setMinimumOperatingWaterLevel(exists.getMinimumOperatingWaterLevel());
        vo.setSingleUnitPower(exists.getSingleUnitPower());
        vo.setSelfFlow(exists.getSelfFlow());
        vo.setInstalledFlow(exists.getInstalledFlow());
        vo.setPumpingFlow(exists.getPumpingFlow());
        vo.setEngineeringGrade(exists.getEngineeringGrade());
        vo.setEngineeringScale(exists.getEngineeringScale());
        vo.setFloodControlDesignStandard(exists.getFloodControlDesignStandard());
        vo.setConstructionTime(exists.getConstructionTime());
        vo.setManagementDepartment(toStringList(exists.getManagementDepartment()));
        vo.setPumpStationImages(toStringList(exists.getPumpStationImages()));
        vo.setPumpStationOverview(exists.getPumpStationOverview());
        return vo;
    }

    /**
     * 新增泵站
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPumpStation(PumpStationSaveReqVO reqVO) {
        Long facilityId = createFacilityBase(reqVO);
        String finalCode = resolveFacilityCode(reqVO.getPumpStationCode(), null, facilityId);

        YzPumpStationDO pumpStation = new YzPumpStationDO();
        pumpStation.setId(SNOWFLAKE.nextId());
        pumpStation.setFacilityId(facilityId);
        pumpStation.setPumpStationCode(finalCode);
        pumpStation.setPumpStationName(reqVO.getPumpStationName());
        pumpStation.setPumpStationType(reqVO.getPumpStationType());
        pumpStation.setDivisionCode(toStringArray(reqVO.getDivisionCode()));
        pumpStation.setPumpStationPosition(reqVO.getPumpStationPosition());
        pumpStation.setLongitude(reqVO.getLongitude());
        pumpStation.setLatitude(reqVO.getLatitude());
        pumpStation.setEngineeringGrade(reqVO.getEngineeringGrade());
        pumpStation.setEngineeringScale(reqVO.getEngineeringScale());
        pumpStation.setFloodControlDesignStandard(reqVO.getFloodControlDesignStandard());
        pumpStation.setInstalledCapacity(toMw(reqVO.getInstalledCapacityKw()));
        pumpStation.setCapacityFlow(reqVO.getCapacityFlow());
        pumpStation.setUnitCount(reqVO.getUnitCount());
        pumpStation.setNormalWaterLevel(reqVO.getNormalWaterLevel());
        pumpStation.setPreDropWaterLevel(reqVO.getPreDropWaterLevel());
        pumpStation.setMinimumOperatingWaterLevel(reqVO.getMinimumOperatingWaterLevel());
        pumpStation.setSingleUnitPower(reqVO.getSingleUnitPower());
        pumpStation.setSelfFlow(reqVO.getSelfFlow());
        pumpStation.setInstalledFlow(reqVO.getInstalledFlow());
        pumpStation.setPumpingFlow(reqVO.getPumpingFlow());
        pumpStation.setConstructionTime(reqVO.getConstructionTime());
        pumpStation.setManagementDepartment(toStringArray(reqVO.getManagementDepartment()));
        pumpStation.setPumpStationImages(validateAndConvertImages(reqVO.getPumpStationImages()));
        pumpStation.setPumpStationOverview(reqVO.getPumpStationOverview());
        pumpStationMapper.insert(pumpStation);
        return pumpStation.getId();
    }

    /**
     * 编辑泵站
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePumpStation(PumpStationSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.invalidParamException("泵站ID不能为空");
        }
        YzPumpStationDO exists = pumpStationMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_070, "泵站不存在或已被删除");
        }
        Long facilityId = ensureFacilityBase(reqVO, exists.getFacilityId());
        LocalDateTime now = LocalDateTime.now();

        YzPumpStationDO update = new YzPumpStationDO();
        update.setId(exists.getId());
        update.setFacilityId(facilityId);
        update.setPumpStationCode(StrUtil.trimToNull(reqVO.getPumpStationCode()));
        update.setPumpStationName(reqVO.getPumpStationName());
        update.setPumpStationType(StrUtil.trimToNull(reqVO.getPumpStationType()));
        update.setDivisionCode(toStringArray(reqVO.getDivisionCode()));
        update.setPumpStationPosition(StrUtil.trimToNull(reqVO.getPumpStationPosition()));
        update.setLongitude(reqVO.getLongitude());
        update.setLatitude(reqVO.getLatitude());
        update.setEngineeringGrade(StrUtil.trimToNull(reqVO.getEngineeringGrade()));
        update.setEngineeringScale(StrUtil.trimToNull(reqVO.getEngineeringScale()));
        update.setFloodControlDesignStandard(StrUtil.trimToNull(reqVO.getFloodControlDesignStandard()));
        update.setInstalledCapacity(toMw(reqVO.getInstalledCapacityKw()));
        update.setCapacityFlow(reqVO.getCapacityFlow());
        update.setUnitCount(reqVO.getUnitCount());
        update.setNormalWaterLevel(reqVO.getNormalWaterLevel());
        update.setPreDropWaterLevel(reqVO.getPreDropWaterLevel());
        update.setMinimumOperatingWaterLevel(reqVO.getMinimumOperatingWaterLevel());
        update.setSingleUnitPower(reqVO.getSingleUnitPower());
        update.setSelfFlow(reqVO.getSelfFlow());
        update.setInstalledFlow(reqVO.getInstalledFlow());
        update.setPumpingFlow(reqVO.getPumpingFlow());
        update.setConstructionTime(reqVO.getConstructionTime());
        update.setManagementDepartment(toStringArray(reqVO.getManagementDepartment()));
        update.setPumpStationImages(validateAndConvertImages(reqVO.getPumpStationImages()));
        update.setPumpStationOverview(StrUtil.trimToNull(reqVO.getPumpStationOverview()));
        update.setUpdateTime(now);
        pumpStationMapper.updateById(update);
        applyExplicitNullUpdate(exists.getId(), update, now);
    }

    /**
     * 删除泵站
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePumpStation(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("泵站ID不能为空");
        }
        YzPumpStationDO exists = pumpStationMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_070, "泵站不存在或已被删除");
        }
        pumpStationMapper.deleteById(id);
        if (exists.getFacilityId() != null) {
            facilityBaseMapper.deleteById(exists.getFacilityId());
        }
    }

    private PumpStationPageRespVO buildPageResp(YzPumpStationDO item, Map<String, String> typeMap) {
        PumpStationPageRespVO resp = new PumpStationPageRespVO();
        resp.setId(item.getId());
        resp.setPumpStationName(item.getPumpStationName());
        resp.setPumpStationCode(item.getPumpStationCode());
        resp.setPumpStationTypeLabel(resolveLabel(item.getPumpStationType(), typeMap));
        resp.setDivisionCode(toStringList(item.getDivisionCode()));
        resp.setLongitude(item.getLongitude());
        resp.setLatitude(item.getLatitude());
        resp.setInstalledCapacityKw(toKw(item.getInstalledCapacity()));
        resp.setCapacityFlow(item.getCapacityFlow());
        resp.setUnitCount(item.getUnitCount());
        resp.setSelfFlow(item.getSelfFlow());
        resp.setInstalledFlow(item.getInstalledFlow());
        resp.setPumpingFlow(item.getPumpingFlow());
        resp.setNormalWaterLevel(item.getNormalWaterLevel());
        resp.setPreDropWaterLevel(item.getPreDropWaterLevel());
        resp.setMinimumOperatingWaterLevel(item.getMinimumOperatingWaterLevel());
        resp.setSingleUnitPower(item.getSingleUnitPower());
        resp.setPumpStationOverview(item.getPumpStationOverview());
        return resp;
    }

    private PumpStationExportExcelVO buildExportExcelVO(YzPumpStationDO item,
                                                       Map<String, String> typeMap,
                                                       Map<String, String> gradeMap,
                                                       Map<String, String> deptMap) {
        PumpStationExportExcelVO vo = new PumpStationExportExcelVO();
        vo.setPumpStationName(item.getPumpStationName());
        vo.setPumpStationTypeLabel(resolveLabel(item.getPumpStationType(), typeMap));
        vo.setEngineeringGradeLabel(resolveLabel(item.getEngineeringGrade(), gradeMap));
        vo.setInstalledCapacityKw(toKw(item.getInstalledCapacity()));
        vo.setPumpStationPosition(item.getPumpStationPosition());
        vo.setManagementDepartmentLabel(String.join("、", resolveLabels(item.getManagementDepartment(), deptMap)));
        return vo;
    }

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        return StrUtil.blankToDefault(map.get(value), value);
    }

    private List<String> resolveLabels(String[] values, Map<String, String> map) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        return Arrays.stream(values)
                .filter(StrUtil::isNotBlank)
                .map(v -> resolveLabel(v, map))
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dict : list) {
            if (StrUtil.isBlank(dict.getValue())) {
                continue;
            }
            result.put(dict.getValue(), StrUtil.blankToDefault(dict.getLabel(), dict.getValue()));
        }
        return result;
    }

    private String[] toStringArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<String> normalized = list.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        return normalized.isEmpty() ? null : normalized.toArray(new String[0]);
    }

    private List<String> toStringList(String[] array) {
        if (array == null || array.length == 0) {
            return List.of();
        }
        return Arrays.stream(array)
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private String[] validateAndConvertImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        List<String> normalized = images.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (normalized.size() > 5) {
            throw ServiceExceptionUtil.invalidParamException("泵站图片最多只能上传 5 张");
        }
        return normalized.isEmpty() ? null : normalized.toArray(new String[0]);
    }

    /**
     * 创建泵站基础设施记录（设施基础表）
     */
    private Long createFacilityBase(PumpStationSaveReqVO reqVO) {
        Long baseId = SNOWFLAKE.nextId();
        String facilityCode = String.valueOf(baseId);
        Geometry geom = buildPointGeometry(reqVO.getLongitude(), reqVO.getLatitude());

        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(facilityCode);
        base.setFacilityName(reqVO.getPumpStationName());
        base.setFacilityType(FACILITY_TYPE_PUMP_STATION);
        base.setGeom(geom);
        base.setGeomType(geom != null ? geom.getGeometryType() : null);
        base.setSrid(geom != null ? GEOM_SRID : null);
        base.setSourceType("system");
        facilityBaseMapper.insert(base);
        return baseId;
    }

    /**
     * 确保基础设施存在并同步基础信息（名称、类型、编码、点位）
     */
    private Long ensureFacilityBase(PumpStationSaveReqVO reqVO, Long facilityId) {
        if (facilityId == null) {
            return createFacilityBase(reqVO);
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null) {
            return createFacilityBase(reqVO);
        }
        Geometry geom = buildPointGeometry(reqVO.getLongitude(), reqVO.getLatitude());
        base.setFacilityName(reqVO.getPumpStationName());
        base.setFacilityType(FACILITY_TYPE_PUMP_STATION);
        base.setGeom(geom);
        base.setGeomType(geom != null ? geom.getGeometryType() : null);
        base.setSrid(geom != null ? GEOM_SRID : null);
        facilityBaseMapper.updateById(base);
        if (geom == null) {
            facilityBaseMapper.update(null, new LambdaUpdateWrapper<YzWaterFacilityBaseDO>()
                    .eq(YzWaterFacilityBaseDO::getId, base.getId())
                    .set(YzWaterFacilityBaseDO::getUpdateTime, LocalDateTime.now())
                    .setSql("geom = NULL, geom_type = NULL, srid = NULL"));
        }
        return base.getId();
    }

    /**
     * updateById 默认不更新 null 字段，这里显式将被清空的字段写回为 NULL。
     */
    private void applyExplicitNullUpdate(Long id, YzPumpStationDO update, LocalDateTime now) {
        List<String> clearColumns = new ArrayList<>();
        if (update.getPumpStationCode() == null) {
            clearColumns.add("pump_station_code = NULL");
        }
        if (update.getDivisionCode() == null) {
            clearColumns.add("division_code = NULL");
        }
        if (update.getPumpStationType() == null) {
            clearColumns.add("pump_station_type = NULL");
        }
        if (update.getPumpStationPosition() == null) {
            clearColumns.add("pump_station_position = NULL");
        }
        if (update.getLongitude() == null) {
            clearColumns.add("longitude = NULL");
        }
        if (update.getLatitude() == null) {
            clearColumns.add("latitude = NULL");
        }
        if (update.getEngineeringGrade() == null) {
            clearColumns.add("engineering_grade = NULL");
        }
        if (update.getEngineeringScale() == null) {
            clearColumns.add("engineering_scale = NULL");
        }
        if (update.getFloodControlDesignStandard() == null) {
            clearColumns.add("flood_control_design_standard = NULL");
        }
        if (update.getInstalledCapacity() == null) {
            clearColumns.add("installed_capacity = NULL");
        }
        if (update.getCapacityFlow() == null) {
            clearColumns.add("capacity_flow = NULL");
        }
        if (update.getUnitCount() == null) {
            clearColumns.add("unit_count = NULL");
        }
        if (update.getNormalWaterLevel() == null) {
            clearColumns.add("normal_water_level = NULL");
        }
        if (update.getPreDropWaterLevel() == null) {
            clearColumns.add("pre_drop_water_level = NULL");
        }
        if (update.getMinimumOperatingWaterLevel() == null) {
            clearColumns.add("minimum_operating_water_level = NULL");
        }
        if (update.getSingleUnitPower() == null) {
            clearColumns.add("single_unit_power = NULL");
        }
        if (update.getSelfFlow() == null) {
            clearColumns.add("self_flow = NULL");
        }
        if (update.getInstalledFlow() == null) {
            clearColumns.add("installed_flow = NULL");
        }
        if (update.getPumpingFlow() == null) {
            clearColumns.add("pumping_flow = NULL");
        }
        if (update.getConstructionTime() == null) {
            clearColumns.add("construction_time = NULL");
        }
        if (update.getManagementDepartment() == null) {
            clearColumns.add("management_department = NULL");
        }
        if (update.getPumpStationImages() == null) {
            clearColumns.add("pump_station_images = NULL");
        }
        if (update.getPumpStationOverview() == null) {
            clearColumns.add("pump_station_overview = NULL");
        }
        if (clearColumns.isEmpty()) {
            return;
        }
        pumpStationMapper.update(null, new LambdaUpdateWrapper<YzPumpStationDO>()
                .eq(YzPumpStationDO::getId, id)
                .set(YzPumpStationDO::getUpdateTime, now)
                .setSql(String.join(", ", clearColumns)));
    }

    private String resolveFacilityCode(String preferred, String fallback, Long baseId) {
        String preferredClean = StrUtil.blankToDefault(preferred, "").trim();
        if (StrUtil.isNotBlank(preferredClean)) {
            return preferredClean;
        }
        String fallbackClean = StrUtil.blankToDefault(fallback, "").trim();
        if (StrUtil.isNotBlank(fallbackClean)) {
            return fallbackClean;
        }
        return String.valueOf(baseId);
    }

    private Geometry buildPointGeometry(BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        double lon = longitude.doubleValue();
        double lat = latitude.doubleValue();
        if (!Double.isFinite(lon) || !Double.isFinite(lat)) {
            return null;
        }
        Geometry point = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
        point.setSRID(GEOM_SRID);
        return point;
    }

    private BigDecimal toKw(BigDecimal mw) {
        if (mw == null) {
            return null;
        }
        return mw.multiply(KW_PER_MW).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal toMw(BigDecimal kw) {
        if (kw == null) {
            return null;
        }
        return kw.divide(KW_PER_MW, 6, RoundingMode.HALF_UP);
    }
}
