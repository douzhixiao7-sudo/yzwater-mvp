package com.sydigit.yzwater.module.iot.service.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchImportReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationPreviewRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingImportItemRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSyncRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataMappingDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * IoT 实时数据点位映射 Service 接口
 */
public interface IotRealtimeDataMappingService {

    Long createMapping(@Valid IotRealtimeDataMappingSaveReqVO createReqVO);

    void updateMapping(@Valid IotRealtimeDataMappingSaveReqVO updateReqVO);

    void deleteMapping(Long id);

    void deleteMappingList(Collection<Long> ids);

    IotRealtimeDataMappingDO getMapping(Long id);

    PageResult<IotRealtimeDataMappingDO> getMappingPage(IotRealtimeDataMappingPageReqVO pageReqVO);

    List<IotRealtimeDataMappingDO> getMappingListBySourceIdAndEnabledForJob(Long sourceId, Boolean enabled);

    IotRealtimeDataMappingDO validateMappingExists(Long id);

    IotRealtimeDataMappingSyncRespVO syncMappingByDevice(Long deviceId);

    List<String> getPointNameListBySourceAndDevice(Long sourceId, Long deviceId);

    List<IotRealtimeDataMappingImportItemRespVO> getImportPreview(Long sourceId, Long deviceId);

    List<IotRealtimeDataMappingImportItemRespVO> getImportPreviewByTag(Long sourceId, Long deviceId);

    IotRealtimeDataMappingBatchImportRespVO importBatch(IotRealtimeDataMappingBatchImportReqVO reqVO);

    /**
     * 批量新增点位映射预览（按站点设备）
     *
     * @param sourceId 采集源编号
     * @param stationId 站点编号
     * @return 预览列表
     */
    List<IotRealtimeDataMappingBatchAddByStationPreviewRespVO> getBatchAddPreview(Long sourceId, String stationId);

    /**
     * 批量新增点位映射（按站点设备）
     *
     * @param reqVO 请求参数
     * @return 批量新增结果
     */
    IotRealtimeDataMappingBatchAddByStationRespVO batchAddByStation(IotRealtimeDataMappingBatchAddByStationReqVO reqVO);

}
