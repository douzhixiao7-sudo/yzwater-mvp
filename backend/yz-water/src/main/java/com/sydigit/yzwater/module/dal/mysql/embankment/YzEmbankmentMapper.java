package com.sydigit.yzwater.module.dal.mysql.embankment;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 堤防信息 Mapper
 */
@Mapper
public interface YzEmbankmentMapper extends BaseMapperX<YzEmbankmentDO> {

    default LambdaQueryWrapper<YzEmbankmentDO> buildQueryWrapper(EmbankmentPageReqVO reqVO) {
        return new LambdaQueryWrapper<YzEmbankmentDO>()
                .like(reqVO.getEmbankmentCode() != null && !reqVO.getEmbankmentCode().isEmpty(),
                        YzEmbankmentDO::getEmbankmentCode, reqVO.getEmbankmentCode())
                .like(reqVO.getEmbankmentName() != null && !reqVO.getEmbankmentName().isEmpty(),
                        YzEmbankmentDO::getEmbankmentName, reqVO.getEmbankmentName())
                .eq(reqVO.getEmbankmentLevel() != null && !reqVO.getEmbankmentLevel().isEmpty(),
                        YzEmbankmentDO::getEmbankmentLevel, reqVO.getEmbankmentLevel())
                .eq(reqVO.getEmbankmentType() != null && !reqVO.getEmbankmentType().isEmpty(),
                        YzEmbankmentDO::getEmbankmentType, reqVO.getEmbankmentType())
                .orderByDesc(YzEmbankmentDO::getCreateTime);
    }

    /**
     * 按区划代码（division_code）分组统计堤防数量。
     *
     * <p>division_code 为 text[]，存储 /system/area/tree 的 id（字符串形式）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select unnest(division_code) as area_code",
            "  from yz_embankment",
            "  where facility_id is not null",
            "    and division_code is not null",
            "    and COALESCE(deleted, 0) = 0",
            ") t",
            "where t.area_code is not null and btrim(t.area_code) <> ''",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectDivisionCountGroup();

    /**
     * 按区划代码集合查询堤防列表（包含子级匹配）。
     *
     * <p>division_code 为 text[]，入参同样按 text[] 传入，使用数组重叠运算符（&&）匹配任意交集。</p>
     */
    @Select({
            "select id, facility_id, embankment_name, longitude, latitude",
            "from yz_embankment",
            "where facility_id is not null",
            "  and division_code is not null",
            "  and division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0",
            "order by embankment_name asc, create_time desc"
    })
    List<YzEmbankmentDO> selectListByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 按区划代码集合统计提防总数（包含子级匹配）。
     */
    @Select({
            "select",
            "  count(1) as total_count,",
            "  coalesce(sum(e.length_m), 0) as total_length_m,",
            "  coalesce(sum(e.standard_length_m), 0) as total_standard_length_m",
            "from yz_embankment e",
            "left join yz_water_facility_base b on b.id = e.facility_id",
            "where e.facility_id is not null",
            "  and e.division_code is not null",
            "  and e.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(e.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)"
    })
    Map<String, Object> selectSummaryByDivisionCodes(@Param("areaCodes") String[] areaCodes);

    /**
     * 查询区划代码集合下的提防简表（不分页，用于首页展示）。
     */
    @Select({
            "select",
            "  e.id,",
            "  e.facility_id,",
            "  e.embankment_name,",
            "  e.embankment_form,",
            "  e.longitude,",
            "  e.latitude",
            "from yz_embankment e",
            "left join yz_water_facility_base b on b.id = e.facility_id",
            "where e.facility_id is not null",
            "  and e.division_code is not null",
            "  and e.division_code && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(e.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            "order by e.embankment_name asc, e.create_time desc"
    })
    List<YzEmbankmentDO> selectEmbankmentListByDivisionCodes(@Param("areaCodes") String[] areaCodes);
}
