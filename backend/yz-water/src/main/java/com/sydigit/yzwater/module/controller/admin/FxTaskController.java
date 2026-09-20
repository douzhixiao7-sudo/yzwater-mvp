package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxTaskListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxTaskSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 风险隐患点
 */
@Tag(name = "管理后台 - 风险隐患点")
@RestController
@RequestMapping("/fx-task")
@Validated
public class FxTaskController {

    private final FxTaskService taskService;

    public FxTaskController(FxTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询风险隐患点")
    public CommonResult<List<FxTaskListRespVO>> getList() {
        return success(taskService.getList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询风险隐患点详情")
    public CommonResult<FxTaskSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(taskService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增风险隐患点")
    public CommonResult<String> create(@Valid @RequestBody FxTaskSaveReqVO reqVO) {
        return success(taskService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑风险隐患点")
    public CommonResult<Boolean> update(@Valid @RequestBody FxTaskSaveReqVO reqVO) {
        taskService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除风险隐患点")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        taskService.delete(id);
        return success(true);
    }
}
