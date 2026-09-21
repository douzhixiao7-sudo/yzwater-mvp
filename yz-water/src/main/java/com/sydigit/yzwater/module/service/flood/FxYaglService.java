package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxYaglListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxYaglSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxYaglDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxYaglMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 预案管理服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxYaglService {

    private final YzFxYaglMapper yaglMapper;

    /**
     * 列表查询
     */
    public List<FxYaglListRespVO> getList() {
        List<YzFxYaglDO> list = yaglMapper.selectListOrderBySort();
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 详情
     */
    public FxYaglSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("预案 ID 不能为空");
        }
        YzFxYaglDO exists = yaglMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "预案不存在或已删除");
        }
        FxYaglSaveReqVO vo = new FxYaglSaveReqVO();
        vo.setId(exists.getId());
        vo.setName(exists.getName());
        vo.setFiles(toFileList(exists.getFiles()));
        vo.setSort(exists.getSort());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxYaglSaveReqVO reqVO) {
        String id = generateId();
        YzFxYaglDO insert = new YzFxYaglDO();
        insert.setId(id);
        fillFields(insert, reqVO);
        yaglMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxYaglSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("预案 ID 不能为空");
        }
        YzFxYaglDO exists = yaglMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "预案不存在或已删除");
        }
        YzFxYaglDO update = new YzFxYaglDO();
        update.setId(id);
        fillFields(update, reqVO);
        yaglMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("预案 ID 不能为空");
        }
        YzFxYaglDO exists = yaglMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "预案不存在或已删除");
        }
        yaglMapper.deleteById(key);
    }

    private FxYaglListRespVO buildListResp(YzFxYaglDO item) {
        FxYaglListRespVO vo = new FxYaglListRespVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setFiles(toFileList(item.getFiles()));
        vo.setSort(item.getSort());
        return vo;
    }

    private void fillFields(YzFxYaglDO target, FxYaglSaveReqVO source) {
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setFiles(normalizeFiles(source.getFiles()));
        target.setSort(source.getSort() == null ? 0 : source.getSort());
    }

    private String[] normalizeFiles(List<String> files) {
        if (CollUtil.isEmpty(files)) {
            return null;
        }
        List<String> cleaned = files.stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
    }

    private List<String> toFileList(String[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }
        return Arrays.stream(files)
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
