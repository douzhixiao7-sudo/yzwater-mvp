package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryRespVO;
import com.sydigit.yzwater.module.service.gis.GisBufferQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "仪征管理后台 - 缓冲区查询")
@RestController
@RequestMapping("/gis/buffer-query")
@Validated
public class GisBufferQueryController {

    private final GisBufferQueryService bufferQueryService;

    public GisBufferQueryController(GisBufferQueryService bufferQueryService) {
        this.bufferQueryService = bufferQueryService;
    }

    @PostMapping
    @Operation(summary = "创建缓冲区查询")
    public CommonResult<GisBufferQueryRespVO> createBufferQuery(@Valid @RequestBody GisBufferQueryCreateReqVO reqVO) {
        return success(bufferQueryService.createBufferQuery(reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询缓冲区历史")
    public CommonResult<PageResult<GisBufferQueryPageRespVO>> getBufferQueryPage(@Valid GisBufferQueryPageReqVO reqVO) {
        return success(bufferQueryService.getBufferQueryPage(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "缓冲区详情")
    public CommonResult<GisBufferQueryRespVO> getBufferQueryDetail(@PathVariable("id") Long id) {
        return success(bufferQueryService.getBufferQueryDetail(id));
    }
}
