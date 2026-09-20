package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardExcelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardExportExcelVO;
import com.sydigit.yzwater.module.service.signboard.SignboardBfExcelImportService;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardSaveReqVO;
import com.sydigit.yzwater.module.service.signboard.SignboardBfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 大屏-公示牌管理
 */
@Tag(name = "仪征管理后台 - 大屏-公示牌管理")
@RestController
@RequestMapping("/river/signboard-bf")
@Validated
public class SignboardBfController {

    private static final String TEMPLATE_FILE_NAME = "公示牌导入模版.xlsx";
    private static final String TEMPLATE_RESOURCE_PATH = "templates/import/" + TEMPLATE_FILE_NAME;

    private final SignboardBfService signboardService;
    private final SignboardBfExcelImportService signboardExcelImportService;

    public SignboardBfController(SignboardBfService signboardService,
                              SignboardBfExcelImportService signboardExcelImportService) {
        this.signboardService = signboardService;
        this.signboardExcelImportService = signboardExcelImportService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询公示牌")
    public CommonResult<PageResult<SignboardPageRespVO>> getPage(@Valid SignboardPageReqVO reqVO) {
        return success(signboardService.getSignboardPage(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询公示牌详情")
    public CommonResult<SignboardSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(signboardService.getSignboardDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增公示牌")
    public CommonResult<Long> create(@Valid @RequestBody SignboardSaveReqVO reqVO) {
        return success(signboardService.createSignboard(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑公示牌")
    public CommonResult<Boolean> update(@Valid @RequestBody SignboardSaveReqVO reqVO) {
        signboardService.updateSignboard(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公示牌")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        signboardService.deleteSignboard(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出公示牌 Excel")
    public void exportExcel(@Valid SignboardPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<SignboardExportExcelVO> list = signboardService.getSignboardExportList(reqVO);
        ExcelUtils.write(response, "公示牌信息.xls", "数据", SignboardExportExcelVO.class, list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载公示牌导入模板")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        Resource templateResource = resolveTemplateResource();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.addHeader("Content-Disposition",
                "attachment;filename=" + HttpUtils.encodeUtf8(TEMPLATE_FILE_NAME));
        try (InputStream inputStream = templateResource.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传公示牌统计 Excel 并导入公示牌信息（仅读取 sheet1）")
    public CommonResult<SignboardExcelImportRespVO> importExcel(@RequestPart("file") MultipartFile file) {
        return success(signboardExcelImportService.importExcel(file));
    }

    private Resource resolveTemplateResource() throws IOException {
        Resource resource = new ClassPathResource(TEMPLATE_RESOURCE_PATH);
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new IOException("未找到公示牌导入模板文件：" + TEMPLATE_RESOURCE_PATH);
    }
}
