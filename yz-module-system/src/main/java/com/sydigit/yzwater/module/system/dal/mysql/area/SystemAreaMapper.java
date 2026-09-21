package com.sydigit.yzwater.module.system.dal.mysql.area;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SystemAreaMapper extends BaseMapperX<SystemAreaDO> {

    default long selectCountByParentId(Long parentId) {
        return selectCount(new LambdaQueryWrapperX<SystemAreaDO>()
                .eq(SystemAreaDO::getParentId, parentId));
    }

    default List<SystemAreaDO> selectAll() {
        return selectList(new LambdaQueryWrapperX<SystemAreaDO>()
                .orderByAsc(SystemAreaDO::getSort)
                .orderByAsc(SystemAreaDO::getId));
    }

    @Select("""
            SELECT
                id,
                parent_id,
                name,
                type,
                sort,
                ST_AsText(gemo) AS gemo,
                ST_AsGeoJSON(gemo) AS gemo_geo_json,
                creator,
                create_time,
                updater,
                update_time,
                deleted
            FROM system_area
            WHERE id = #{id}
              AND deleted = 0
            """)
    SystemAreaDO selectByIdWithGemo(@Param("id") Long id);

    @Select("""
            <script>
            SELECT
                id,
                name
            FROM system_area
            WHERE deleted = 0
              AND name IN
            <foreach collection="names" item="name" open="(" separator="," close=")">
                #{name}
            </foreach>
            </script>
            """)
    List<SystemAreaDO> selectByNames(@Param("names") List<String> names);

    @Select("""
            <script>
            SELECT
                id,
                parent_id,
                name,
                type,
                sort,
                ST_AsText(gemo) AS gemo,
                ST_AsGeoJSON(gemo) AS gemo_geo_json,
                creator,
                create_time,
                updater,
                update_time,
                deleted
            FROM system_area
            WHERE deleted = 0
              AND id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    List<SystemAreaDO> selectByIdsWithGemo(@Param("ids") List<Long> ids);

    @Update("""
            UPDATE system_area
            SET gemo = CASE
                    WHEN #{gemo} IS NULL OR #{gemo} = '' THEN NULL
                    ELSE ST_Force2D(ST_GeomFromText(#{gemo}, 4490))
                END,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND deleted = 0
            """)
    int updateGemoById(@Param("id") Long id, @Param("gemo") String gemo);

    @Update("""
            UPDATE system_area
            SET gemo = CASE
                    WHEN #{geoJson} IS NULL OR #{geoJson} = '' THEN NULL
                    ELSE ST_SetSRID(ST_Force2D(ST_GeomFromGeoJSON(#{geoJson})), 4490)
                END,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND deleted = 0
            """)
    int updateGemoByIdFromGeoJson(@Param("id") Long id, @Param("geoJson") String geoJson);

    @Update("""
            UPDATE system_area
            SET gemo = CASE
                    WHEN #{geoJson} IS NULL OR #{geoJson} = '' THEN NULL
                    WHEN #{sourceSrid} IS NULL OR #{sourceSrid} = 4490 THEN ST_SetSRID(ST_Force2D(ST_GeomFromGeoJSON(#{geoJson})), 4490)
                    ELSE ST_Transform(ST_SetSRID(ST_Force2D(ST_GeomFromGeoJSON(#{geoJson})), #{sourceSrid}), 4490)
                END,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND deleted = 0
            """)
    int updateGemoByIdFromGeoJsonWithTransform(@Param("id") Long id,
                                              @Param("geoJson") String geoJson,
                                              @Param("sourceSrid") Integer sourceSrid);

    @Update("""
            UPDATE system_area
            SET deleted = 1,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND deleted = 0
            """)
    int deleteByIdLogical(@Param("id") Long id);

    @Delete("DELETE FROM system_area")
    int deleteAllPhysical();
}
