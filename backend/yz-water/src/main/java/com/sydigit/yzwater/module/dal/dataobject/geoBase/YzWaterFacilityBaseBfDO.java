package com.sydigit.yzwater.module.dal.dataobject.geoBase;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;

/**
 * 大屏-BF 水利对象基础信息实体
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "大屏-BF 水利对象基础信息实体")
@TableName(value = "yz_water_facility_base_bf", autoResultMap = true)
public class YzWaterFacilityBaseBfDO extends YzWaterFacilityBaseDO {
}
