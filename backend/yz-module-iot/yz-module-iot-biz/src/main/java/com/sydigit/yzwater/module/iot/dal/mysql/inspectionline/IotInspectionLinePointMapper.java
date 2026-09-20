package com.sydigit.yzwater.module.iot.dal.mysql.inspectionline;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLinePointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionLinePointMapper extends BaseMapperX<IotInspectionLinePointDO> {

    /**
     * 根据线路 ID 查询点位列表。
     *
     * @param lineId 线路 ID
     * @return 点位列表
     */
    default List<IotInspectionLinePointDO> selectListByLineId(Long lineId) {
        if (lineId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionLinePointDO>()
                .eq(IotInspectionLinePointDO::getLineId, lineId)
                .orderByAsc(IotInspectionLinePointDO::getPointSort)
                .orderByAsc(IotInspectionLinePointDO::getId));
    }

    /**
     * 根据线路 ID 删除点位。
     *
     * @param lineId 线路 ID
     */
    default void deleteByLineId(Long lineId) {
        if (lineId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotInspectionLinePointDO>()
                .eq(IotInspectionLinePointDO::getLineId, lineId));
    }

}
