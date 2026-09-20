package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityImportRespVO;
import com.sydigit.yzwater.module.service.geoBase.WaterFacilityImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 水利对象导入接口
 */
@Tag(name = "仪征管理后台 - 水利对象导入")
@RestController
@RequestMapping("/water")
@Validated
public class WaterFacilityController {

    private final WaterFacilityImportService waterFacilityImportService;

    public WaterFacilityController(WaterFacilityImportService waterFacilityImportService) {
        this.waterFacilityImportService = waterFacilityImportService;
    }

    @PostMapping("/import")
    @Operation(summary = "上传水利 GeoJSON ZIP 并导入 PostGIS（Swagger 可直接上传）")
    @PermitAll
    public CommonResult<WaterFacilityImportRespVO> importWaterGeoJson(@RequestPart("file") MultipartFile file) {
        return success(waterFacilityImportService.importZip(file));
    }
}
