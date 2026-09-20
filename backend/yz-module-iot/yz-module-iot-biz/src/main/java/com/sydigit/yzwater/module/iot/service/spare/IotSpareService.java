package com.sydigit.yzwater.module.iot.service.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSparePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSpareSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 备件台账 Service 接口
 */
public interface IotSpareService {

    /**
     * 创建备件台账
     *
     * @param createReqVO 创建请求
     * @return 主键
     */
    Long createSpare(@Valid IotSpareSaveReqVO createReqVO);

    /**
     * 更新备件台账
     *
     * @param updateReqVO 更新请求
     */
    void updateSpare(@Valid IotSpareSaveReqVO updateReqVO);

    /**
     * 删除备件台账
     *
     * @param id 主键
     */
    void deleteSpare(Long id);

    /**
     * 校验备件台账存在
     *
     * @param id 主键
     * @return 备件台账
     */
    IotSpareDO validateSpareExists(Long id);

    /**
     * 获取备件台账
     *
     * @param id 主键
     * @return 备件台账
     */
    IotSpareDO getSpare(Long id);

    /**
     * 获取备件台账基础信息（不含大字段）
     *
     * @param id 主键
     * @return 备件台账
     */
    IotSpareDO getSpareBasic(Long id);

    /**
     * 获取备件台账分页
     *
     * @param pageReqVO 查询条件
     * @return 备件台账分页
     */
    PageResult<IotSpareDO> getSparePage(IotSparePageReqVO pageReqVO);

    /**
     * 按 ID 批量获取备件台账
     *
     * @param ids 主键集合
     * @return 备件台账列表
     */
    List<IotSpareDO> getSpareListByIds(Collection<Long> ids);

    /**
     * 获取全部备件台账
     *
     * @return 备件台账列表
     */
    List<IotSpareDO> getSpareList();

}
