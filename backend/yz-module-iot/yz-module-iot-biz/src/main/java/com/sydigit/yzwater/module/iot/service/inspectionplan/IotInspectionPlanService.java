package com.sydigit.yzwater.module.iot.service.inspectionplan;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanLineOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanStandardOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanTargetOptionRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanTargetDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 巡检计划 Service 接口
 */
public interface IotInspectionPlanService {

    /**
     * 创建巡检计划。
     */
    Long createPlan(@Valid IotInspectionPlanSaveReqVO createReqVO);

    /**
     * 更新巡检计划。
     */
    void updatePlan(@Valid IotInspectionPlanSaveReqVO updateReqVO);

    /**
     * 删除巡检计划。
     */
    void deletePlan(Long id);

    /**
     * 获取巡检计划。
     */
    IotInspectionPlanDO getPlan(Long id);

    /**
     * 校验巡检计划是否存在。
     */
    IotInspectionPlanDO validatePlanExists(Long id);

    /**
     * 分页查询巡检计划。
     */
    PageResult<IotInspectionPlanDO> getPlanPage(IotInspectionPlanPageReqVO pageReqVO);

    /**
     * 根据计划 ID 查询对象列表。
     */
    List<IotInspectionPlanTargetDO> getPlanTargetList(Long planId);

    /**
     * 根据计划 ID 集合查询对象列表。
     */
    List<IotInspectionPlanTargetDO> getPlanTargetList(Collection<Long> planIds);

    /**
     * 查询巡检对象下拉。
     */
    List<IotInspectionPlanTargetOptionRespVO> listTargetOptions(Integer objectType,
                                                                String stationId,
                                                                String keyword,
                                                                Integer limit);

    /**
     * 查询巡检标准下拉。
     */
    List<IotInspectionPlanStandardOptionRespVO> listStandardOptions(String stationId,
                                                                    String inspectionType,
                                                                    String keyword,
                                                                    Integer limit);

    /**
     * 查询巡检线路下拉。
     */
    List<IotInspectionPlanLineOptionRespVO> listLineOptions(String stationId,
                                                            String inspectionType,
                                                            String keyword,
                                                            Integer limit);

    /**
     * 查询标准名称映射。
     */
    Map<Long, String> getStandardNameMap(Collection<Long> standardIds);

    /**
     * 查询线路名称映射。
     */
    Map<Long, String> getLineNameMap(Collection<Long> lineIds);

    /**
     * 扫描到期巡检计划并自动生成任务。
     *
     * @param limit 本次处理上限，<=0 时使用默认值
     * @return 实际处理成功的计划数量
     */
    int generateDuePlanTasks(Integer limit);

}
