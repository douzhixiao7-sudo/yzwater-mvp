package com.sydigit.yzwater.module.controller.admin.vo.gis;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "仪征管理后台 - 缓冲区查询分页请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class GisBufferQueryPageReqVO extends PageParam {
}
