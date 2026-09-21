package com.sydigit.yzwater.module.iot.convert.dispatchplan;

import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanObjectRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanParamRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanObjectDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanObjectParamDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 调度方案 Convert
 */
@Mapper
public interface IotDispatchPlanConvert {

    IotDispatchPlanConvert INSTANCE = Mappers.getMapper(IotDispatchPlanConvert.class);

    @Mapping(target = "planNo", ignore = true)
    @Mapping(target = "objectCount", ignore = true)
    IotDispatchPlanDO convert(IotDispatchPlanSaveReqVO bean);

    @Mapping(target = "planTypeName", ignore = true)
    @Mapping(target = "planStatusName", ignore = true)
    @Mapping(target = "objectNames", ignore = true)
    @Mapping(target = "objects", ignore = true)
    IotDispatchPlanRespVO convert(IotDispatchPlanDO bean);

    @Mapping(target = "params", ignore = true)
    IotDispatchPlanObjectRespVO convert(IotDispatchPlanObjectDO bean);

    IotDispatchPlanParamRespVO convert(IotDispatchPlanObjectParamDO bean);

    @Mapping(target = "objectNames", ignore = true)
    @Mapping(target = "attachmentCount", ignore = true)
    IotDispatchPlanExportExcelVO convertExcel(IotDispatchPlanRespVO bean);
}
