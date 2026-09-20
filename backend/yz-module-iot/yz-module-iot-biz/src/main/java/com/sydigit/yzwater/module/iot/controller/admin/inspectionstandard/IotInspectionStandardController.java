package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionCheckResultConfigVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardItemRecordRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardItemRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardTargetRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionTargetOptionRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemRecordDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardTargetDO;
import com.sydigit.yzwater.module.iot.service.inspectionstandard.IotInspectionCheckResultConfigHelper;
import com.sydigit.yzwater.module.iot.service.inspectionstandard.IotInspectionStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "IoT - 巡检标准")
@RestController
@RequestMapping("/iot/inspection-standard")
@Validated
public class IotInspectionStandardController {

    @Resource
    private IotInspectionStandardService standardService;

    /**
     * 创建巡检标准。
     *
     * @param createReqVO 创建请求
     * @return 标准 ID
     */
    @PostMapping("/create")
    @Operation(summary = "创建巡检标准")
    @PreAuthorize("@ss.hasPermission('iot:inspection-standard:create')")
    public CommonResult<Long> createStandard(@Valid @RequestBody IotInspectionStandardSaveReqVO createReqVO) {
        return success(standardService.createStandard(createReqVO));
    }

    /**
     * 更新巡检标准。
     *
     * @param updateReqVO 更新请求
     * @return 是否成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新巡检标准")
    @PreAuthorize("@ss.hasPermission('iot:inspection-standard:update')")
    public CommonResult<Boolean> updateStandard(@Valid @RequestBody IotInspectionStandardSaveReqVO updateReqVO) {
        standardService.updateStandard(updateReqVO);
        return success(true);
    }

    /**
     * 删除巡检标准。
     *
     * @param id 标准 ID
     * @return 是否成功
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除巡检标准")
    @Parameter(name = "id", description = "标准 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-standard:delete')")
    public CommonResult<Boolean> deleteStandard(@RequestParam("id") Long id) {
        standardService.deleteStandard(id);
        return success(true);
    }

    /**
     * 获取巡检标准详情。
     *
     * @param id 标准 ID
     * @return 标准详情
     */
    @GetMapping("/get")
    @Operation(summary = "获取巡检标准详情")
    @Parameter(name = "id", description = "标准 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-standard:query')")
    public CommonResult<IotInspectionStandardRespVO> getStandard(@RequestParam("id") Long id) {
        IotInspectionStandardDO standard = standardService.getStandard(id);
        return success(convertResp(standard));
    }

    /**
     * 分页查询巡检标准。
     *
     * @param pageReqVO 分页查询参数
     * @return 分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询巡检标准")
    @PreAuthorize("@ss.hasPermission('iot:inspection-standard:query')")
    public CommonResult<PageResult<IotInspectionStandardRespVO>> getStandardPage(@Valid IotInspectionStandardPageReqVO pageReqVO) {
        PageResult<IotInspectionStandardDO> pageResult = standardService.getStandardPage(pageReqVO);
        List<IotInspectionStandardRespVO> list = CollectionUtils.convertList(pageResult.getList(), this::convertSimpleResp);
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    /**
     * 查询适用对象下拉选项。
     */
    @GetMapping("/target-options")
    @Operation(summary = "查询巡检标准适用对象下拉")
    @PreAuthorize("@ss.hasPermission('iot:inspection-standard:query')")
    public CommonResult<List<IotInspectionTargetOptionRespVO>> getTargetOptions(
            @RequestParam("targetType") String targetType,
            @RequestParam(value = "stationId", required = false) String stationId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return success(standardService.listTargetOptions(targetType, stationId, keyword, limit));
    }

    /**
     * 转换标准详情响应，并组装适用对象、检查项与记录模板。
     */
    private IotInspectionStandardRespVO convertResp(IotInspectionStandardDO standard) {
        if (standard == null) {
            return null;
        }
        IotInspectionStandardRespVO respVO = BeanUtils.toBean(standard, IotInspectionStandardRespVO.class);
        respVO.setRemark(IotInspectionCheckResultConfigHelper.extractRemark(standard.getRemark()));
        List<IotInspectionStandardTargetDO> targets = standardService.getStandardTargetList(standard.getId());
        respVO.setStationId(resolveStationId(targets));
        if (targets.isEmpty()) {
            respVO.setTargetTypes(Collections.emptyList());
            respVO.setTargetNames(Collections.emptyList());
            respVO.setTargets(Collections.emptyList());
            return respVO;
        }
        respVO.setTargetTypes(extractTargetTypes(targets));
        respVO.setTargetNames(extractTargetNames(targets));
        if (respVO.getTargetCount() == null) {
            respVO.setTargetCount(targets.size());
        }

        List<Long> targetIds = CollectionUtils.convertList(targets, IotInspectionStandardTargetDO::getId);
        List<IotInspectionStandardItemDO> items = standardService.getStandardItemListByTargetIds(targetIds);
        List<Long> itemIds = CollectionUtils.convertList(items, IotInspectionStandardItemDO::getId);
        List<IotInspectionStandardItemRecordDO> records = standardService.getStandardItemRecordList(itemIds);
        List<IotInspectionCheckResultConfigVO> legacyConfigs =
                IotInspectionCheckResultConfigHelper.extractLegacyConfigsFromRemark(standard.getRemark());

        respVO.setTargets(buildTargetRespList(targets, items, records, legacyConfigs));
        return respVO;
    }

    /**
     * 转换分页场景响应，默认仅返回对象概要，不加载检查项详情。
     */
    private IotInspectionStandardRespVO convertSimpleResp(IotInspectionStandardDO standard) {
        IotInspectionStandardRespVO respVO = BeanUtils.toBean(standard, IotInspectionStandardRespVO.class);
        respVO.setRemark(IotInspectionCheckResultConfigHelper.extractRemark(standard.getRemark()));
        List<IotInspectionStandardTargetDO> targets = standardService.getStandardTargetList(standard.getId());
        respVO.setStationId(resolveStationId(targets));
        respVO.setTargetTypes(extractTargetTypes(targets));
        respVO.setTargetNames(extractTargetNames(targets));
        if (respVO.getTargetCount() == null) {
            respVO.setTargetCount(targets.size());
        }
        respVO.setTargets(Collections.emptyList());
        return respVO;
    }

    /**
     * 解析所属站点，默认取第一个对象的站点编码。
     */
    private String resolveStationId(List<IotInspectionStandardTargetDO> targets) {
        if (targets == null || targets.isEmpty()) {
            return null;
        }
        for (IotInspectionStandardTargetDO target : targets) {
            if (target != null && target.getStationId() != null && !target.getStationId().isBlank()) {
                return target.getStationId();
            }
        }
        return null;
    }

    /**
     * 提取适用对象类型列表，去重并保持顺序。
     */
    private List<String> extractTargetTypes(List<IotInspectionStandardTargetDO> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(targets.stream()
                .map(IotInspectionStandardTargetDO::getTargetType)
                .filter(type -> type != null && !type.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * 提取适用对象名称列表，去重并保持顺序。
     */
    private List<String> extractTargetNames(List<IotInspectionStandardTargetDO> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(targets.stream()
                .map(IotInspectionStandardTargetDO::getTargetName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * 组装适用对象层级响应。
     */
    private List<IotInspectionStandardTargetRespVO> buildTargetRespList(List<IotInspectionStandardTargetDO> targets,
                                                                         List<IotInspectionStandardItemDO> items,
                                                                         List<IotInspectionStandardItemRecordDO> records,
                                                                         List<IotInspectionCheckResultConfigVO> legacyConfigs) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<IotInspectionStandardItemDO>> targetItemMap = (items == null
                ? Collections.<IotInspectionStandardItemDO>emptyList()
                : items).stream().collect(Collectors.groupingBy(IotInspectionStandardItemDO::getTargetRefId));
        Map<Long, List<IotInspectionStandardItemRecordDO>> itemRecordMap = (records == null
                ? Collections.<IotInspectionStandardItemRecordDO>emptyList()
                : records).stream().collect(Collectors.groupingBy(IotInspectionStandardItemRecordDO::getStandardItemId));

        return CollectionUtils.convertList(targets, target -> {
            IotInspectionStandardTargetRespVO targetResp = BeanUtils.toBean(target, IotInspectionStandardTargetRespVO.class);
            List<IotInspectionCheckResultConfigVO> checkResultConfigs =
                    IotInspectionCheckResultConfigHelper.fromJson(target.getCheckResultConfigsJson());
            if (checkResultConfigs.isEmpty() && legacyConfigs != null && !legacyConfigs.isEmpty()) {
                checkResultConfigs = BeanUtils.toBean(legacyConfigs,
                        IotInspectionCheckResultConfigVO.class);
            }
            targetResp.setCheckResultConfigs(checkResultConfigs);
            List<IotInspectionStandardItemDO> targetItems = targetItemMap.getOrDefault(target.getId(), Collections.emptyList());
            List<IotInspectionStandardItemRespVO> itemRespList = CollectionUtils.convertList(targetItems, item -> {
                IotInspectionStandardItemRespVO itemResp = BeanUtils.toBean(item, IotInspectionStandardItemRespVO.class);
                List<IotInspectionStandardItemRecordDO> itemRecords = itemRecordMap.getOrDefault(item.getId(), Collections.emptyList());
                itemResp.setRecordTemplates(BeanUtils.toBean(itemRecords, IotInspectionStandardItemRecordRespVO.class));
                return itemResp;
            });
            targetResp.setItems(itemRespList);
            if (targetResp.getItemCount() == null) {
                targetResp.setItemCount(itemRespList.size());
            }
            return targetResp;
        });
    }

}
