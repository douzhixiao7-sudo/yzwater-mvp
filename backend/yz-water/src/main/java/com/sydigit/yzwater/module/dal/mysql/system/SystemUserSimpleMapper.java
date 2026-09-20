package com.sydigit.yzwater.module.dal.mysql.system;

import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackUserSimpleRespVO;
import com.sydigit.yzwater.module.dal.mysql.system.dto.SystemUserRoleRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 系统用户查询 Mapper（仅用于跨模块的只读查询）
 */
@Mapper
public interface SystemUserSimpleMapper {

    /**
     * 查询所有正常状态用户（排除纯游客：仅有游客角色、无其他业务角色），按状态更新时间倒序
     */
    @Select("""
            SELECT
              id,
              nickname
            FROM (
              SELECT DISTINCT
                u.id, u.nickname, COALESCE(u.update_time, u.create_time) AS order_time
              FROM system_users u
              WHERE u.status = 0
                AND COALESCE(u.deleted, 0) = 0
                AND NOT (
                  EXISTS (
                    SELECT 1
                    FROM system_user_role ur
                    JOIN system_role r
                      ON r.id = ur.role_id
                     AND COALESCE(r.deleted, 0) = 0
                    WHERE ur.user_id = u.id
                      AND COALESCE(ur.deleted, 0) = 0
                      AND TRIM(LOWER(r.code)) = 'guest'
                  )
                  AND NOT EXISTS (
                    SELECT 1
                    FROM system_user_role ur
                    JOIN system_role r
                      ON r.id = ur.role_id
                     AND COALESCE(r.deleted, 0) = 0
                    WHERE ur.user_id = u.id
                      AND COALESCE(ur.deleted, 0) = 0
                      AND TRIM(LOWER(r.code)) <> 'guest'
                  )
                )
            ) AS subquery
            ORDER BY order_time DESC;
            """)
    List<ProblemFeedbackUserSimpleRespVO> selectNormalUsersExcludeAdminAndGuest();

    /**
     * 根据用户 ID 列表查询用户昵称
     */
    @Select("""
            <script>
            SELECT
              u.id       AS id,
              COALESCE(NULLIF(TRIM(u.nickname), ''), u.username) AS nickname
            FROM system_users u
            WHERE u.id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    List<ProblemFeedbackUserSimpleRespVO> selectUsersByIds(@Param("ids") Collection<Long> ids);

    /**
     * 查询用户拥有的角色编码列表
     */
    @Select("""
            SELECT r.code
            FROM system_user_role ur
            JOIN system_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND (ur.deleted IS NULL OR ur.deleted = 0)
              AND (r.deleted IS NULL OR r.deleted = 0)
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 批量查询用户角色明细
     */
    @Select("""
            <script>
            SELECT
              ur.user_id AS userId,
              r.name AS roleName,
              r.sort AS roleSort,
              r.id AS roleId
            FROM system_user_role ur
            JOIN system_role r
              ON r.id = ur.role_id
             AND (r.deleted IS NULL OR r.deleted = 0)
             AND TRIM(LOWER(r.code)) &lt;&gt; 'guest'
            WHERE (ur.deleted IS NULL OR ur.deleted = 0)
              AND ur.user_id IN
            <foreach collection="userIds" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    List<SystemUserRoleRow> selectUserRoleRowsByUserIds(@Param("userIds") Collection<Long> userIds);

    /**
     * 判断用户是否为纯游客（仅有游客角色，无其他业务角色）
     */
    @Select("""
            SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END
            FROM system_users u
            WHERE u.id = #{userId}
              AND u.status = 0
              AND COALESCE(u.deleted, 0) = 0
              AND EXISTS (
                SELECT 1
                FROM system_user_role ur
                JOIN system_role r
                  ON r.id = ur.role_id
                 AND COALESCE(r.deleted, 0) = 0
                WHERE ur.user_id = u.id
                  AND COALESCE(ur.deleted, 0) = 0
                  AND TRIM(LOWER(r.code)) = 'guest'
              )
              AND NOT EXISTS (
                SELECT 1
                FROM system_user_role ur
                JOIN system_role r
                  ON r.id = ur.role_id
                 AND COALESCE(r.deleted, 0) = 0
                WHERE ur.user_id = u.id
                  AND COALESCE(ur.deleted, 0) = 0
                  AND TRIM(LOWER(r.code)) <> 'guest'
              )
            """)
    Boolean selectIsPureGuestByUserId(@Param("userId") Long userId);

    /**
     * 根据手机号查询用户ID（仅返回启用且未删除的用户）
     */
    @Select("""
            SELECT u.id
            FROM system_users u
            WHERE u.mobile = #{mobile}
              AND u.status = 0
              AND (u.deleted IS NULL OR u.deleted = 0)
            LIMIT 1
            """)
    Long selectIdByMobile(@Param("mobile") String mobile);

    /**
     * 根据用户ID查询手机号（仅返回启用且未删除的用户）
     */
    @Select("""
            SELECT u.mobile
            FROM system_users u
            WHERE u.id = #{userId}
              AND u.status = 0
              AND (u.deleted IS NULL OR u.deleted = 0)
            LIMIT 1
            """)
    String selectMobileByUserId(@Param("userId") Long userId);
}
