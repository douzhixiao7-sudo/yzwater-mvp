package com.sydigit.yzwater.module.iot.service.inspectionline;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePointOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLineSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLineDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLinePointDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 巡检线路 Service 接口。
 */
public interface IotInspectionLineService {

    /**
     * 创建巡检线路。
     *
     * @param createReqVO 创建请求
     * @return 线路 ID
     */
    Long createLine(@Valid IotInspectionLineSaveReqVO createReqVO);

    /**
     * 更新巡检线路。
     *
     * @param updateReqVO 更新请求
     */
    void updateLine(@Valid IotInspectionLineSaveReqVO updateReqVO);

    /**
     * 删除巡检线路。
     *
     * @param id 线路 ID
     */
    void deleteLine(Long id);

    /**
     * 获取巡检线路。
     *
     * @param id 线路 ID
     * @return 线路信息
     */
    IotInspectionLineDO getLine(Long id);

    /**
     * 校验巡检线路是否存在。
     *
     * @param id 线路 ID
     * @return 线路信息
     */
    IotInspectionLineDO validateLineExists(Long id);

    /**
     * 巡检线路分页查询。
     *
     * @param pageReqVO 分页请求
     * @return 分页结果
     */
    PageResult<IotInspectionLineDO> getLinePage(IotInspectionLinePageReqVO pageReqVO);

    /**
     * 根据线路 ID 查询点位列表。
     *
     * @param lineId 线路 ID
     * @return 点位列表
     */
    List<IotInspectionLinePointDO> getLinePointList(Long lineId);

    /**
     * 查询可选设备点位列表。
     *
     * @param stationId 站点
     * @param keyword   关键字
     * @param limit     最大条数
     * @return 点位候选列表
     */
    List<IotInspectionLinePointOptionRespVO> listPointOptions(String stationId, String keyword, Integer limit);

}
