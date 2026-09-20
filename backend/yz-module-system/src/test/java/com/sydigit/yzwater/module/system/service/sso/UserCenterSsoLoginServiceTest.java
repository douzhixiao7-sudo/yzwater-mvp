package com.sydigit.yzwater.module.system.service.sso;

import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.enums.UserTypeEnum;
import com.sydigit.yzwater.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import com.sydigit.yzwater.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.sydigit.yzwater.module.system.dal.dataobject.permission.RoleDO;
import com.sydigit.yzwater.module.system.dal.dataobject.user.AdminUserDO;
import com.sydigit.yzwater.module.system.dal.mysql.permission.RoleMapper;
import com.sydigit.yzwater.module.system.service.logger.LoginLogService;
import com.sydigit.yzwater.module.system.service.oauth2.OAuth2TokenService;
import com.sydigit.yzwater.module.system.service.permission.PermissionService;
import com.sydigit.yzwater.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCenterSsoLoginServiceTest {

    @Mock
    private UserCenterSsoService userCenterSsoService;
    @Mock
    private AdminUserService userService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private OAuth2TokenService oauth2TokenService;
    @Mock
    private LoginLogService loginLogService;

    @InjectMocks
    private UserCenterSsoLoginServiceImpl loginService;

    @Test
    void loginByCode_shouldLoginExistingUserAndMergeYzadminRole() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("preferred_username", "alice01");
        userInfo.put("phone", "13800138000");
        AdminUserDO user = new AdminUserDO()
                .setId(10L)
                .setUsername("alice01")
                .setNickname("Alice")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        RoleDO yzadminRole = new RoleDO().setId(99L).setCode("yzadmin").setName("yzadmin");

        when(userCenterSsoService.getUserInfoByCode("code-1")).thenReturn(userInfo);
        when(userService.getUserByUsername("alice01")).thenReturn(user);
        when(roleMapper.selectByCode("yzadmin")).thenReturn(yzadminRole);
        when(permissionService.getUserRoleIdListByUserId(10L)).thenReturn(Set.of(5L));
        when(oauth2TokenService.createAccessToken(eq(10L), eq(UserTypeEnum.ADMIN.getValue()), eq("default"), eq(null)))
                .thenReturn(buildAccessToken(10L, "access-1", "refresh-1"));

        AuthLoginRespVO respVO = loginService.loginByCode("code-1");

        assertEquals("access-1", respVO.getAccessToken());
        assertEquals("refresh-1", respVO.getRefreshToken());
        verify(userService, never()).createUser(any());
        verify(permissionService).assignUserRole(10L, Set.of(5L, 99L));
    }

    @Test
    void loginByCode_shouldCreateUserAndAssignYzadminWhenUserMissing() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("preferred_username", "bob1234");
        userInfo.put("phone", "13800138001");
        userInfo.put("name", "Bob");
        RoleDO yzadminRole = new RoleDO().setId(99L).setName("yzadmin");
        AdminUserDO createdUser = new AdminUserDO()
                .setId(20L)
                .setUsername("bob1234")
                .setNickname("Bob")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());

        when(userCenterSsoService.getUserInfoByCode("code-2")).thenReturn(userInfo);
        when(userService.getUserByUsername("bob1234")).thenReturn(null);
        when(userService.getUserByMobile("13800138001")).thenReturn(null);
        when(roleMapper.selectByCode("yzadmin")).thenReturn(null);
        when(roleMapper.selectByName("yzadmin")).thenReturn(yzadminRole);
        when(userService.createUser(any())).thenReturn(20L);
        when(userService.getUser(20L)).thenReturn(createdUser);
        when(permissionService.getUserRoleIdListByUserId(20L)).thenReturn(Set.of());
        when(oauth2TokenService.createAccessToken(eq(20L), eq(UserTypeEnum.ADMIN.getValue()), eq("default"), eq(null)))
                .thenReturn(buildAccessToken(20L, "access-2", "refresh-2"));

        AuthLoginRespVO respVO = loginService.loginByCode("code-2");

        assertEquals("access-2", respVO.getAccessToken());
        ArgumentCaptor<com.sydigit.yzwater.module.system.controller.admin.user.vo.user.UserSaveReqVO> captor =
                ArgumentCaptor.forClass(com.sydigit.yzwater.module.system.controller.admin.user.vo.user.UserSaveReqVO.class);
        verify(userService).createUser(captor.capture());
        assertEquals("bob1234", captor.getValue().getUsername());
        assertEquals("Bob", captor.getValue().getNickname());
        assertEquals("13800138001", captor.getValue().getMobile());
        verify(permissionService).assignUserRole(20L, Set.of(99L));
    }

    @Test
    void loginByCode_shouldThrowWhenYzadminRoleMissing() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("preferred_username", "charlie9");
        AdminUserDO user = new AdminUserDO()
                .setId(30L)
                .setUsername("charlie9")
                .setNickname("Charlie")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());

        when(userCenterSsoService.getUserInfoByCode("code-3")).thenReturn(userInfo);
        when(userService.getUserByUsername("charlie9")).thenReturn(user);
        when(roleMapper.selectByCode("yzadmin")).thenReturn(null);
        when(roleMapper.selectByName("yzadmin")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loginService.loginByCode("code-3"));
        assertEquals("未找到用户中心自动授权角色 yzadmin", exception.getMessage());
    }

    private OAuth2AccessTokenDO buildAccessToken(Long userId, String accessToken, String refreshToken) {
        OAuth2AccessTokenDO tokenDO = new OAuth2AccessTokenDO();
        tokenDO.setUserId(userId);
        tokenDO.setUserType(UserTypeEnum.ADMIN.getValue());
        tokenDO.setAccessToken(accessToken);
        tokenDO.setRefreshToken(refreshToken);
        tokenDO.setExpiresTime(LocalDateTime.now().plusHours(2));
        return tokenDO;
    }
}
