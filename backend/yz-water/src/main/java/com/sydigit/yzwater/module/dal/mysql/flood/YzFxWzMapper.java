package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzListReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 防汛物资 Mapper
 */
@Mapper
public interface YzFxWzMapper extends BaseMapperX<YzFxWzDO> {

    /**
     * 按排序查询
     */
    default List<YzFxWzDO> selectListOrderBySort() {
        return selectList(buildListQueryWrapper(new FxWzListReqVO()));
    }

    /**
     * 按条件查询
     */
    default List<YzFxWzDO> selectList(FxWzListReqVO reqVO) {
        return selectList(buildListQueryWrapper(reqVO));
    }

    private LambdaQueryWrapper<YzFxWzDO> buildListQueryWrapper(FxWzListReqVO reqVO) {
        FxWzListReqVO query = reqVO == null ? new FxWzListReqVO() : reqVO;
        return new LambdaQueryWrapper<YzFxWzDO>()
                .like(StrUtil.isNotBlank(query.getMaterialName()), YzFxWzDO::getMaterialName, query.getMaterialName())
                .eq(StrUtil.isNotBlank(query.getMaterialType()), YzFxWzDO::getMaterialType, query.getMaterialType())
                .eq(StrUtil.isNotBlank(query.getUnit()), YzFxWzDO::getUnit, query.getUnit())
                .orderByAsc(YzFxWzDO::getSort)
                .orderByAsc(YzFxWzDO::getMaterialName)
                .orderByDesc(YzFxWzDO::getCreateTime);
    }

    /**
     * 按储备单位查询
     */
    default List<YzFxWzDO> selectListByWarehouseId(String warehouseId) {
        return selectList(new LambdaQueryWrapper<YzFxWzDO>()
                .eq(YzFxWzDO::getUnitId, warehouseId)
                .orderByAsc(YzFxWzDO::getSort)
                .orderByAsc(YzFxWzDO::getMaterialName)
                .orderByDesc(YzFxWzDO::getCreateTime));
    }

    /**
     * 按物资类型查询
     */
    default List<YzFxWzDO> selectListByMaterialType(String materialType) {
        return selectList(new LambdaQueryWrapper<YzFxWzDO>()
                .eq(YzFxWzDO::getMaterialType, materialType)
                .orderByAsc(YzFxWzDO::getSort)
                .orderByAsc(YzFxWzDO::getMaterialName)
                .orderByDesc(YzFxWzDO::getCreateTime));
    }
}
