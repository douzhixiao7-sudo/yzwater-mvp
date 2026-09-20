package com.sydigit.yzwater.module.iot.service.device;

import com.sydigit.yzwater.module.iot.controller.admin.device.vo.location.IotDeviceLocationSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceLocationDO;
import com.sydigit.yzwater.module.iot.service.device.dto.IotDeviceLocationNode;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备位置 Service 接口
 */
public interface IotDeviceLocationService {

    /**
     * 获得位置树
     *
     * @return 树形列表
     */
    List<IotDeviceLocationNode> getLocationTree();

    /**
     * 创建位置
     *
     * @param createReqVO 创建信息
     * @return 位置编号
     */
    Long createLocation(@Valid IotDeviceLocationSaveReqVO createReqVO);

    /**
     * 更新位置
     *
     * @param updateReqVO 更新信息
     */
    void updateLocation(@Valid IotDeviceLocationSaveReqVO updateReqVO);

    /**
     * 删除位置
     *
     * @param id 位置编号
     */
    void deleteLocation(Long id);

    /**
     * 获得位置详情
     *
     * @param id 位置编号
     * @return 位置信息
     */
    IotDeviceLocationDO getLocation(Long id);

    /**
     * 校验位置存在
     *
     * @param id 位置编号
     * @return 位置信息
     */
    IotDeviceLocationDO validateLocationExists(Long id);

}
