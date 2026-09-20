package com.sydigit.yzwater.module.iot.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.sydigit.yzwater.framework.dict.core.DictFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.device.IotDeviceSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyTagSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.imports.IotProductImportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.imports.IotProductImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.thingmodel.vo.IotThingModelSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDevicePropertyTagDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelBoolOrEnumDataSpecs;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelNumericDataSpec;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDevicePropertyTagMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyTagService;
import com.sydigit.yzwater.module.iot.service.thingmodel.IotThingModelService;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.framework.tenant.core.util.TenantUtils;
import com.sydigit.yzwater.framework.datapermission.core.util.DataPermissionUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * IoT 产品 Excel 导入 Service
 */
@Service
@Validated
@Slf4j
public class IotProductImportService {

    private static final String DEFAULT_NAME_PREFIX = "金斗河-";
    private static final String DEFAULT_CODEC_TYPE = "Alink";
    private static final Long DEFAULT_CATEGORY_ID = 6L;
    private static final Integer DEFAULT_DEVICE_TYPE = 0;
    private static final Integer DEFAULT_LOCATION_TYPE = 2;
    private static final Integer DEFAULT_NET_TYPE = 1;
    private static final String DEFAULT_STATION_ID = "2";
    private static final String DEFAULT_NUMERIC_MIN = "-999999";
    private static final String DEFAULT_NUMERIC_MAX = "999999";
    private static final String DEFAULT_NUMERIC_STEP_INT = "1";
    private static final String DEFAULT_NUMERIC_STEP_FLOAT = "0.01";
    private static final Integer DEFAULT_TEXT_LENGTH = 255;

    @Resource
    private IotProductService productService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDevicePropertyTagService devicePropertyTagService;
    @Resource
    private IotDevicePropertyTagMapper devicePropertyTagMapper;
    @Resource
    private IotDeviceMapper deviceMapper;

    public IotProductImportRespVO importExcel(MultipartFile file, String namePrefix) throws IOException {
        List<IotProductImportExcelVO> rows = readExcel(file);
        IotProductImportRespVO respVO = new IotProductImportRespVO();
        respVO.setTotalCount(rows.size());

        List<String> createdProductNames = new ArrayList<>();
        List<String> createdDeviceNames = new ArrayList<>();
        List<String> createdThingModelIdentifiers = new ArrayList<>();
        Map<Integer, String> failureMessages = new LinkedHashMap<>();

        Map<String, ProductContext> productMap = new HashMap<>();
        Map<String, ProductContext> existingProductMap = buildExistingProductMap();
        Map<String, Long> deviceMap = new HashMap<>();

        int successCount = 0;
        for (int index = 0; index < rows.size(); index++) {
            int rowNumber = index + 1;
            IotProductImportExcelVO row = rows.get(index);
            if (row == null) {
                continue;
            }
            String productName = normalizeText(row.getProductName());
            String deviceName = normalizeText(row.getDeviceName());
            String tagName = normalizeText(row.getTagName());
            String thingModelName = normalizeText(row.getThingModelName());
            String identifier = normalizeText(row.getThingModelIdentifier());
            String dataType = normalizeDataType(row.getDataType());
            String unitLabel = normalizeText(row.getUnitName());

            if (isEmptyRow(productName, deviceName, tagName, thingModelName, identifier, dataType, unitLabel)) {
                continue;
            }
            if (isHeaderRow(productName, deviceName, thingModelName)) {
                continue;
            }
            if (StrUtil.isBlank(productName)
                    || StrUtil.isBlank(deviceName)
                    || StrUtil.isBlank(tagName)
                    || StrUtil.isBlank(thingModelName)
                    || StrUtil.isBlank(identifier)
                    || StrUtil.isBlank(dataType)) {
                failureMessages.put(rowNumber, "必填列存在空值");
                continue;
            }
            if (!isSupportedDataType(dataType)) {
                failureMessages.put(rowNumber, "数据类型不支持: " + dataType);
                continue;
            }

            try {
                ProductContext product = getOrCreateProduct(productName, namePrefix, productMap,
                        existingProductMap, createdProductNames);
                Long deviceId = getCachedDeviceId(product, deviceName, deviceMap);
                if (deviceId == null) {
                    IotDeviceDO existDevice = findDeviceByProductIdAndName(product.productId, deviceName);
                    if (existDevice == null) {
                        existDevice = deviceService.getDeviceFromCache(product.productKey, deviceName);
                    }
                    if (existDevice != null) {
                        deviceId = existDevice.getId();
                        deviceMap.put(buildDeviceKey(product, deviceName), deviceId);
                    }
                }
                boolean createdThingModel = ensureThingModel(product, thingModelName, identifier,
                        dataType, unitLabel, createdThingModelIdentifiers);
                if (deviceId == null) {
                    deviceId = createDevice(product, deviceName, deviceMap, createdDeviceNames);
                }
                saveTag(deviceId, identifier, tagName);
                if (createdThingModel || deviceId != null) {
                    successCount++;
                }
            } catch (Exception ex) {
                String message = StrUtil.blankToDefault(ex.getMessage(), "导入失败");
                failureMessages.put(rowNumber, message);
                log.warn("[importExcel][第 {} 行导入失败]", rowNumber, ex);
            }
        }

        respVO.setSuccessCount(successCount);
        respVO.setFailureCount(failureMessages.size());
        respVO.setCreatedProductNames(createdProductNames);
        respVO.setCreatedDeviceNames(createdDeviceNames);
        respVO.setCreatedThingModelIdentifiers(createdThingModelIdentifiers);
        respVO.setFailureMessages(failureMessages);
        return respVO;
    }

    private List<IotProductImportExcelVO> readExcel(MultipartFile file) throws IOException {
        return FastExcelFactory.read(file.getInputStream(), IotProductImportExcelVO.class, null)
                .autoCloseStream(false)
                .headRowNumber(0)
                .doReadAllSync();
    }

    private boolean isEmptyRow(String productName, String deviceName, String tagName,
                               String thingModelName, String identifier, String dataType, String unitLabel) {
        return StrUtil.isAllBlank(productName, deviceName, tagName, thingModelName, identifier, dataType, unitLabel);
    }

    private boolean isHeaderRow(String productName, String deviceName, String thingModelName) {
        return "产品名称".equals(productName)
                || "设备名称".equals(deviceName)
                || "功能名称".equals(thingModelName);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = StrUtil.trim(text);
        return StrUtil.removePrefix(trimmed, "\uFEFF");
    }

    private String normalizeDataType(String dataType) {
        String normalized = normalizeText(dataType);
        if (normalized == null) {
            return null;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if ("boolean".equals(lower)) {
            return IotDataSpecsDataTypeEnum.BOOL.getDataType();
        }
        if ("integer".equals(lower)) {
            return IotDataSpecsDataTypeEnum.INT.getDataType();
        }
        return lower;
    }

    private boolean isSupportedDataType(String dataType) {
        return IotDataSpecsDataTypeEnum.INT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.FLOAT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.DOUBLE.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.BOOL.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.TEXT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.DATE.getDataType().equals(dataType);
    }

    private Map<String, ProductContext> buildExistingProductMap() {
        Map<String, ProductContext> result = new HashMap<>();
        List<IotProductDO> products = productService.getProductList();
        if (CollUtil.isEmpty(products)) {
            return result;
        }
        for (IotProductDO product : products) {
            if (product == null || StrUtil.isBlank(product.getName()) || StrUtil.isBlank(product.getProductKey())) {
                continue;
            }
            result.putIfAbsent(product.getName(),
                    new ProductContext(product.getId(), product.getProductKey(), product.getName()));
        }
        return result;
    }

    private ProductContext getOrCreateProduct(String rawName, String namePrefix,
                                              Map<String, ProductContext> productMap,
                                              Map<String, ProductContext> existingProductMap,
                                              List<String> createdProductNames) {
        String prefix = StrUtil.blankToDefault(namePrefix, DEFAULT_NAME_PREFIX);
        String finalName = rawName.startsWith(prefix) ? rawName : prefix + rawName;
        ProductContext context = productMap.get(finalName);
        if (context != null) {
            return context;
        }
        ProductContext exist = existingProductMap.get(finalName);
        if (exist != null) {
            productMap.put(finalName, exist);
            return exist;
        }
        IotProductSaveReqVO reqVO = new IotProductSaveReqVO();
        reqVO.setName(finalName);
        reqVO.setProductKey(generateProductKey());
        reqVO.setCategoryId(DEFAULT_CATEGORY_ID);
        reqVO.setCodecType(DEFAULT_CODEC_TYPE);
        reqVO.setDeviceType(DEFAULT_DEVICE_TYPE);
        reqVO.setLocationType(DEFAULT_LOCATION_TYPE);
        reqVO.setNetType(DEFAULT_NET_TYPE);
        Long productId = productService.createProduct(reqVO);
        context = new ProductContext(productId, reqVO.getProductKey(), finalName);
        productMap.put(finalName, context);
        createdProductNames.add(finalName);
        return context;
    }

    private Long getCachedDeviceId(ProductContext product, String deviceName, Map<String, Long> deviceMap) {
        String deviceKey = buildDeviceKey(product, deviceName);
        return deviceMap.get(deviceKey);
    }

    private Long createDevice(ProductContext product, String deviceName,
                              Map<String, Long> deviceMap, List<String> createdDeviceNames) {
        String deviceKey = buildDeviceKey(product, deviceName);
        IotDeviceDO existDevice = findDeviceByProductIdAndName(product.productId, deviceName);
        if (existDevice != null) {
            deviceMap.put(deviceKey, existDevice.getId());
            return existDevice.getId();
        }
        IotDeviceSaveReqVO deviceReqVO = new IotDeviceSaveReqVO();
        deviceReqVO.setDeviceName(deviceName);
        deviceReqVO.setProductId(product.productId);
        deviceReqVO.setStationId(DEFAULT_STATION_ID);
        deviceReqVO.setLocationType(DEFAULT_LOCATION_TYPE);
        Long deviceId;
        try {
            deviceId = deviceService.createDevice(deviceReqVO);
        } catch (DuplicateKeyException ex) {
            IotDeviceDO conflictDevice = findDeviceByProductIdAndName(product.productId, deviceName);
            if (conflictDevice != null) {
                deviceMap.put(deviceKey, conflictDevice.getId());
                return conflictDevice.getId();
            }
            throw ex;
        }
        deviceMap.put(deviceKey, deviceId);
        createdDeviceNames.add(deviceName);
        return deviceId;
    }

    private String buildDeviceKey(ProductContext product, String deviceName) {
        return product.productKey + "::" + deviceName;
    }

    private IotDeviceDO findDeviceByProductIdAndName(Long productId, String deviceName) {
        IotDeviceDO device = deviceMapper.selectByProductIdAndDeviceName(productId, deviceName);
        if (device != null) {
            return device;
        }
        return DataPermissionUtils.executeIgnore(() ->
                TenantUtils.executeIgnore(() -> deviceMapper.selectByProductIdAndDeviceName(productId, deviceName)));
    }

    private boolean ensureThingModel(ProductContext product, String thingModelName, String identifier,
                                     String dataType, String unitLabel,
                                     List<String> createdThingModelIdentifiers) {
        loadIdentifiersIfNeeded(product);
        if (product.identifiers.contains(identifier)) {
            return false;
        }
        IotThingModelSaveReqVO reqVO = new IotThingModelSaveReqVO();
        reqVO.setProductId(product.productId);
        reqVO.setProductKey(product.productKey);
        reqVO.setIdentifier(identifier);
        reqVO.setName(thingModelName);
        reqVO.setType(IotThingModelTypeEnum.PROPERTY.getType());
        reqVO.setProperty(buildProperty(thingModelName, identifier, dataType, unitLabel));
        thingModelService.createThingModel(reqVO);
        product.identifiers.add(identifier);
        createdThingModelIdentifiers.add(identifier);
        return true;
    }

    private void loadIdentifiersIfNeeded(ProductContext product) {
        if (product.identifiersLoaded) {
            return;
        }
        List<IotThingModelDO> existModels = thingModelService.getThingModelListByProductId(product.productId);
        if (CollUtil.isNotEmpty(existModels)) {
            for (IotThingModelDO model : existModels) {
                if (model != null && StrUtil.isNotBlank(model.getIdentifier())) {
                    product.identifiers.add(model.getIdentifier());
                }
            }
        }
        product.identifiersLoaded = true;
    }

    private ThingModelProperty buildProperty(String name, String identifier, String dataType, String unitLabel) {
        ThingModelProperty property = new ThingModelProperty();
        property.setName(name);
        property.setIdentifier(identifier);
        property.setAccessMode(IotThingModelAccessModeEnum.READ_WRITE.getMode());
        property.setDataType(dataType);
        property.setRequired(Boolean.FALSE);
        if (IotDataSpecsDataTypeEnum.BOOL.getDataType().equals(dataType)) {
            property.setDataSpecsList(buildBoolDataSpecs());
            return property;
        }
        if (IotDataSpecsDataTypeEnum.INT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.FLOAT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.DOUBLE.getDataType().equals(dataType)) {
            property.setDataSpecs(buildNumericDataSpecs(dataType, unitLabel));
            return property;
        }
        if (IotDataSpecsDataTypeEnum.TEXT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.DATE.getDataType().equals(dataType)) {
            property.setDataSpecs(buildTextOrDateSpecs(dataType));
            return property;
        }
        throw new IllegalArgumentException("数据类型不支持: " + dataType);
    }

    private List<ThingModelDataSpecs> buildBoolDataSpecs() {
        List<ThingModelDataSpecs> specs = new ArrayList<>(2);
        ThingModelBoolOrEnumDataSpecs off = new ThingModelBoolOrEnumDataSpecs();
        off.setDataType(IotDataSpecsDataTypeEnum.BOOL.getDataType());
        off.setName("关");
        off.setValue(0);
        specs.add(off);
        ThingModelBoolOrEnumDataSpecs on = new ThingModelBoolOrEnumDataSpecs();
        on.setDataType(IotDataSpecsDataTypeEnum.BOOL.getDataType());
        on.setName("开");
        on.setValue(1);
        specs.add(on);
        return specs;
    }

    private ThingModelNumericDataSpec buildNumericDataSpecs(String dataType, String unitLabel) {
        ThingModelNumericDataSpec spec = new ThingModelNumericDataSpec();
        spec.setDataType(dataType);
        spec.setMin(DEFAULT_NUMERIC_MIN);
        spec.setMax(DEFAULT_NUMERIC_MAX);
        spec.setStep(resolveNumericStep(dataType));
        String unitValue = resolveUnitValue(unitLabel);
        if (unitValue != null) {
            spec.setUnit(unitValue);
            spec.setUnitName(unitLabel);
        }
        return spec;
    }

    private String resolveNumericStep(String dataType) {
        if (IotDataSpecsDataTypeEnum.FLOAT.getDataType().equals(dataType)
                || IotDataSpecsDataTypeEnum.DOUBLE.getDataType().equals(dataType)) {
            return DEFAULT_NUMERIC_STEP_FLOAT;
        }
        return DEFAULT_NUMERIC_STEP_INT;
    }

    private ThingModelDateOrTextDataSpecs buildTextOrDateSpecs(String dataType) {
        ThingModelDateOrTextDataSpecs spec = new ThingModelDateOrTextDataSpecs();
        spec.setDataType(dataType);
        if (IotDataSpecsDataTypeEnum.TEXT.getDataType().equals(dataType)) {
            spec.setLength(DEFAULT_TEXT_LENGTH);
        }
        return spec;
    }

    private String resolveUnitValue(String unitLabel) {
        if (StrUtil.isBlank(unitLabel)) {
            return null;
        }
        return DictFrameworkUtils.parseDictDataValue(DictTypeConstants.IOT_THING_MODEL_UNIT, unitLabel);
    }

    private void saveTag(Long deviceId, String identifier, String tagName) {
        if (deviceId == null || StrUtil.isBlank(identifier) || StrUtil.isBlank(tagName)) {
            return;
        }
        IotDevicePropertyTagDO existTag = devicePropertyTagMapper.selectOne(new LambdaQueryWrapperX<IotDevicePropertyTagDO>()
                .eq(IotDevicePropertyTagDO::getDeviceId, deviceId)
                .eq(IotDevicePropertyTagDO::getIdentifier, identifier));
        if (existTag != null) {
            if (!StrUtil.equals(existTag.getTagName(), tagName)) {
                IotDevicePropertyTagDO updateObj = new IotDevicePropertyTagDO();
                updateObj.setTagName(tagName);
                devicePropertyTagMapper.update(updateObj, new LambdaQueryWrapperX<IotDevicePropertyTagDO>()
                        .eq(IotDevicePropertyTagDO::getDeviceId, deviceId)
                        .eq(IotDevicePropertyTagDO::getIdentifier, identifier));
            }
            return;
        }
        IotDevicePropertyTagSaveReqVO reqVO = new IotDevicePropertyTagSaveReqVO();
        reqVO.setDeviceId(deviceId);
        IotDevicePropertyTagSaveReqVO.Item item = new IotDevicePropertyTagSaveReqVO.Item();
        item.setIdentifier(identifier);
        item.setTagName(tagName);
        reqVO.setItems(List.of(item));
        devicePropertyTagService.saveTagNames(reqVO);
    }

    private String generateProductKey() {
        return IdUtil.fastSimpleUUID();
    }

    private static class ProductContext {
        private final Long productId;
        private final String productKey;
        private final String productName;
        private final Set<String> identifiers = new HashSet<>();
        private boolean identifiersLoaded = false;

        private ProductContext(Long productId, String productKey, String productName) {
            this.productId = productId;
            this.productKey = productKey;
            this.productName = productName;
        }
    }

}
