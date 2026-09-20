package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZhbDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZhbMemberDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZhbMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZhbMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 防汛指挥部服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxZhbService {

    private final YzFxZhbMapper zhbMapper;
    private final YzFxZhbMemberMapper memberMapper;

    /**
     * 分页查询
     */
    public PageResult<FxZhbPageRespVO> getPage(FxZhbPageReqVO reqVO) {
        LambdaQueryWrapper<YzFxZhbDO> wrapper = zhbMapper.buildQueryWrapper(reqVO);
        PageResult<YzFxZhbDO> page = zhbMapper.selectPage(reqVO, wrapper);
        List<FxZhbPageRespVO> list = page.getList().stream()
                .filter(Objects::nonNull)
                .map(this::buildPageResp)
                .toList();
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 详情
     */
    public FxZhbDetailRespVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("指挥部ID不能为空");
        }
        YzFxZhbDO exists = zhbMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_091, "指挥部不存在或已删除");
        }
        List<YzFxZhbMemberDO> members = memberMapper.selectByCommandDepartmentId(key);
        return buildDetailResp(exists, members);
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxZhbSaveReqVO reqVO) {
        String id = generateId();
        YzFxZhbDO insert = new YzFxZhbDO();
        insert.setId(id);
        fillMainFields(insert, reqVO);
        zhbMapper.insert(insert);
        saveMembers(id, normalizeMembers(reqVO.getMembers()));
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxZhbSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("指挥部ID不能为空");
        }
        YzFxZhbDO exists = zhbMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_091, "指挥部不存在或已删除");
        }
        YzFxZhbDO update = new YzFxZhbDO();
        update.setId(id);
        fillMainFields(update, reqVO);
        zhbMapper.updateById(update);
        mergeMembers(id, normalizeMembers(reqVO.getMembers()));
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("指挥部ID不能为空");
        }
        YzFxZhbDO exists = zhbMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_091, "指挥部不存在或已删除");
        }
        zhbMapper.deleteById(key);
        memberMapper.deleteByCommandDepartmentId(key);
    }

    /**
     * 导出（不分页）
     */
    public List<FxZhbExportExcelVO> getExportList(FxZhbPageReqVO reqVO) {
        LambdaQueryWrapper<YzFxZhbDO> wrapper = zhbMapper.buildQueryWrapper(reqVO);
        List<YzFxZhbDO> list = zhbMapper.selectList(wrapper);
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildExportVO)
                .toList();
    }

    private FxZhbPageRespVO buildPageResp(YzFxZhbDO item) {
        FxZhbPageRespVO vo = new FxZhbPageRespVO();
        vo.setId(item.getId());
        vo.setName(StrUtil.blankToDefault(item.getName(), ""));
        vo.setAddr(item.getAddr());
        vo.setTel(item.getTel());
        vo.setFax(item.getFax());
        vo.setAreaCode(item.getAreaCode());
        vo.setZipCode(item.getZipCode());
        return vo;
    }

    private FxZhbDetailRespVO buildDetailResp(YzFxZhbDO item, List<YzFxZhbMemberDO> members) {
        FxZhbDetailRespVO vo = new FxZhbDetailRespVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setAddr(item.getAddr());
        vo.setTel(item.getTel());
        vo.setFax(item.getFax());
        vo.setAreaCode(item.getAreaCode());
        vo.setZipCode(item.getZipCode());
        vo.setSort(item.getSort());
        vo.setMembers(members.stream()
                .filter(Objects::nonNull)
                .map(this::buildMemberSaveReq)
                .toList());
        return vo;
    }

    private FxZhbExportExcelVO buildExportVO(YzFxZhbDO item) {
        FxZhbExportExcelVO vo = new FxZhbExportExcelVO();
        vo.setName(item.getName());
        vo.setAddr(item.getAddr());
        vo.setTel(item.getTel());
        vo.setFax(item.getFax());
        vo.setAreaCode(item.getAreaCode());
        vo.setZipCode(item.getZipCode());
        return vo;
    }

    private FxZhbMemberSaveReqVO buildMemberSaveReq(YzFxZhbMemberDO item) {
        FxZhbMemberSaveReqVO vo = new FxZhbMemberSaveReqVO();
        vo.setId(item.getId());
        vo.setCommandDepartmentId(item.getCommandDepartmentId());
        vo.setName(item.getName());
        vo.setTitle(item.getTitle());
        vo.setTel(item.getTel());
        vo.setSort(item.getSort());
        return vo;
    }

    private void fillMainFields(YzFxZhbDO target, FxZhbSaveReqVO reqVO) {
        target.setName(StrUtil.trimToNull(reqVO.getName()));
        target.setAddr(StrUtil.trimToNull(reqVO.getAddr()));
        target.setTel(StrUtil.trimToNull(reqVO.getTel()));
        target.setFax(StrUtil.trimToNull(reqVO.getFax()));
        target.setAreaCode(StrUtil.trimToNull(reqVO.getAreaCode()));
        target.setZipCode(StrUtil.trimToNull(reqVO.getZipCode()));
        target.setSort(reqVO.getSort());
    }

    private void saveMembers(String commandDepartmentId, List<FxZhbMemberSaveReqVO> members) {
        if (members.isEmpty()) {
            return;
        }
        for (int i = 0; i < members.size(); i++) {
            FxZhbMemberSaveReqVO member = members.get(i);
            YzFxZhbMemberDO insert = new YzFxZhbMemberDO();
            insert.setId(generateId());
            insert.setCommandDepartmentId(commandDepartmentId);
            fillMemberFields(insert, member, i + 1);
            memberMapper.insert(insert);
        }
    }

    private void mergeMembers(String commandDepartmentId, List<FxZhbMemberSaveReqVO> members) {
        List<YzFxZhbMemberDO> existing = memberMapper.selectByCommandDepartmentId(commandDepartmentId);
        Map<String, YzFxZhbMemberDO> existsMap = existing.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(YzFxZhbMemberDO::getId, Function.identity(), (a, b) -> a));
        Set<String> keepIds = new HashSet<>();
        for (int i = 0; i < members.size(); i++) {
            FxZhbMemberSaveReqVO member = members.get(i);
            String memberId = StrUtil.trimToNull(member.getId());
            if (memberId != null && existsMap.containsKey(memberId)) {
                YzFxZhbMemberDO update = new YzFxZhbMemberDO();
                update.setId(memberId);
                update.setCommandDepartmentId(commandDepartmentId);
                fillMemberFields(update, member, i + 1);
                memberMapper.updateById(update);
                keepIds.add(memberId);
                continue;
            }
            YzFxZhbMemberDO insert = new YzFxZhbMemberDO();
            insert.setId(generateId());
            insert.setCommandDepartmentId(commandDepartmentId);
            fillMemberFields(insert, member, i + 1);
            memberMapper.insert(insert);
        }

        if (!existing.isEmpty()) {
            List<String> removeIds = existing.stream()
                    .map(YzFxZhbMemberDO::getId)
                    .filter(StrUtil::isNotBlank)
                    .filter(id -> !keepIds.contains(id))
                    .toList();
            if (!removeIds.isEmpty()) {
                memberMapper.deleteBatchIds(removeIds);
            }
        }
    }

    private void fillMemberFields(YzFxZhbMemberDO target, FxZhbMemberSaveReqVO source, int defaultSort) {
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setTitle(StrUtil.trimToNull(source.getTitle()));
        target.setTel(StrUtil.trimToNull(source.getTel()));
        Integer sort = source.getSort();
        target.setSort(sort == null ? defaultSort : sort);
    }

    private List<FxZhbMemberSaveReqVO> normalizeMembers(List<FxZhbMemberSaveReqVO> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<FxZhbMemberSaveReqVO> result = new ArrayList<>();
        for (FxZhbMemberSaveReqVO member : members) {
            if (member == null) {
                continue;
            }
            if (StrUtil.isAllBlank(member.getName(), member.getTitle(), member.getTel())) {
                continue;
            }
            result.add(member);
        }
        return result;
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
