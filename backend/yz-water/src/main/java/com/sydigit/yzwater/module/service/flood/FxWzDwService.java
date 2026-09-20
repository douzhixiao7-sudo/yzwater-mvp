package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzDwListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzDwGeomMigrateRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzDwSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDwDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxWzDwMapper;
import lombok.RequiredArgsConstructor;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

/**
 * 防汛物资-单位服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxWzDwService {

    private static final int GEOM_SRID = 4490;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final YzFxWzDwMapper wzDwMapper;
    private final ObjectMapper objectMapper;

    /**
     * 列表查询
     */
    public List<FxWzDwListRespVO> getList() {
        List<YzFxWzDwDO> list = wzDwMapper.selectListOrderBySort();
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 详情
     */
    public FxWzDwSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("单位ID不能为空");
        }
        YzFxWzDwDO exists = wzDwMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "单位不存在或已删除");
        }
        FxWzDwSaveReqVO vo = new FxWzDwSaveReqVO();
        vo.setId(exists.getId());
        vo.setUnitName(exists.getUnitName());
        vo.setAddress(exists.getAddress());
        vo.setSort(exists.getSort());
        vo.setIsDelegateStorage(normalizeDelegateStorage(exists.getIsDelegateStorage()));
        vo.setGeometryGeoJson(buildGeometryGeoJson(exists.getGeom()));
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxWzDwSaveReqVO reqVO) {
        String id = generateId();
        YzFxWzDwDO insert = new YzFxWzDwDO();
        insert.setId(id);
        fillFields(insert, reqVO);
        wzDwMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxWzDwSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("单位ID不能为空");
        }
        YzFxWzDwDO exists = wzDwMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "单位不存在或已删除");
        }
        YzFxWzDwDO update = new YzFxWzDwDO();
        update.setId(id);
        fillFields(update, reqVO);
        if (reqVO.getSort() == null) {
            update.setSort(exists.getSort());
        }
        wzDwMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("单位ID不能为空");
        }
        YzFxWzDwDO exists = wzDwMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "单位不存在或已删除");
        }
        wzDwMapper.deleteById(key);
    }

    /**
     * 将 pos 字段迁移到 geom 字段（SRID=4490）
     */
    public FxWzDwGeomMigrateRespVO migratePosToGeom() {
        List<YzFxWzDwDO> list = wzDwMapper.selectListForGeomMigrate();
        FxWzDwGeomMigrateRespVO resp = new FxWzDwGeomMigrateRespVO();
        if (list == null || list.isEmpty()) {
            resp.setMessage("未找到需要迁移的数据");
            return resp;
        }
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (YzFxWzDwDO item : list) {
            if (item == null || StrUtil.isBlank(item.getId())) {
                skipped++;
                continue;
            }
            if (item.getGeom() != null) {
                skipped++;
                continue;
            }
            String pos = StrUtil.trimToNull(item.getPos());
            if (pos == null) {
                skipped++;
                continue;
            }
            Point point = parsePoint(pos);
            if (point == null) {
                failed++;
                continue;
            }
            YzFxWzDwDO update = new YzFxWzDwDO();
            update.setId(item.getId());
            update.setGeom(point);
            wzDwMapper.updateById(update);
            success++;
        }
        resp.setSuccessCount(success);
        resp.setFailedCount(failed);
        resp.setMessage(buildMessage(list.size(), success, failed, skipped));
        return resp;
    }

    /**
     * pos 格式：{"center":[纬度,经度]}，需转换为 POINT(经度 纬度)
     */
    private Point parsePoint(String pos) {
        try {
            JsonNode root = objectMapper.readTree(pos);
            JsonNode center = root == null ? null : root.get("center");
            if (center == null || !center.isArray() || center.size() < 2) {
                return null;
            }
            double latitude = center.get(0).asDouble(Double.NaN);
            double longitude = center.get(1).asDouble(Double.NaN);
            if (!isFinite(latitude) || !isFinite(longitude)) {
                return null;
            }
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return null;
            }
            Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
            point.setSRID(GEOM_SRID);
            return point;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private FxWzDwListRespVO buildListResp(YzFxWzDwDO item) {
        FxWzDwListRespVO vo = new FxWzDwListRespVO();
        vo.setId(item.getId());
        vo.setUnitName(item.getUnitName());
        vo.setAddress(item.getAddress());
        vo.setIsDelegateStorage(normalizeDelegateStorage(item.getIsDelegateStorage()));
        return vo;
    }

    private void fillFields(YzFxWzDwDO target, FxWzDwSaveReqVO source) {
        target.setUnitName(StrUtil.trimToNull(source.getUnitName()));
        target.setAddress(StrUtil.trimToNull(source.getAddress()));
        target.setSort(source.getSort());
        target.setIsDelegateStorage(normalizeDelegateStorage(source.getIsDelegateStorage()));
        target.setGeom(parseGeometryGeoJson(source.getGeometryGeoJson()));
    }

    private Integer normalizeDelegateStorage(Integer value) {
        return Objects.equals(value, 1) ? 1 : 0;
    }

    private Geometry parseGeometryGeoJson(String geometryGeoJson) {
        if (StrUtil.isBlank(geometryGeoJson)) {
            throw ServiceExceptionUtil.invalidParamException("位置信息不能为空");
        }
        Geometry geometry;
        try {
            geometry = new GeometryJSON().read(geometryGeoJson);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.invalidParamException("位置信息格式不正确");
        }
        if (geometry == null || geometry.isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("位置信息不能为空");
        }
        if (!"Point".equalsIgnoreCase(geometry.getGeometryType())) {
            throw ServiceExceptionUtil.invalidParamException("仅支持点位");
        }
        geometry.setSRID(GEOM_SRID);
        return geometry;
    }

    private String buildGeometryGeoJson(Geometry geometry) {
        if (geometry == null) {
            return "";
        }
        try {
            StringWriter writer = new StringWriter();
            new GeometryJSON().write(geometry, writer);
            return writer.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private String generateId() {
        return cn.hutool.core.util.IdUtil.fastSimpleUUID();
    }

    private String buildMessage(int total, int success, int failed, int skipped) {
        return "总数" + total + "，成功" + success + "，失败" + failed + "，跳过" + skipped;
    }
}
