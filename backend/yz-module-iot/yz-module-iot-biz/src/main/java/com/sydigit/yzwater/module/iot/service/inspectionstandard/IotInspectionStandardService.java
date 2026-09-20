package com.sydigit.yzwater.module.iot.service.inspectionstandard;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionTargetOptionRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemRecordDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardTargetDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 巡检标准 Service 接口。
 */
public interface IotInspectionStandardService {

    /**
     * 创建巡检标准。
     */
    Long createStandard(@Valid IotInspectionStandardSaveReqVO createReqVO);

    /**
     * 更新巡检标准。
     */
    void updateStandard(@Valid IotInspectionStandardSaveReqVO updateReqVO);

    /**
     * 删除巡检标准。
     */
    void deleteStandard(Long id);

    /**
     * 获取巡检标准。
     */
    IotInspectionStandardDO getStandard(Long id);

    /**
     * 校验巡检标准是否存在。
     */
    IotInspectionStandardDO validateStandardExists(Long id);

    /**
     * 巡检标准分页查询。
     */
    PageResult<IotInspectionStandardDO> getStandardPage(IotInspectionStandardPageReqVO pageReqVO);

    /**
     * 根据标准 ID 查询适用对象列表。
     */
    List<IotInspectionStandardTargetDO> getStandardTargetList(Long standardId);

    /**
     * 根据适用对象 ID 集合查询检查项列表。
     */
    List<IotInspectionStandardItemDO> getStandardItemListByTargetIds(Collection<Long> targetIds);

    /**
     * 根据检查项 ID 集合查询记录模板列表。
     */
    List<IotInspectionStandardItemRecordDO> getStandardItemRecordList(Collection<Long> itemIds);

    /**
     * 获取适用对象下拉选项。
     */
    List<IotInspectionTargetOptionRespVO> listTargetOptions(String targetType, String stationId, String keyword, Integer limit);

}
