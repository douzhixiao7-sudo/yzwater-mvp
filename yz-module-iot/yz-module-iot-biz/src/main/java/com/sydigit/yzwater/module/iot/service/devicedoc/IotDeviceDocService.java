package com.sydigit.yzwater.module.iot.service.devicedoc;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.doc.IotDeviceDocDO;
import jakarta.validation.Valid;

/**
 * 设备技术资料 Service 接口
 */
public interface IotDeviceDocService {

    /**
     * 创建技术资料
     *
     * @param createReqVO 新增请求
     * @return 主键
     */
    Long createDeviceDoc(@Valid IotDeviceDocSaveReqVO createReqVO);

    /**
     * 更新技术资料
     *
     * @param updateReqVO 修改请求
     */
    void updateDeviceDoc(@Valid IotDeviceDocSaveReqVO updateReqVO);

    /**
     * 删除技术资料
     *
     * @param id 主键
     */
    void deleteDeviceDoc(Long id);

    /**
     * 获得技术资料
     *
     * @param id 主键
     * @return 技术资料
     */
    IotDeviceDocDO getDeviceDoc(Long id);

    /**
     * 获得技术资料分页
     *
     * @param pageReqVO 分页查询
     * @return 分页数据
     */
    PageResult<IotDeviceDocDO> getDeviceDocPage(IotDeviceDocPageReqVO pageReqVO);

}
