package com.sydigit.yzwater.module.iot.service.dispatchmanage;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePlanOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageReceiverUserRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageSubmitResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceivePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveRunLogOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveSubmitReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 调度管理 Service
 */
public interface IotDispatchManageService {

    /**
     * 创建调度指令
     */
    Long createInstruction(@Valid IotDispatchManageSaveReqVO createReqVO);

    /**
     * 更新调度指令
     */
    void updateInstruction(@Valid IotDispatchManageSaveReqVO updateReqVO);

    /**
     * 删除调度指令
     */
    void deleteInstruction(Long id);

    /**
     * 获取调度指令详情
     */
    IotDispatchManageRespVO getInstruction(Long id);

    /**
     * 分页查询调度指令
     */
    PageResult<IotDispatchManageRespVO> getInstructionPage(IotDispatchManagePageReqVO pageReqVO);

    /**
     * 导出查询调度指令
     */
    List<IotDispatchManageRespVO> getInstructionList(IotDispatchManagePageReqVO reqVO);

    /**
     * 提交执行反馈
     */
    void submitResult(@Valid IotDispatchManageSubmitResultReqVO reqVO);

    /**
     * 获取调度方案选项
     *
     * @param planStatus 方案状态（可选）
     * @param stationId  站点编号（可选）
     */
    List<IotDispatchManagePlanOptionRespVO> getPlanOptions(Integer planStatus, String stationId);

    /**
     * 根据部门查询接收人
     */
    List<IotDispatchManageReceiverUserRespVO> getReceiverUserList(Long deptId);

    /**
     * 查询执行人（全系统启用用户，排除游客角色）
     */
    List<IotDispatchManageReceiverUserRespVO> getExecutorUserList(Long deptId);

    /**
     * 调令接受分页查询
     */
    PageResult<IotDispatchReceiveRespVO> getReceivePage(@Valid IotDispatchReceivePageReqVO reqVO);

    /**
     * 调令接受详情
     */
    IotDispatchReceiveRespVO getReceive(Long id);

    /**
     * 接收调令
     */
    void acceptReceive(Long id);

    /**
     * 提交调令执行结果
     */
    void submitReceiveResult(@Valid IotDispatchReceiveSubmitReqVO reqVO);

    /**
     * 调令接受导出查询
     */
    List<IotDispatchReceiveRespVO> getReceiveList(@Valid IotDispatchReceivePageReqVO reqVO);

    /**
     * 运行日志选项
     */
    List<IotDispatchReceiveRunLogOptionRespVO> getReceiveRunLogOptions(String stationId);
}
