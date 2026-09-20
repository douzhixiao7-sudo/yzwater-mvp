package com.sydigit.yzwater.module.iot.service.shiftschedule;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleCalendarReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleImportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftSchedulePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleShiftOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleTeamOptionRespVO;

import java.util.List;

/**
 * 员工排班 Service
 */
public interface IotShiftScheduleService {

    /**
     * 创建员工排班
     */
    Long createShiftSchedule(IotShiftScheduleSaveReqVO createReqVO);

    /**
     * 更新员工排班
     */
    void updateShiftSchedule(IotShiftScheduleSaveReqVO updateReqVO);

    /**
     * 删除员工排班
     */
    void deleteShiftSchedule(Long id);

    /**
     * 获取员工排班详情
     */
    IotShiftScheduleRespVO getShiftSchedule(Long id);

    /**
     * 分页查询员工排班
     */
    PageResult<IotShiftScheduleRespVO> getShiftSchedulePage(IotShiftSchedulePageReqVO pageReqVO);

    /**
     * 导出查询员工排班
     */
    List<IotShiftScheduleRespVO> getShiftScheduleList(IotShiftSchedulePageReqVO reqVO);

    /**
     * 获取月历排班数据
     */
    List<IotShiftScheduleRespVO> getShiftScheduleCalendar(IotShiftScheduleCalendarReqVO reqVO);

    /**
     * 获取班次选项
     */
    List<IotShiftScheduleShiftOptionRespVO> getShiftOptions(String stationId);

    /**
     * 获取班组选项
     */
    List<IotShiftScheduleTeamOptionRespVO> getTeamOptions(String stationId);

    /**
     * 导入员工排班
     */
    IotShiftScheduleImportRespVO importShiftSchedule(List<IotShiftScheduleImportExcelVO> importList);
}
