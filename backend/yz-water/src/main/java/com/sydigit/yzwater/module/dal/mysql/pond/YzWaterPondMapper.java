package com.sydigit.yzwater.module.dal.mysql.pond;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.pond.YzWaterPondDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 坑塘业务表 Mapper
 */
@Mapper
public interface YzWaterPondMapper extends BaseMapperX<YzWaterPondDO> {

    default LambdaQueryWrapper<YzWaterPondDO> buildQueryWrapper(WaterPondPageReqVO reqVO) {
        return buildQueryWrapper(reqVO, Collections.emptyList());
    }

    default LambdaQueryWrapper<YzWaterPondDO> buildQueryWrapper(WaterPondPageReqVO reqVO,
                                                                Collection<String> villageCodes) {
        return buildFilterWrapper(reqVO, villageCodes)
                .orderByDesc(YzWaterPondDO::getUpdateTime, YzWaterPondDO::getCreateTime);
    }

    /** 统计/筛选共用条件，不含排序，避免拖慢聚合查询 */
    default LambdaQueryWrapper<YzWaterPondDO> buildFilterWrapper(WaterPondPageReqVO reqVO,
                                                                 Collection<String> villageCodes) {
        WaterPondPageReqVO query = reqVO == null ? new WaterPondPageReqVO() : reqVO;
        LambdaQueryWrapper<YzWaterPondDO> wrapper = new LambdaQueryWrapper<YzWaterPondDO>()
                .like(StrUtil.isNotBlank(query.getResourceName()),
                        YzWaterPondDO::getResourceName, query.getResourceName())
                .like(StrUtil.isNotBlank(query.getResourceCode()),
                        YzWaterPondDO::getResourceCode, query.getResourceCode());
        if (villageCodes != null && !villageCodes.isEmpty()) {
            wrapper.in(YzWaterPondDO::getVillageCode, villageCodes);
        }
        return wrapper
                .like(StrUtil.isNotBlank(query.getVillageName()),
                        YzWaterPondDO::getVillageName, query.getVillageName())
                .like(StrUtil.isNotBlank(query.getLocationDesc()),
                        YzWaterPondDO::getLocationDesc, query.getLocationDesc())
                .like(StrUtil.isNotBlank(query.getOwnerUnit()),
                        YzWaterPondDO::getOwnerUnit, query.getOwnerUnit())
                .eq(StrUtil.isNotBlank(query.getOwnershipType()),
                        YzWaterPondDO::getOwnershipType, query.getOwnershipType())
                .like(StrUtil.isNotBlank(query.getLandType()),
                        YzWaterPondDO::getLandType, query.getLandType())
                .eq(StrUtil.isNotBlank(query.getUsageStatus()),
                        YzWaterPondDO::getUsageStatus, query.getUsageStatus())
                .eq(StrUtil.isNotBlank(query.getResourceNature()),
                        YzWaterPondDO::getResourceNature, query.getResourceNature())
                .eq(StrUtil.isNotBlank(query.getOccupationStatus()),
                        YzWaterPondDO::getOccupationStatus, query.getOccupationStatus())
                .eq(StrUtil.isNotBlank(query.getResourceType()),
                        YzWaterPondDO::getResourceType, query.getResourceType())
                .like(StrUtil.isNotBlank(query.getRemark()),
                        YzWaterPondDO::getRemark, query.getRemark())
                .like(StrUtil.isNotBlank(query.getSurveyor()),
                        YzWaterPondDO::getSurveyor, query.getSurveyor())
                .like(StrUtil.isNotBlank(query.getEastTo()),
                        YzWaterPondDO::getEastTo, query.getEastTo())
                .like(StrUtil.isNotBlank(query.getSouthTo()),
                        YzWaterPondDO::getSouthTo, query.getSouthTo())
                .like(StrUtil.isNotBlank(query.getWestTo()),
                        YzWaterPondDO::getWestTo, query.getWestTo())
                .like(StrUtil.isNotBlank(query.getNorthTo()),
                        YzWaterPondDO::getNorthTo, query.getNorthTo())
                .ge(query.getAreaMuMin() != null, YzWaterPondDO::getAreaMu, query.getAreaMuMin())
                .le(query.getAreaMuMax() != null, YzWaterPondDO::getAreaMu, query.getAreaMuMax())
                .ge(query.getAreaSqmMin() != null, YzWaterPondDO::getAreaSqm, query.getAreaSqmMin())
                .le(query.getAreaSqmMax() != null, YzWaterPondDO::getAreaSqm, query.getAreaSqmMax())
                .ge(query.getOccupyFarmAreaMin() != null, YzWaterPondDO::getOccupyFarmArea, query.getOccupyFarmAreaMin())
                .le(query.getOccupyFarmAreaMax() != null, YzWaterPondDO::getOccupyFarmArea, query.getOccupyFarmAreaMax());
    }

    default QueryWrapper<YzWaterPondDO> buildFilterQueryWrapper(WaterPondPageReqVO reqVO,
                                                                Collection<String> villageCodes) {
        WaterPondPageReqVO query = reqVO == null ? new WaterPondPageReqVO() : reqVO;
        QueryWrapper<YzWaterPondDO> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getResourceName()), "resource_name", query.getResourceName())
                .like(StrUtil.isNotBlank(query.getResourceCode()), "resource_code", query.getResourceCode());
        if (villageCodes != null && !villageCodes.isEmpty()) {
            wrapper.in("village_code", villageCodes);
        }
        return wrapper
                .like(StrUtil.isNotBlank(query.getVillageName()), "village_name", query.getVillageName())
                .like(StrUtil.isNotBlank(query.getLocationDesc()), "location_desc", query.getLocationDesc())
                .like(StrUtil.isNotBlank(query.getOwnerUnit()), "owner_unit", query.getOwnerUnit())
                .eq(StrUtil.isNotBlank(query.getOwnershipType()), "ownership_type", query.getOwnershipType())
                .like(StrUtil.isNotBlank(query.getLandType()), "land_type", query.getLandType())
                .eq(StrUtil.isNotBlank(query.getUsageStatus()), "usage_status", query.getUsageStatus())
                .eq(StrUtil.isNotBlank(query.getResourceNature()), "resource_nature", query.getResourceNature())
                .eq(StrUtil.isNotBlank(query.getOccupationStatus()), "occupation_status", query.getOccupationStatus())
                .eq(StrUtil.isNotBlank(query.getResourceType()), "resource_type", query.getResourceType())
                .like(StrUtil.isNotBlank(query.getRemark()), "remark", query.getRemark())
                .like(StrUtil.isNotBlank(query.getSurveyor()), "surveyor", query.getSurveyor())
                .like(StrUtil.isNotBlank(query.getEastTo()), "east_to", query.getEastTo())
                .like(StrUtil.isNotBlank(query.getSouthTo()), "south_to", query.getSouthTo())
                .like(StrUtil.isNotBlank(query.getWestTo()), "west_to", query.getWestTo())
                .like(StrUtil.isNotBlank(query.getNorthTo()), "north_to", query.getNorthTo())
                .ge(query.getAreaMuMin() != null, "area_mu", query.getAreaMuMin())
                .le(query.getAreaMuMax() != null, "area_mu", query.getAreaMuMax())
                .ge(query.getAreaSqmMin() != null, "area_sqm", query.getAreaSqmMin())
                .le(query.getAreaSqmMax() != null, "area_sqm", query.getAreaSqmMax())
                .ge(query.getOccupyFarmAreaMin() != null, "occupy_farm_area", query.getOccupyFarmAreaMin())
                .le(query.getOccupyFarmAreaMax() != null, "occupy_farm_area", query.getOccupyFarmAreaMax());
    }

    default Map<String, Object> selectOverviewStats(WaterPondPageReqVO reqVO, Collection<String> villageCodes) {
        QueryWrapper<YzWaterPondDO> wrapper = buildFilterQueryWrapper(reqVO, villageCodes);
        wrapper.select(
                "count(1) as total_count",
                "coalesce(sum(area_sqm), 0) as total_area_sqm",
                "coalesce(sum(area_mu), 0) as total_area_mu",
                "count(distinct nullif(btrim(village_code), '')) as village_code_count"
        );
        List<Map<String, Object>> rows = selectMaps(wrapper);
        return CollUtil.isEmpty(rows) ? Collections.emptyMap() : rows.get(0);
    }

    default List<Map<String, Object>> selectGroupByVillageCode(WaterPondPageReqVO reqVO,
                                                               Collection<String> villageCodes) {
        QueryWrapper<YzWaterPondDO> wrapper = buildFilterQueryWrapper(reqVO, villageCodes);
        wrapper.select(
                "coalesce(nullif(btrim(village_code), ''), '') as code",
                "max(nullif(btrim(village_name), '')) as name",
                "count(1) as cnt",
                "coalesce(sum(area_sqm), 0) as area_sqm"
        );
        wrapper.last("group by coalesce(nullif(btrim(village_code), ''), '') order by cnt desc");
        return selectMaps(wrapper);
    }

    default List<Map<String, Object>> selectGroupByColumn(String column,
                                                          WaterPondPageReqVO reqVO,
                                                          Collection<String> villageCodes) {
        if (!"ownership_type".equals(column) && !"usage_status".equals(column) && !"resource_type".equals(column)) {
            return List.of();
        }
        String expr = "coalesce(nullif(btrim(" + column + "), ''), '未填写')";
        QueryWrapper<YzWaterPondDO> wrapper = buildFilterQueryWrapper(reqVO, villageCodes);
        wrapper.select(expr + " as name", "count(1) as cnt", "coalesce(sum(area_sqm), 0) as area_sqm");
        wrapper.last("group by " + expr + " order by cnt desc");
        return selectMaps(wrapper);
    }

    @Select({
            "select resource_code",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and resource_code is not null",
            "  and btrim(resource_code) <> ''"
    })
    List<String> selectAllResourceCodes();

    @Select({
            "select distinct usage_status",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and usage_status is not null",
            "  and btrim(usage_status) <> ''",
            "order by usage_status"
    })
    List<String> selectDistinctUsageStatus();

    @Select({
            "select distinct resource_nature",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and resource_nature is not null",
            "  and btrim(resource_nature) <> ''",
            "order by resource_nature"
    })
    List<String> selectDistinctResourceNature();

    @Select({
            "select distinct ownership_type",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and ownership_type is not null",
            "  and btrim(ownership_type) <> ''",
            "order by ownership_type"
    })
    List<String> selectDistinctOwnershipType();

    @Select({
            "select distinct occupation_status",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and occupation_status is not null",
            "  and btrim(occupation_status) <> ''",
            "order by occupation_status"
    })
    List<String> selectDistinctOccupationStatus();

    @Select({
            "select distinct resource_type",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and resource_type is not null",
            "  and btrim(resource_type) <> ''",
            "order by resource_type"
    })
    List<String> selectDistinctResourceType();

    default List<YzWaterPondDO> selectByResourceCodes(Collection<String> resourceCodes) {
        if (resourceCodes == null || resourceCodes.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<YzWaterPondDO>()
                .in(YzWaterPondDO::getResourceCode, resourceCodes));
    }

    /**
     * 按 village_code 分组统计坑塘数量（用于一张图左侧区划数量）。
     */
    @Select({
            "select village_code as area_code, count(1) as cnt",
            "from yz_water_pond",
            "where COALESCE(deleted, 0) = 0",
            "  and village_code is not null",
            "  and btrim(village_code) <> ''",
            "group by village_code"
    })
    List<Map<String, Object>> selectVillageCodeCountGroup();

    /**
     * 大屏坑塘数量（区划 + 视口 bbox 过滤）。
     */
    @Select({
            "<script>",
            "select count(1)",
            "from yz_water_pond p",
            "where coalesce(p.deleted, 0) = 0",
            "<if test='id != null'>",
            "  and p.id = #{id}",
            "</if>",
            "<if test='villageCodes != null and villageCodes.length > 0'>",
            "  and p.village_code in",
            "  <foreach collection='villageCodes' item='code' open='(' separator=',' close=')'>",
            "    #{code}",
            "  </foreach>",
            "</if>",
            "<if test='minLon != null and maxLon != null and minLat != null and maxLat != null'>",
            "  and p.center_lon is not null",
            "  and p.center_lat is not null",
            "  and p.center_lon &gt;= #{minLon}",
            "  and p.center_lon &lt;= #{maxLon}",
            "  and p.center_lat &gt;= #{minLat}",
            "  and p.center_lat &lt;= #{maxLat}",
            "</if>",
            "<if test='q != null'>",
            "  <if test='q.resourceName != null and q.resourceName != \"\"'>",
            "    AND p.resource_name ILIKE ('%' || CAST(#{q.resourceName} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.resourceCode != null and q.resourceCode != \"\"'>",
            "    AND p.resource_code ILIKE ('%' || CAST(#{q.resourceCode} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.locationDesc != null and q.locationDesc != \"\"'>",
            "    AND p.location_desc ILIKE ('%' || CAST(#{q.locationDesc} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.ownerUnit != null and q.ownerUnit != \"\"'>",
            "    AND p.owner_unit ILIKE ('%' || CAST(#{q.ownerUnit} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.ownershipType != null and q.ownershipType != \"\"'>",
            "    AND p.ownership_type = CAST(#{q.ownershipType} AS TEXT)",
            "  </if>",
            "  <if test='q.resourceType != null and q.resourceType != \"\"'>",
            "    AND p.resource_type = CAST(#{q.resourceType} AS TEXT)",
            "  </if>",
            "  <if test='q.landType != null and q.landType != \"\"'>",
            "    AND p.land_type ILIKE ('%' || CAST(#{q.landType} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.usageStatus != null and q.usageStatus != \"\"'>",
            "    AND p.usage_status = CAST(#{q.usageStatus} AS TEXT)",
            "  </if>",
            "  <if test='q.resourceNature != null and q.resourceNature != \"\"'>",
            "    AND p.resource_nature = CAST(#{q.resourceNature} AS TEXT)",
            "  </if>",
            "  <if test='q.occupationStatus != null and q.occupationStatus != \"\"'>",
            "    AND p.occupation_status = CAST(#{q.occupationStatus} AS TEXT)",
            "  </if>",
            "  <if test='q.villageName != null and q.villageName != \"\"'>",
            "    AND p.village_name ILIKE ('%' || CAST(#{q.villageName} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.remark != null and q.remark != \"\"'>",
            "    AND p.remark ILIKE ('%' || CAST(#{q.remark} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.surveyor != null and q.surveyor != \"\"'>",
            "    AND p.surveyor ILIKE ('%' || CAST(#{q.surveyor} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.eastTo != null and q.eastTo != \"\"'>",
            "    AND p.east_to ILIKE ('%' || CAST(#{q.eastTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.southTo != null and q.southTo != \"\"'>",
            "    AND p.south_to ILIKE ('%' || CAST(#{q.southTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.westTo != null and q.westTo != \"\"'>",
            "    AND p.west_to ILIKE ('%' || CAST(#{q.westTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.northTo != null and q.northTo != \"\"'>",
            "    AND p.north_to ILIKE ('%' || CAST(#{q.northTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.areaMuMin != null'>",
            "    AND p.area_mu &gt;= CAST(#{q.areaMuMin} AS NUMERIC)",
            "  </if>",
            "  <if test='q.areaMuMax != null'>",
            "    AND p.area_mu &lt;= CAST(#{q.areaMuMax} AS NUMERIC)",
            "  </if>",
            "  <if test='q.areaSqmMin != null'>",
            "    AND p.area_sqm &gt;= CAST(#{q.areaSqmMin} AS NUMERIC)",
            "  </if>",
            "  <if test='q.areaSqmMax != null'>",
            "    AND p.area_sqm &lt;= CAST(#{q.areaSqmMax} AS NUMERIC)",
            "  </if>",
            "  <if test='q.occupyFarmAreaMin != null'>",
            "    AND p.occupy_farm_area &gt;= CAST(#{q.occupyFarmAreaMin} AS NUMERIC)",
            "  </if>",
            "  <if test='q.occupyFarmAreaMax != null'>",
            "    AND p.occupy_farm_area &lt;= CAST(#{q.occupyFarmAreaMax} AS NUMERIC)",
            "  </if>",
            "</if>",
            "</script>"
    })
    Long countScreenPondPage(@Param("id") Long id,
                             @Param("villageCodes") String[] villageCodes,
                             @Param("minLon") BigDecimal minLon,
                             @Param("minLat") BigDecimal minLat,
                             @Param("maxLon") BigDecimal maxLon,
                             @Param("maxLat") BigDecimal maxLat,
                             @Param("q") WaterPondPageReqVO q);

    /**
     * 大屏坑塘分页列表：属性 + 中心点 + GeoJSON 面（库内一次查询）。
     */
    @Select({
            "<script>",
            "select",
            "  p.id,",
            "  p.facility_id,",
            "  p.resource_code,",
            "  p.resource_name,",
            "  p.location_desc,",
            "  p.village_name,",
            "  p.village_code,",
            "  p.owner_unit,",
            "  p.owner_person,",
            "  p.ownership_type,",
            "  p.land_type,",
            "  p.area_sqm,",
            "  p.area_mu,",
            "  p.occupy_farm_area,",
            "  p.east_to,",
            "  p.south_to,",
            "  p.west_to,",
            "  p.north_to,",
            "  p.usage_status,",
            "  p.resource_nature,",
            "  p.occupation_status,",
            "  p.surveyor,",
            "  p.surveyor_phone,",
            "  p.remark,",
            "  p.resource_type,",
            "  p.center_lon,",
            "  p.center_lat,",
            "  <choose>",
            "    <when test='includeGeometry != null and includeGeometry == false'>",
            "      null as geometry_geojson",
            "    </when>",
            "    <otherwise>",
            "      case",
            "        when p.geom is not null and not st_isempty(p.geom) then st_asgeojson(p.geom)",
            "        when b.geom is not null and not st_isempty(b.geom) then st_asgeojson(b.geom)",
            "        else null",
            "      end as geometry_geojson",
            "    </otherwise>",
            "  </choose>",
            "from yz_water_pond p",
            "left join yz_water_facility_base b on b.id = p.facility_id and coalesce(b.deleted, 0) = 0",
            "where coalesce(p.deleted, 0) = 0",
            "<if test='id != null'>",
            "  and p.id = #{id}",
            "</if>",
            "<if test='villageCodes != null and villageCodes.length > 0'>",
            "  and p.village_code in",
            "  <foreach collection='villageCodes' item='code' open='(' separator=',' close=')'>",
            "    #{code}",
            "  </foreach>",
            "</if>",
            "<if test='minLon != null and maxLon != null and minLat != null and maxLat != null'>",
            "  and p.center_lon is not null",
            "  and p.center_lat is not null",
            "  and p.center_lon &gt;= #{minLon}",
            "  and p.center_lon &lt;= #{maxLon}",
            "  and p.center_lat &gt;= #{minLat}",
            "  and p.center_lat &lt;= #{maxLat}",
            "</if>",
            "<if test='q != null'>",
            "  <if test='q.resourceName != null and q.resourceName != \"\"'>",
            "    AND p.resource_name ILIKE ('%' || CAST(#{q.resourceName} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.resourceCode != null and q.resourceCode != \"\"'>",
            "    AND p.resource_code ILIKE ('%' || CAST(#{q.resourceCode} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.locationDesc != null and q.locationDesc != \"\"'>",
            "    AND p.location_desc ILIKE ('%' || CAST(#{q.locationDesc} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.ownerUnit != null and q.ownerUnit != \"\"'>",
            "    AND p.owner_unit ILIKE ('%' || CAST(#{q.ownerUnit} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.ownershipType != null and q.ownershipType != \"\"'>",
            "    AND p.ownership_type = CAST(#{q.ownershipType} AS TEXT)",
            "  </if>",
            "  <if test='q.resourceType != null and q.resourceType != \"\"'>",
            "    AND p.resource_type = CAST(#{q.resourceType} AS TEXT)",
            "  </if>",
            "  <if test='q.landType != null and q.landType != \"\"'>",
            "    AND p.land_type ILIKE ('%' || CAST(#{q.landType} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.usageStatus != null and q.usageStatus != \"\"'>",
            "    AND p.usage_status = CAST(#{q.usageStatus} AS TEXT)",
            "  </if>",
            "  <if test='q.resourceNature != null and q.resourceNature != \"\"'>",
            "    AND p.resource_nature = CAST(#{q.resourceNature} AS TEXT)",
            "  </if>",
            "  <if test='q.occupationStatus != null and q.occupationStatus != \"\"'>",
            "    AND p.occupation_status = CAST(#{q.occupationStatus} AS TEXT)",
            "  </if>",
            "  <if test='q.villageName != null and q.villageName != \"\"'>",
            "    AND p.village_name ILIKE ('%' || CAST(#{q.villageName} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.remark != null and q.remark != \"\"'>",
            "    AND p.remark ILIKE ('%' || CAST(#{q.remark} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.surveyor != null and q.surveyor != \"\"'>",
            "    AND p.surveyor ILIKE ('%' || CAST(#{q.surveyor} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.eastTo != null and q.eastTo != \"\"'>",
            "    AND p.east_to ILIKE ('%' || CAST(#{q.eastTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.southTo != null and q.southTo != \"\"'>",
            "    AND p.south_to ILIKE ('%' || CAST(#{q.southTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.westTo != null and q.westTo != \"\"'>",
            "    AND p.west_to ILIKE ('%' || CAST(#{q.westTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.northTo != null and q.northTo != \"\"'>",
            "    AND p.north_to ILIKE ('%' || CAST(#{q.northTo} AS TEXT) || '%')",
            "  </if>",
            "  <if test='q.areaMuMin != null'>",
            "    AND p.area_mu &gt;= CAST(#{q.areaMuMin} AS NUMERIC)",
            "  </if>",
            "  <if test='q.areaMuMax != null'>",
            "    AND p.area_mu &lt;= CAST(#{q.areaMuMax} AS NUMERIC)",
            "  </if>",
            "  <if test='q.areaSqmMin != null'>",
            "    AND p.area_sqm &gt;= CAST(#{q.areaSqmMin} AS NUMERIC)",
            "  </if>",
            "  <if test='q.areaSqmMax != null'>",
            "    AND p.area_sqm &lt;= CAST(#{q.areaSqmMax} AS NUMERIC)",
            "  </if>",
            "  <if test='q.occupyFarmAreaMin != null'>",
            "    AND p.occupy_farm_area &gt;= CAST(#{q.occupyFarmAreaMin} AS NUMERIC)",
            "  </if>",
            "  <if test='q.occupyFarmAreaMax != null'>",
            "    AND p.occupy_farm_area &lt;= CAST(#{q.occupyFarmAreaMax} AS NUMERIC)",
            "  </if>",
            "</if>",
            "order by p.id desc",
            "limit #{pageSize} offset #{offset}",
            "</script>"
    })
    List<Map<String, Object>> selectScreenPondPage(@Param("id") Long id,
                                                   @Param("villageCodes") String[] villageCodes,
                                                   @Param("minLon") BigDecimal minLon,
                                                   @Param("minLat") BigDecimal minLat,
                                                   @Param("maxLon") BigDecimal maxLon,
                                                   @Param("maxLat") BigDecimal maxLat,
                                                   @Param("includeGeometry") Boolean includeGeometry,
                                                   @Param("offset") int offset,
                                                   @Param("pageSize") int pageSize,
                                                   @Param("q") WaterPondPageReqVO q);

    /**
     * 坑塘矢量瓦片（MVT）。按 WebMercator 瓦片 z/x/y 现查现切。
     * 低缩放简化保性能；z&gt;=15 不简化，避免镜头变化时轮廓抖动、尖三角。
     *
     * <p>图层名：ponds。属性：id(字符串，避免前端精度丢失), resource_name, resource_code,
     * ownership_type, resource_type, usage_status, area_sqm, area_mu, center_lon, center_lat。</p>
     *
     * <p>过滤条件与台账 {@link WaterPondPageReqVO} 对齐；可选缓冲区（centerLon/centerLat/bufferRadiusM，单位米）。</p>
     */
    @Select({
            "<script>",
            "WITH bounds AS (",
            // z/x/y 已在 Service 层校验为合法整数，用 ${} 避免 PG 无法推断 CASE WHEN #{z} 的类型
            "  SELECT ST_TileEnvelope(${z}, ${x}, ${y}) AS geom",
            "),",
            "bounds_4326 AS (",
            "  SELECT ST_Transform(geom, 4326) AS geom FROM bounds",
            "),",
            "src AS (",
            "  SELECT",
            "    p.id::text AS id,",
            "    p.resource_name,",
            "    p.resource_code,",
            "    p.ownership_type,",
            "    p.resource_type,",
            "    p.usage_status,",
            "    p.area_sqm,",
            "    p.area_mu,",
            "    p.center_lon,",
            "    p.center_lat,",
            "    CASE",
            "      WHEN p.geom IS NOT NULL AND NOT ST_IsEmpty(p.geom) THEN p.geom",
            "      WHEN b.geom IS NOT NULL AND NOT ST_IsEmpty(b.geom) THEN b.geom",
            "      ELSE NULL",
            "    END AS geom",
            "  FROM yz_water_pond p",
            "  LEFT JOIN yz_water_facility_base b ON b.id = p.facility_id AND COALESCE(b.deleted, 0) = 0",
            "  WHERE COALESCE(p.deleted, 0) = 0",
            "  <if test='villageCodes != null and villageCodes.length &gt; 0'>",
            "    AND p.village_code IN",
            "    <foreach collection='villageCodes' item='code' open='(' separator=',' close=')'>",
            "      CAST(#{code} AS TEXT)",
            "    </foreach>",
            "  </if>",
            "  <if test='q != null'>",
            "    <if test='q.resourceName != null and q.resourceName != \"\"'>",
            "      AND p.resource_name ILIKE ('%' || CAST(#{q.resourceName} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.resourceCode != null and q.resourceCode != \"\"'>",
            "      AND p.resource_code ILIKE ('%' || CAST(#{q.resourceCode} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.locationDesc != null and q.locationDesc != \"\"'>",
            "      AND p.location_desc ILIKE ('%' || CAST(#{q.locationDesc} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.ownerUnit != null and q.ownerUnit != \"\"'>",
            "      AND p.owner_unit ILIKE ('%' || CAST(#{q.ownerUnit} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.ownershipType != null and q.ownershipType != \"\"'>",
            "      AND p.ownership_type = CAST(#{q.ownershipType} AS TEXT)",
            "    </if>",
            "    <if test='q.resourceType != null and q.resourceType != \"\"'>",
            "      AND p.resource_type = CAST(#{q.resourceType} AS TEXT)",
            "    </if>",
            "    <if test='q.landType != null and q.landType != \"\"'>",
            "      AND p.land_type ILIKE ('%' || CAST(#{q.landType} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.usageStatus != null and q.usageStatus != \"\"'>",
            "      AND p.usage_status = CAST(#{q.usageStatus} AS TEXT)",
            "    </if>",
            "    <if test='q.resourceNature != null and q.resourceNature != \"\"'>",
            "      AND p.resource_nature = CAST(#{q.resourceNature} AS TEXT)",
            "    </if>",
            "    <if test='q.occupationStatus != null and q.occupationStatus != \"\"'>",
            "      AND p.occupation_status = CAST(#{q.occupationStatus} AS TEXT)",
            "    </if>",
            "    <if test='q.villageName != null and q.villageName != \"\"'>",
            "      AND p.village_name ILIKE ('%' || CAST(#{q.villageName} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.remark != null and q.remark != \"\"'>",
            "      AND p.remark ILIKE ('%' || CAST(#{q.remark} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.surveyor != null and q.surveyor != \"\"'>",
            "      AND p.surveyor ILIKE ('%' || CAST(#{q.surveyor} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.eastTo != null and q.eastTo != \"\"'>",
            "      AND p.east_to ILIKE ('%' || CAST(#{q.eastTo} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.southTo != null and q.southTo != \"\"'>",
            "      AND p.south_to ILIKE ('%' || CAST(#{q.southTo} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.westTo != null and q.westTo != \"\"'>",
            "      AND p.west_to ILIKE ('%' || CAST(#{q.westTo} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.northTo != null and q.northTo != \"\"'>",
            "      AND p.north_to ILIKE ('%' || CAST(#{q.northTo} AS TEXT) || '%')",
            "    </if>",
            "    <if test='q.areaMuMin != null'>",
            "      AND p.area_mu &gt;= CAST(#{q.areaMuMin} AS NUMERIC)",
            "    </if>",
            "    <if test='q.areaMuMax != null'>",
            "      AND p.area_mu &lt;= CAST(#{q.areaMuMax} AS NUMERIC)",
            "    </if>",
            "    <if test='q.areaSqmMin != null'>",
            "      AND p.area_sqm &gt;= CAST(#{q.areaSqmMin} AS NUMERIC)",
            "    </if>",
            "    <if test='q.areaSqmMax != null'>",
            "      AND p.area_sqm &lt;= CAST(#{q.areaSqmMax} AS NUMERIC)",
            "    </if>",
            "    <if test='q.occupyFarmAreaMin != null'>",
            "      AND p.occupy_farm_area &gt;= CAST(#{q.occupyFarmAreaMin} AS NUMERIC)",
            "    </if>",
            "    <if test='q.occupyFarmAreaMax != null'>",
            "      AND p.occupy_farm_area &lt;= CAST(#{q.occupyFarmAreaMax} AS NUMERIC)",
            "    </if>",
            "  </if>",
            "),",
            "mvtgeom AS (",
            "  SELECT",
            "    ST_AsMVTGeom(",
            "      CASE",
            "        WHEN ${z} &gt;= 15 THEN ST_Transform(s.geom, 3857)",
            "        ELSE ST_SimplifyPreserveTopology(",
            "          ST_Transform(s.geom, 3857),",
            "          CASE",
            "            WHEN ${z} &lt;= 8 THEN 80.0",
            "            WHEN ${z} &lt;= 10 THEN 40.0",
            "            WHEN ${z} &lt;= 12 THEN 15.0",
            "            ELSE 6.0",
            "          END",
            "        )",
            "      END,",
            "      bounds.geom,",
            "      4096,",
            "      256,",
            "      true",
            "    ) AS geom,",
            "    s.id,",
            "    s.resource_name,",
            "    s.resource_code,",
            "    s.ownership_type,",
            "    s.resource_type,",
            "    s.usage_status,",
            "    s.area_sqm,",
            "    s.area_mu,",
            "    s.center_lon,",
            "    s.center_lat",
            "  FROM src s",
            "  CROSS JOIN bounds",
            "  CROSS JOIN bounds_4326",
            "  WHERE s.geom IS NOT NULL",
            "    AND s.geom &amp;&amp; bounds_4326.geom",
            "    AND ST_Intersects(s.geom, bounds_4326.geom)",
            "  <if test='centerLon != null and centerLat != null and bufferRadiusM != null'>",
            "    AND ST_DWithin(",
            "      s.geom::geography,",
            "      ST_SetSRID(ST_MakePoint(CAST(#{centerLon} AS DOUBLE PRECISION), CAST(#{centerLat} AS DOUBLE PRECISION)), 4490)::geography,",
            "      CAST(#{bufferRadiusM} AS DOUBLE PRECISION)",
            "    )",
            "  </if>",
            ")",
            "SELECT ST_AsMVT(mvtgeom.*, 'ponds', 4096, 'geom') AS tile FROM mvtgeom",
            "</script>"
    })
    Map<String, Object> selectPondMvtTile(@Param("z") int z,
                                          @Param("x") int x,
                                          @Param("y") int y,
                                          @Param("villageCodes") String[] villageCodes,
                                          @Param("q") WaterPondPageReqVO q,
                                          @Param("centerLon") BigDecimal centerLon,
                                          @Param("centerLat") BigDecimal centerLat,
                                          @Param("bufferRadiusM") BigDecimal bufferRadiusM);
}
