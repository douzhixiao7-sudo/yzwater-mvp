package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxTaskListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxTaskSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxTaskDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxTaskMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import lombok.RequiredArgsConstructor;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 风险隐患点服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxTaskService {

    private static final int GEOM_SRID = 4490;
    private static final int MAX_GEOMETRY_JSON_LENGTH = 512_000;
    private static final Set<String> ALLOWED_GEOJSON_TYPES = Set.of("Point", "LineString", "MultiLineString");

    private final YzFxTaskMapper taskMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final ObjectMapper objectMapper;

    /**
     * 列表查询
     */
    public List<FxTaskListRespVO> getList() {
        return taskMapper.selectListOrderBySort().stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 详情
     */
    public FxTaskSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("风险隐患点 ID 不能为空");
        }
        YzFxTaskDO exists = taskMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "风险隐患点不存在或已删除");
        }
        FxTaskSaveReqVO vo = new FxTaskSaveReqVO();
        vo.setId(exists.getId());
        vo.setCode(exists.getCode());
        vo.setName(exists.getName());
        vo.setAddr(exists.getAddr());
        vo.setRiverChannelId(exists.getRiverChannelId());
        vo.setRiverChannelName(exists.getRiverChannelName());
        vo.setLevel(exists.getLevel());
        vo.setContent(exists.getContent());
        vo.setCounterMeasures(exists.getCounterMeasures());
        vo.setFiles(toFileList(exists.getFiles()));
        vo.setSort(exists.getSort());
        vo.setGeometryGeoJson(buildGeometryGeoJson(exists.getGeom(), exists.getGeomMeta()));
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxTaskSaveReqVO reqVO) {
        YzFxTaskDO insert = new YzFxTaskDO();
        insert.setId(generateId());
        fillFields(insert, reqVO, true);
        taskMapper.insert(insert);
        return insert.getId();
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxTaskSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("风险隐患点 ID 不能为空");
        }
        YzFxTaskDO exists = taskMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "风险隐患点不存在或已删除");
        }
        YzFxTaskDO update = new YzFxTaskDO();
        update.setId(id);
        fillFields(update, reqVO, false);
        taskMapper.updateById(update);
    }

    /**
     * 将库内几何与元数据合并为前端 GeoJSON 字符串（只读，供大屏等场景复用）
     */
    public String toGeometryGeoJson(Geometry geometry, String geomMeta) {
        return buildGeometryGeoJson(geometry, geomMeta);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("风险隐患点 ID 不能为空");
        }
        YzFxTaskDO exists = taskMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "风险隐患点不存在或已删除");
        }
        taskMapper.deleteById(key);
    }

    private FxTaskListRespVO buildListResp(YzFxTaskDO item) {
        FxTaskListRespVO vo = new FxTaskListRespVO();
        vo.setId(item.getId());
        vo.setCode(item.getCode());
        vo.setName(item.getName());
        vo.setAddr(item.getAddr());
        vo.setRiverChannelId(item.getRiverChannelId());
        vo.setRiverChannelName(item.getRiverChannelName());
        vo.setLevel(item.getLevel());
        vo.setContent(item.getContent());
        vo.setCounterMeasures(item.getCounterMeasures());
        vo.setFiles(toFileList(item.getFiles()));
        vo.setSort(item.getSort());
        return vo;
    }

    private void fillRiverChannel(YzFxTaskDO target, Long riverChannelId) {
        if (riverChannelId == null) {
            target.setRiverChannelId(null);
            target.setRiverChannelName(null);
            return;
        }
        YzRiverChannelDO channel = riverChannelMapper.selectById(riverChannelId);
        if (channel == null || StrUtil.isBlank(channel.getRiverName())) {
            throw ServiceExceptionUtil.invalidParamException("所属河道不存在");
        }
        target.setRiverChannelId(riverChannelId);
        target.setRiverChannelName(StrUtil.trim(channel.getRiverName()));
    }

    private void fillFields(YzFxTaskDO target, FxTaskSaveReqVO source, boolean createMode) {
        target.setCode(resolveCode(source.getCode(), createMode));
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setAddr(StrUtil.trimToNull(source.getAddr()));
        fillRiverChannel(target, source.getRiverChannelId());
        target.setLevel(StrUtil.trimToNull(source.getLevel()));
        target.setContent(StrUtil.trimToNull(source.getContent()));
        target.setCounterMeasures(StrUtil.trimToNull(source.getCounterMeasures()));
        target.setFiles(normalizeFiles(source.getFiles()));
        target.setSort(source.getSort() == null ? 0 : source.getSort());
        ParsedGeometryPayload payload = parseGeometryPayload(source.getGeometryGeoJson());
        target.setGeom(payload.geometry());
        target.setGeomMeta(payload.geomMeta());
    }

    private record ParsedGeometryPayload(Geometry geometry, String geomMeta) {
    }

    private ParsedGeometryPayload parseGeometryPayload(String geometryGeoJson) {
        String raw = StrUtil.trimToNull(geometryGeoJson);
        if (raw == null) {
            return new ParsedGeometryPayload(null, null);
        }
        if (raw.length() > MAX_GEOMETRY_JSON_LENGTH) {
            throw ServiceExceptionUtil.invalidParamException("地图位置信息过大");
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            if (root == null || !root.hasNonNull("type")) {
                throw ServiceExceptionUtil.invalidParamException("地图位置信息格式不正确");
            }
            String geoType = root.get("type").asText();
            if (!ALLOWED_GEOJSON_TYPES.contains(geoType)) {
                throw ServiceExceptionUtil.invalidParamException("地图仅支持打点、单条线段或多条线段");
            }
            ObjectNode clean = objectMapper.createObjectNode();
            clean.put("type", geoType);
            clean.set("coordinates", root.get("coordinates"));
            Geometry geometry = new GeometryJSON().read(clean.toString());
            if (geometry == null || geometry.isEmpty()) {
                return new ParsedGeometryPayload(null, null);
            }
            if (!isAllowedTaskGeometry(geometry)) {
                throw ServiceExceptionUtil.invalidParamException("地图仅支持打点、单条线段或多条线段");
            }
            geometry.setSRID(GEOM_SRID);
            FxTaskGeometryMetaBuilder.validateLineGeometry(geometry);
            ObjectNode inputMeta = FxTaskGeometryMetaBuilder.extractInputMeta(root, objectMapper);
            String geomMeta = FxTaskGeometryMetaBuilder.buildGeomMetaJson(geometry, inputMeta, objectMapper);
            return new ParsedGeometryPayload(geometry, geomMeta);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ServiceExceptionUtil.invalidParamException("地图位置信息格式不正确");
        }
    }

    private String buildGeometryGeoJson(Geometry geometry, String geomMeta) {
        if (geometry == null) {
            return "";
        }
        try {
            StringWriter writer = new StringWriter();
            new GeometryJSON().write(geometry, writer);
            JsonNode storedMeta = StrUtil.isBlank(geomMeta) ? null : objectMapper.readTree(geomMeta);
            String effectiveMeta = FxTaskGeometryMetaBuilder.buildGeomMetaJson(geometry, storedMeta, objectMapper);
            if (StrUtil.isBlank(effectiveMeta)) {
                return writer.toString();
            }
            ObjectNode root = (ObjectNode) objectMapper.readTree(writer.toString());
            FxTaskGeometryMetaBuilder.mergeMetaToGeometryRoot(root, objectMapper.readTree(effectiveMeta));
            return root.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private String[] normalizeFiles(List<String> files) {
        if (CollUtil.isEmpty(files)) {
            return null;
        }
        List<String> cleaned = files.stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
    }

    private List<String> toFileList(String[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }
        return Arrays.stream(files)
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private String resolveCode(String code, boolean createMode) {
        String input = StrUtil.trimToNull(code);
        if (input != null) {
            return input;
        }
        return createMode ? generateCode() : null;
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }

    private String generateCode() {
        return "FXYH-" + IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
    }

    private static boolean isAllowedTaskGeometry(Geometry geometry) {
        return geometry instanceof Point
                || geometry instanceof LineString
                || geometry instanceof MultiLineString;
    }
}
