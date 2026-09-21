package com.sydigit.yzwater.module.iot.service.inspectionline;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePointOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePointSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLineSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLineDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLinePointDO;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan.IotInspectionPlanMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionline.IotInspectionLineMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionline.IotInspectionLinePointMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectiontask.IotInspectionTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_LINE_DELETE_FORBIDDEN_REFERENCED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_LINE_NAME_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_LINE_NOT_EXISTS;

/**
 * 巡检线路 Service 实现。
 */
@Service
@Validated
public class IotInspectionLineServiceImpl implements IotInspectionLineService {

    private static final int DEFAULT_POINT_OPTION_LIMIT = 300;
    private static final int MAX_POINT_OPTION_LIMIT = 1000;

    @Resource
    private IotInspectionLineMapper lineMapper;
    @Resource
    private IotInspectionLinePointMapper linePointMapper;
    @Resource
    private IotInspectionPlanMapper planMapper;
    @Resource
    private IotInspectionTaskMapper taskMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建巡检线路及点位。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLine(IotInspectionLineSaveReqVO createReqVO) {
        validateLineNameUnique(null, createReqVO.getLineName());

        IotInspectionLineDO line = BeanUtils.toBean(createReqVO, IotInspectionLineDO.class);
        line.setPointCount(createReqVO.getPoints() == null ? 0 : createReqVO.getPoints().size());
        lineMapper.insert(line);

        savePoints(line.getId(), createReqVO.getStationId(), createReqVO.getPoints());
        return line.getId();
    }

    /**
     * 更新巡检线路及点位。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLine(IotInspectionLineSaveReqVO updateReqVO) {
        validateLineExists(updateReqVO.getId());
        validateLineNameUnique(updateReqVO.getId(), updateReqVO.getLineName());

        IotInspectionLineDO updateObj = BeanUtils.toBean(updateReqVO, IotInspectionLineDO.class);
        updateObj.setPointCount(updateReqVO.getPoints() == null ? 0 : updateReqVO.getPoints().size());
        lineMapper.updateById(updateObj);

        linePointMapper.deleteByLineId(updateReqVO.getId());
        savePoints(updateReqVO.getId(), updateReqVO.getStationId(), updateReqVO.getPoints());
    }

    /**
     * 删除巡检线路。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLine(Long id) {
        validateLineExists(id);
        validateLineNotReferenced(id);
        linePointMapper.deleteByLineId(id);
        lineMapper.deleteById(id);
    }

    /**
     * 校验巡检线路未被计划或任务引用。
     */
    private void validateLineNotReferenced(Long lineId) {
        if (planMapper.countByLineId(lineId) > 0 || taskMapper.countByLineId(lineId) > 0) {
            throw exception(INSPECTION_LINE_DELETE_FORBIDDEN_REFERENCED);
        }
    }

    /**
     * 获取巡检线路详情。
     */
    @Override
    public IotInspectionLineDO getLine(Long id) {
        lineMapper.syncUseCount();
        return lineMapper.selectById(id);
    }

    /**
     * 校验巡检线路是否存在。
     */
    @Override
    public IotInspectionLineDO validateLineExists(Long id) {
        IotInspectionLineDO line = lineMapper.selectById(id);
        if (line == null) {
            throw exception(INSPECTION_LINE_NOT_EXISTS);
        }
        return line;
    }

    /**
     * 巡检线路分页查询。
     */
    @Override
    public PageResult<IotInspectionLineDO> getLinePage(IotInspectionLinePageReqVO pageReqVO) {
        lineMapper.syncUseCount();
        return lineMapper.selectPage(pageReqVO);
    }

    /**
     * 查询线路下点位列表。
     */
    @Override
    public List<IotInspectionLinePointDO> getLinePointList(Long lineId) {
        return linePointMapper.selectListByLineId(lineId);
    }

    /**
     * 查询可选设备点位列表。
     */
    @Override
    public List<IotInspectionLinePointOptionRespVO> listPointOptions(String stationId, String keyword, Integer limit) {
        String normalizedStationId = trimToNull(stationId);
        if (normalizedStationId == null) {
            return Collections.emptyList();
        }
        int validLimit = normalizeLimit(limit);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.id, d.device_name, d.nickname, d.station_id, d.longitude, d.latitude, ")
                .append("COALESCE(NULLIF(TRIM(dl.name), ''), d.address) AS address_display ")
                .append("FROM iot_device d ")
                .append("LEFT JOIN iot_device_location dl ON dl.id::text = TRIM(d.address) ")
                .append("WHERE d.deleted = 0 AND d.station_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(normalizedStationId);

        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword != null) {
            sql.append(" AND (d.nickname LIKE ? OR d.device_name LIKE ? OR d.serial_number LIKE ?")
                    .append(" OR d.address LIKE ? OR dl.name LIKE ?)");
            String fuzzyKeyword = "%" + normalizedKeyword + "%";
            args.add(fuzzyKeyword);
            args.add(fuzzyKeyword);
            args.add(fuzzyKeyword);
            args.add(fuzzyKeyword);
            args.add(fuzzyKeyword);
        }
        sql.append(" ORDER BY d.update_time DESC, d.id DESC LIMIT ?");
        args.add(validLimit);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Long deviceId = rs.getLong("id");
            String pointName = StrUtil.blankToDefault(rs.getString("nickname"),
                    StrUtil.blankToDefault(rs.getString("device_name"), "设备-" + deviceId));
            return new IotInspectionLinePointOptionRespVO(
                    deviceId,
                    pointName,
                    rs.getString("device_name"),
                    rs.getString("station_id"),
                    rs.getBigDecimal("longitude"),
                    rs.getBigDecimal("latitude"),
                    rs.getString("address_display"));
        }, args.toArray());
    }

    /**
     * 保存线路点位，并兜底填充排序。
     */
    private void savePoints(Long lineId, String stationId, List<IotInspectionLinePointSaveReqVO> points) {
        if (points == null || points.isEmpty()) {
            return;
        }
        for (int i = 0; i < points.size(); i++) {
            IotInspectionLinePointSaveReqVO pointReq = points.get(i);
            IotInspectionLinePointDO point = BeanUtils.toBean(pointReq, IotInspectionLinePointDO.class);
            point.setId(null);
            point.setLineId(lineId);
            point.setStationId(stationId);
            point.setPointSort(point.getPointSort() == null ? i + 1 : point.getPointSort());
            linePointMapper.insert(point);
        }
    }

    /**
     * 校验同站点下线路名称唯一。
     */
    private void validateLineNameUnique(Long id, String lineName) {
        if (StrUtil.isBlank(lineName)) {
            return;
        }
        IotInspectionLineDO line = lineMapper.selectByName(lineName);
        if (line == null) {
            return;
        }
        if (!Objects.equals(line.getId(), id)) {
            throw exception(INSPECTION_LINE_NAME_EXISTS);
        }
    }

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
     * 限制点位候选查询条数。
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_POINT_OPTION_LIMIT;
        }
        return Math.min(limit, MAX_POINT_OPTION_LIMIT);
    }

}
