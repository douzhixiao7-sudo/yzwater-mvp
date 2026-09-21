package com.sydigit.yzwater.module.iot.controller.admin.product;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.MapUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.imports.IotProductImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.product.IotProductRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductCategoryDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.service.product.IotProductCategoryService;
import com.sydigit.yzwater.module.iot.service.product.IotProductImportService;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 产品")
@RestController
@RequestMapping("/iot/product")
@Validated
public class IotProductController {

    @Resource
    private IotProductService productService;
    @Resource
    private IotProductCategoryService categoryService;
    @Resource
    private IotProductImportService productImportService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('iot:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody IotProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody IotProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新产品状态")
    @Parameter(name = "id", description = "编号", required = true)
    @Parameter(name = "status", description = "状态", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> updateProductStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        productService.updateProductStatus(id, status);
        return success(true);
    }

    @PutMapping("/publish-all")
    @Operation(summary = "批量发布未发布的产品")
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Integer> publishAllUnpublishedProducts() {
        return success(productService.publishAllUnpublished());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<IotProductRespVO> getProduct(@RequestParam("id") Long id) {
        IotProductDO product = productService.getProduct(id);
        if (product == null) {
            return success(null);
        }
        // 拼接数据
        IotProductCategoryDO category = categoryService.getProductCategory(product.getCategoryId());
        return success(BeanUtils.toBean(product, IotProductRespVO.class, bean -> {
            if (category != null) {
                bean.setCategoryName(category.getName());
            }
        }));
    }

    @GetMapping("/get-by-key")
    @Operation(summary = "通过 ProductKey 获得产品")
    @Parameter(name = "productKey", description = "产品Key", required = true, example = "abc123")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<IotProductRespVO> getProductByKey(@RequestParam("productKey") String productKey) {
        IotProductDO product = productService.getProductByProductKey(productKey);
        if (product == null) {
            return success(null);
        }
        // 拼接数据
        IotProductCategoryDO category = categoryService.getProductCategory(product.getCategoryId());
        return success(BeanUtils.toBean(product, IotProductRespVO.class, bean -> {
            if (category != null) {
                bean.setCategoryName(category.getName());
            }
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<PageResult<IotProductRespVO>> getProductPage(@Valid IotProductPageReqVO pageReqVO) {
        PageResult<IotProductDO> pageResult = productService.getProductPage(pageReqVO);
        // 拼接数据
        Map<Long, IotProductCategoryDO> categoryMap = categoryService.getProductCategoryMap(
                convertList(pageResult.getList(), IotProductDO::getCategoryId));
        return success(BeanUtils.toBean(pageResult, IotProductRespVO.class, bean -> {
            MapUtils.findAndThen(categoryMap, bean.getCategoryId(),
                    category -> bean.setCategoryName(category.getName()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('iot:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid IotProductPageReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        CommonResult<PageResult<IotProductRespVO>> result = getProductPage(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "产品.xls", "数据", IotProductRespVO.class,
                result.getData().getList());
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "导入产品 Excel")
    @PreAuthorize("@ss.hasPermission('iot:product:create')")
    public CommonResult<IotProductImportRespVO> importProductExcel(
            @Parameter(name = "file", description = "Excel 文件", required = true)
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "namePrefix", required = false) String namePrefix) throws IOException {
        return success(productImportService.importExcel(file, namePrefix));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取产品的精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<IotProductRespVO>> getProductSimpleList() {
        List<IotProductDO> list = productService.getProductList();
        Map<Long, IotProductCategoryDO> categoryMap = categoryService.getProductCategoryMap(
                convertList(list, IotProductDO::getCategoryId));
        return success(convertList(list, product -> {
            IotProductRespVO respVO = new IotProductRespVO()
                    .setId(product.getId())
                    .setName(product.getName())
                    .setStatus(product.getStatus())
                    .setDeviceType(product.getDeviceType())
                    .setLocationType(product.getLocationType())
                    .setCategoryId(product.getCategoryId());
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> respVO.setCategoryName(category.getName()));
            return respVO;
        }));
    }

}
