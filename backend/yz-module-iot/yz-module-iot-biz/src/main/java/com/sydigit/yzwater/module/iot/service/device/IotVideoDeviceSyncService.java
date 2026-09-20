package com.sydigit.yzwater.module.iot.service.device;

import com.sydigit.yzwater.module.iot.controller.admin.device.vo.device.IotVideoDeviceSyncRespVO;

/**
 * 视频摄像头与 IoT 设备同步 Service
 */
public interface IotVideoDeviceSyncService {

    /**
     * 同步视频区域、视频摄像头并将结果写入 IoT 设备
     *
     * @return 同步结果
     */
    IotVideoDeviceSyncRespVO syncVideoDevices();
}

