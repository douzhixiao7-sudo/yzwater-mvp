package com.sydigit.yzwater.module.iot.service.inspectionstandard;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardItemRecordSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardItemSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardTargetSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionTargetOptionRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemRecordDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardTargetDO;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan.IotInspectionPlanMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardItemMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardItemRecordMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardTargetMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectiontask.IotInspectionTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_STANDARD_DELETE_FORBIDDEN_REFERENCED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_STANDARD_NAME_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_STANDARD_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_STANDARD_TARGET_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_STANDARD_TARGET_TYPE_INVALID;

/**
 * 巡检标准 Service 实现。
 */
@Service
@Validated
public class IotInspectionStandardServiceImpl implements IotInspectionStandardService {

    private static final String TARGET_TYPE_DEVICE = "device";
    private static final int DEFAULT_TARGET_OPTION_LIMIT = 100;
    private static final int MAX_TARGET_OPTION_LIMIT = 500;

    /**
     * 设施类型映射配置。
     */
    private static final Map<String, FacilityTargetMeta> FACILITY_TARGET_META_MAP = Map.of(
            "river", new FacilityTargetMeta("yz_river_channel", "river_name"),
            "river_section", new FacilityTargetMeta("yz_river_section", "section_name"),
            "reservoir", new FacilityTargetMeta("yz_water_reservoir", "reservoir_name"),
            "irrigation", new FacilityTargetMeta("yz_irrigation_district", "irrigation_district_name"),
            "signboard", new FacilityTargetMeta("yz_signboard", "signboard_name"),
            "pump_station", new FacilityTargetMeta("yz_pump_station", "pump_station_name"),
            "dike", new FacilityTargetMeta("yz_embankment", "embankment_name"),
            "flood_prevention_material", new FacilityTargetMeta("yz_flood_prevention_material", "warehouse_name")
    );

    /**
     * 支持的对象类型集合。
     */
    private static final Set<String> SUPPORTED_TARGET_TYPES;

    static {
        Set<String> targetTypes = new HashSet<>(FACILITY_TARGET_META_MAP.keySet());
        targetTypes.add(TARGET_TYPE_DEVICE);
        SUPPORTED_TARGET_TYPES = Collections.unmodifiableSet(targetTypes);
    }

    @Resource
    private IotInspectionStandardMapper standardMapper;
    @Resource
    private IotInspectionStandardTargetMapper standardTargetMapper;
    @Resource
    private IotInspectionStandardItemMapper standardItemMapper;
    @Resource
    private IotInspectionStandardItemRecordMapper standardItemRecordMapper;
    @Resource
    private IotInspectionPlanMapper planMapper;
    @Resource
    private IotInspectionTaskMapper taskMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建巡检标准及其适用对象、检查项。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStandard(IotInspectionStandardSaveReqVO createReqVO) {
        validateStandardNameUnique(null, createReqVO.getStandardName());

        String stationId = trimToNull(createReqVO.getStationId());
        List<IotInspectionStandardTargetSaveReqVO> targets = normalizeTargets(createReqVO.getTargets());
        int totalItemCount = countTotalItems(targets);

        IotInspectionStandardDO standard = BeanUtils.toBean(createReqVO, IotInspectionStandardDO.class);
        standard.setSuggestCycleValue(buildSuggestCycleValue(createReqVO.getSuggestCycleValue()));
        standard.setTargetCount(targets.size());
        standard.setItemCount(totalItemCount);
        standard.setRemark(IotInspectionCheckResultConfigHelper.extractRemark(createReqVO.getRemark()));
        standardMapper.insert(standard);

        saveTargetsAndItems(standard.getId(), stationId, targets);
        return standard.getId();
    }

    /**
     * 更新巡检标准及其适用对象、检查项。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStandard(IotInspectionStandardSaveReqVO updateReqVO) {
        validateStandardExists(updateReqVO.getId());
        validateStandardNameUnique(updateReqVO.getId(), updateReqVO.getStandardName());

        String stationId = trimToNull(updateReqVO.getStationId());
        List<IotInspectionStandardTargetSaveReqVO> targets = normalizeTargets(updateReqVO.getTargets());
        int totalItemCount = countTotalItems(targets);

        IotInspectionStandardDO updateObj = BeanUtils.toBean(updateReqVO, IotInspectionStandardDO.class);
        updateObj.setSuggestCycleValue(buildSuggestCycleValue(updateReqVO.getSuggestCycleValue()));
        updateObj.setTargetCount(targets.size());
        updateObj.setItemCount(totalItemCount);
        updateObj.setRemark(IotInspectionCheckResultConfigHelper.extractRemark(updateReqVO.getRemark()));
        standardMapper.updateById(updateObj);

        replaceTargetsAndItems(updateReqVO.getId(), stationId, targets);
    }

    /**
     * 删除巡检标准。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStandard(Long id) {
        validateStandardExists(id);
        validateStandardNotReferenced(id);
        clearTargetsAndItems(id);
        standardMapper.deleteById(id);
    }

    /**
     * 校验巡检标准未被计划或任务引用。
     */
    private void validateStandardNotReferenced(Long standardId) {
        if (planMapper.countByStandardId(standardId) > 0 || taskMapper.countByStandardId(standardId) > 0) {
            throw exception(INSPECTION_STANDARD_DELETE_FORBIDDEN_REFERENCED);
        }
    }

    /**
     * 获取巡检标准详情。
     */
    @Override
    public IotInspectionStandardDO getStandard(Long id) {
        return standardMapper.selectById(id);
    }

    /**
     * 校验巡检标准是否存在。
     */
    @Override
    public IotInspectionStandardDO validateStandardExists(Long id) {
        IotInspectionStandardDO standard = standardMapper.selectById(id);
        if (standard == null) {
            throw exception(INSPECTION_STANDARD_NOT_EXISTS);
        }
        return standard;
    }

    /**
     * 巡检标准分页查询。
     */
    @Override
    public PageResult<IotInspectionStandardDO> getStandardPage(IotInspectionStandardPageReqVO pageReqVO) {
        if (StrUtil.isNotBlank(pageReqVO.getStationId()) || StrUtil.isNotBlank(pageReqVO.getTargetType())) {
            String normalizedTargetType = normalizeTargetType(pageReqVO.getTargetType());
            if (StrUtil.isNotBlank(pageReqVO.getTargetType()) && normalizedTargetType == null) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
            List<Long> standardIds = standardTargetMapper.selectStandardIdsByFilter(
                    trimToNull(pageReqVO.getStationId()), normalizedTargetType);
            if (standardIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
            pageReqVO.setStandardIds(new ArrayList<>(new LinkedHashSet<>(standardIds)));
        }
        return standardMapper.selectPage(pageReqVO);
    }

    /**
     * 根据标准 ID 查询适用对象列表。
     */
    @Override
    public List<IotInspectionStandardTargetDO> getStandardTargetList(Long standardId) {
        return standardTargetMapper.selectListByStandardId(standardId);
    }

    /**
     * 根据适用对象 ID 集合查询检查项列表。
     */
    @Override
    public List<IotInspectionStandardItemDO> getStandardItemListByTargetIds(Collection<Long> targetIds) {
        return standardItemMapper.selectListByTargetRefIds(targetIds);
    }

    /**
     * 获取检查项对应记录模板。
     */
    @Override
    public List<IotInspectionStandardItemRecordDO> getStandardItemRecordList(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }
        return standardItemRecordMapper.selectListByStandardItemIds(itemIds);
    }

    /**
     * 获取适用对象下拉。
     */
    @Override
    public List<IotInspectionTargetOptionRespVO> listTargetOptions(String targetType, String stationId, String keyword, Integer limit) {
        String normalizedTargetType = normalizeTargetType(targetType);
        if (normalizedTargetType == null) {
            return Collections.emptyList();
        }
        int validLimit = normalizeLimit(limit);
        if (TARGET_TYPE_DEVICE.equals(normalizedTargetType)) {
            return queryDeviceOptions(trimToNull(stationId), keyword, validLimit);
        }
        return queryFacilityOptions(normalizedTargetType, keyword, validLimit);
    }

    /**
     * 用新对象和检查项替换原数据。
     */
    private void replaceTargetsAndItems(Long standardId, String stationId, List<IotInspectionStandardTargetSaveReqVO> targets) {
        clearTargetsAndItems(standardId);
        saveTargetsAndItems(standardId, stationId, targets);
    }

    /**
     * 清理标准下的对象、检查项、记录项。
     */
    private void clearTargetsAndItems(Long standardId) {
        List<IotInspectionStandardItemDO> oldItems = standardItemMapper.selectListByStandardId(standardId);
        if (!oldItems.isEmpty()) {
            List<Long> itemIds = CollectionUtils.convertList(oldItems, IotInspectionStandardItemDO::getId);
            standardItemRecordMapper.deleteByStandardItemIds(itemIds);
        }
        standardItemMapper.deleteByStandardId(standardId);
        standardTargetMapper.deleteByStandardId(standardId);
    }

    /**
     * 保存适用对象和检查项。
     */
    private void saveTargetsAndItems(Long standardId, String stationId, List<IotInspectionStandardTargetSaveReqVO> targets) {
        for (int targetIndex = 0; targetIndex < targets.size(); targetIndex++) {
            IotInspectionStandardTargetSaveReqVO targetReq = targets.get(targetIndex);
            String normalizedTargetType = normalizeTargetType(targetReq.getTargetType());
            if (normalizedTargetType == null) {
                throw exception(INSPECTION_STANDARD_TARGET_TYPE_INVALID);
            }
            Long targetId = targetReq.getTargetId();
            String targetName = resolveAndValidateTargetName(normalizedTargetType, targetId, stationId);

            IotInspectionStandardTargetDO target = new IotInspectionStandardTargetDO();
            target.setStandardId(standardId);
            target.setTargetType(normalizedTargetType);
            target.setTargetId(targetId);
            target.setStationId(StrUtil.blankToDefault(stationId, ""));
            target.setTargetName(targetName);
            target.setSort(targetReq.getSort() == null ? targetIndex + 1 : targetReq.getSort());
            target.setItemCount(targetReq.getItems() == null ? 0 : targetReq.getItems().size());
            target.setCheckResultConfigsJson(IotInspectionCheckResultConfigHelper.toJson(targetReq.getCheckResultConfigs()));
            standardTargetMapper.insert(target);

            saveItems(standardId, target.getId(), targetReq.getItems());

            IotInspectionStandardTargetDO countUpdate = new IotInspectionStandardTargetDO();
            countUpdate.setId(target.getId());
            countUpdate.setItemCount(targetReq.getItems() == null ? 0 : targetReq.getItems().size());
            standardTargetMapper.updateById(countUpdate);
        }
    }

    /**
     * 保存检查项并维护记录模板。
     */
    private void saveItems(Long standardId, Long targetRefId, List<IotInspectionStandardItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            IotInspectionStandardItemSaveReqVO itemReq = items.get(itemIndex);
            IotInspectionStandardItemDO item = BeanUtils.toBean(itemReq, IotInspectionStandardItemDO.class);
            item.setId(null);
            item.setStandardId(standardId);
            item.setTargetRefId(targetRefId);
            item.setSort(item.getSort() == null ? itemIndex + 1 : item.getSort());
            item.setNeedUploadAttachment(item.getNeedUploadAttachment() == null ? 0 : item.getNeedUploadAttachment());
            standardItemMapper.insert(item);

            saveItemRecords(item.getId(), itemReq.getRecordTemplates());

            IotInspectionStandardItemDO countUpdate = new IotInspectionStandardItemDO();
            countUpdate.setId(item.getId());
            countUpdate.setRecordCount(itemReq.getRecordTemplates() == null ? 0 : itemReq.getRecordTemplates().size());
            standardItemMapper.updateById(countUpdate);
        }
    }

    /**
     * 保存检查项对应的记录模板。
     */
    private void saveItemRecords(Long itemId, List<IotInspectionStandardItemRecordSaveReqVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (int i = 0; i < records.size(); i++) {
            IotInspectionStandardItemRecordSaveReqVO recordReq = records.get(i);
            IotInspectionStandardItemRecordDO record = BeanUtils.toBean(recordReq, IotInspectionStandardItemRecordDO.class);
            record.setId(null);
            record.setStandardItemId(itemId);
            record.setSort(record.getSort() == null ? i + 1 : record.getSort());
            record.setRequiredFlag(record.getRequiredFlag() == null ? 0 : record.getRequiredFlag());
            record.setValueType(StrUtil.isBlank(record.getValueType()) ? "TEXT" : record.getValueType());
            standardItemRecordMapper.insert(record);
        }
    }

    /**
     * 校验标准名称唯一。
     */
    private void validateStandardNameUnique(Long id, String standardName) {
        if (StrUtil.isBlank(standardName)) {
            return;
        }
        IotInspectionStandardDO standard = standardMapper.selectByName(standardName);
        if (standard == null) {
            return;
        }
        if (!Objects.equals(standard.getId(), id)) {
            throw exception(INSPECTION_STANDARD_NAME_EXISTS);
        }
    }

    /**
     * 规范化适用对象列表并去重。
     */
    private List<IotInspectionStandardTargetSaveReqVO> normalizeTargets(List<IotInspectionStandardTargetSaveReqVO> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> exists = new LinkedHashSet<>();
        List<IotInspectionStandardTargetSaveReqVO> result = new ArrayList<>();
        for (IotInspectionStandardTargetSaveReqVO target : targets) {
            if (target == null || target.getTargetId() == null) {
                continue;
            }
            String normalizedTargetType = normalizeTargetType(target.getTargetType());
            if (normalizedTargetType == null) {
                continue;
            }
            target.setTargetType(normalizedTargetType);
            String key = normalizedTargetType + "#" + target.getTargetId();
            if (exists.add(key)) {
                result.add(target);
            }
        }
        if (result.isEmpty()) {
            throw exception(INSPECTION_STANDARD_TARGET_NOT_EXISTS);
        }
        return result;
    }

    /**
     * 统计检查项总数。
     */
    private int countTotalItems(List<IotInspectionStandardTargetSaveReqVO> targets) {
        int total = 0;
        for (IotInspectionStandardTargetSaveReqVO target : targets) {
            if (target == null || target.getItems() == null) {
                continue;
            }
            total += target.getItems().size();
        }
        return total;
    }

    /**
     * 建议周期值默认固定为1。
     */
    private Integer buildSuggestCycleValue(Integer suggestCycleValue) {
        if (suggestCycleValue == null || suggestCycleValue < 1) {
            return 1;
        }
        return suggestCycleValue;
    }

    /**
     * 校验并解析适用对象名称。
     */
    private String resolveAndValidateTargetName(String targetType, Long targetId, String stationId) {
        if (targetId == null) {
            throw exception(INSPECTION_STANDARD_TARGET_NOT_EXISTS);
        }
        if (TARGET_TYPE_DEVICE.equals(targetType)) {
            return resolveDeviceName(targetId, stationId);
        }
        return resolveFacilityName(targetType, targetId);
    }

    /**
     * 查询设备名称并校验设备存在。
     */
    private String resolveDeviceName(Long targetId, String stationId) {
        if (StrUtil.isBlank(stationId)) {
            throw exception(INSPECTION_STANDARD_TARGET_NOT_EXISTS);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, station_id, ")
                .append("COALESCE(NULLIF(nickname, ''), NULLIF(device_name, ''), CONCAT('设备-', id)) AS name ")
                .append("FROM iot_device WHERE deleted = 0 AND id = ?");
        List<Object> args = new ArrayList<>();
        args.add(targetId);
        sql.append(" AND station_id = ?");
        args.add(stationId);

        List<IotInspectionTargetOptionRespVO> rows = jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionTargetOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("name"),
                        TARGET_TYPE_DEVICE,
                        rs.getString("station_id")),
                args.toArray());
        if (rows.isEmpty()) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return rows.get(0).getName();
    }

    /**
     * 查询水利设施名称并校验对象存在。
     */
    private String resolveFacilityName(String targetType, Long targetId) {
        FacilityTargetMeta meta = FACILITY_TARGET_META_MAP.get(targetType);
        if (meta == null) {
            throw exception(INSPECTION_STANDARD_TARGET_TYPE_INVALID);
        }
        String sql = "SELECT id, " + meta.nameColumn + " AS name FROM " + meta.tableName
                + " WHERE id = ? AND (deleted = 0 OR deleted IS NULL)";
        List<IotInspectionTargetOptionRespVO> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new IotInspectionTargetOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("name"),
                        targetType,
                        null),
                targetId);
        if (rows.isEmpty()) {
            throw exception(INSPECTION_STANDARD_TARGET_NOT_EXISTS);
        }
        return StrUtil.blankToDefault(rows.get(0).getName(), targetType + "-" + targetId);
    }

    /**
     * 查询设备下拉选项。
     */
    private List<IotInspectionTargetOptionRespVO> queryDeviceOptions(String stationId, String keyword, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, station_id, ")
                .append("COALESCE(NULLIF(nickname, ''), NULLIF(device_name, ''), CONCAT('设备-', id)) AS name ")
                .append("FROM iot_device WHERE deleted = 0");
        List<Object> args = new ArrayList<>();

        if (StrUtil.isNotBlank(stationId)) {
            sql.append(" AND station_id = ?");
            args.add(stationId);
        }
        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword != null) {
            sql.append(" AND (")
                    .append("nickname LIKE ? OR device_name LIKE ? OR serial_number LIKE ?")
                    .append(")");
            String fuzzyKeyword = "%" + normalizedKeyword + "%";
            args.add(fuzzyKeyword);
            args.add(fuzzyKeyword);
            args.add(fuzzyKeyword);
        }
        sql.append(" ORDER BY update_time DESC, id DESC LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionTargetOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("name"),
                        TARGET_TYPE_DEVICE,
                        rs.getString("station_id")),
                args.toArray());
    }

    /**
     * 查询水利设施下拉选项。
     */
    private List<IotInspectionTargetOptionRespVO> queryFacilityOptions(String targetType, String keyword, int limit) {
        FacilityTargetMeta meta = FACILITY_TARGET_META_MAP.get(targetType);
        if (meta == null) {
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, ").append(meta.nameColumn).append(" AS name ")
                .append("FROM ").append(meta.tableName)
                .append(" WHERE (deleted = 0 OR deleted IS NULL)");
        List<Object> args = new ArrayList<>();

        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword != null) {
            sql.append(" AND ").append(meta.nameColumn).append(" LIKE ?");
            args.add("%" + normalizedKeyword + "%");
        }
        sql.append(" ORDER BY ").append(meta.nameColumn).append(" ASC, id ASC LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionTargetOptionRespVO(
                        rs.getLong("id"),
                        StrUtil.blankToDefault(rs.getString("name"), targetType + "-" + rs.getLong("id")),
                        targetType,
                        null),
                args.toArray());
    }

    /**
     * 规范化对象类型。
     */
    private String normalizeTargetType(String targetType) {
        String normalizedType = trimToNull(targetType);
        if (normalizedType == null) {
            return null;
        }
        normalizedType = normalizedType.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_TARGET_TYPES.contains(normalizedType)) {
            return null;
        }
        return normalizedType;
    }

    /**
     * 限制下拉查询条数。
     */

    /**
     * 去除前后空白，空字符串返回 null。
     */
    private String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }


    /**
     * 限制下拉查询条数。
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_TARGET_OPTION_LIMIT;
        }
        return Math.min(limit, MAX_TARGET_OPTION_LIMIT);
    }

    /**
     * 设施类型元数据。
     */
    private static final class FacilityTargetMeta {
        private final String tableName;
        private final String nameColumn;

        private FacilityTargetMeta(String tableName, String nameColumn) {
            this.tableName = tableName;
            this.nameColumn = nameColumn;
        }
    }

}
