package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentGisImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentSaveReqVO;
import com.sydigit.yzwater.module.service.embankment.EmbankmentExcelImportService;
import com.sydigit.yzwater.module.service.embankment.EmbankmentGisExcelImportService;
import com.sydigit.yzwater.module.service.embankment.EmbankmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
 * 仪征管理后台 - 堤防管理
 */
@Tag(name = "仪征管理后台 - 堤防管理")
@RestController
@RequestMapping("/embankment")
@Validated
public class EmbankmentController {

    private static final String TEMPLATE_FILE_NAME = "堤防导入模版.xlsx";
    private static final String TEMPLATE_RESOURCE_PATH = "templates/import/" + TEMPLATE_FILE_NAME;

    private final EmbankmentService embankmentService;
    private final EmbankmentExcelImportService embankmentExcelImportService;
    private final EmbankmentGisExcelImportService embankmentGisExcelImportService;

    public EmbankmentController(EmbankmentService embankmentService,
                                EmbankmentExcelImportService embankmentExcelImportService,
                                EmbankmentGisExcelImportService embankmentGisExcelImportService) {
        this.embankmentService = embankmentService;
        this.embankmentExcelImportService = embankmentExcelImportService;
        this.embankmentGisExcelImportService = embankmentGisExcelImportService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询堤防")
    public CommonResult<PageResult<EmbankmentPageRespVO>> getPage(@Valid EmbankmentPageReqVO reqVO) {
        return success(embankmentService.getEmbankmentPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出堤防 Excel")
    public void exportExcel(@Valid EmbankmentPageReqVO reqVO,
                            HttpServletResponse response) throws IOException {
        List<EmbankmentExportExcelVO> list = embankmentService.getEmbankmentExportList(reqVO);
        ExcelUtils.write(response, "堤防信息.xls", "数据", EmbankmentExportExcelVO.class, list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载堤防导入模板")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        Resource templateResource = resolveTemplateResource();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.addHeader("Content-Disposition",
                "attachment;filename=" + HttpUtils.encodeUtf8(TEMPLATE_FILE_NAME));
        try (InputStream inputStream = templateResource.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询堤防详情")
    public CommonResult<EmbankmentSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(embankmentService.getEmbankmentDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增堤防")
    public CommonResult<Long> create(@Valid @RequestBody EmbankmentSaveReqVO reqVO) {
        // 新增堤防时，同时写入水利对象基础表与堤防表，保持数据一致性
        return success(embankmentService.createEmbankment(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑堤防")
    public CommonResult<Boolean> update(@Valid @RequestBody EmbankmentSaveReqVO reqVO) {
        embankmentService.updateEmbankment(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除堤防")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        embankmentService.deleteEmbankment(id);
        return success(true);
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传 Excel 并导入堤防数据")
    public CommonResult<EmbankmentImportRespVO> importExcel(@RequestPart("file") MultipartFile file) {
        return success(embankmentExcelImportService.importExcel(file));
    }

    @PostMapping(value = "/import-gis-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传堤防 GIS Excel 并按堤防名称更新 GIS 与经纬度")
    public CommonResult<EmbankmentGisImportRespVO> importGisExcel(@RequestPart("file") MultipartFile file) {
        return success(embankmentGisExcelImportService.importExcel(file));
    }

    private Resource resolveTemplateResource() throws IOException {
        Resource resource = new ClassPathResource(TEMPLATE_RESOURCE_PATH);
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new IOException("未找到堤防导入模板文件：" + TEMPLATE_RESOURCE_PATH);
    }
}

