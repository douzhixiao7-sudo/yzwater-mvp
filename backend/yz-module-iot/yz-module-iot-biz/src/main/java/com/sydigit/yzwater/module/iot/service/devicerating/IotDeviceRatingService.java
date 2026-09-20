package com.sydigit.yzwater.module.iot.service.devicerating;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.rating.IotDeviceRatingDO;

import java.util.List;

/**
 * 设备评级 Service 接口
 */
public interface IotDeviceRatingService {

    /**
     * 创建设备评级
     *
     * @param createReqVO 创建信息
     * @return 评级记录 ID
     */
    Long createDeviceRating(IotDeviceRatingSaveReqVO createReqVO);

    /**
     * 更新设备评级
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceRating(IotDeviceRatingSaveReqVO updateReqVO);

    /**
     * 删除设备评级
     *
     * @param id 评级记录 ID
     */
    void deleteDeviceRating(Long id);

    /**
     * 获取设备评级
     *
     * @param id 评级记录 ID
     * @return 评级记录
     */
    IotDeviceRatingDO getDeviceRating(Long id);

    /**
     * 获取设备评级分页
     *
     * @param pageReqVO 查询条件
     * @return 分页数据
     */
    PageResult<IotDeviceRatingDO> getDeviceRatingPage(IotDeviceRatingPageReqVO pageReqVO);

    /**
     * 获取设备评级列表
     *
     * @param pageReqVO 查询条件
     * @return 列表数据
     */
    List<IotDeviceRatingDO> getDeviceRatingList(IotDeviceRatingPageReqVO pageReqVO);
}
