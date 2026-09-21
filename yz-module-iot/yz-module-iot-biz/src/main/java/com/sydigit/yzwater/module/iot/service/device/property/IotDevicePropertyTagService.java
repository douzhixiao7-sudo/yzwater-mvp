package com.sydigit.yzwater.module.iot.service.device.property;

import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyTagSaveReqVO;

import java.util.Map;

/**
 * IoT 设备属性标签 Service 接口
 */
public interface IotDevicePropertyTagService {

    /**
     * 获取设备属性标签映射
     *
     * @param deviceId 设备编号
     * @return 标识符 -> 标签名称
     */
    Map<String, String> getTagNameMap(Long deviceId);

    /**
     * 批量保存设备属性标签
     *
     * @param reqVO 保存请求
     */
    void saveTagNames(IotDevicePropertyTagSaveReqVO reqVO);

}
