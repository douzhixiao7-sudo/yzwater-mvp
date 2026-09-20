package com.sydigit.yzwater.module.service.embankment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentGisImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentGisImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.mysql.embankment.YzEmbankmentMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.util.GaussKrugerCoordinateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 堤防 GIS Excel 导入服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class EmbankmentGisExcelImportService {

    private static final int TARGET_SRID = 4490;
    private static final double CENTRAL_MERIDIAN = 120D;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final EmbankmentService embankmentService;
    private final YzEmbankmentMapper embankmentMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;

    @Transactional(rollbackFor = Exception.class)
    public EmbankmentGisImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<EmbankmentGisImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), EmbankmentGisImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(1)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        EmbankmentGisImportRespVO respVO = new EmbankmentGisImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            respVO.setMessage("文件中未读取到任何数据");
            return respVO;
        }

        Map<String, GroupContext> groupMap = new LinkedHashMap<>();
        int validRowCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            EmbankmentGisImportExcelVO row = rows.get(i);
            int rowNumber = i + 2;
            if (row == null || isEmptyRow(row)) {
                continue;
            }
            validRowCount++;
            try {
                String embankmentName = normalizeText(row.getEmbankmentName());
                if (StrUtil.isBlank(embankmentName)) {
                    throw new IllegalArgumentException("堤防名称不能为空");
                }
                Coordinate start = parseCoordinate(row.getStartX(), row.getStartY(), "起点", rowNumber);
                Coordinate end = parseCoordinate(row.getEndX(), row.getEndY(), "讫点", rowNumber);
                LineString lineString = GEOMETRY_FACTORY.createLineString(new Coordinate[]{start, end});
                lineString.setSRID(TARGET_SRID);

                GroupContext group = groupMap.computeIfAbsent(embankmentName, key -> new GroupContext(rowNumber));
                group.getLines().add(lineString);
                group.setLastRowNumber(rowNumber);
            } catch (Exception ex) {
                respVO.addItem(buildItem(rowNumber, normalizeText(row.getEmbankmentName()),
                        "skip", false, StrUtil.blankToDefault(ex.getMessage(), "坐标解析失败")));
                respVO.setSkipCount(respVO.getSkipCount() + 1);
            }
        }

        respVO.setRowCount(validRowCount);
        respVO.setNameGroupCount(groupMap.size());
        if (groupMap.isEmpty()) {
            respVO.setMessage("未找到可导入的堤防 GIS 数据");
            return respVO;
        }

        int successCount = 0;
        int createFacilityCount = 0;
        int updateCount = 0;
        int skipCount = respVO.getSkipCount();
        for (Map.Entry<String, GroupContext> entry : groupMap.entrySet()) {
            String embankmentName = entry.getKey();
            GroupContext group = entry.getValue();
            try {
                if (CollUtil.isEmpty(group.getLines())) {
                    throw new IllegalArgumentException("未生成有效线段");
                }
                List<YzEmbankmentDO> embankments = embankmentMapper.selectList(new LambdaQueryWrapper<YzEmbankmentDO>()
                        .eq(YzEmbankmentDO::getEmbankmentName, embankmentName)
                        .eq(YzEmbankmentDO::getDeleted, 0));
                if (CollUtil.isEmpty(embankments)) {
                    throw new IllegalArgumentException("未匹配到堤防名称");
                }
                Geometry mergedGeometry = mergeGeometry(group.getLines());
                Point centerPoint = resolveCenterPoint(mergedGeometry);
                if (mergedGeometry == null || mergedGeometry.isEmpty() || centerPoint == null || centerPoint.isEmpty()) {
                    throw new IllegalArgumentException("未生成有效 GIS 或中心点");
                }

                int groupCreateFacilityCount = 0;
                int groupUpdateCount = 0;
                for (YzEmbankmentDO embankment : embankments) {
                    boolean createFacility = embankment.getFacilityId() == null
                            || facilityBaseMapper.selectById(embankment.getFacilityId()) == null;
                    Long facilityId = embankmentService.ensureFacilityBaseForExisting(embankment);

                    facilityBaseMapper.clearGeomById(facilityId);
                    YzWaterFacilityBaseDO baseUpdate = new YzWaterFacilityBaseDO();
                    baseUpdate.setId(facilityId);
                    baseUpdate.setGeomType(mergedGeometry.getGeometryType());
                    baseUpdate.setGeom(mergedGeometry);
                    baseUpdate.setSrid(TARGET_SRID);
                    facilityBaseMapper.updateById(baseUpdate);

                    YzEmbankmentDO embankmentUpdate = new YzEmbankmentDO();
                    embankmentUpdate.setId(embankment.getId());
                    embankmentUpdate.setFacilityId(facilityId);
                    embankmentUpdate.setLongitude(toBigDecimal(centerPoint.getX()));
                    embankmentUpdate.setLatitude(toBigDecimal(centerPoint.getY()));
                    embankmentMapper.updateById(embankmentUpdate);

                    groupUpdateCount++;
                    if (createFacility) {
                        groupCreateFacilityCount++;
                    }
                }
                successCount++;
                updateCount += groupUpdateCount;
                createFacilityCount += groupCreateFacilityCount;
            } catch (Exception ex) {
                skipCount++;
                respVO.addItem(buildItem(group.getFirstRowNumber(), embankmentName,
                        "skip", false, StrUtil.blankToDefault(ex.getMessage(), "更新失败")));
            }
        }

        respVO.setSuccessCount(successCount);
        respVO.setCreateFacilityCount(createFacilityCount);
        respVO.setUpdateCount(updateCount);
        respVO.setSkipCount(skipCount);
        respVO.setMessage(skipCount > 0 ? "部分堤防 GIS 导入失败，请查看明细" : "堤防 GIS 导入完成");
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private boolean isEmptyRow(EmbankmentGisImportExcelVO row) {
        return StrUtil.isAllBlank(
                normalizeText(row.getEmbankmentName()),
                normalizeText(row.getStartX()),
                normalizeText(row.getStartY()),
                normalizeText(row.getEndX()),
                normalizeText(row.getEndY())
        );
    }

    private String normalizeText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        normalized = StrUtil.removePrefix(normalized, "\"");
        normalized = StrUtil.removeSuffix(normalized, "\"");
        normalized = StrUtil.removePrefix(normalized, "'");
        normalized = StrUtil.removeSuffix(normalized, "'");
        return StrUtil.trimToNull(normalized);
    }

    private Coordinate parseCoordinate(String rawX, String rawY, String pointName, int rowNumber) {
        double x = parseDouble(rawX, pointName + "坐标X", rowNumber);
        double y = parseDouble(rawY, pointName + "坐标Y", rowNumber);
        double[] lonLat = GaussKrugerCoordinateUtils.inverseGaussKrugerTo4490(x, y, CENTRAL_MERIDIAN);
        return new Coordinate(lonLat[0], lonLat[1]);
    }

    private double parseDouble(String text, String columnName, int rowNumber) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            throw new IllegalArgumentException(columnName + "不能为空");
        }
        try {
            return Double.parseDouble(normalized.replace(",", "").replace("，", ""));
        } catch (Exception ex) {
            throw new IllegalArgumentException(columnName + "格式错误（第 " + rowNumber + " 行）");
        }
    }

    private Geometry mergeGeometry(List<LineString> lineStrings) {
        Geometry merged = lineStrings.size() == 1 ? lineStrings.get(0) : UnaryUnionOp.union(new ArrayList<>(lineStrings));
        if (merged != null) {
            merged.setSRID(TARGET_SRID);
        }
        return merged;
    }

    private Point resolveCenterPoint(Geometry geometry) {
        LineString longestLine = findLongestLine(geometry);
        if (longestLine == null || longestLine.isEmpty() || longestLine.getLength() <= 0) {
            return null;
        }
        LengthIndexedLine indexedLine = new LengthIndexedLine(longestLine);
        Coordinate midpoint = indexedLine.extractPoint(longestLine.getLength() / 2);
        Point point = GEOMETRY_FACTORY.createPoint(midpoint);
        point.setSRID(TARGET_SRID);
        return point;
    }

    private LineString findLongestLine(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof LineString lineString) {
            return lineString;
        }
        LineString longest = null;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry child = geometry.getGeometryN(i);
            LineString candidate = findLongestLine(child);
            if (candidate == null) {
                continue;
            }
            if (longest == null || candidate.getLength() > longest.getLength()) {
                longest = candidate;
            }
        }
        return longest;
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private EmbankmentGisImportRespVO.Item buildItem(Integer rowNumber, String embankmentName,
                                                     String action, boolean success, String message) {
        EmbankmentGisImportRespVO.Item item = new EmbankmentGisImportRespVO.Item();
        item.setRowNumber(rowNumber);
        item.setEmbankmentName(embankmentName);
        item.setAction(action);
        item.setSuccess(success);
        item.setMessage(message);
        return item;
    }

    @Getter
    private static class GroupContext {

        private final Integer firstRowNumber;
        private Integer lastRowNumber;
        private final List<LineString> lines = new ArrayList<>();

        private GroupContext(Integer firstRowNumber) {
            this.firstRowNumber = firstRowNumber;
            this.lastRowNumber = firstRowNumber;
        }

        private void setLastRowNumber(Integer lastRowNumber) {
            this.lastRowNumber = lastRowNumber;
        }
    }
}
