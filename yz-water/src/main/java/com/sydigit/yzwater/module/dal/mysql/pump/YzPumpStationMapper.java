package com.sydigit.yzwater.module.dal.mysql.pump;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 泵站表 Mapper
 */
@Mapper
public interface YzPumpStationMapper extends BaseMapperX<YzPumpStationDO> {

    default LambdaQueryWrapper<YzPumpStationDO> buildQueryWrapper(PumpStationPageReqVO reqVO) {
        LambdaQueryWrapper<YzPumpStationDO> wrapper = new LambdaQueryWrapper<YzPumpStationDO>()
                .like(StrUtil.isNotBlank(reqVO.getPumpStationName()),
                        YzPumpStationDO::getPumpStationName, reqVO.getPumpStationName())
                .eq(StrUtil.isNotBlank(reqVO.getPumpStationType()),
                        YzPumpStationDO::getPumpStationType, reqVO.getPumpStationType())
                .orderByDesc(YzPumpStationDO::getUpdateTime, YzPumpStationDO::getCreateTime);

        if (StrUtil.isNotBlank(reqVO.getDivisionCode())) {
            // 区划代码存储为 PostgreSQL text[]，这里按单个区划节点筛选：数组中包含该值即可命中
            wrapper.apply("array_position(division_code, {0}) is not null", reqVO.getDivisionCode());
        }
        return wrapper;
    }

    /**
     * 按区划代码（division_code）分组统计泵站数量。
     *
     * <p>division_code 为 text[]，存储 /system/area/tree 的 id（字符串形式）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select unnest(division_code) as area_code",
            "  from yz_pump_station",
            "  where facility_id is not null",
            "    and division_code is not null",
            "    and COALESCE(deleted, 0) = 0",
            ") t",
            "where t.area_code is not null and btrim(t.area_code) <> ''",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectDivisionCountGroup();

    /**
     * 按区划代码集合查询泵站列表（包含子级匹配）。
     *
     * <p>division_code 为 text[]，入参同样按 text[] 传入，使用数组重叠运算符（&&）匹配任意交集。</p>
     */
    @Select({
            "select id, facility_id, pump_station_name, longitude, latitude",
            "from yz_pump_station",
            "where facility_id is not null",
            "  and division_code is not null",
            "  and division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0",
            "order by pump_station_name asc, create_time desc"
    })
    List<YzPumpStationDO> selectListByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 按区划代码集合统计泵站总数（包含子级匹配）。
     */
    @Select({
            "select",
            "  count(1) as total_count,",
            "  coalesce(sum(p.self_flow), 0) as total_self_flow,",
            "  coalesce(sum(p.installed_flow), 0) as total_installed_flow,",
            "  coalesce(sum(p.pumping_flow), 0) as total_pumping_flow",
            "from yz_pump_station p",
            "left join yz_water_facility_base b on b.id = p.facility_id",
            "where p.facility_id is not null",
            "  and p.division_code is not null",
            "  and p.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(p.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)"
    })
    Map<String, Object> selectSummaryByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 按区划代码集合分组统计泵站类型数量（包含子级匹配）。
     */
    @Select({
            "select pump_station_type as pump_station_type, count(1) as cnt",
            "from yz_pump_station p",
            "left join yz_water_facility_base b on b.id = p.facility_id",
            "where p.facility_id is not null",
            "  and p.division_code is not null",
            "  and p.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(p.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            "group by pump_station_type"
    })
    List<Map<String, Object>> selectPumpStationTypeCountByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 查询区划代码集合下的泵站简表（不分页，用于首页展示）。
     */
    @Select({
            "select",
            "  p.id,",
            "  p.facility_id,",
            "  p.pump_station_name,",
            "  p.pump_station_type,",
            "  p.engineering_grade,",
            "  p.longitude,",
            "  p.latitude",
            "from yz_pump_station p",
            "left join yz_water_facility_base b on b.id = p.facility_id",
            "where p.facility_id is not null",
            "  and p.division_code is not null",
            "  and p.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(p.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            "order by p.pump_station_name asc, p.create_time desc"
    })
    List<YzPumpStationDO> selectPumpStationListByDivisionCodes(@Param("areaCodes") String[] areaCodes);
}
