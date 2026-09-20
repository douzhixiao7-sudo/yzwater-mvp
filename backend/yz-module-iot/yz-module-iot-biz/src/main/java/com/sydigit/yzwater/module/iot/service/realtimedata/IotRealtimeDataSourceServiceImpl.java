package com.sydigit.yzwater.module.iot.service.realtimedata;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourcePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataSourceDO;
import com.sydigit.yzwater.module.iot.dal.mysql.realtimedata.IotRealtimeDataSourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.REALTIME_DATA_SOURCE_CODE_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.REALTIME_DATA_SOURCE_NOT_EXISTS;

/**
 * IoT 实时数据采集源 Service 实现类
 */
@Service
@Validated
public class IotRealtimeDataSourceServiceImpl implements IotRealtimeDataSourceService {

    @Resource
    private IotRealtimeDataSourceMapper sourceMapper;

    @Override
    public Long createSource(IotRealtimeDataSourceSaveReqVO createReqVO) {
        validateCodeUnique(createReqVO.getCode(), null);
        IotRealtimeDataSourceDO source = BeanUtils.toBean(createReqVO, IotRealtimeDataSourceDO.class);
        sourceMapper.insert(source);
        return source.getId();
    }

    @Override
    public void updateSource(IotRealtimeDataSourceSaveReqVO updateReqVO) {
        validateSourceExists(updateReqVO.getId());
        validateCodeUnique(updateReqVO.getCode(), updateReqVO.getId());
        IotRealtimeDataSourceDO updateObj = BeanUtils.toBean(updateReqVO, IotRealtimeDataSourceDO.class);
        sourceMapper.updateById(updateObj);
    }

    @Override
    public void deleteSource(Long id) {
        validateSourceExists(id);
        sourceMapper.deleteById(id);
    }

    @Override
    public IotRealtimeDataSourceDO getSource(Long id) {
        return sourceMapper.selectById(id);
    }

    @Override
    public PageResult<IotRealtimeDataSourceDO> getSourcePage(IotRealtimeDataSourcePageReqVO pageReqVO) {
        return sourceMapper.selectPage(pageReqVO);
    }

    @Override
    @TenantIgnore
    public List<IotRealtimeDataSourceDO> getSourceListByEnabledForJob(Boolean enabled) {
        return sourceMapper.selectListByEnabled(enabled);
    }

    @Override
    public IotRealtimeDataSourceDO validateSourceExists(Long id) {
        IotRealtimeDataSourceDO source = sourceMapper.selectById(id);
        if (source == null) {
            throw exception(REALTIME_DATA_SOURCE_NOT_EXISTS);
        }
        return source;
    }

    private void validateCodeUnique(String code, Long id) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        IotRealtimeDataSourceDO source = sourceMapper.selectByCode(code);
        if (source == null) {
            return;
        }
        if (id == null || !source.getId().equals(id)) {
            throw exception(REALTIME_DATA_SOURCE_CODE_EXISTS);
        }
    }

}
