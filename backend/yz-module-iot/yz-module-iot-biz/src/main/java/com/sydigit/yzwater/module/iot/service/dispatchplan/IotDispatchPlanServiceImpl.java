package com.sydigit.yzwater.module.iot.service.dispatchplan;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanObjectRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanObjectSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanParamRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanParamSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanSaveReqVO;
import com.sydigit.yzwater.module.iot.convert.dispatchplan.IotDispatchPlanConvert;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceLocationDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanObjectDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanObjectParamDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceLocationMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage.IotDispatchInstructionMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan.IotDispatchPlanMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan.IotDispatchPlanObjectMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan.IotDispatchPlanObjectParamMapper;
import com.sydigit.yzwater.module.iot.enums.dispatchplan.IotDispatchPlanObjectTypeEnum;
import com.sydigit.yzwater.module.iot.enums.dispatchplan.IotDispatchPlanStatusEnum;
import com.sydigit.yzwater.module.iot.enums.dispatchplan.IotDispatchPlanTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_ARCHIVE_STATUS_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_NAME_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_OBJECT_CUSTOM_NAME_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_OBJECT_DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_OBJECT_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_OBJECT_LOCATION_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_OBJECT_PARAM_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_OBJECT_TYPE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_PREPARE_USER_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_STATUS_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_TYPE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_PLAN_USED_LOCKED;

/**
 * 调度方案 Service 实现
 */
@Service
@Validated
public class IotDispatchPlanServiceImpl implements IotDispatchPlanService {

    @Resource
    private IotDispatchPlanMapper planMapper;
    @Resource
    private IotDispatchPlanObjectMapper objectMapper;
    @Resource
    private IotDispatchPlanObjectParamMapper paramMapper;
    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private IotDeviceLocationMapper locationMapper;
    @Resource
    private IotDispatchInstructionMapper instructionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(IotDispatchPlanSaveReqVO createReqVO) {
        String planName = trimToNull(createReqVO.getPlanName());
        validatePlanNameUnique(null, planName);
        validatePlanType(createReqVO.getPlanType());
        List<PlanObjectPayload> objectPayloads = normalizeObjectPayloads(createReqVO.getObjects());

        IotDispatchPlanDO plan = IotDispatchPlanConvert.INSTANCE.convert(createReqVO);
        plan.setPlanName(planName);
        plan.setPlanNo(generatePlanNo());
        fillPrepareFieldsForCreate(plan);
        plan.setPlanType(createReqVO.getPlanType().trim());
        plan.setStationId(trimToEmpty(createReqVO.getStationId()));
        plan.setPlanStatus(normalizePlanStatus(createReqVO.getPlanStatus()));
        plan.setCoreTarget(trimToEmpty(createReqVO.getCoreTarget()));
        plan.setProjectName(trimToEmpty(createReqVO.getProjectName()));
        plan.setExpectedEffect(trimToEmpty(createReqVO.getExpectedEffect()));
        plan.setPrepareDesc(trimToEmpty(createReqVO.getPrepareDesc()));
        plan.setRemark(trimToEmpty(createReqVO.getRemark()));
        plan.setAttachments(normalizeAttachments(createReqVO.getAttachments()));
        plan.setObjectCount(objectPayloads.size());
        planMapper.insert(plan);

        saveObjectsAndParams(plan.getId(), objectPayloads);
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(IotDispatchPlanSaveReqVO updateReqVO) {
        IotDispatchPlanDO existed = validatePlanExists(updateReqVO.getId());
        validatePlanNotUsed(existed.getId());
        String planName = trimToNull(updateReqVO.getPlanName());
        validatePlanNameUnique(existed.getId(), planName);
        validatePlanType(updateReqVO.getPlanType());
        List<PlanObjectPayload> objectPayloads = normalizeObjectPayloads(updateReqVO.getObjects());

        IotDispatchPlanDO updateObj = IotDispatchPlanConvert.INSTANCE.convert(updateReqVO);
        updateObj.setId(existed.getId());
        updateObj.setPlanName(planName);
        updateObj.setPlanType(updateReqVO.getPlanType().trim());
        updateObj.setStationId(trimToEmpty(updateReqVO.getStationId()));
        updateObj.setPrepareUserId(updateReqVO.getPrepareUserId() != null
                ? updateReqVO.getPrepareUserId() : existed.getPrepareUserId());
        updateObj.setPrepareUserName(resolvePrepareUserName(updateReqVO.getPrepareUserName(),
                existed.getPrepareUserName()));
        updateObj.setPrepareOrgName(trimToEmpty(updateReqVO.getPrepareOrgName()));
        updateObj.setPrepareTime(updateReqVO.getPrepareTime() == null ? existed.getPrepareTime() : updateReqVO.getPrepareTime());
        updateObj.setPlanStatus(updateReqVO.getPlanStatus() == null
                ? existed.getPlanStatus() : normalizePlanStatus(updateReqVO.getPlanStatus()));
        updateObj.setCoreTarget(trimToEmpty(updateReqVO.getCoreTarget()));
        updateObj.setProjectName(trimToEmpty(updateReqVO.getProjectName()));
        updateObj.setExpectedEffect(trimToEmpty(updateReqVO.getExpectedEffect()));
        updateObj.setPrepareDesc(trimToEmpty(updateReqVO.getPrepareDesc()));
        updateObj.setRemark(trimToEmpty(updateReqVO.getRemark()));
        updateObj.setAttachments(updateReqVO.getAttachments() == null
                ? existed.getAttachments()
                : normalizeAttachments(updateReqVO.getAttachments()));
        updateObj.setObjectCount(objectPayloads.size());
        planMapper.updateById(updateObj);

        paramMapper.deleteByPlanId(existed.getId());
        objectMapper.deleteByPlanId(existed.getId());
        saveObjectsAndParams(existed.getId(), objectPayloads);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        validatePlanExists(id);
        validatePlanNotUsed(id);
        paramMapper.deleteByPlanId(id);
        objectMapper.deleteByPlanId(id);
        planMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archivePlan(Long id) {
        IotDispatchPlanDO existed = validatePlanExists(id);
        if (Objects.equals(existed.getPlanStatus(), IotDispatchPlanStatusEnum.ARCHIVED.getStatus())) {
            return;
        }
        if (Objects.equals(existed.getPlanStatus(), IotDispatchPlanStatusEnum.VOIDED.getStatus())) {
            throw exception(DISPATCH_PLAN_ARCHIVE_STATUS_INVALID);
        }
        IotDispatchPlanDO updateObj = new IotDispatchPlanDO();
        updateObj.setId(id);
        updateObj.setPlanStatus(IotDispatchPlanStatusEnum.ARCHIVED.getStatus());
        planMapper.updateById(updateObj);
    }

    @Override
    public IotDispatchPlanRespVO getPlan(Long id) {
        IotDispatchPlanDO plan = validatePlanExists(id);
        List<IotDispatchPlanObjectDO> objectList = objectMapper.selectListByPlanId(id);
        List<IotDispatchPlanObjectParamDO> paramList = paramMapper.selectListByPlanId(id);
        return assembleResp(plan, objectList, paramList, instructionMapper.selectCountByPlanId(id));
    }

    @Override
    public PageResult<IotDispatchPlanRespVO> getPlanPage(IotDispatchPlanPageReqVO pageReqVO) {
        PageResult<IotDispatchPlanDO> pageResult = planMapper.selectPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        List<Long> planIds = CollectionUtils.convertList(pageResult.getList(), IotDispatchPlanDO::getId);
        List<IotDispatchPlanObjectDO> objects = objectMapper.selectListByPlanIds(planIds);
        List<IotDispatchPlanObjectParamDO> params = paramMapper.selectListByPlanIds(planIds);
        Map<Long, List<IotDispatchPlanObjectDO>> objectMap = objects.stream()
                .collect(Collectors.groupingBy(IotDispatchPlanObjectDO::getPlanId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<IotDispatchPlanObjectParamDO>> paramMap = params.stream()
                .collect(Collectors.groupingBy(IotDispatchPlanObjectParamDO::getPlanId, LinkedHashMap::new, Collectors.toList()));

        List<IotDispatchPlanRespVO> list = new ArrayList<>(pageResult.getList().size());
        for (IotDispatchPlanDO plan : pageResult.getList()) {
            list.add(assembleResp(plan,
                    objectMap.getOrDefault(plan.getId(), Collections.emptyList()),
                    paramMap.getOrDefault(plan.getId(), Collections.emptyList()),
                    instructionMapper.selectCountByPlanId(plan.getId())));
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<IotDispatchPlanRespVO> getPlanList(IotDispatchPlanPageReqVO reqVO) {
        List<IotDispatchPlanDO> planList = planMapper.selectListByReqVO(reqVO);
        if (planList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> planIds = CollectionUtils.convertList(planList, IotDispatchPlanDO::getId);
        List<IotDispatchPlanObjectDO> objects = objectMapper.selectListByPlanIds(planIds);
        List<IotDispatchPlanObjectParamDO> params = paramMapper.selectListByPlanIds(planIds);
        Map<Long, List<IotDispatchPlanObjectDO>> objectMap = objects.stream()
                .collect(Collectors.groupingBy(IotDispatchPlanObjectDO::getPlanId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<IotDispatchPlanObjectParamDO>> paramMap = params.stream()
                .collect(Collectors.groupingBy(IotDispatchPlanObjectParamDO::getPlanId, LinkedHashMap::new, Collectors.toList()));

        List<IotDispatchPlanRespVO> list = new ArrayList<>(planList.size());
        for (IotDispatchPlanDO plan : planList) {
            list.add(assembleResp(plan,
                    objectMap.getOrDefault(plan.getId(), Collections.emptyList()),
                    paramMap.getOrDefault(plan.getId(), Collections.emptyList()),
                    instructionMapper.selectCountByPlanId(plan.getId())));
        }
        return list;
    }

    /**
     * 校验方案存在
     */
    private IotDispatchPlanDO validatePlanExists(Long id) {
        IotDispatchPlanDO plan = planMapper.selectById(id);
        if (plan == null) {
            throw exception(DISPATCH_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    /**
     * 校验方案是否已被使用
     */
    private void validatePlanNotUsed(Long planId) {
        if (planId == null) {
            return;
        }
        if (instructionMapper.selectCountByPlanId(planId) > 0) {
            throw exception(DISPATCH_PLAN_USED_LOCKED);
        }
    }

    /**
     * 校验方案名称唯一
     */
    private void validatePlanNameUnique(Long id, String planName) {
        IotDispatchPlanDO existed = planMapper.selectByPlanName(planName);
        if (existed == null) {
            return;
        }
        if (!Objects.equals(existed.getId(), id)) {
            throw exception(DISPATCH_PLAN_NAME_EXISTS);
        }
    }

    /**
     * 校验方案类型
     */
    private void validatePlanType(String planType) {
        if (!IotDispatchPlanTypeEnum.isValid(planType)) {
            throw exception(DISPATCH_PLAN_TYPE_INVALID);
        }
    }

    /**
     * 填充创建时编制信息
     */
    private void fillPrepareFieldsForCreate(IotDispatchPlanDO plan) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (plan.getPrepareUserId() == null) {
            plan.setPrepareUserId(loginUserId);
        }
        plan.setPrepareUserName(resolvePrepareUserName(plan.getPrepareUserName(), null));
        if (plan.getPrepareTime() == null) {
            plan.setPrepareTime(LocalDateTime.now());
        }
    }

    /**
     * 解析编制人姓名
     */
    private String resolvePrepareUserName(String reqUserName, String fallback) {
        String userName = trimToNull(reqUserName);
        if (userName != null) {
            return userName;
        }
        String loginNickname = trimToNull(SecurityFrameworkUtils.getLoginUserNickname());
        if (loginNickname != null) {
            return loginNickname;
        }
        String fallbackValue = trimToNull(fallback);
        if (fallbackValue != null) {
            return fallbackValue;
        }
        throw exception(DISPATCH_PLAN_PREPARE_USER_EMPTY);
    }

    /**
     * 规范化方案状态
     */
    private Integer normalizePlanStatus(Integer status) {
        Integer normalized = status == null ? IotDispatchPlanStatusEnum.DRAFT.getStatus() : status;
        if (!IotDispatchPlanStatusEnum.isValid(normalized)) {
            throw exception(DISPATCH_PLAN_STATUS_INVALID);
        }
        return normalized;
    }

    /**
     * 规范化附件列表
     */
    private String[] normalizeAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new String[0];
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String attachment : attachments) {
            String value = trimToNull(attachment);
            if (value != null) {
                values.add(value);
            }
        }
        if (values.isEmpty()) {
            return new String[0];
        }
        return values.toArray(String[]::new);
    }

    /**
     * 规范化并校验对象载荷
     */
    private List<PlanObjectPayload> normalizeObjectPayloads(List<IotDispatchPlanObjectSaveReqVO> objects) {
        if (objects == null || objects.isEmpty()) {
            throw exception(DISPATCH_PLAN_OBJECT_EMPTY);
        }
        List<PlanObjectPayload> payloads = new ArrayList<>();
        Set<Long> deviceIds = new LinkedHashSet<>();
        Set<Long> locationIds = new LinkedHashSet<>();

        for (IotDispatchPlanObjectSaveReqVO object : objects) {
            if (object == null) {
                continue;
            }
            Integer objectType = object.getObjectType();
            if (!IotDispatchPlanObjectTypeEnum.isValid(objectType)) {
                throw exception(DISPATCH_PLAN_OBJECT_TYPE_INVALID);
            }
            List<PlanParamPayload> params = normalizeParamPayloads(object.getParams());
            PlanObjectPayload payload = new PlanObjectPayload();
            payload.objectType = objectType;
            payload.objectName = trimToNull(object.getObjectName());
            payload.deviceId = object.getDeviceId();
            payload.locationId = object.getLocationId();
            payload.remark = trimToEmpty(object.getRemark());
            payload.params = params;
            payloads.add(payload);

            if (Objects.equals(objectType, IotDispatchPlanObjectTypeEnum.DEVICE.getType())) {
                if (payload.deviceId == null) {
                    throw exception(DISPATCH_PLAN_OBJECT_DEVICE_NOT_EXISTS);
                }
                deviceIds.add(payload.deviceId);
                if (payload.locationId != null) {
                    locationIds.add(payload.locationId);
                }
            }
        }
        if (payloads.isEmpty()) {
            throw exception(DISPATCH_PLAN_OBJECT_EMPTY);
        }

        Map<Long, IotDeviceDO> deviceMap = queryDeviceMap(deviceIds);
        Map<Long, IotDeviceLocationDO> locationMap = queryLocationMap(locationIds);

        for (PlanObjectPayload payload : payloads) {
            if (Objects.equals(payload.objectType, IotDispatchPlanObjectTypeEnum.DEVICE.getType())) {
                IotDeviceDO device = deviceMap.get(payload.deviceId);
                if (device == null) {
                    throw exception(DISPATCH_PLAN_OBJECT_DEVICE_NOT_EXISTS);
                }
                if (payload.locationId != null && !locationMap.containsKey(payload.locationId)) {
                    throw exception(DISPATCH_PLAN_OBJECT_LOCATION_NOT_EXISTS);
                }
                if (StrUtil.isBlank(payload.objectName)) {
                    payload.objectName = StrUtil.blankToDefault(device.getNickname(),
                            StrUtil.blankToDefault(device.getDeviceName(), "设备-" + device.getId()));
                }
            } else {
                if (StrUtil.isBlank(payload.objectName)) {
                    throw exception(DISPATCH_PLAN_OBJECT_CUSTOM_NAME_EMPTY);
                }
                payload.deviceId = null;
                payload.locationId = null;
            }
        }
        return payloads;
    }

    /**
     * 规范化参数载荷
     */
    private List<PlanParamPayload> normalizeParamPayloads(List<IotDispatchPlanParamSaveReqVO> params) {
        if (params == null || params.isEmpty()) {
            throw exception(DISPATCH_PLAN_OBJECT_PARAM_EMPTY);
        }
        List<PlanParamPayload> payloads = new ArrayList<>();
        int sort = 1;
        for (IotDispatchPlanParamSaveReqVO param : params) {
            if (param == null) {
                continue;
            }
            String paramName = trimToNull(param.getParamName());
            String paramValue = trimToNull(param.getParamValue());
            if (paramName == null || paramValue == null) {
                continue;
            }
            PlanParamPayload payload = new PlanParamPayload();
            payload.paramSort = sort++;
            payload.paramName = paramName;
            payload.paramValue = paramValue;
            payload.paramUnit = trimToEmpty(param.getParamUnit());
            payload.remark = trimToEmpty(param.getRemark());
            payloads.add(payload);
        }
        if (payloads.isEmpty()) {
            throw exception(DISPATCH_PLAN_OBJECT_PARAM_EMPTY);
        }
        return payloads;
    }

    /**
     * 查询设备映射
     */
    private Map<Long, IotDeviceDO> queryDeviceMap(Collection<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IotDeviceDO> devices = deviceMapper.selectBatchIds(deviceIds);
        if (devices == null || devices.isEmpty()) {
            return Collections.emptyMap();
        }
        return devices.stream().collect(Collectors.toMap(IotDeviceDO::getId, item -> item, (a, b) -> a));
    }

    /**
     * 查询位置映射
     */
    private Map<Long, IotDeviceLocationDO> queryLocationMap(Collection<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IotDeviceLocationDO> locations = locationMapper.selectBatchIds(locationIds);
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyMap();
        }
        return locations.stream().collect(Collectors.toMap(IotDeviceLocationDO::getId, item -> item, (a, b) -> a));
    }

    /**
     * 保存对象与参数
     */
    private void saveObjectsAndParams(Long planId, List<PlanObjectPayload> payloads) {
        for (int i = 0; i < payloads.size(); i++) {
            PlanObjectPayload payload = payloads.get(i);
            IotDispatchPlanObjectDO object = new IotDispatchPlanObjectDO();
            object.setPlanId(planId);
            object.setObjectSort(i + 1);
            object.setObjectType(payload.objectType);
            object.setObjectName(payload.objectName);
            object.setDeviceId(payload.deviceId);
            object.setLocationId(payload.locationId);
            object.setRemark(payload.remark);
            objectMapper.insert(object);

            for (PlanParamPayload paramPayload : payload.params) {
                IotDispatchPlanObjectParamDO param = new IotDispatchPlanObjectParamDO();
                param.setPlanId(planId);
                param.setObjectId(object.getId());
                param.setParamSort(paramPayload.paramSort);
                param.setParamName(paramPayload.paramName);
                param.setParamValue(paramPayload.paramValue);
                param.setParamUnit(paramPayload.paramUnit);
                param.setRemark(paramPayload.remark);
                paramMapper.insert(param);
            }
        }
    }

    /**
     * 组装响应对象
     */
    private IotDispatchPlanRespVO assembleResp(IotDispatchPlanDO plan,
                                               List<IotDispatchPlanObjectDO> objectList,
                                               List<IotDispatchPlanObjectParamDO> paramList,
                                               Long usedCount) {
        IotDispatchPlanRespVO respVO = IotDispatchPlanConvert.INSTANCE.convert(plan);
        respVO.setPlanTypeName(IotDispatchPlanTypeEnum.getNameByType(plan.getPlanType()));
        respVO.setPlanStatusName(IotDispatchPlanStatusEnum.getNameByStatus(plan.getPlanStatus()));
        respVO.setAttachments(plan.getAttachments() == null
                ? Collections.emptyList()
                : Arrays.asList(plan.getAttachments()));

        Map<Long, List<IotDispatchPlanObjectParamDO>> paramMap = paramList.stream()
                .collect(Collectors.groupingBy(IotDispatchPlanObjectParamDO::getObjectId, LinkedHashMap::new, Collectors.toList()));
        List<IotDispatchPlanObjectRespVO> objectRespList = new ArrayList<>(objectList.size());
        List<String> objectNames = new ArrayList<>(objectList.size());
        for (IotDispatchPlanObjectDO object : objectList) {
            IotDispatchPlanObjectRespVO objectResp = IotDispatchPlanConvert.INSTANCE.convert(object);
            List<IotDispatchPlanParamRespVO> params = CollectionUtils.convertList(
                    paramMap.getOrDefault(object.getId(), Collections.emptyList()),
                    IotDispatchPlanConvert.INSTANCE::convert);
            objectResp.setParams(params);
            objectRespList.add(objectResp);
            objectNames.add(object.getObjectName());
        }
        respVO.setObjects(objectRespList);
        respVO.setObjectNames(objectNames);
        respVO.setObjectCount(respVO.getObjectCount() == null ? objectRespList.size() : respVO.getObjectCount());
        respVO.setUsedCount(usedCount == null ? 0L : usedCount);
        return respVO;
    }

    /**
     * 生成调度方案编号（系统唯一标识，不使用固定业务格式）
     */
    private String generatePlanNo() {
        return IdUtil.fastSimpleUUID().toUpperCase();
    }

    private String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimToEmpty(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized;
    }

    /**
     * 对象载荷
     */
    private static class PlanObjectPayload {
        private Integer objectType;
        private String objectName;
        private Long deviceId;
        private Long locationId;
        private String remark;
        private List<PlanParamPayload> params;
    }

    /**
     * 参数载荷
     */
    private static class PlanParamPayload {
        private Integer paramSort;
        private String paramName;
        private String paramValue;
        private String paramUnit;
        private String remark;
    }
}
