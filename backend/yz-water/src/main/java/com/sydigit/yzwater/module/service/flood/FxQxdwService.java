package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxQxdwListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxQxdwSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxQxdwDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxQxdwMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 市级防汛抢险队伍服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxQxdwService {

    private final YzFxQxdwMapper qxdwMapper;

    /**
     * 列表查询
     */
    public List<FxQxdwListRespVO> getList() {
        List<YzFxQxdwDO> list = qxdwMapper.selectListOrderBySort();
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 导出 Excel 行数据（显式 5 列，不含「队伍名称」）
     */
    public List<List<Object>> getExportExcelDataRows() {
        List<YzFxQxdwDO> list = qxdwMapper.selectListOrderBySort();
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildExportDataRow)
                .toList();
    }

    /**
     * 详情
     */
    public FxQxdwSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("抢险队伍ID不能为空");
        }
        YzFxQxdwDO exists = qxdwMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "抢险队伍不存在或已删除");
        }
        FxQxdwSaveReqVO vo = new FxQxdwSaveReqVO();
        vo.setId(exists.getId());
        vo.setUnitName(exists.getUnitName());
        vo.setTeamName(exists.getTeamName());
        vo.setPlanCount(exists.getPlanCount());
        vo.setContactName(exists.getContactName());
        vo.setContactPhone(exists.getContactPhone());
        vo.setRemark(exists.getRemark());
        vo.setSort(exists.getSort());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxQxdwSaveReqVO reqVO) {
        String id = generateId();
        YzFxQxdwDO insert = new YzFxQxdwDO();
        insert.setId(id);
        fillFields(insert, reqVO);
        qxdwMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxQxdwSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("抢险队伍ID不能为空");
        }
        YzFxQxdwDO exists = qxdwMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "抢险队伍不存在或已删除");
        }
        Integer sort = reqVO.getSort() == null ? exists.getSort() : reqVO.getSort();
        qxdwMapper.update(null, new LambdaUpdateWrapper<YzFxQxdwDO>()
                .eq(YzFxQxdwDO::getId, id)
                .set(YzFxQxdwDO::getUnitName, StrUtil.trimToNull(reqVO.getUnitName()))
                .set(YzFxQxdwDO::getTeamName, StrUtil.trimToNull(reqVO.getTeamName()))
                .set(YzFxQxdwDO::getPlanCount, reqVO.getPlanCount())
                .set(YzFxQxdwDO::getContactName, StrUtil.trimToNull(reqVO.getContactName()))
                .set(YzFxQxdwDO::getContactPhone, StrUtil.trimToNull(reqVO.getContactPhone()))
                .set(YzFxQxdwDO::getRemark, StrUtil.trimToNull(reqVO.getRemark()))
                .set(YzFxQxdwDO::getSort, sort));
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("抢险队伍ID不能为空");
        }
        YzFxQxdwDO exists = qxdwMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "抢险队伍不存在或已删除");
        }
        qxdwMapper.deleteById(key);
    }

    private FxQxdwListRespVO buildListResp(YzFxQxdwDO item) {
        FxQxdwListRespVO vo = new FxQxdwListRespVO();
        vo.setId(item.getId());
        vo.setUnitName(item.getUnitName());
        vo.setTeamName(item.getTeamName());
        vo.setPlanCount(item.getPlanCount());
        vo.setContactName(item.getContactName());
        vo.setContactPhone(item.getContactPhone());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private List<Object> buildExportDataRow(YzFxQxdwDO item) {
        List<Object> row = new ArrayList<>(5);
        row.add(item.getUnitName());
        row.add(item.getPlanCount());
        row.add(item.getContactName());
        row.add(item.getContactPhone());
        row.add(item.getRemark());
        return row;
    }

    private void fillFields(YzFxQxdwDO target, FxQxdwSaveReqVO source) {
        target.setUnitName(StrUtil.trimToNull(source.getUnitName()));
        target.setTeamName(StrUtil.trimToNull(source.getTeamName()));
        target.setPlanCount(source.getPlanCount());
        target.setContactName(StrUtil.trimToNull(source.getContactName()));
        target.setContactPhone(StrUtil.trimToNull(source.getContactPhone()));
        target.setRemark(StrUtil.trimToNull(source.getRemark()));
        target.setSort(source.getSort());
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
