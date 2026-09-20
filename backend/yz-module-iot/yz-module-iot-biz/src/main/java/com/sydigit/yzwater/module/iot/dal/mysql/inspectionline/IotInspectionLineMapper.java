package com.sydigit.yzwater.module.iot.dal.mysql.inspectionline;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IotInspectionLineMapper extends BaseMapperX<IotInspectionLineDO> {

    /**
     * 同步线路使用次数（按巡检任务引用统计）。
     */
    default void syncUseCount() {
        resetUseCount();
        fillUseCountByReference();
    }

    /**
     * 先清零使用次数，避免无引用线路保留旧值。
     */
    @Update("UPDATE yz_equipment_inspection_line SET use_count = 0 WHERE deleted = 0")
    void resetUseCount();

    /**
     * 根据巡检任务引用回填使用次数。
     */
    @Update("""
            UPDATE yz_equipment_inspection_line l
            SET use_count = ref.use_count
            FROM (
                SELECT line_id, tenant_id, COUNT(1) AS use_count
                FROM yz_equipment_inspection_task
                WHERE deleted = 0 AND line_id IS NOT NULL
                GROUP BY line_id, tenant_id
            ) ref
            WHERE l.deleted = 0
              AND l.id = ref.line_id
              AND l.tenant_id = ref.tenant_id
            """)
    void fillUseCountByReference();

    /**
     * 分页查询巡检线路。
     *
     * @param reqVO 分页请求
     * @return 分页结果
     */
    default PageResult<IotInspectionLineDO> selectPage(IotInspectionLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotInspectionLineDO>()
                .eqIfPresent(IotInspectionLineDO::getStationId, reqVO.getStationId())
                .likeIfPresent(IotInspectionLineDO::getLineName, reqVO.getLineName())
                .eqIfPresent(IotInspectionLineDO::getInspectionType, reqVO.getInspectionType())
                .eqIfPresent(IotInspectionLineDO::getAreaType, reqVO.getAreaType())
                .geIfPresent(IotInspectionLineDO::getPointCount, reqVO.getMinPointCount())
                .leIfPresent(IotInspectionLineDO::getPointCount, reqVO.getMaxPointCount())
                .geIfPresent(IotInspectionLineDO::getUseCount, reqVO.getMinUseCount())
                .leIfPresent(IotInspectionLineDO::getUseCount, reqVO.getMaxUseCount())
                .eqIfPresent(IotInspectionLineDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotInspectionLineDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotInspectionLineDO::getId));
    }

    /**
     * 按线路名称查询，用于名称唯一性校验。
     *
     * @param lineName 线路名称
     * @return 线路信息
     */
    default IotInspectionLineDO selectByName(String lineName) {
        if (StrUtil.isBlank(lineName)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotInspectionLineDO>()
                .eq(IotInspectionLineDO::getLineName, lineName));
    }

}
