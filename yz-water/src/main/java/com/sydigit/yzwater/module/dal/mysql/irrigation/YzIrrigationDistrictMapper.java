package com.sydigit.yzwater.module.dal.mysql.irrigation;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.irrigation.YzIrrigationDistrictDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 灌区信息表 Mapper
 */
@Mapper
public interface YzIrrigationDistrictMapper extends BaseMapperX<YzIrrigationDistrictDO> {

    default LambdaQueryWrapper<YzIrrigationDistrictDO> buildQueryWrapper(IrrigationDistrictPageReqVO reqVO) {
        LambdaQueryWrapper<YzIrrigationDistrictDO> wrapper = new LambdaQueryWrapper<YzIrrigationDistrictDO>()
                .like(StrUtil.isNotBlank(reqVO.getIrrigationDistrictCode()),
                        YzIrrigationDistrictDO::getIrrigationDistrictCode, reqVO.getIrrigationDistrictCode())
                .like(StrUtil.isNotBlank(reqVO.getIrrigationDistrictName()),
                        YzIrrigationDistrictDO::getIrrigationDistrictName, reqVO.getIrrigationDistrictName())
                .eq(reqVO.getFacilityId() != null,
                        YzIrrigationDistrictDO::getFacilityId, reqVO.getFacilityId())
                .eq(StrUtil.isNotBlank(reqVO.getBasinCode()),
                        YzIrrigationDistrictDO::getBasinCode, reqVO.getBasinCode())
                .eq(reqVO.getIsEcologicalRedLine() != null,
                        YzIrrigationDistrictDO::getIsEcologicalRedLine, reqVO.getIsEcologicalRedLine())
                .eq(reqVO.getIsDevelopmentBoundary() != null,
                        YzIrrigationDistrictDO::getIsDevelopmentBoundary, reqVO.getIsDevelopmentBoundary())
                .like(StrUtil.isNotBlank(reqVO.getLeaderName()),
                        YzIrrigationDistrictDO::getLeaderName, reqVO.getLeaderName())
                .like(StrUtil.isNotBlank(reqVO.getLeaderPhone()),
                        YzIrrigationDistrictDO::getLeaderPhone, reqVO.getLeaderPhone())
                .eq(StrUtil.isNotBlank(reqVO.getIrrigationDistrictType()),
                        YzIrrigationDistrictDO::getIrrigationDistrictType, reqVO.getIrrigationDistrictType())
                .orderByDesc(YzIrrigationDistrictDO::getUpdateTime);

        if (StrUtil.isNotBlank(reqVO.getDivisionCode())) {
            // 行政区划存储为 PostgreSQL text[]，这里按单个区划节点筛选：数组中包含该值即可命中
            wrapper.apply("array_position(division_code, {0}) is not null", reqVO.getDivisionCode());
        }
        return wrapper;
    }

    /**
     * 按行政区划（division_code）分组统计灌区数量。
     *
     * <p>division_code 为 text[]，存储 /system/area/tree 的 id（字符串形式）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select unnest(division_code) as area_code",
            "  from yz_irrigation_district d",
            "  left join yz_water_facility_base b on b.id = d.facility_id",
            "  where d.facility_id is not null",
            "    and d.division_code is not null",
            "    and COALESCE(d.deleted, 0) = 0",
            "    and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            ") t",
            "where t.area_code is not null and btrim(t.area_code) <> ''",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectDivisionCountGroup();

    /**
     * 按区划代码集合统计灌区总数（包含子级匹配）。
     */
    @Select({
            "select",
            "  count(1) as total_count,",
            "  coalesce(sum(d.actual_irrigable_area), 0) as total_actual_irrigable_area,",
            "  coalesce(sum(d.basic_farmland_area_km2), 0) as total_basic_farmland_area_km2,",
            "  coalesce(sum(d.main_canal_length_m), 0) as total_main_canal_length_m",
            "from yz_irrigation_district d",
            "left join yz_water_facility_base b on b.id = d.facility_id",
            "where d.facility_id is not null",
            "  and d.division_code is not null",
            "  and d.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(d.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)"
    })
    Map<String, Object> selectSummaryByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 查询区划代码集合下的灌区简表（不分页，用于首页展示）。
     *
     * <p>包含 GeoJSON 几何（从设施基础表读取），便于在地图上展示并绑定点击事件。</p>
     */
    @Select({
            "select",
            "  d.id as irrigation_district_id,",
            "  d.facility_id as facility_base_id,",
            "  d.irrigation_district_name as irrigation_district_name,",
            "  d.actual_irrigable_area as actual_irrigable_area,",
            "  d.basic_farmland_area_km2 as basic_farmland_area_km2,",
            "  st_asgeojson(b.geom) as geometry_geojson",
            "from yz_irrigation_district d",
            "left join yz_water_facility_base b on b.id = d.facility_id",
            "where d.facility_id is not null",
            "  and d.division_code is not null",
            "  and d.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(d.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            "order by d.irrigation_district_name asc, d.create_time desc"
    })
    List<Map<String, Object>> selectIrrigationDistrictListByDivisionCodes(@Param("areaCodes") String[] areaCodes);
}
