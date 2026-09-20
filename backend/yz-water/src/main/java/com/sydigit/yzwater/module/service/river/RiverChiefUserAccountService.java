package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.util.validation.ValidationUtils;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefUserSyncRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import com.sydigit.yzwater.module.system.dal.dataobject.permission.RoleDO;
import com.sydigit.yzwater.module.system.dal.dataobject.user.AdminUserDO;
import com.sydigit.yzwater.module.system.dal.mysql.permission.RoleMapper;
import com.sydigit.yzwater.module.system.service.permission.PermissionService;
import com.sydigit.yzwater.module.system.service.user.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 河长账号同步服务
 *
 * <p>将“当前有效的河长记录”或“本次新增的河长记录”同步为后台用户（system_users），并按 headLevel 赋予对应角色。</p>
 */
@Service
@RequiredArgsConstructor
public class RiverChiefUserAccountService {

    /**
     * 河长部门（system_dept.id）
     */
    private static final Long CHIEF_DEPT_ID = 2007636156463968257L;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,30}$");

    private final YzRiverChannelManagementMapper managementMapper;
    private final AdminUserService adminUserService;
    private final PermissionService permissionService;
    private final RoleMapper roleMapper;

    /**
     * 同步并绑定：仅处理传入的河长记录（严格模式）。
     *
     * <p>用于“创建/编辑河长信息”场景：若参数不合法或角色缺失，直接抛出 400，避免出现“河长已保存但账号未生成”的不一致。</p>
     */
    public void syncAndBindStrict(List<YzRiverChannelManagementDO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        List<YzRiverChannelManagementDO> validRecords = records.stream()
                .filter(record -> StrUtil.isNotBlank(StrUtil.trim(record == null ? null : record.getHeadContact())))
                .toList();
        if (validRecords.isEmpty()) {
            return;
        }
        Map<String, List<YzRiverChannelManagementDO>> grouped = groupByContact(validRecords);
        for (Map.Entry<String, List<YzRiverChannelManagementDO>> entry : grouped.entrySet()) {
            String contact = entry.getKey();
            List<YzRiverChannelManagementDO> contactRecords = entry.getValue();
            validateContactStrict(contact);

            String nickname = resolveNickname(contact, contactRecords);
            Set<String> headLevels = resolveHeadLevels(contactRecords);
            if (CollUtil.isEmpty(headLevels)) {
                throw ServiceExceptionUtil.invalidParamException("联系电话{}：河长级别不能为空", contact);
            }

            Set<Long> roleIds = resolveRoleIdsStrict(contact, headLevels);
            Long userId = upsertUserAndResetPassword(contact, nickname);
            permissionService.assignUserRole(userId, roleIds);

            List<Long> recordIds = contactRecords.stream()
                    .map(YzRiverChannelManagementDO::getId)
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
            if (!recordIds.isEmpty()) {
                managementMapper.updateUserIdByIds(recordIds, userId);
            }
        }
    }

    /**
     * 同步当前有效河长账号（尽量处理，不阻塞）。
     */
    public RiverChiefUserSyncRespVO syncAndBindCurrentChiefUsers() {
        RiverChiefUserSyncRespVO respVO = new RiverChiefUserSyncRespVO();
        List<YzRiverChannelManagementDO> records = managementMapper.selectCurrentEffectiveChiefs();
        respVO.setTotalChiefCount(records.size());

        Map<String, List<YzRiverChannelManagementDO>> grouped = groupByContact(records);
        respVO.setTotalAccountCount(grouped.size());

        for (Map.Entry<String, List<YzRiverChannelManagementDO>> entry : grouped.entrySet()) {
            String contact = entry.getKey();
            List<YzRiverChannelManagementDO> contactRecords = entry.getValue();
            try {
                if (StrUtil.isBlank(contact)) {
                    respVO.addSkip("河长联系电话为空，已跳过");
                    continue;
                }
                if (!USERNAME_PATTERN.matcher(contact).matches()) {
                    respVO.addSkip("联系电话=" + contact + "：不符合账号格式（仅数字/字母，长度4-30），已跳过");
                    continue;
                }
                if (!ValidationUtils.isMobile(contact)) {
                    respVO.addSkip("联系电话=" + contact + "：不符合手机号格式，已跳过");
                    continue;
                }

                String nickname = resolveNickname(contact, contactRecords);
                Set<String> headLevels = resolveHeadLevels(contactRecords);
                if (CollUtil.isEmpty(headLevels)) {
                    respVO.addFailure("联系电话=" + contact + "：河长级别为空，无法匹配角色，已跳过");
                    continue;
                }

                Set<Long> roleIds = new HashSet<>();
                boolean roleResolveFailed = false;
                for (String headLevel : headLevels) {
                    RoleDO role = roleMapper.selectByCode(headLevel);
                    if (role == null) {
                        respVO.addFailure("联系电话=" + contact + "：未找到角色（role.code=" + headLevel + "），已跳过");
                        roleResolveFailed = true;
                        continue;
                    }
                    if (!CommonStatusEnum.isEnable(role.getStatus())) {
                        respVO.addFailure("联系电话=" + contact + "：角色已停用（role.code=" + headLevel + "），已跳过");
                        roleResolveFailed = true;
                        continue;
                    }
                    roleIds.add(role.getId());
                }
                if (roleResolveFailed || roleIds.isEmpty()) {
                    continue;
                }

                AdminUserDO user = adminUserService.getUserByUsername(contact);
                if (user == null) {
                    user = adminUserService.getUserByMobile(contact);
                }

                Long userId;
                if (user == null) {
                    userId = createUser(contact, nickname);
                    respVO.addCreated("联系电话=" + contact + "：已创建用户（userId=" + userId + "）");
                } else {
                    userId = updateUser(user.getId(), contact, nickname);
                    respVO.addUpdated("联系电话=" + contact + "：已更新用户（userId=" + userId + "）");
                }

                permissionService.assignUserRole(userId, roleIds);
                adminUserService.updateUserPassword(userId, "123456");

                List<Long> recordIds = contactRecords.stream()
                        .map(YzRiverChannelManagementDO::getId)
                        .filter(id -> id != null)
                        .distinct()
                        .toList();
                if (!recordIds.isEmpty()) {
                    managementMapper.updateUserIdByIds(recordIds, userId);
                }
            } catch (Exception ex) {
                respVO.addFailure("联系电话=" + contact + "：同步失败，原因=" + ex.getMessage());
            }
        }
        return respVO;
    }

    private static Map<String, List<YzRiverChannelManagementDO>> groupByContact(List<YzRiverChannelManagementDO> records) {
        Map<String, List<YzRiverChannelManagementDO>> grouped = new HashMap<>();
        for (YzRiverChannelManagementDO record : records) {
            String contact = StrUtil.trim(record == null ? null : record.getHeadContact());
            grouped.computeIfAbsent(contact, k -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    private static void validateContactStrict(String contact) {
        if (StrUtil.isBlank(contact)) {
            throw ServiceExceptionUtil.invalidParamException("河长联系电话不能为空");
        }
        if (!USERNAME_PATTERN.matcher(contact).matches()) {
            throw ServiceExceptionUtil.invalidParamException("河长联系电话{}不符合账号格式（仅数字/字母，长度4-30）", contact);
        }
        if (!ValidationUtils.isMobile(contact)) {
            throw ServiceExceptionUtil.invalidParamException("河长联系电话{}不是合法手机号", contact);
        }
    }

    private static String resolveNickname(String contact, List<YzRiverChannelManagementDO> records) {
        String nickname = null;
        for (YzRiverChannelManagementDO record : CollUtil.defaultIfEmpty(records, List.of())) {
            if (record == null) {
                continue;
            }
            if (StrUtil.isBlank(nickname) && StrUtil.isNotBlank(record.getHeadName())) {
                nickname = StrUtil.trim(record.getHeadName());
            }
        }
        return StrUtil.blankToDefault(nickname, contact);
    }

    private static Set<String> resolveHeadLevels(List<YzRiverChannelManagementDO> records) {
        Set<String> headLevels = new HashSet<>();
        for (YzRiverChannelManagementDO record : CollUtil.defaultIfEmpty(records, List.of())) {
            if (record == null) {
                continue;
            }
            if (StrUtil.isNotBlank(record.getHeadLevel())) {
                headLevels.add(StrUtil.trim(record.getHeadLevel()));
            }
        }
        return headLevels;
    }

    private Set<Long> resolveRoleIdsStrict(String contact, Set<String> headLevels) {
        Set<Long> roleIds = new HashSet<>();
        for (String headLevel : headLevels) {
            RoleDO role = roleMapper.selectByCode(headLevel);
            if (role == null) {
                throw ServiceExceptionUtil.invalidParamException("联系电话{}：未找到角色（role.code={}）", contact, headLevel);
            }
            if (!CommonStatusEnum.isEnable(role.getStatus())) {
                throw ServiceExceptionUtil.invalidParamException("联系电话{}：角色已停用（role.code={}）", contact, headLevel);
            }
            roleIds.add(role.getId());
        }
        if (roleIds.isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("联系电话{}：未匹配到任何可用角色", contact);
        }
        return roleIds;
    }

    private Long upsertUserAndResetPassword(String contact, String nickname) {
        AdminUserDO user = adminUserService.getUserByUsername(contact);
        if (user == null) {
            user = adminUserService.getUserByMobile(contact);
        }
        if (user == null) {
            return createUser(contact, nickname);
        }
        return updateUser(user.getId(), contact, nickname);
    }

    private Long createUser(String contact, String nickname) {
        UserSaveReqVO createReqVO = new UserSaveReqVO();
        createReqVO.setUsername(contact);
        createReqVO.setNickname(nickname);
        createReqVO.setMobile(contact);
        createReqVO.setDeptId(CHIEF_DEPT_ID);
        createReqVO.setPassword("123456");
        Long userId = adminUserService.createUser(createReqVO);
        adminUserService.updateUserPassword(userId, "123456");
        return userId;
    }

    private Long updateUser(Long userId, String contact, String nickname) {
        UserSaveReqVO updateReqVO = new UserSaveReqVO();
        updateReqVO.setId(userId);
        updateReqVO.setUsername(contact);
        updateReqVO.setNickname(nickname);
        updateReqVO.setMobile(contact);
        updateReqVO.setDeptId(CHIEF_DEPT_ID);
        adminUserService.updateUser(updateReqVO);
        adminUserService.updateUserPassword(userId, "123456");
        return userId;
    }
}
