package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehousePageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 防汛物资仓库属性表 Mapper
 */
@Mapper
public interface YzFloodPreventionMaterialWarehouseMapper extends BaseMapperX<YzFloodPreventionMaterialWarehouseDO> {

    default LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO> buildQueryWrapper(FloodPreventionMaterialWarehousePageReqVO reqVO) {
        LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO> wrapper = new LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO>()
                .like(StrUtil.isNotBlank(reqVO.getWarehouseName()),
                        YzFloodPreventionMaterialWarehouseDO::getWarehouseName, reqVO.getWarehouseName())
                .like(StrUtil.isNotBlank(reqVO.getBelongUnit()),
                        YzFloodPreventionMaterialWarehouseDO::getBelongUnit, reqVO.getBelongUnit())
                .orderByDesc(YzFloodPreventionMaterialWarehouseDO::getCreateTime);

        if (StrUtil.isNotBlank(reqVO.getDivisionCode())) {
            // 行政划分存储为 PostgreSQL text[]，这里按单个区划节点筛选：数组中包含该值即可命中
            wrapper.apply("array_position(division_code, {0}) is not null", reqVO.getDivisionCode());
        }
        return wrapper;
    }

    /**
     * 按行政划分（division_code）分组统计仓库数量。
     *
     * <p>division_code 为 text[]，存储 /system/area/tree 的 id（字符串形式）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select unnest(division_code) as area_code",
            "  from yz_flood_prevention_material",
            "  where division_code is not null",
            "    and COALESCE(deleted, 0) = 0",
            ") t",
            "where t.area_code is not null and btrim(t.area_code) <> ''",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectDivisionCountGroup();

    /**
     * 按区划代码集合统计仓库总数（包含子级匹配）。
     */
    @Select({
            "select count(1) as total_count",
            "from yz_flood_prevention_material",
            "where division_code is not null",
            "  and division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0"
    })
    Map<String, Object> selectSummaryByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 查询区划代码集合下的仓库简表（不分页，用于首页展示）。
     */
    @Select({
            "select",
            "  id,",
            "  warehouse_name,",
            "  belong_unit,",
            "  material_type,",
            "  longitude,",
            "  latitude",
            "from yz_flood_prevention_material",
            "where division_code is not null",
            "  and division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0",
            "order by warehouse_name asc, create_time desc"
    })
    List<YzFloodPreventionMaterialWarehouseDO> selectWarehouseListByDivisionCodes(@Param("areaCodes") String[] areaCodes);
}
