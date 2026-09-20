package com.sydigit.yzwater.module.service.irrigation;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.irrigation.YzIrrigationDistrictDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import lombok.RequiredArgsConstructor;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 灌区服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class IrrigationDistrictService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final int DEFAULT_SRID = 4490;
    private static final String FACILITY_TYPE_IRRIGATION = "irrigation";

    private final YzIrrigationDistrictMapper irrigationDistrictMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 分页查询
     */
    public PageResult<IrrigationDistrictPageRespVO> getPage(IrrigationDistrictPageReqVO reqVO) {
        LambdaQueryWrapper<YzIrrigationDistrictDO> wrapper = irrigationDistrictMapper.buildQueryWrapper(reqVO);
        PageResult<YzIrrigationDistrictDO> page = irrigationDistrictMapper.selectPage(reqVO, wrapper);
        List<IrrigationDistrictPageRespVO> list = page.getList().stream()
                .filter(Objects::nonNull)
                .map(this::buildPageResp)
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 导出（不分页）
     */
    public List<IrrigationDistrictExportExcelVO> getExportList(IrrigationDistrictPageReqVO reqVO) {
        LambdaQueryWrapper<YzIrrigationDistrictDO> wrapper = irrigationDistrictMapper.buildQueryWrapper(reqVO);
        List<YzIrrigationDistrictDO> list = irrigationDistrictMapper.selectList(wrapper);
        Map<String, String> basinMap = loadDictLabelMap(ZdConstants.ZD_SZLY);
        Map<String, String> typeMap = loadDictLabelMap(ZdConstants.ZD_GQLX);
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> buildExportExcelVO(item, basinMap, typeMap))
                .collect(Collectors.toList());
    }

    /**
     * 详情（用于新增/编辑回显）
     */
    public IrrigationDistrictSaveReqVO getDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("灌区ID不能为空");
        }
        YzIrrigationDistrictDO exists = irrigationDistrictMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "灌区不存在或已被删除");
        }
        IrrigationDistrictSaveReqVO vo = new IrrigationDistrictSaveReqVO();
        vo.setId(exists.getId());
        vo.setFacilityId(exists.getFacilityId());
        vo.setIrrigationDistrictCode(exists.getIrrigationDistrictCode());
        vo.setIrrigationDistrictName(exists.getIrrigationDistrictName());
        vo.setIrrigationDistrictImages(toStringList(exists.getIrrigationDistrictImages()));
        vo.setRemarks(exists.getRemarks());
        vo.setBasinCode(exists.getBasinCode());
        vo.setDivisionCode(toStringList(exists.getDivisionCode()));
        vo.setDesignIrrigationArea(exists.getDesignIrrigationArea());
        vo.setActualIrrigableArea(exists.getActualIrrigableArea());
        vo.setBasicFarmlandAreaKm2(exists.getBasicFarmlandAreaKm2());
        vo.setIsEcologicalRedLine(exists.getIsEcologicalRedLine());
        vo.setIsDevelopmentBoundary(exists.getIsDevelopmentBoundary());
        vo.setMainCanalLengthM(exists.getMainCanalLengthM());
        vo.setLeaderName(exists.getLeaderName());
        vo.setLeaderPhone(exists.getLeaderPhone());
        vo.setManagementUnit(toStringList(exists.getManagementUnit()));
        vo.setIrrigationDistrictType(exists.getIrrigationDistrictType());
        if (exists.getFacilityId() != null) {
            YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(exists.getFacilityId());
            if (base != null) {
                vo.setGeomType(base.getGeomType());
                vo.setSrid(base.getSrid());
                Geometry geom = base.getGeom();
                if (geom != null) {
                    try {
                        StringWriter writer = new StringWriter();
                        new GeometryJSON().write(geom, writer);
                        vo.setGeometryGeoJson(writer.toString());
                    } catch (Exception ignored) {
                        // GeoJSON 转换失败时不影响详情查询
                    }
                }
            }
        }
        return vo;
    }

    /**
     * 按设施ID查询灌区详情
     */
    public IrrigationDistrictSaveReqVO getDetailByFacilityId(Long facilityId) {
        if (facilityId == null) {
            throw ServiceExceptionUtil.invalidParamException("设施ID不能为空");
        }
        YzIrrigationDistrictDO exists = irrigationDistrictMapper.selectOne(new LambdaQueryWrapper<YzIrrigationDistrictDO>()
                .eq(YzIrrigationDistrictDO::getFacilityId, facilityId)
                .eq(YzIrrigationDistrictDO::getDeleted, 0));
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "灌区不存在或已删除");
        }
        return getDetail(exists.getId());
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(IrrigationDistrictSaveReqVO reqVO) {
        Long id = SNOWFLAKE.nextId();
        String irrigationDistrictCode = generateIrrigationDistrictCode();
        Long facilityId = ensureFacilityBase(reqVO, reqVO.getFacilityId(), irrigationDistrictCode);
        applyFacilityGeometry(facilityId, reqVO.getGeometryGeoJson(), reqVO.getSrid());

        YzIrrigationDistrictDO insert = new YzIrrigationDistrictDO();
        insert.setId(id);
        insert.setFacilityId(facilityId);
        insert.setIrrigationDistrictCode(irrigationDistrictCode);
        insert.setIrrigationDistrictName(StrUtil.trimToNull(reqVO.getIrrigationDistrictName()));
        insert.setIrrigationDistrictImages(toStringArray(reqVO.getIrrigationDistrictImages()));
        insert.setRemarks(StrUtil.trimToNull(reqVO.getRemarks()));
        insert.setBasinCode(StrUtil.trimToNull(reqVO.getBasinCode()));
        insert.setDivisionCode(toStringArray(reqVO.getDivisionCode()));
        insert.setDesignIrrigationArea(reqVO.getDesignIrrigationArea());
        insert.setActualIrrigableArea(reqVO.getActualIrrigableArea());
        insert.setBasicFarmlandAreaKm2(reqVO.getBasicFarmlandAreaKm2());
        insert.setIsEcologicalRedLine(reqVO.getIsEcologicalRedLine());
        insert.setIsDevelopmentBoundary(reqVO.getIsDevelopmentBoundary());
        insert.setMainCanalLengthM(reqVO.getMainCanalLengthM());
        insert.setLeaderName(StrUtil.trimToNull(reqVO.getLeaderName()));
        insert.setLeaderPhone(StrUtil.trimToNull(reqVO.getLeaderPhone()));
        insert.setManagementUnit(toStringArray(reqVO.getManagementUnit()));
        insert.setIrrigationDistrictType(StrUtil.trimToNull(reqVO.getIrrigationDistrictType()));
        irrigationDistrictMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(IrrigationDistrictSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.invalidParamException("灌区ID不能为空");
        }
        YzIrrigationDistrictDO exists = irrigationDistrictMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "灌区不存在或已被删除");
        }

        String irrigationDistrictCode = resolveIrrigationDistrictCode(exists.getIrrigationDistrictCode());
        Long facilityId = ensureFacilityBase(reqVO, exists.getFacilityId(), irrigationDistrictCode);
        applyFacilityGeometry(facilityId, reqVO.getGeometryGeoJson(), reqVO.getSrid());
        LocalDateTime now = LocalDateTime.now();

        YzIrrigationDistrictDO update = new YzIrrigationDistrictDO();
        update.setId(reqVO.getId());
        update.setFacilityId(facilityId);
        update.setIrrigationDistrictCode(irrigationDistrictCode);
        update.setIrrigationDistrictName(StrUtil.trimToNull(reqVO.getIrrigationDistrictName()));
        update.setIrrigationDistrictImages(toStringArray(reqVO.getIrrigationDistrictImages()));
        update.setRemarks(StrUtil.trimToNull(reqVO.getRemarks()));
        update.setBasinCode(StrUtil.trimToNull(reqVO.getBasinCode()));
        update.setDivisionCode(toStringArrayKeepEmpty(reqVO.getDivisionCode()));
        update.setDesignIrrigationArea(reqVO.getDesignIrrigationArea());
        update.setActualIrrigableArea(reqVO.getActualIrrigableArea());
        update.setBasicFarmlandAreaKm2(reqVO.getBasicFarmlandAreaKm2());
        update.setIsEcologicalRedLine(reqVO.getIsEcologicalRedLine());
        update.setIsDevelopmentBoundary(reqVO.getIsDevelopmentBoundary());
        update.setMainCanalLengthM(reqVO.getMainCanalLengthM());
        update.setLeaderName(StrUtil.trimToNull(reqVO.getLeaderName()));
        update.setLeaderPhone(StrUtil.trimToNull(reqVO.getLeaderPhone()));
        update.setManagementUnit(toStringArray(reqVO.getManagementUnit()));
        update.setIrrigationDistrictType(StrUtil.trimToNull(reqVO.getIrrigationDistrictType()));
        update.setUpdateTime(now);
        irrigationDistrictMapper.updateById(update);
        applyExplicitNullUpdate(reqVO.getId(), update, now);
    }

    /**
     * 按灌区名称上传 geometry.json 更新 GIS
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGeometryByDistrictName(String irrigationDistrictName, MultipartFile file) {
        String normalizedName = StrUtil.trimToNull(irrigationDistrictName);
        if (normalizedName == null) {
            throw ServiceExceptionUtil.invalidParamException("灌区名称不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("geometry.json 文件不能为空");
        }

        List<YzIrrigationDistrictDO> list = irrigationDistrictMapper.selectList(
                new LambdaQueryWrapper<YzIrrigationDistrictDO>()
                        .eq(YzIrrigationDistrictDO::getIrrigationDistrictName, normalizedName)
                        .eq(YzIrrigationDistrictDO::getDeleted, 0));
        if (list.isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("未找到名称为【" + normalizedName + "】的灌区");
        }
        if (list.size() > 1) {
            throw ServiceExceptionUtil.invalidParamException("存在多个名称为【" + normalizedName + "】的灌区，请先处理重名数据");
        }

        YzIrrigationDistrictDO exists = list.get(0);
        String geometryGeoJson = readGeometryJsonFile(file);

        IrrigationDistrictSaveReqVO syncReqVO = new IrrigationDistrictSaveReqVO();
        syncReqVO.setIrrigationDistrictCode(resolveIrrigationDistrictCode(exists.getIrrigationDistrictCode()));
        syncReqVO.setIrrigationDistrictName(exists.getIrrigationDistrictName());
        syncReqVO.setBasinCode(exists.getBasinCode());
        syncReqVO.setDivisionCode(toStringList(exists.getDivisionCode()));
        Long facilityId = ensureFacilityBase(syncReqVO, exists.getFacilityId(), syncReqVO.getIrrigationDistrictCode());

        applyFacilityGeometry(facilityId, null, null);
        applyFacilityGeometry(facilityId, geometryGeoJson, DEFAULT_SRID);

        YzIrrigationDistrictDO update = new YzIrrigationDistrictDO();
        update.setId(exists.getId());
        update.setFacilityId(facilityId);
        update.setUpdateTime(LocalDateTime.now());
        irrigationDistrictMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("灌区ID不能为空");
        }
        YzIrrigationDistrictDO exists = irrigationDistrictMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "灌区不存在或已被删除");
        }
        irrigationDistrictMapper.deleteById(id);
        if (exists.getFacilityId() != null) {
            facilityBaseMapper.deleteById(exists.getFacilityId());
        }
    }

    private IrrigationDistrictPageRespVO buildPageResp(YzIrrigationDistrictDO item) {
        IrrigationDistrictPageRespVO vo = new IrrigationDistrictPageRespVO();
        vo.setId(item.getId());
        vo.setFacilityId(item.getFacilityId());
        vo.setIrrigationDistrictCode(StrUtil.blankToDefault(item.getIrrigationDistrictCode(), ""));
        vo.setIrrigationDistrictName(StrUtil.blankToDefault(item.getIrrigationDistrictName(), ""));
        vo.setBasinCode(item.getBasinCode());
        vo.setDivisionCode(toStringList(item.getDivisionCode()));
        vo.setDesignIrrigationArea(item.getDesignIrrigationArea());
        vo.setActualIrrigableArea(item.getActualIrrigableArea());
        vo.setBasicFarmlandAreaKm2(item.getBasicFarmlandAreaKm2());
        vo.setIsEcologicalRedLine(item.getIsEcologicalRedLine());
        vo.setIsDevelopmentBoundary(item.getIsDevelopmentBoundary());
        vo.setMainCanalLengthM(item.getMainCanalLengthM());
        vo.setLeaderName(item.getLeaderName());
        vo.setLeaderPhone(item.getLeaderPhone());
        vo.setManagementUnit(toStringList(item.getManagementUnit()));
        vo.setIrrigationDistrictType(item.getIrrigationDistrictType());
        return vo;
    }

    private IrrigationDistrictExportExcelVO buildExportExcelVO(YzIrrigationDistrictDO item,
                                                              Map<String, String> basinMap,
                                                              Map<String, String> typeMap) {
        IrrigationDistrictExportExcelVO vo = new IrrigationDistrictExportExcelVO();
        vo.setIrrigationDistrictCode(item.getIrrigationDistrictCode());
        vo.setIrrigationDistrictName(item.getIrrigationDistrictName());
        vo.setBasinCode(resolveDictLabel(item.getBasinCode(), basinMap));
        vo.setDivisionCode(String.join("，", toStringList(item.getDivisionCode())));
        vo.setIrrigationDistrictImages(String.join("，", toStringList(item.getIrrigationDistrictImages())));
        vo.setDesignIrrigationArea(item.getDesignIrrigationArea());
        vo.setActualIrrigableArea(item.getActualIrrigableArea());
        vo.setBasicFarmlandAreaKm2(item.getBasicFarmlandAreaKm2());
        vo.setIsEcologicalRedLine(formatYesNo(item.getIsEcologicalRedLine()));
        vo.setIsDevelopmentBoundary(formatYesNo(item.getIsDevelopmentBoundary()));
        vo.setMainCanalLengthM(item.getMainCanalLengthM());
        vo.setLeaderName(item.getLeaderName());
        vo.setLeaderPhone(item.getLeaderPhone());
        vo.setManagementUnit(String.join("，", toStringList(item.getManagementUnit())));
        vo.setIrrigationDistrictType(resolveDictLabel(item.getIrrigationDistrictType(), typeMap));
        vo.setRemarks(item.getRemarks());
        return vo;
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        Map<String, String> map = new HashMap<>();
        for (DictDataRespDTO item : list) {
            if (item == null || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            map.put(item.getValue(), StrUtil.blankToDefault(item.getLabel(), item.getValue()));
        }
        return map;
    }

    private String resolveDictLabel(String value, Map<String, String> map) {
        String v = StrUtil.trimToNull(value);
        if (v == null) {
            return "";
        }
        return map.getOrDefault(v, v);
    }

    private String formatYesNo(Integer value) {
        if (value == null) {
            return "";
        }
        if (value == 0) {
            return "否";
        }
        if (value == 1) {
            return "是";
        }
        return String.valueOf(value);
    }

    private String[] toStringArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<String> cleaned = list.stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .limit(50)
                .collect(Collectors.toList());
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.toArray(new String[0]);
    }

    /**
     * 灌区编码由后端统一维护，新增时使用雪花算法生成。
     */
    private String generateIrrigationDistrictCode() {
        return String.valueOf(SNOWFLAKE.nextId());
    }

    /**
     * 编辑时保持原编码不变；历史空值数据在保存时自动补齐。
     */
    private String resolveIrrigationDistrictCode(String existingCode) {
        String normalizedCode = StrUtil.trimToNull(existingCode);
        return normalizedCode != null ? normalizedCode : generateIrrigationDistrictCode();
    }

    /**
     * 允许将空列表转换为“空数组”，用于显式清空 text[] 字段
     */
    private String[] toStringArrayKeepEmpty(List<String> list) {
        if (list == null) {
            return null;
        }
        List<String> cleaned = list.stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .limit(50)
                .collect(Collectors.toList());
        return cleaned.toArray(new String[0]);
    }

    private List<String> toStringList(String[] arr) {
        if (arr == null || arr.length == 0) {
            return List.of();
        }
        return Arrays.stream(arr)
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 确保设施基础表存在并同步基础字段（名称/类型/编码/流域/行政区划）
     */
    private Long ensureFacilityBase(IrrigationDistrictSaveReqVO reqVO, Long facilityId, String irrigationDistrictCode) {
        if (facilityId == null) {
            return createFacilityBase(reqVO, irrigationDistrictCode);
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null) {
            return createFacilityBase(reqVO, irrigationDistrictCode);
        }
        base.setFacilityName(StrUtil.trimToNull(reqVO.getIrrigationDistrictName()));
        base.setFacilityType(FACILITY_TYPE_IRRIGATION);
        base.setFacilityCode(StrUtil.trimToNull(irrigationDistrictCode));
        base.setBasinCode(StrUtil.trimToNull(reqVO.getBasinCode()));
        base.setAdminRegionCode(firstDivisionCode(reqVO.getDivisionCode()));
        facilityBaseMapper.updateById(base);
        return base.getId();
    }

    private Long createFacilityBase(IrrigationDistrictSaveReqVO reqVO, String irrigationDistrictCode) {
        Long baseId = SNOWFLAKE.nextId();
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityName(StrUtil.trimToNull(reqVO.getIrrigationDistrictName()));
        base.setFacilityType(FACILITY_TYPE_IRRIGATION);
        base.setFacilityCode(StrUtil.trimToNull(irrigationDistrictCode));
        base.setBasinCode(StrUtil.trimToNull(reqVO.getBasinCode()));
        base.setAdminRegionCode(firstDivisionCode(reqVO.getDivisionCode()));
        base.setSourceType("system");
        facilityBaseMapper.insert(base);
        return baseId;
    }

    private String readGeometryJsonFile(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String trimmed = StrUtil.trimToNull(content);
            if (trimmed == null) {
                throw ServiceExceptionUtil.invalidParamException("geometry.json 文件内容不能为空");
            }
            return trimmed;
        } catch (IOException ex) {
            throw ServiceExceptionUtil.invalidParamException("读取 geometry.json 文件失败");
        }
    }

    private String firstDivisionCode(List<String> divisionCode) {
        if (divisionCode == null || divisionCode.isEmpty()) {
            return null;
        }
        for (String item : divisionCode) {
            String cleaned = StrUtil.trimToNull(item);
            if (cleaned != null) {
                return cleaned;
            }
        }
        return null;
    }

    /**
     * 将 GeoJSON 几何写入设施基础表（geom/geomType/srid）
     */
    private void applyFacilityGeometry(Long facilityId, String geometryGeoJson, Integer srid) {
        if (facilityId == null) {
            return;
        }
        if (StrUtil.isBlank(geometryGeoJson)) {
            facilityBaseMapper.clearGeomById(facilityId);
            return;
        }

        Geometry geometry;
        try {
            geometry = new GeometryJSON().read(geometryGeoJson);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.invalidParamException(StrUtil.blankToDefault(ex.getMessage(), "GeoJSON 格式不正确"));
        }
        if (geometry == null) {
            throw ServiceExceptionUtil.invalidParamException("几何为空");
        }
        int finalSrid = srid != null ? srid : DEFAULT_SRID;
        geometry.setSRID(finalSrid);

        YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
        update.setId(facilityId);
        update.setGeomType(geometry.getGeometryType());
        update.setSrid(finalSrid);
        update.setGeom(geometry);
        facilityBaseMapper.updateById(update);
    }

    /**
     * updateById 默认不更新 null 字段，这里显式将被清空的字段写回为 NULL。
     */
    private void applyExplicitNullUpdate(Long id, YzIrrigationDistrictDO update, LocalDateTime now) {
        List<String> clearColumns = new ArrayList<>();
        if (update.getIrrigationDistrictCode() == null) {
            clearColumns.add("irrigation_district_code = NULL");
        }
        if (update.getIrrigationDistrictImages() == null) {
            clearColumns.add("irrigation_district_images = NULL");
        }
        if (update.getRemarks() == null) {
            clearColumns.add("remarks = NULL");
        }
        if (update.getBasinCode() == null) {
            clearColumns.add("basin_code = NULL");
        }
        if (update.getDivisionCode() == null) {
            clearColumns.add("division_code = NULL");
        }
        if (update.getDesignIrrigationArea() == null) {
            clearColumns.add("design_irrigation_area = NULL");
        }
        if (update.getActualIrrigableArea() == null) {
            clearColumns.add("actual_irrigable_area = NULL");
        }
        if (update.getBasicFarmlandAreaKm2() == null) {
            clearColumns.add("basic_farmland_area_km2 = NULL");
        }
        if (update.getIsEcologicalRedLine() == null) {
            clearColumns.add("is_ecological_red_line = NULL");
        }
        if (update.getIsDevelopmentBoundary() == null) {
            clearColumns.add("is_development_boundary = NULL");
        }
        if (update.getMainCanalLengthM() == null) {
            clearColumns.add("main_canal_length_m = NULL");
        }
        if (update.getLeaderName() == null) {
            clearColumns.add("leader_name = NULL");
        }
        if (update.getLeaderPhone() == null) {
            clearColumns.add("leader_phone = NULL");
        }
        if (update.getManagementUnit() == null) {
            clearColumns.add("management_unit = NULL");
        }
        if (update.getIrrigationDistrictType() == null) {
            clearColumns.add("irrigation_district_type = NULL");
        }
        if (clearColumns.isEmpty()) {
            return;
        }
        irrigationDistrictMapper.update(null, new LambdaUpdateWrapper<YzIrrigationDistrictDO>()
                .eq(YzIrrigationDistrictDO::getId, id)
                .set(YzIrrigationDistrictDO::getUpdateTime, now)
                .setSql(String.join(", ", clearColumns)));
    }
}
