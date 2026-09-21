package com.sydigit.yzwater.module.iot.service.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckUpdateReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareCheckDO;
import jakarta.validation.Valid;

/**
 * 备件盘点 Service 接口
 */
public interface IotSpareCheckService {

    /**
     * 创建盘点记录
     *
     * @param createReqVO 创建请求
     * @return 主键
     */
    Long createCheck(@Valid IotSpareCheckSaveReqVO createReqVO);

    /**
     * 修改盘点记录
     *
     * @param updateReqVO 修改请求
     */
    void updateCheck(@Valid IotSpareCheckUpdateReqVO updateReqVO);

    /**
     * 删除盘点记录
     *
     * @param id 主键
     */
    void deleteCheck(Long id);

    /**
     * 反馈盘点结果
     *
     * @param id 主键
     */
    void applyCheckResult(Long id);

    /**
     * 反审批盘点结果
     *
     * @param id 主键
     */
    void reverseApplyCheckResult(Long id);

    /**
     * 获取盘点记录
     *
     * @param id 主键
     * @return 盘点记录
     */
    IotSpareCheckDO getCheck(Long id);

    /**
     * 获取盘点记录分页
     *
     * @param pageReqVO 查询条件
     * @return 盘点记录分页
     */
    PageResult<IotSpareCheckDO> getCheckPage(IotSpareCheckPageReqVO pageReqVO);

}
