package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzWarningBroadcastStationDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzWarningBroadcastStationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

/**
 * 预警广播站服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class WarningBroadcastStationService {

    private final YzWarningBroadcastStationMapper warningBroadcastStationMapper;

    /**
     * 列表查询
     */
    public List<WarningBroadcastStationListRespVO> getList(WarningBroadcastStationListReqVO reqVO) {
        List<YzWarningBroadcastStationDO> list = warningBroadcastStationMapper.selectListByCondition(reqVO);
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 详情
     */
    public WarningBroadcastStationSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("预警广播站ID不能为空");
        }
        YzWarningBroadcastStationDO exists = warningBroadcastStationMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_120, "预警广播站不存在或已删除");
        }
        WarningBroadcastStationSaveReqVO vo = new WarningBroadcastStationSaveReqVO();
        vo.setId(exists.getId());
        vo.setName(exists.getName());
        vo.setLongitude(exists.getLongitude());
        vo.setLatitude(exists.getLatitude());
        vo.setAdminDivision(exists.getAdminDivision());
        vo.setCode(exists.getCode());
        vo.setQuantity(exists.getQuantity());
        vo.setSort(exists.getSort());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(WarningBroadcastStationSaveReqVO reqVO) {
        String id = generateId();
        YzWarningBroadcastStationDO insert = new YzWarningBroadcastStationDO();
        insert.setId(id);
        fillFields(insert, reqVO);
        warningBroadcastStationMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(WarningBroadcastStationSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("预警广播站ID不能为空");
        }
        YzWarningBroadcastStationDO exists = warningBroadcastStationMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_120, "预警广播站不存在或已删除");
        }
        YzWarningBroadcastStationDO update = new YzWarningBroadcastStationDO();
        update.setId(id);
        fillFields(update, reqVO);
        if (reqVO.getSort() == null) {
            update.setSort(exists.getSort());
        }
        warningBroadcastStationMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("预警广播站ID不能为空");
        }
        YzWarningBroadcastStationDO exists = warningBroadcastStationMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_120, "预警广播站不存在或已删除");
        }
        warningBroadcastStationMapper.deleteById(key);
    }

    private WarningBroadcastStationListRespVO buildListResp(YzWarningBroadcastStationDO item) {
        WarningBroadcastStationListRespVO vo = new WarningBroadcastStationListRespVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setLongitude(item.getLongitude());
        vo.setLatitude(item.getLatitude());
        vo.setAdminDivision(item.getAdminDivision());
        vo.setCode(item.getCode());
        vo.setQuantity(item.getQuantity());
        return vo;
    }

    private void fillFields(YzWarningBroadcastStationDO target, WarningBroadcastStationSaveReqVO source) {
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setLongitude(source.getLongitude());
        target.setLatitude(source.getLatitude());
        target.setAdminDivision(StrUtil.trimToNull(source.getAdminDivision()));
        target.setCode(StrUtil.trimToNull(source.getCode()));
        target.setQuantity(source.getQuantity());
        target.setSort(source.getSort());
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}

