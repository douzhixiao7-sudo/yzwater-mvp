package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehouseExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehouseMapPointRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehousePageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehousePageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehouseSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 防汛物资仓库服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FloodPreventionMaterialWarehouseService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzFloodPreventionMaterialWarehouseMapper warehouseMapper;

    /**
     * 分页查询
     */
    public PageResult<FloodPreventionMaterialWarehousePageRespVO> getPage(FloodPreventionMaterialWarehousePageReqVO reqVO) {
        LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO> wrapper = warehouseMapper.buildQueryWrapper(reqVO);
        PageResult<YzFloodPreventionMaterialWarehouseDO> page = warehouseMapper.selectPage(reqVO, wrapper);
        List<FloodPreventionMaterialWarehousePageRespVO> list = page.getList().stream()
                .filter(Objects::nonNull)
                .map(this::buildPageResp)
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 导出（不分页）
     */
    public List<FloodPreventionMaterialWarehouseExportExcelVO> getExportList(FloodPreventionMaterialWarehousePageReqVO reqVO) {
        LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO> wrapper = warehouseMapper.buildQueryWrapper(reqVO);
        List<YzFloodPreventionMaterialWarehouseDO> list = warehouseMapper.selectList(wrapper);
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildExportExcelVO)
                .collect(Collectors.toList());
    }

    /**
     * 详情（用于新增/编辑回显）
     */
    public FloodPreventionMaterialWarehouseSaveReqVO getDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("仓库ID不能为空");
        }
        YzFloodPreventionMaterialWarehouseDO exists = warehouseMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "仓库不存在或已被删除");
        }
        FloodPreventionMaterialWarehouseSaveReqVO vo = new FloodPreventionMaterialWarehouseSaveReqVO();
        vo.setId(exists.getId());
        vo.setWarehouseName(exists.getWarehouseName());
        vo.setSpecificLocation(exists.getSpecificLocation());
        vo.setLongitude(exists.getLongitude());
        vo.setLatitude(exists.getLatitude());
        vo.setBelongUnit(exists.getBelongUnit());
        vo.setLeaderName(exists.getLeaderName());
        vo.setLeaderPhone(exists.getLeaderPhone());
        vo.setMaterialType(exists.getMaterialType());
        vo.setIsDelegateStorage(normalizeDelegateStorage(exists.getIsDelegateStorage()));
        vo.setWarehouseImages(toStringList(exists.getWarehouseImages()));
        vo.setRemarks(exists.getRemarks());
        vo.setDivisionCode(toStringList(exists.getDivisionCode()));
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(FloodPreventionMaterialWarehouseSaveReqVO reqVO) {
        Long id = SNOWFLAKE.nextId();
        YzFloodPreventionMaterialWarehouseDO insert = new YzFloodPreventionMaterialWarehouseDO();
        insert.setId(id);
        insert.setWarehouseName(reqVO.getWarehouseName());
        insert.setSpecificLocation(reqVO.getSpecificLocation());
        insert.setLongitude(reqVO.getLongitude());
        insert.setLatitude(reqVO.getLatitude());
        insert.setBelongUnit(reqVO.getBelongUnit());
        insert.setLeaderName(reqVO.getLeaderName());
        insert.setLeaderPhone(reqVO.getLeaderPhone());
        insert.setMaterialType(reqVO.getMaterialType());
        insert.setIsDelegateStorage(normalizeDelegateStorage(reqVO.getIsDelegateStorage()));
        insert.setWarehouseImages(toStringArray(reqVO.getWarehouseImages()));
        insert.setRemarks(reqVO.getRemarks());
        insert.setDivisionCode(toStringArray(reqVO.getDivisionCode()));
        warehouseMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FloodPreventionMaterialWarehouseSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.invalidParamException("仓库ID不能为空");
        }
        YzFloodPreventionMaterialWarehouseDO exists = warehouseMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "仓库不存在或已被删除");
        }

        YzFloodPreventionMaterialWarehouseDO update = new YzFloodPreventionMaterialWarehouseDO();
        update.setId(reqVO.getId());
        update.setWarehouseName(reqVO.getWarehouseName());
        update.setSpecificLocation(reqVO.getSpecificLocation());
        update.setLongitude(reqVO.getLongitude());
        update.setLatitude(reqVO.getLatitude());
        update.setBelongUnit(reqVO.getBelongUnit());
        update.setLeaderName(reqVO.getLeaderName());
        update.setLeaderPhone(reqVO.getLeaderPhone());
        update.setMaterialType(reqVO.getMaterialType());
        update.setIsDelegateStorage(normalizeDelegateStorage(reqVO.getIsDelegateStorage()));
        update.setWarehouseImages(toStringArray(reqVO.getWarehouseImages()));
        update.setRemarks(reqVO.getRemarks());
        update.setDivisionCode(toStringArray(reqVO.getDivisionCode()));
        warehouseMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("仓库ID不能为空");
        }
        YzFloodPreventionMaterialWarehouseDO exists = warehouseMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_090, "仓库不存在或已被删除");
        }
        warehouseMapper.deleteById(id);
    }

    private FloodPreventionMaterialWarehousePageRespVO buildPageResp(YzFloodPreventionMaterialWarehouseDO item) {
        FloodPreventionMaterialWarehousePageRespVO vo = new FloodPreventionMaterialWarehousePageRespVO();
        vo.setId(item.getId());
        vo.setWarehouseName(StrUtil.blankToDefault(item.getWarehouseName(), ""));
        vo.setSpecificLocation(item.getSpecificLocation());
        vo.setBelongUnit(item.getBelongUnit());
        vo.setLeaderName(item.getLeaderName());
        vo.setLeaderPhone(item.getLeaderPhone());
        vo.setDivisionCode(toStringList(item.getDivisionCode()));
        return vo;
    }

    private FloodPreventionMaterialWarehouseExportExcelVO buildExportExcelVO(YzFloodPreventionMaterialWarehouseDO item) {
        FloodPreventionMaterialWarehouseExportExcelVO vo = new FloodPreventionMaterialWarehouseExportExcelVO();
        vo.setWarehouseName(item.getWarehouseName());
        vo.setSpecificLocation(item.getSpecificLocation());
        vo.setLongitude(item.getLongitude());
        vo.setLatitude(item.getLatitude());
        vo.setBelongUnit(item.getBelongUnit());
        vo.setLeaderName(item.getLeaderName());
        vo.setLeaderPhone(item.getLeaderPhone());
        vo.setMaterialType(item.getMaterialType());
        vo.setDivisionCode(String.join("、", toStringList(item.getDivisionCode())));
        vo.setWarehouseImages(String.join("、", toStringList(item.getWarehouseImages())));
        vo.setRemarks(item.getRemarks());
        return vo;
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
     * 地图点位列表（仅返回含有效坐标的仓库，供险工路径关联）
     */
    public List<FloodPreventionMaterialWarehouseMapPointRespVO> getMapPointList() {
        List<YzFloodPreventionMaterialWarehouseDO> list = warehouseMapper.selectList(
                new LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO>()
                        .isNotNull(YzFloodPreventionMaterialWarehouseDO::getLongitude)
                        .isNotNull(YzFloodPreventionMaterialWarehouseDO::getLatitude)
                        .orderByAsc(YzFloodPreventionMaterialWarehouseDO::getWarehouseName)
                        .orderByDesc(YzFloodPreventionMaterialWarehouseDO::getCreateTime)
        );
        return list.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .filter(this::hasValidCoordinate)
                .map(this::buildMapPointResp)
                .collect(Collectors.toList());
    }

    private boolean hasValidCoordinate(YzFloodPreventionMaterialWarehouseDO item) {
        return toDouble(item.getLongitude()) != null && toDouble(item.getLatitude()) != null;
    }

    private Double toDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        double parsed = value.doubleValue();
        return Double.isFinite(parsed) ? parsed : null;
    }

    private FloodPreventionMaterialWarehouseMapPointRespVO buildMapPointResp(YzFloodPreventionMaterialWarehouseDO item) {
        FloodPreventionMaterialWarehouseMapPointRespVO vo = new FloodPreventionMaterialWarehouseMapPointRespVO();
        vo.setId(item.getId());
        vo.setWarehouseName(StrUtil.blankToDefault(item.getWarehouseName(), ""));
        vo.setLongitude(item.getLongitude());
        vo.setLatitude(item.getLatitude());
        return vo;
    }

    private Integer normalizeDelegateStorage(Integer value) {
        return Objects.equals(value, 1) ? 1 : 0;
    }
}
