package com.sydigit.yzwater.module.iot.service.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoAuditReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareIoDO;
import jakarta.validation.Valid;

/**
 * 备件出入库 Service 接口
 */
public interface IotSpareIoService {

    /**
     * 创建出入库记录
     *
     * @param createReqVO 创建请求
     * @return 主键
     */
    Long createSpareIo(@Valid IotSpareIoSaveReqVO createReqVO);

    /**
     * 更新出入库记录
     *
     * @param updateReqVO 更新请求
     */
    void updateSpareIo(@Valid IotSpareIoSaveReqVO updateReqVO);

    /**
     * 删除出入库记录
     *
     * @param id 主键
     */
    void deleteSpareIo(Long id);

    /**
     * 审批出入库记录
     *
     * @param auditReqVO 审批请求
     */
    void auditSpareIo(@Valid IotSpareIoAuditReqVO auditReqVO);

    /**
     * 获取出入库记录
     *
     * @param id 主键
     * @return 出入库记录
     */
    IotSpareIoDO getSpareIo(Long id);

    /**
     * 获取出入库记录分页
     *
     * @param pageReqVO 查询条件
     * @return 出入库记录分页
     */
    PageResult<IotSpareIoDO> getSpareIoPage(IotSpareIoPageReqVO pageReqVO);

}
