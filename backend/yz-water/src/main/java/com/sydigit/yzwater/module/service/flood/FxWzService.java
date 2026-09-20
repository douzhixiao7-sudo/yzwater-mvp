package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzMaterialMapPointRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzWarehouseOptionRespVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxWzMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物资管理服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxWzService {

    private final YzFxWzMapper wzMapper;
    private final YzFloodPreventionMaterialWarehouseMapper warehouseMapper;

    /**
     * 列表查询
     */
    public List<FxWzListRespVO> getList(FxWzListReqVO reqVO) {
        List<YzFxWzDO> list = wzMapper.selectList(reqVO);
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 详情
     */
    public FxWzSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("物资 ID 不能为空");
        }
        YzFxWzDO exists = wzMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "物资不存在或已删除");
        }
        FxWzSaveReqVO vo = new FxWzSaveReqVO();
        vo.setId(exists.getId());
        vo.setMaterialName(exists.getMaterialName());
        vo.setQuantity(exists.getQuantity());
        vo.setUnit(exists.getUnit());
        vo.setMaterialType(exists.getMaterialType());
        vo.setIsDelegateStorage(normalizeDelegateStorage(exists.getIsDelegateStorage()));
        vo.setWarehouseId(exists.getUnitId());
        vo.setRemark(exists.getRemark());
        vo.setWarehouseAddress(exists.getWarehouseAddress());
        vo.setLongitude(exists.getLongitude());
        vo.setLatitude(exists.getLatitude());
        vo.setSort(exists.getSort());
        vo.setContactPerson(exists.getContactPerson());
        vo.setContactInfo(exists.getContactInfo());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxWzSaveReqVO reqVO) {
        YzFloodPreventionMaterialWarehouseDO warehouse = getWarehouseById(reqVO.getWarehouseId());

        String id = generateId();
        YzFxWzDO insert = new YzFxWzDO();
        insert.setId(id);
        fillFields(insert, reqVO, warehouse);
        wzMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxWzSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("物资 ID 不能为空");
        }
        YzFxWzDO exists = wzMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "物资不存在或已删除");
        }

        YzFloodPreventionMaterialWarehouseDO warehouse = getWarehouseById(reqVO.getWarehouseId());
        YzFxWzDO update = new YzFxWzDO();
        update.setId(id);
        fillFields(update, reqVO, warehouse);
        wzMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("物资 ID 不能为空");
        }
        YzFxWzDO exists = wzMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "物资不存在或已删除");
        }
        wzMapper.deleteById(key);
    }

    /**
     * 地图点位列表（仅返回含有效坐标的防汛物资，供风险隐患点路线关联）
     */
    public List<FxWzMaterialMapPointRespVO> getMaterialMapPointList() {
        List<YzFxWzDO> list = wzMapper.selectList(
                new LambdaQueryWrapper<YzFxWzDO>()
                        .isNotNull(YzFxWzDO::getLongitude)
                        .isNotNull(YzFxWzDO::getLatitude)
                        .orderByAsc(YzFxWzDO::getSort)
                        .orderByAsc(YzFxWzDO::getMaterialName)
                        .orderByDesc(YzFxWzDO::getCreateTime)
        );
        return list.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getId()))
                .filter(this::hasValidCoordinate)
                .map(this::buildMaterialMapPointResp)
                .collect(Collectors.toList());
    }

    /**
     * 按物资 ID 查询同仓库地址（或同坐标）下的全部防汛物资
     */
    public List<YzFxWzDO> listMaterialsAtSameLocation(String materialId) {
        YzFxWzDO anchor = findMaterialById(materialId);
        if (anchor == null) {
            return List.of();
        }
        return listMaterialsAtSameLocation(anchor);
    }

    /**
     * 按路线终点坐标/标签匹配防汛物资（linkedWarehouseId 缺失时的兜底）
     */
    public List<YzFxWzDO> listMaterialsAtEndpoint(BigDecimal longitude, BigDecimal latitude, String label) {
        List<YzFxWzDO> geolocatedMaterials = listGeolocatedMaterials();
        Double lng = toDouble(longitude);
        Double lat = toDouble(latitude);
        if (lng != null && lat != null) {
            List<YzFxWzDO> matchedByCoordinate = geolocatedMaterials.stream()
                    .filter(item -> coordsNear(lng, lat, item))
                    .toList();
            if (!matchedByCoordinate.isEmpty()) {
                return matchedByCoordinate;
            }
        }
        return matchMaterialsByLabel(label, geolocatedMaterials);
    }

    public YzFxWzDO findMaterialById(String materialId) {
        String key = StrUtil.trimToNull(materialId);
        if (key == null) {
            return null;
        }
        return wzMapper.selectById(key);
    }

    private List<YzFxWzDO> listMaterialsAtSameLocation(YzFxWzDO anchor) {
        return listGeolocatedMaterials().stream()
                .filter(item -> isSameLocation(anchor, item))
                .toList();
    }

    private List<YzFxWzDO> listGeolocatedMaterials() {
        return wzMapper.selectList(
                new LambdaQueryWrapper<YzFxWzDO>()
                        .isNotNull(YzFxWzDO::getLongitude)
                        .isNotNull(YzFxWzDO::getLatitude)
                        .orderByAsc(YzFxWzDO::getSort)
                        .orderByAsc(YzFxWzDO::getMaterialName)
                        .orderByDesc(YzFxWzDO::getCreateTime)
        ).stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidCoordinate)
                .toList();
    }

    private List<YzFxWzDO> matchMaterialsByLabel(String label, List<YzFxWzDO> candidates) {
        String normalizedLabel = StrUtil.trimToNull(label);
        if (normalizedLabel == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(item -> normalizedLabel.equals(StrUtil.trimToNull(item.getWarehouseAddress()))
                        || normalizedLabel.equals(StrUtil.trimToNull(item.getStorageUnit()))
                        || normalizedLabel.equals(StrUtil.trimToNull(item.getMaterialName())))
                .toList();
    }

    private boolean isSameLocation(YzFxWzDO left, YzFxWzDO right) {
        if (left == null || right == null) {
            return false;
        }
        if (Objects.equals(left.getId(), right.getId())) {
            return true;
        }
        String leftAddress = StrUtil.trimToNull(left.getWarehouseAddress());
        String rightAddress = StrUtil.trimToNull(right.getWarehouseAddress());
        if (leftAddress != null && leftAddress.equals(rightAddress)) {
            return true;
        }
        Double leftLng = toDouble(left.getLongitude());
        Double leftLat = toDouble(left.getLatitude());
        return leftLng != null && leftLat != null && coordsNear(leftLng, leftLat, right);
    }

    private boolean coordsNear(double lng, double lat, YzFxWzDO item) {
        Double itemLng = toDouble(item.getLongitude());
        Double itemLat = toDouble(item.getLatitude());
        if (itemLng == null || itemLat == null) {
            return false;
        }
        return Math.abs(itemLng - lng) < 1e-4 && Math.abs(itemLat - lat) < 1e-4;
    }

    /**
     * 储备单位下拉
     */
    public List<FxWzWarehouseOptionRespVO> getWarehouseOptions() {
        List<YzFloodPreventionMaterialWarehouseDO> warehouseList = warehouseMapper.selectList(
                new LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO>()
                        .orderByAsc(YzFloodPreventionMaterialWarehouseDO::getWarehouseName)
                        .orderByDesc(YzFloodPreventionMaterialWarehouseDO::getCreateTime)
        );
        return warehouseList.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .map(this::buildWarehouseOption)
                .toList();
    }

    private FxWzListRespVO buildListResp(YzFxWzDO item) {
        FxWzListRespVO vo = new FxWzListRespVO();
        vo.setId(item.getId());
        vo.setMaterialName(item.getMaterialName());
        vo.setQuantity(item.getQuantity());
        vo.setUnit(item.getUnit());
        vo.setMaterialType(item.getMaterialType());
        vo.setStorageUnit(item.getStorageUnit());
        vo.setIsDelegateStorage(normalizeDelegateStorage(item.getIsDelegateStorage()));
        vo.setWarehouseId(item.getUnitId());
        vo.setRemark(item.getRemark());
        vo.setWarehouseAddress(item.getWarehouseAddress());
        vo.setLongitude(item.getLongitude());
        vo.setLatitude(item.getLatitude());
        vo.setSort(item.getSort());
        vo.setContactPerson(item.getContactPerson());
        vo.setContactInfo(item.getContactInfo());
        return vo;
    }

    private FxWzWarehouseOptionRespVO buildWarehouseOption(YzFloodPreventionMaterialWarehouseDO item) {
        FxWzWarehouseOptionRespVO vo = new FxWzWarehouseOptionRespVO();
        vo.setId(String.valueOf(item.getId()));
        vo.setWarehouseName(StrUtil.blankToDefault(item.getWarehouseName(), ""));
        return vo;
    }

    private boolean hasValidCoordinate(YzFxWzDO item) {
        return toDouble(item.getLongitude()) != null && toDouble(item.getLatitude()) != null;
    }

    private Double toDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        double parsed = value.doubleValue();
        return Double.isFinite(parsed) ? parsed : null;
    }

    private FxWzMaterialMapPointRespVO buildMaterialMapPointResp(YzFxWzDO item) {
        FxWzMaterialMapPointRespVO vo = new FxWzMaterialMapPointRespVO();
        vo.setId(item.getId());
        vo.setMaterialName(StrUtil.blankToDefault(item.getMaterialName(), ""));
        vo.setStorageUnit(StrUtil.trimToNull(item.getStorageUnit()));
        vo.setWarehouseAddress(StrUtil.trimToNull(item.getWarehouseAddress()));
        vo.setLongitude(item.getLongitude());
        vo.setLatitude(item.getLatitude());
        return vo;
    }

    private void fillFields(YzFxWzDO target, FxWzSaveReqVO source, YzFloodPreventionMaterialWarehouseDO warehouse) {
        target.setMaterialName(StrUtil.trimToNull(source.getMaterialName()));
        target.setQuantity(source.getQuantity());
        target.setUnit(StrUtil.trimToNull(source.getUnit()));
        target.setUnitId(String.valueOf(warehouse.getId()));
        target.setStorageUnit(StrUtil.trimToNull(warehouse.getWarehouseName()));
        target.setIsDelegateStorage(normalizeDelegateStorage(source.getIsDelegateStorage()));
        target.setRemark(StrUtil.trimToNull(source.getRemark()));
        target.setWarehouseAddress(StrUtil.trimToNull(source.getWarehouseAddress()));
        target.setLongitude(source.getLongitude());
        target.setLatitude(source.getLatitude());
        target.setSort(source.getSort() == null ? 0 : source.getSort());
        target.setContactPerson(StrUtil.trimToNull(source.getContactPerson()));
        target.setContactInfo(StrUtil.trimToNull(source.getContactInfo()));
    }

    private Integer normalizeDelegateStorage(Integer value) {
        return value != null && value == 1 ? 1 : 0;
    }

    private YzFloodPreventionMaterialWarehouseDO getWarehouseById(String warehouseId) {
        String idStr = StrUtil.trimToNull(warehouseId);
        if (idStr == null) {
            throw ServiceExceptionUtil.invalidParamException("储备单位不能为空");
        }
        Long id;
        try {
            id = Long.valueOf(idStr);
        } catch (NumberFormatException ex) {
            throw ServiceExceptionUtil.invalidParamException("储备单位 ID 格式不正确");
        }
        YzFloodPreventionMaterialWarehouseDO warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "储备单位不存在或已删除");
        }
        return warehouse;
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
