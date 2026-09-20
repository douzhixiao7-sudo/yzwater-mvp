package com.sydigit.yzwater.module.iot.service.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourcePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataSourceDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 实时数据采集源 Service 接口
 */
public interface IotRealtimeDataSourceService {

    Long createSource(@Valid IotRealtimeDataSourceSaveReqVO createReqVO);

    void updateSource(@Valid IotRealtimeDataSourceSaveReqVO updateReqVO);

    void deleteSource(Long id);

    IotRealtimeDataSourceDO getSource(Long id);

    PageResult<IotRealtimeDataSourceDO> getSourcePage(IotRealtimeDataSourcePageReqVO pageReqVO);

    List<IotRealtimeDataSourceDO> getSourceListByEnabledForJob(Boolean enabled);

    IotRealtimeDataSourceDO validateSourceExists(Long id);

}