package com.sydigit.yzwater.module.iot.service.shiftteam;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamMemberRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamMemberDO;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftScheduleRefMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMemberMapper;
import com.sydigit.yzwater.module.system.api.user.AdminUserApi;
import com.sydigit.yzwater.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_TEAM_DELETE_FORBIDDEN_REFERENCED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_TEAM_LEADER_NOT_MEMBER;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_TEAM_LEADER_REQUIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_TEAM_NAME_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_TEAM_NOT_EXISTS;

/**
 * 班组 Service 实现
 */
@Service
@Validated
public class IotShiftTeamServiceImpl implements IotShiftTeamService {

    private static final String TEAM_NO_PREFIX = "BZ-";

    @Resource
    private IotShiftTeamMapper shiftTeamMapper;
    @Resource
    private IotShiftTeamMemberMapper shiftTeamMemberMapper;
    @Resource
    private IotShiftScheduleRefMapper shiftScheduleRefMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShiftTeam(IotShiftTeamSaveReqVO createReqVO) {
        String teamName = trimToNull(createReqVO.getTeamName());
        validateTeamNameUnique(null, teamName);
        TeamMemberContext memberContext = buildAndValidateMemberContext(createReqVO.getLeaderUserId(), createReqVO.getMemberUserIds());

        IotShiftTeamDO team = new IotShiftTeamDO();
        team.setTeamNo(generateTeamNo());
        team.setTeamName(teamName);
        team.setStationId(trimToEmpty(createReqVO.getStationId()));
        team.setLeaderUserId(createReqVO.getLeaderUserId());
        team.setLeaderUserName(resolveLeaderName(memberContext.userMap, createReqVO.getLeaderUserId()));
        team.setMemberCount(memberContext.memberUserIds.size());
        team.setStatus(0);
        team.setRemark(trimToEmpty(createReqVO.getRemark()));
        shiftTeamMapper.insert(team);

        saveMembers(team.getId(), createReqVO.getLeaderUserId(), memberContext);
        return team.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShiftTeam(IotShiftTeamSaveReqVO updateReqVO) {
        IotShiftTeamDO existed = validateShiftTeamExists(updateReqVO.getId());
        String teamName = trimToNull(updateReqVO.getTeamName());
        validateTeamNameUnique(existed.getId(), teamName);
        TeamMemberContext memberContext = buildAndValidateMemberContext(updateReqVO.getLeaderUserId(), updateReqVO.getMemberUserIds());

        IotShiftTeamDO updateObj = new IotShiftTeamDO();
        updateObj.setId(existed.getId());
        updateObj.setTeamName(teamName);
        updateObj.setStationId(trimToEmpty(updateReqVO.getStationId()));
        updateObj.setLeaderUserId(updateReqVO.getLeaderUserId());
        updateObj.setLeaderUserName(resolveLeaderName(memberContext.userMap, updateReqVO.getLeaderUserId()));
        updateObj.setMemberCount(memberContext.memberUserIds.size());
        updateObj.setRemark(trimToEmpty(updateReqVO.getRemark()));
        shiftTeamMapper.updateById(updateObj);

        shiftTeamMemberMapper.deleteByTeamId(existed.getId());
        saveMembers(existed.getId(), updateReqVO.getLeaderUserId(), memberContext);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShiftTeam(Long id) {
        validateShiftTeamExists(id);
        validateCanDelete(id);
        shiftTeamMemberMapper.deleteByTeamId(id);
        shiftTeamMapper.deleteById(id);
    }

    @Override
    public IotShiftTeamRespVO getShiftTeam(Long id) {
        IotShiftTeamDO team = validateShiftTeamExists(id);
        List<IotShiftTeamMemberDO> members = shiftTeamMemberMapper.selectListByTeamId(id);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(Collections.singletonList(team));
        return buildResp(team, members, creatorMap);
    }

    @Override
    public PageResult<IotShiftTeamRespVO> getShiftTeamPage(IotShiftTeamPageReqVO pageReqVO) {
        PageResult<IotShiftTeamDO> pageResult = shiftTeamMapper.selectPage(pageReqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(pageResult.getList());
        List<IotShiftTeamRespVO> respList = CollectionUtils.convertList(pageResult.getList(),
                item -> buildBaseResp(item, creatorMap));
        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public List<IotShiftTeamRespVO> getShiftTeamList(IotShiftTeamPageReqVO reqVO) {
        List<IotShiftTeamDO> list = shiftTeamMapper.selectListByReqVO(reqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(list);
        return CollectionUtils.convertList(list, item -> buildBaseResp(item, creatorMap));
    }

    /**
     * 校验班组存在
     */
    private IotShiftTeamDO validateShiftTeamExists(Long id) {
        IotShiftTeamDO team = shiftTeamMapper.selectById(id);
        if (team == null) {
            throw exception(SHIFT_TEAM_NOT_EXISTS);
        }
        return team;
    }

    /**
     * 校验班组名称唯一
     */
    private void validateTeamNameUnique(Long id, String teamName) {
        IotShiftTeamDO existed = shiftTeamMapper.selectByTeamName(teamName);
        if (existed == null) {
            return;
        }
        if (!Objects.equals(existed.getId(), id)) {
            throw exception(SHIFT_TEAM_NAME_EXISTS);
        }
    }

    /**
     * 组装并校验成员上下文
     */
    private TeamMemberContext buildAndValidateMemberContext(Long leaderUserId, List<Long> memberUserIds) {
        if (leaderUserId == null) {
            throw exception(SHIFT_TEAM_LEADER_REQUIRED);
        }
        LinkedHashSet<Long> memberSet = new LinkedHashSet<>();
        if (memberUserIds != null) {
            for (Long memberUserId : memberUserIds) {
                if (memberUserId != null) {
                    memberSet.add(memberUserId);
                }
            }
        }
        if (!memberSet.contains(leaderUserId)) {
            throw exception(SHIFT_TEAM_LEADER_NOT_MEMBER);
        }
        List<Long> normalizedIds = new ArrayList<>(memberSet);
        adminUserApi.validateUserList(normalizedIds);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(normalizedIds);

        TeamMemberContext context = new TeamMemberContext();
        context.memberUserIds = normalizedIds;
        context.userMap = userMap;
        return context;
    }

    /**
     * 保存班组成员
     */
    private void saveMembers(Long teamId, Long leaderUserId, TeamMemberContext memberContext) {
        int memberSort = 1;
        for (Long memberUserId : memberContext.memberUserIds) {
            AdminUserRespDTO user = memberContext.userMap.get(memberUserId);
            IotShiftTeamMemberDO member = new IotShiftTeamMemberDO();
            member.setTeamId(teamId);
            member.setUserId(memberUserId);
            member.setUserName(user == null ? "" : trimToEmpty(user.getNickname()));
            member.setMobile(user == null ? "" : trimToEmpty(user.getMobile()));
            member.setPostName("");
            member.setIsLeader(Objects.equals(memberUserId, leaderUserId) ? 1 : 0);
            member.setMemberSort(memberSort++);
            member.setStatus(0);
            member.setRemark("");
            shiftTeamMemberMapper.insert(member);
        }
    }

    /**
     * 校验是否可删除
     */
    private void validateCanDelete(Long teamId) {
        if (!Boolean.TRUE.equals(shiftScheduleRefMapper.existsShiftScheduleTable())) {
            return;
        }
        Long count = shiftScheduleRefMapper.selectCountByTeamId(teamId);
        if (count != null && count > 0) {
            throw exception(SHIFT_TEAM_DELETE_FORBIDDEN_REFERENCED);
        }
    }

    /**
     * 生成班组编号（BZ-001 递增）
     */
    private String generateTeamNo() {
        Integer maxSeq = shiftTeamMapper.selectMaxTeamNoSeq(TEAM_NO_PREFIX, TEAM_NO_PREFIX.length());
        int nextSeq = maxSeq == null ? 1 : maxSeq + 1;
        return TEAM_NO_PREFIX + String.format("%03d", nextSeq);
    }

    /**
     * 构建基础响应对象
     */
    private IotShiftTeamRespVO buildBaseResp(IotShiftTeamDO team, Map<Long, AdminUserRespDTO> creatorMap) {
        IotShiftTeamRespVO respVO = new IotShiftTeamRespVO();
        respVO.setId(team.getId());
        respVO.setTeamNo(team.getTeamNo());
        respVO.setTeamName(team.getTeamName());
        respVO.setStationId(team.getStationId());
        respVO.setLeaderUserId(team.getLeaderUserId());
        respVO.setLeaderUserName(team.getLeaderUserName());
        respVO.setMemberCount(team.getMemberCount());
        respVO.setRemark(team.getRemark());
        respVO.setCreator(resolveCreatorName(team.getCreator(), creatorMap));
        respVO.setCreateTime(team.getCreateTime());
        respVO.setMemberUserIds(Collections.emptyList());
        respVO.setMembers(Collections.emptyList());
        return respVO;
    }

    /**
     * 构建详情响应对象
     */
    private IotShiftTeamRespVO buildResp(IotShiftTeamDO team, List<IotShiftTeamMemberDO> members,
                                         Map<Long, AdminUserRespDTO> creatorMap) {
        IotShiftTeamRespVO respVO = buildBaseResp(team, creatorMap);
        if (members == null || members.isEmpty()) {
            return respVO;
        }
        List<Long> memberUserIds = CollectionUtils.convertList(members, IotShiftTeamMemberDO::getUserId);
        List<IotShiftTeamMemberRespVO> memberRespList = CollectionUtils.convertList(members, item -> {
            IotShiftTeamMemberRespVO memberRespVO = new IotShiftTeamMemberRespVO();
            memberRespVO.setUserId(item.getUserId());
            memberRespVO.setUserName(item.getUserName());
            memberRespVO.setMobile(item.getMobile());
            memberRespVO.setLeader(Objects.equals(item.getIsLeader(), 1));
            return memberRespVO;
        });
        respVO.setMemberUserIds(memberUserIds);
        respVO.setMembers(memberRespList);
        return respVO;
    }

    /**
     * 批量构建创建人映射
     */
    private Map<Long, AdminUserRespDTO> buildCreatorMap(List<IotShiftTeamDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashSet<Long> creatorIds = new LinkedHashSet<>();
        for (IotShiftTeamDO team : list) {
            Long creatorId = parseUserId(team == null ? null : team.getCreator());
            if (creatorId != null) {
                creatorIds.add(creatorId);
            }
        }
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserApi.getUserMap(creatorIds);
    }

    /**
     * 解析创建人中文名称
     */
    private String resolveCreatorName(String creator, Map<Long, AdminUserRespDTO> creatorMap) {
        Long creatorId = parseUserId(creator);
        if (creatorId == null) {
            return trimToEmpty(creator);
        }
        AdminUserRespDTO user = creatorMap.get(creatorId);
        if (user == null) {
            return trimToEmpty(creator);
        }
        String nickname = trimToNull(user.getNickname());
        return nickname == null ? String.valueOf(creatorId) : nickname;
    }

    private Long parseUserId(String creator) {
        String value = trimToNull(creator);
        if (value == null || !StrUtil.isNumeric(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    private String resolveLeaderName(Map<Long, AdminUserRespDTO> userMap, Long leaderUserId) {
        AdminUserRespDTO leader = userMap.get(leaderUserId);
        return leader == null ? "" : trimToEmpty(leader.getNickname());
    }

    private String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimToEmpty(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized;
    }

    /**
     * 成员上下文
     */
    private static class TeamMemberContext {
        private List<Long> memberUserIds;
        private Map<Long, AdminUserRespDTO> userMap;
    }
}
