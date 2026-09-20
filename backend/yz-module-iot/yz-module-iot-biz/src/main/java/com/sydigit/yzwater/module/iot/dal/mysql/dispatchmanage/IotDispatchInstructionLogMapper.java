package com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage.IotDispatchInstructionLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度指令日志 Mapper
 */
@Mapper
public interface IotDispatchInstructionLogMapper extends BaseMapperX<IotDispatchInstructionLogDO> {
}

