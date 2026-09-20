package com.sydigit.yzwater.module.dal.mysql.rivers;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Collection;
import com.sydigit.yzwater.module.dal.mysql.rivers.dto.RiverChiefFacilityGroupRow;
import com.sydigit.yzwater.module.dal.mysql.rivers.dto.RiverChiefInfoGroupRow;

/**
 * 河长信息 Mapper
 */
@Mapper
public interface YzRiverChannelManagementMapper extends BaseMapperX<YzRiverChannelManagementDO> {

    String RESOLVED_REFERENCE_TYPE = "COALESCE(reference_type,"
            + " CASE"
            + " WHEN water_reservoir_id IS NOT NULL THEN 'reservoir'"
            + " WHEN river_section_id IS NOT NULL THEN 'river_section'"
            + " WHEN river_channel_id IS NOT NULL THEN 'river'"
            + " ELSE NULL"
            + " END)";

    String RESOLVED_REFERENCE_ID = "COALESCE(reference_id, water_reservoir_id, river_section_id, river_channel_id)";
    String NORMALIZED_HEAD_NAME = "NULLIF(replace(regexp_replace(COALESCE(head_name, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String NORMALIZED_PARAM_HEAD_NAME = "NULLIF(replace(regexp_replace(COALESCE(#{headName,jdbcType=VARCHAR}, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String RESOLVED_REFERENCE_TYPE_BY_F = "COALESCE(f.reference_type,"
            + " CASE"
            + " WHEN f.water_reservoir_id IS NOT NULL THEN 'reservoir'"
            + " WHEN f.river_section_id IS NOT NULL THEN 'river_section'"
            + " WHEN f.river_channel_id IS NOT NULL THEN 'river'"
            + " ELSE NULL"
            + " END)";
    String RESOLVED_REFERENCE_ID_BY_F = "COALESCE(f.reference_id, f.water_reservoir_id, f.river_section_id, f.river_channel_id)";
    String RESOLVED_REFERENCE_TYPE_BY_M = "COALESCE(m.reference_type,"
            + " CASE"
            + " WHEN m.water_reservoir_id IS NOT NULL THEN 'reservoir'"
            + " WHEN m.river_section_id IS NOT NULL THEN 'river_section'"
            + " WHEN m.river_channel_id IS NOT NULL THEN 'river'"
            + " ELSE NULL"
            + " END)";
    String RESOLVED_REFERENCE_ID_BY_M = "COALESCE(m.reference_id, m.water_reservoir_id, m.river_section_id, m.river_channel_id)";
    String NORMALIZED_HEAD_NAME_BY_M = "NULLIF(replace(regexp_replace(COALESCE(m.head_name, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String NORMALIZED_HEAD_LEVEL = "NULLIF(BTRIM(COALESCE(head_level, '')), '')";
    String NORMALIZED_PARAM_HEAD_LEVEL = "NULLIF(BTRIM(COALESCE(#{headLevel,jdbcType=VARCHAR}, '')), '')";
    String NORMALIZED_HEAD_LEVEL_BY_M = "NULLIF(BTRIM(COALESCE(m.head_level, '')), '')";
    String NORMALIZED_HEAD_LEVEL_BY_F = "NULLIF(BTRIM(COALESCE(f.head_level, '')), '')";
    /** 与 Java {@code normalizeChiefPosition} 一致：去空白/全角空格后为空则 NULL，同名空职务合并为一组 */
    String NORMALIZED_HEAD_POSITION = "NULLIF(replace(regexp_replace(COALESCE(head_position, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String NORMALIZED_HEAD_POSITION_BY_M = "NULLIF(replace(regexp_replace(COALESCE(m.head_position, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String NORMALIZED_HEAD_POSITION_BY_F = "NULLIF(replace(regexp_replace(COALESCE(f.head_position, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String NORMALIZED_PARAM_HEAD_POSITION = "NULLIF(replace(regexp_replace(COALESCE(#{headPosition,jdbcType=VARCHAR}, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    String NORMALIZED_EFFECTIVE_FROM_BY_M = "COALESCE(to_char(m.effective_from, 'YYYY-MM-DD HH24:MI:SS.US'), '')";
    String NORMALIZED_EFFECTIVE_FROM_BY_F = "COALESCE(to_char(f.effective_from, 'YYYY-MM-DD HH24:MI:SS.US'), '')";
    String NORMALIZED_EFFECTIVE_FROM = "COALESCE(to_char(effective_from, 'YYYY-MM-DD HH24:MI:SS.US'), '')";
    String NORMALIZED_PARAM_EFFECTIVE_FROM = "COALESCE(to_char(CAST(#{effectiveFrom,jdbcType=TIMESTAMP} AS timestamp), 'YYYY-MM-DD HH24:MI:SS.US'), '')";
    String TOTAL_CHIEF_QUERY_CONDITION = "COALESCE(reference_type, '') = 'total_chief'";
    String TOTAL_CHIEF_QUERY_CONDITION_BY_M = "COALESCE(m.reference_type, '') = 'total_chief'";
    String TOTAL_CHIEF_EXCLUDE_CONDITION_BY_M = "NOT (" + TOTAL_CHIEF_QUERY_CONDITION_BY_M + ")";
    String BF_REFERENCE_EXCLUDE_CONDITION = "COALESCE(reference_type, '') NOT IN ('river_bf', 'river_section_bf', 'reservoir_bf')";
    String BF_REFERENCE_EXCLUDE_CONDITION_BY_M = "COALESCE(m.reference_type, '') NOT IN ('river_bf', 'river_section_bf', 'reservoir_bf')";
    String BF_REFERENCE_EXCLUDE_CONDITION_BY_F = "COALESCE(f.reference_type, '') NOT IN ('river_bf', 'river_section_bf', 'reservoir_bf')";

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT",
            "    " + RESOLVED_REFERENCE_TYPE + " AS reference_type,",
            "    " + RESOLVED_REFERENCE_ID + " AS reference_id",
            "  FROM yz_river_channel_management",
            "  WHERE effective_to IS NULL AND is_current = 1",
            "    <if test='headLevel != null and headLevel != \"\"'>",
            "      AND head_level = #{headLevel}",
            "    </if>",
            "    <if test='headName != null and headName != \"\"'>",
            "      AND head_name LIKE ('%' || CAST(#{headName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "    </if>",
            "    <if test='referenceType != null and referenceType != \"\"'>",
            "      AND " + RESOLVED_REFERENCE_TYPE + " = #{referenceType}",
            "    </if>",
            "    <choose>",
            "      <when test='referenceType == \"river\"'>",
            "        AND EXISTS (SELECT 1 FROM yz_river_channel r WHERE r.id = " + RESOLVED_REFERENCE_ID + " AND r.deleted = 0)",
            "      </when>",
            "      <when test='referenceType == \"river_section\"'>",
            "        AND EXISTS (SELECT 1 FROM yz_river_section s WHERE s.id = " + RESOLVED_REFERENCE_ID + " AND s.deleted = 0)",
            "      </when>",
            "      <when test='referenceType == \"reservoir\"'>",
            "        AND EXISTS (SELECT 1 FROM yz_water_reservoir w WHERE w.id = " + RESOLVED_REFERENCE_ID + " AND w.deleted = 0)",
            "      </when>",
            "      <otherwise>",
            "        AND (",
            "          (" + RESOLVED_REFERENCE_TYPE + " = 'river' AND EXISTS (SELECT 1 FROM yz_river_channel r WHERE r.id = " + RESOLVED_REFERENCE_ID + " AND r.deleted = 0))",
            "          OR (" + RESOLVED_REFERENCE_TYPE + " = 'river_section' AND EXISTS (SELECT 1 FROM yz_river_section s WHERE s.id = " + RESOLVED_REFERENCE_ID + " AND s.deleted = 0))",
            "          OR (" + RESOLVED_REFERENCE_TYPE + " = 'reservoir' AND EXISTS (SELECT 1 FROM yz_water_reservoir w WHERE w.id = " + RESOLVED_REFERENCE_ID + " AND w.deleted = 0))",
            "        )",
            "      </otherwise>",
            "    </choose>",
            "    <if test='ids != null and ids.size() &gt; 0'>",
            "      AND " + RESOLVED_REFERENCE_ID + " IN",
            "      <foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "        #{id}",
            "      </foreach>",
            "    </if>",
            "  GROUP BY 1, 2",
            ") t",
            "</script>"
    })
    long countCurrentFacilityGroup(@Param("referenceType") String referenceType,
                                  @Param("headLevel") String headLevel,
                                  @Param("headName") String headName,
                                  @Param("ids") List<Long> ids);

    @Select({
            "<script>",
            "SELECT",
            "  " + RESOLVED_REFERENCE_TYPE + " AS referenceType,",
            "  " + RESOLVED_REFERENCE_ID + " AS referenceId,",
            "  MAX(effective_from) AS effectiveFrom",
            "FROM yz_river_channel_management",
            "WHERE effective_to IS NULL AND is_current = 1",
            "  <if test='headLevel != null and headLevel != \"\"'>",
            "    AND head_level = #{headLevel}",
            "  </if>",
            "  <if test='headName != null and headName != \"\"'>",
            "    AND head_name LIKE ('%' || CAST(#{headName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "  </if>",
            "  <if test='referenceType != null and referenceType != \"\"'>",
            "    AND " + RESOLVED_REFERENCE_TYPE + " = #{referenceType}",
            "  </if>",
            "  <choose>",
            "    <when test='referenceType == \"river\"'>",
            "      AND EXISTS (SELECT 1 FROM yz_river_channel r WHERE r.id = " + RESOLVED_REFERENCE_ID + " AND r.deleted = 0)",
            "    </when>",
            "    <when test='referenceType == \"river_section\"'>",
            "      AND EXISTS (SELECT 1 FROM yz_river_section s WHERE s.id = " + RESOLVED_REFERENCE_ID + " AND s.deleted = 0)",
            "    </when>",
            "    <when test='referenceType == \"reservoir\"'>",
            "      AND EXISTS (SELECT 1 FROM yz_water_reservoir w WHERE w.id = " + RESOLVED_REFERENCE_ID + " AND w.deleted = 0)",
            "    </when>",
            "    <otherwise>",
            "      AND (",
            "        (" + RESOLVED_REFERENCE_TYPE + " = 'river' AND EXISTS (SELECT 1 FROM yz_river_channel r WHERE r.id = " + RESOLVED_REFERENCE_ID + " AND r.deleted = 0))",
            "        OR (" + RESOLVED_REFERENCE_TYPE + " = 'river_section' AND EXISTS (SELECT 1 FROM yz_river_section s WHERE s.id = " + RESOLVED_REFERENCE_ID + " AND s.deleted = 0))",
            "        OR (" + RESOLVED_REFERENCE_TYPE + " = 'reservoir' AND EXISTS (SELECT 1 FROM yz_water_reservoir w WHERE w.id = " + RESOLVED_REFERENCE_ID + " AND w.deleted = 0))",
            "      )",
            "    </otherwise>",
            "  </choose>",
            "  <if test='ids != null and ids.size() &gt; 0'>",
            "    AND " + RESOLVED_REFERENCE_ID + " IN",
            "    <foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "      #{id}",
            "    </foreach>",
            "  </if>",
            "GROUP BY 1, 2",
            "ORDER BY effectiveFrom DESC, referenceId DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<RiverChiefFacilityGroupRow> selectCurrentFacilityGroupPage(@Param("referenceType") String referenceType,
                                                                    @Param("headLevel") String headLevel,
                                                                    @Param("headName") String headName,
                                                                    @Param("ids") List<Long> ids,
                                                                    @Param("offset") int offset,
                                                                    @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT",
            "  " + RESOLVED_REFERENCE_TYPE + " AS referenceType,",
            "  " + RESOLVED_REFERENCE_ID + " AS referenceId,",
            "  MAX(effective_from) AS effectiveFrom",
            "FROM yz_river_channel_management",
            "WHERE effective_to IS NULL AND is_current = 1",
            "  <if test='headLevel != null and headLevel != \"\"'>",
            "    AND head_level = #{headLevel}",
            "  </if>",
            "  <if test='headName != null and headName != \"\"'>",
            "    AND head_name LIKE ('%' || CAST(#{headName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "  </if>",
            "  <if test='referenceType != null and referenceType != \"\"'>",
            "    AND " + RESOLVED_REFERENCE_TYPE + " = #{referenceType}",
            "  </if>",
            "  <choose>",
            "    <when test='referenceType == \"river\"'>",
            "      AND EXISTS (SELECT 1 FROM yz_river_channel r WHERE r.id = " + RESOLVED_REFERENCE_ID + " AND r.deleted = 0)",
            "    </when>",
            "    <when test='referenceType == \"river_section\"'>",
            "      AND EXISTS (SELECT 1 FROM yz_river_section s WHERE s.id = " + RESOLVED_REFERENCE_ID + " AND s.deleted = 0)",
            "    </when>",
            "    <when test='referenceType == \"reservoir\"'>",
            "      AND EXISTS (SELECT 1 FROM yz_water_reservoir w WHERE w.id = " + RESOLVED_REFERENCE_ID + " AND w.deleted = 0)",
            "    </when>",
            "    <otherwise>",
            "      AND (",
            "        (" + RESOLVED_REFERENCE_TYPE + " = 'river' AND EXISTS (SELECT 1 FROM yz_river_channel r WHERE r.id = " + RESOLVED_REFERENCE_ID + " AND r.deleted = 0))",
            "        OR (" + RESOLVED_REFERENCE_TYPE + " = 'river_section' AND EXISTS (SELECT 1 FROM yz_river_section s WHERE s.id = " + RESOLVED_REFERENCE_ID + " AND s.deleted = 0))",
            "        OR (" + RESOLVED_REFERENCE_TYPE + " = 'reservoir' AND EXISTS (SELECT 1 FROM yz_water_reservoir w WHERE w.id = " + RESOLVED_REFERENCE_ID + " AND w.deleted = 0))",
            "      )",
            "    </otherwise>",
            "  </choose>",
            "  <if test='ids != null and ids.size() &gt; 0'>",
            "    AND " + RESOLVED_REFERENCE_ID + " IN",
            "    <foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "      #{id}",
            "    </foreach>",
            "  </if>",
            "GROUP BY 1, 2",
            "ORDER BY effectiveFrom DESC, referenceId DESC",
            "</script>"
    })
    List<RiverChiefFacilityGroupRow> selectCurrentFacilityGroupList(@Param("referenceType") String referenceType,
                                                                    @Param("headLevel") String headLevel,
                                                                    @Param("headName") String headName,
                                                                    @Param("ids") List<Long> ids);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT 1",
            "  FROM yz_river_channel_management m",
            "  WHERE m.effective_to IS NULL AND m.is_current = 1 AND COALESCE(m.deleted, 0) = 0",
            "    AND " + NORMALIZED_HEAD_NAME_BY_M + " IS NOT NULL",
            "    AND " + TOTAL_CHIEF_EXCLUDE_CONDITION_BY_M,
            "    AND " + BF_REFERENCE_EXCLUDE_CONDITION_BY_M,
            "    <if test='headName != null and headName != \"\"'>",
            "      AND m.head_name LIKE ('%' || CAST(#{headName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "    </if>",
            "    <if test='headLevel != null and headLevel.size() > 0'>",
            "      AND m.head_level IN ",
            "      <foreach collection='headLevel' item='item' open='(' separator=',' close=')'>",
            "        #{item}",
            "      </foreach>",
            "    </if>",
            "    <if test='administrativeRegionLiteral != null and administrativeRegionLiteral != \"\"'>",
            "      AND m.administrative_region &amp;&amp; CAST(#{administrativeRegionLiteral,jdbcType=VARCHAR} AS text[])",
            "    </if>",
            "    <if test='(referenceType != null and referenceType != \"\") or (referenceName != null and referenceName != \"\")'>",
            "      AND EXISTS (",
            "        SELECT 1",
            "        FROM yz_river_channel_management f",
            "        WHERE f.effective_to IS NULL AND f.is_current = 1 AND COALESCE(f.deleted, 0) = 0",
            "          AND " + BF_REFERENCE_EXCLUDE_CONDITION_BY_F,
            "          AND NULLIF(replace(regexp_replace(COALESCE(f.head_name, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '') = " + NORMALIZED_HEAD_NAME_BY_M,
            "          AND COALESCE(" + NORMALIZED_HEAD_LEVEL_BY_F + ", '') = COALESCE(" + NORMALIZED_HEAD_LEVEL_BY_M + ", '')",
            "          AND COALESCE(" + NORMALIZED_HEAD_POSITION_BY_F + ", '') = COALESCE(" + NORMALIZED_HEAD_POSITION_BY_M + ", '')",
            "          <if test='referenceType != null and referenceType != \"\"'>",
            "            <choose>",
            "              <when test='referenceType == \"river\" and referenceName != null and referenceName != \"\"'>",
            "                AND (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river' OR " + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river_section')",
            "              </when>",
            "              <otherwise>",
            "                AND " + RESOLVED_REFERENCE_TYPE_BY_F + " = #{referenceType}",
            "              </otherwise>",
            "            </choose>",
            "          </if>",
            "          <if test='referenceName != null and referenceName != \"\"'>",
            "            AND (",
            "              (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river' AND EXISTS (",
            "                 SELECT 1 FROM yz_river_channel r",
            "                 WHERE r.id = " + RESOLVED_REFERENCE_ID_BY_F + " AND r.deleted = 0",
            "                   AND r.river_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "              ))",
            "              OR (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river_section' AND EXISTS (",
            "                 SELECT 1 FROM yz_river_section s",
            "                 LEFT JOIN yz_river_channel c ON c.id = s.river_channel_id AND c.deleted = 0",
            "                 WHERE s.id = " + RESOLVED_REFERENCE_ID_BY_F + " AND s.deleted = 0",
            "                   AND (",
            "                     s.section_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "                     <if test='referenceType == \"river\"'>",
            "                       OR c.river_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "                     </if>",
            "                   )",
            "              ))",
            "              OR (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'reservoir' AND EXISTS (",
            "                 SELECT 1 FROM yz_water_reservoir w",
            "                 WHERE w.id = " + RESOLVED_REFERENCE_ID_BY_F + " AND w.deleted = 0",
            "                   AND w.reservoir_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "              ))",
            "            )",
            "          </if>",
            "      )",
            "    </if>",
            "  GROUP BY " + NORMALIZED_HEAD_NAME_BY_M + ", " + NORMALIZED_HEAD_LEVEL_BY_M + ", " + NORMALIZED_HEAD_POSITION_BY_M,
            ") t",
            "</script>"
    })
    long countCurrentChiefGroup(@Param("headName") String headName,
                                @Param("headLevel") List<String> headLevel,
                                @Param("administrativeRegionLiteral") String administrativeRegionLiteral,
                                @Param("referenceType") String referenceType,
                                @Param("referenceName") String referenceName);

    @Select({
            "<script>",
            "SELECT",
            "  m.id AS id,",
            "  m.head_name AS headName,",
            "  m.head_level AS headLevel,",
            "  m.head_position AS headPosition,",
            "  m.head_unit AS headUnit,",
            "  m.head_contact AS headContact,",
            "  m.administrative_region AS administrativeRegion,",
            "  m.effective_from AS effectiveFrom",
            "FROM yz_river_channel_management m",
            "INNER JOIN (",
            "  SELECT MAX(id) AS id",
            "  FROM yz_river_channel_management m",
            "  WHERE m.effective_to IS NULL AND m.is_current = 1 AND COALESCE(m.deleted, 0) = 0",
            "    AND " + NORMALIZED_HEAD_NAME_BY_M + " IS NOT NULL",
            "    AND " + TOTAL_CHIEF_EXCLUDE_CONDITION_BY_M,
            "    AND " + BF_REFERENCE_EXCLUDE_CONDITION_BY_M,
            "    <if test='headName != null and headName != \"\"'>",
            "      AND m.head_name LIKE ('%' || CAST(#{headName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "    </if>",
            "    <if test='headLevel != null and headLevel.size() > 0'>",
            "      AND m.head_level IN ",
            "      <foreach collection='headLevel' item='item' open='(' separator=',' close=')'>",
            "        #{item}",
            "      </foreach>",
            "    </if>",
            "    <if test='administrativeRegionLiteral != null and administrativeRegionLiteral != \"\"'>",
            "      AND m.administrative_region &amp;&amp; CAST(#{administrativeRegionLiteral,jdbcType=VARCHAR} AS text[])",
            "    </if>",
            "    <if test='(referenceType != null and referenceType != \"\") or (referenceName != null and referenceName != \"\")'>",
            "      AND EXISTS (",
            "        SELECT 1",
            "        FROM yz_river_channel_management f",
            "        WHERE f.effective_to IS NULL AND f.is_current = 1 AND COALESCE(f.deleted, 0) = 0",
            "          AND " + BF_REFERENCE_EXCLUDE_CONDITION_BY_F,
            "          AND NULLIF(replace(regexp_replace(COALESCE(f.head_name, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '') = " + NORMALIZED_HEAD_NAME_BY_M,
            "          AND COALESCE(" + NORMALIZED_HEAD_LEVEL_BY_F + ", '') = COALESCE(" + NORMALIZED_HEAD_LEVEL_BY_M + ", '')",
            "          AND COALESCE(" + NORMALIZED_HEAD_POSITION_BY_F + ", '') = COALESCE(" + NORMALIZED_HEAD_POSITION_BY_M + ", '')",
            "          <if test='referenceType != null and referenceType != \"\"'>",
            "            <choose>",
            "              <when test='referenceType == \"river\" and referenceName != null and referenceName != \"\"'>",
            "                AND (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river' OR " + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river_section')",
            "              </when>",
            "              <otherwise>",
            "                AND " + RESOLVED_REFERENCE_TYPE_BY_F + " = #{referenceType}",
            "              </otherwise>",
            "            </choose>",
            "          </if>",
            "          <if test='referenceName != null and referenceName != \"\"'>",
            "            AND (",
            "              (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river' AND EXISTS (",
            "                 SELECT 1 FROM yz_river_channel r",
            "                 WHERE r.id = " + RESOLVED_REFERENCE_ID_BY_F + " AND r.deleted = 0",
            "                   AND r.river_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "              ))",
            "              OR (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'river_section' AND EXISTS (",
            "                 SELECT 1 FROM yz_river_section s",
            "                 LEFT JOIN yz_river_channel c ON c.id = s.river_channel_id AND c.deleted = 0",
            "                 WHERE s.id = " + RESOLVED_REFERENCE_ID_BY_F + " AND s.deleted = 0",
            "                   AND (",
            "                     s.section_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "                     <if test='referenceType == \"river\"'>",
            "                       OR c.river_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "                     </if>",
            "                   )",
            "              ))",
            "              OR (" + RESOLVED_REFERENCE_TYPE_BY_F + " = 'reservoir' AND EXISTS (",
            "                 SELECT 1 FROM yz_water_reservoir w",
            "                 WHERE w.id = " + RESOLVED_REFERENCE_ID_BY_F + " AND w.deleted = 0",
            "                   AND w.reservoir_name LIKE ('%' || CAST(#{referenceName,jdbcType=VARCHAR} AS TEXT) || '%')",
            "              ))",
            "            )",
            "          </if>",
            "      )",
            "    </if>",
            "  GROUP BY " + NORMALIZED_HEAD_NAME_BY_M + ", " + NORMALIZED_HEAD_LEVEL_BY_M + ", " + NORMALIZED_HEAD_POSITION_BY_M,
            ") g ON g.id = m.id",
            "ORDER BY m.effective_from DESC, m.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    @Results(id = "riverChiefInfoGroupResultMap", value = {
            @Result(column = "administrativeRegion", property = "administrativeRegion", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    })
    List<RiverChiefInfoGroupRow> selectCurrentChiefGroupPage(@Param("headName") String headName,
                                                              @Param("headLevel") List<String> headLevel,
                                                              @Param("administrativeRegionLiteral") String administrativeRegionLiteral,
                                                              @Param("referenceType") String referenceType,
                                                              @Param("referenceName") String referenceName,
                                                              @Param("offset") int offset,
                                                              @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT * FROM yz_river_channel_management",
            "WHERE effective_to IS NULL AND is_current = 1 AND COALESCE(deleted, 0) = 0",
            "  AND " + TOTAL_CHIEF_QUERY_CONDITION,
            "ORDER BY effective_from DESC NULLS LAST, id DESC",
            "</script>"
    })
    @ResultMap("riverChannelManagementResultMap")
    List<YzRiverChannelManagementDO> selectCurrentTotalChiefs();

    @Select({
            "<script>",
            "SELECT * FROM yz_river_channel_management",
            "WHERE effective_to IS NULL AND is_current = 1 AND COALESCE(deleted, 0) = 0",
            "  AND " + BF_REFERENCE_EXCLUDE_CONDITION,
            "  AND " + NORMALIZED_HEAD_NAME + " = " + NORMALIZED_PARAM_HEAD_NAME,
            "  AND COALESCE(" + NORMALIZED_HEAD_LEVEL + ", '') = COALESCE(" + NORMALIZED_PARAM_HEAD_LEVEL + ", '')",
            "  AND COALESCE(" + NORMALIZED_HEAD_POSITION + ", '') = COALESCE(" + NORMALIZED_PARAM_HEAD_POSITION + ", '')",
            "ORDER BY id ASC",
            "</script>"
    })
    @ResultMap("riverChannelManagementResultMap")
    List<YzRiverChannelManagementDO> selectCurrentByChiefDimension(@Param("headName") String headName,
                                                                   @Param("headLevel") String headLevel,
                                                                   @Param("headPosition") String headPosition);

    @Select({
            "<script>",
            "SELECT * FROM yz_river_channel_management",
            "WHERE effective_to IS NULL AND is_current = 1",
            "  AND COALESCE(reference_type,",
            "    CASE",
            "      WHEN water_reservoir_id IS NOT NULL THEN 'reservoir'",
            "      WHEN river_section_id IS NOT NULL THEN 'river_section'",
            "      WHEN river_channel_id IS NOT NULL THEN 'river'",
            "      ELSE NULL",
            "    END) = #{referenceType}",
            "  AND COALESCE(reference_id, water_reservoir_id, river_section_id, river_channel_id) = #{referenceId}",
            "  <if test='headLevel != null and headLevel != \"\"'>",
            "    AND head_level = #{headLevel}",
            "  </if>",
            "ORDER BY id ASC",
            "</script>"
    })
    @Results(id = "riverChannelManagementResultMap", value = {
            @Result(column = "administrative_region", property = "administrativeRegion", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    })
    List<YzRiverChannelManagementDO> selectCurrentByResolvedReference(@Param("referenceType") String referenceType,
                                                                      @Param("referenceId") Long referenceId,
                                                                      @Param("headLevel") String headLevel);

    @Select({
            "<script>",
            "SELECT * FROM yz_river_channel_management",
            "WHERE effective_to IS NULL AND is_current = 1",
            "  AND COALESCE(reference_type,",
            "    CASE",
            "      WHEN water_reservoir_id IS NOT NULL THEN 'reservoir'",
            "      WHEN river_section_id IS NOT NULL THEN 'river_section'",
            "      WHEN river_channel_id IS NOT NULL THEN 'river'",
            "      ELSE NULL",
            "    END) = #{referenceType}",
            "  AND COALESCE(reference_id, water_reservoir_id, river_section_id, river_channel_id) IN",
            "  <foreach collection='referenceIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "ORDER BY id ASC",
            "</script>"
    })
    @ResultMap("riverChannelManagementResultMap")
    List<YzRiverChannelManagementDO> selectCurrentByResolvedReferenceIds(@Param("referenceType") String referenceType,
                                                                         @Param("referenceIds") List<Long> referenceIds);

    @Select({
            "SELECT * FROM yz_river_channel_management",
            "WHERE COALESCE(reference_type,",
            "    CASE",
            "      WHEN water_reservoir_id IS NOT NULL THEN 'reservoir'",
            "      WHEN river_section_id IS NOT NULL THEN 'river_section'",
            "      WHEN river_channel_id IS NOT NULL THEN 'river'",
            "      ELSE NULL",
            "    END) = #{referenceType}",
            "  AND COALESCE(reference_id, water_reservoir_id, river_section_id, river_channel_id) = #{referenceId}",
            "ORDER BY (effective_to IS NULL) DESC, effective_to DESC, effective_from DESC, id DESC"
    })
    @ResultMap("riverChannelManagementResultMap")
    List<YzRiverChannelManagementDO> selectTimelineByResolvedReference(@Param("referenceType") String referenceType,
                                                                       @Param("referenceId") Long referenceId);

    /**
     * 查询当前生效的河长记录（effective_to 为空且 is_current=1）。
     */
    default List<YzRiverChannelManagementDO> selectCurrentEffectiveChiefs() {
        return selectList(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .notIn(YzRiverChannelManagementDO::getReferenceType, List.of("river_bf", "river_section_bf", "reservoir_bf"))
                .orderByAsc(YzRiverChannelManagementDO::getId));
    }

    /**
     * 按用户 ID 查询当前生效的河长记录
     */
    default List<YzRiverChannelManagementDO> selectCurrentByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .in(YzRiverChannelManagementDO::getUserId, userIds)
                .isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .notIn(YzRiverChannelManagementDO::getReferenceType, List.of("river_bf", "river_section_bf", "reservoir_bf"))
                .orderByAsc(YzRiverChannelManagementDO::getId));
    }

    /**
     * 按用户ID查询已删除的河长记录用户ID
     */
    @Select({
            "<script>",
            "SELECT DISTINCT user_id",
            "FROM yz_river_channel_management",
            "WHERE user_id IS NOT NULL",
            "  AND COALESCE(deleted, 0) = 1",
            "  AND user_id IN",
            "  <foreach collection='userIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "</script>"
    })
    List<Long> selectDeletedUserIdsByUserIds(@Param("userIds") Collection<Long> userIds);

    default List<YzRiverChannelManagementDO> selectCurrentByHeadNames(Collection<String> headNames) {
        if (headNames == null || headNames.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .in(YzRiverChannelManagementDO::getHeadName, headNames)
                .isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .notIn(YzRiverChannelManagementDO::getReferenceType, List.of("river_bf", "river_section_bf", "reservoir_bf"))
                .orderByAsc(YzRiverChannelManagementDO::getId));
    }

    /**
     * 按主键批量回写用户ID。
     */
    default void updateUserIdByIds(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                .set(YzRiverChannelManagementDO::getUserId, userId)
                .in(YzRiverChannelManagementDO::getId, ids));
    }
}
