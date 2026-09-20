package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberExportReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZhbDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZhbMemberDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZhbMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZhbMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 防汛指挥部成员服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxZhbMemberService {

    private final YzFxZhbMemberMapper memberMapper;
    private final YzFxZhbMapper zhbMapper;

    /**
     * 分页查询
     */
    public PageResult<FxZhbMemberPageRespVO> getPage(FxZhbMemberPageReqVO reqVO) {
        LambdaQueryWrapper<YzFxZhbMemberDO> wrapper = memberMapper.buildQueryWrapper(reqVO);
        PageResult<YzFxZhbMemberDO> page = memberMapper.selectPage(reqVO, wrapper);
        List<FxZhbMemberPageRespVO> list = page.getList().stream()
                .filter(Objects::nonNull)
                .map(this::buildPageResp)
                .toList();
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 详情
     */
    public FxZhbMemberSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("成员ID不能为空");
        }
        YzFxZhbMemberDO exists = memberMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "成员不存在或已删除");
        }
        FxZhbMemberSaveReqVO vo = new FxZhbMemberSaveReqVO();
        vo.setId(exists.getId());
        vo.setCommandDepartmentId(exists.getCommandDepartmentId());
        vo.setName(exists.getName());
        vo.setTitle(exists.getTitle());
        vo.setTel(exists.getTel());
        vo.setSort(exists.getSort());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxZhbMemberSaveReqVO reqVO) {
        if (StrUtil.isBlank(reqVO.getCommandDepartmentId())) {
            throw ServiceExceptionUtil.invalidParamException("指挥部ID不能为空");
        }
        String id = generateId();
        YzFxZhbMemberDO insert = new YzFxZhbMemberDO();
        insert.setId(id);
        insert.setCommandDepartmentId(StrUtil.trimToNull(reqVO.getCommandDepartmentId()));
        fillMemberFields(insert, reqVO);
        memberMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxZhbMemberSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("成员ID不能为空");
        }
        YzFxZhbMemberDO exists = memberMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "成员不存在或已删除");
        }
        String commandDepartmentId = StrUtil.trimToNull(reqVO.getCommandDepartmentId());
        if (commandDepartmentId == null) {
            commandDepartmentId = exists.getCommandDepartmentId();
        }
        YzFxZhbMemberDO update = new YzFxZhbMemberDO();
        update.setId(id);
        update.setCommandDepartmentId(commandDepartmentId);
        fillMemberFields(update, reqVO);
        memberMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("成员ID不能为空");
        }
        YzFxZhbMemberDO exists = memberMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "成员不存在或已删除");
        }
        memberMapper.deleteById(key);
    }

    /**
     * 导出（不分页）
     */
    public List<FxZhbMemberExportExcelVO> getExportList(FxZhbMemberExportReqVO reqVO) {
        LambdaQueryWrapper<YzFxZhbMemberDO> wrapper = memberMapper.buildQueryWrapper(reqVO);
        List<YzFxZhbMemberDO> list = memberMapper.selectList(wrapper);
        Map<String, String> deptNameMap = loadDepartmentNameMap(list);
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> buildExportVO(item, deptNameMap))
                .toList();
    }

    private FxZhbMemberPageRespVO buildPageResp(YzFxZhbMemberDO item) {
        FxZhbMemberPageRespVO vo = new FxZhbMemberPageRespVO();
        vo.setId(item.getId());
        vo.setCommandDepartmentId(item.getCommandDepartmentId());
        vo.setName(item.getName());
        vo.setTitle(item.getTitle());
        vo.setTel(item.getTel());
        vo.setSort(item.getSort());
        return vo;
    }

    private FxZhbMemberExportExcelVO buildExportVO(YzFxZhbMemberDO item, Map<String, String> deptNameMap) {
        FxZhbMemberExportExcelVO vo = new FxZhbMemberExportExcelVO();
        vo.setCommandDepartmentName(deptNameMap.getOrDefault(item.getCommandDepartmentId(), "-"));
        vo.setName(item.getName());
        vo.setTitle(item.getTitle());
        vo.setTel(item.getTel());
        vo.setSort(item.getSort());
        return vo;
    }

    private void fillMemberFields(YzFxZhbMemberDO target, FxZhbMemberSaveReqVO source) {
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setTitle(StrUtil.trimToNull(source.getTitle()));
        target.setTel(StrUtil.trimToNull(source.getTel()));
        target.setSort(source.getSort());
    }

    private Map<String, String> loadDepartmentNameMap(List<YzFxZhbMemberDO> list) {
        Set<String> deptIds = list.stream()
                .filter(Objects::nonNull)
                .map(YzFxZhbMemberDO::getCommandDepartmentId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return zhbMapper.selectBatchIds(deptIds).stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(YzFxZhbDO::getId,
                        item -> StrUtil.blankToDefault(item.getName(), item.getId()),
                        (a, b) -> a));
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
