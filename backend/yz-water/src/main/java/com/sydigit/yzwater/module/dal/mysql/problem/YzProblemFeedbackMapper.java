package com.sydigit.yzwater.module.dal.mysql.problem;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.problem.YzProblemFeedbackDO;
import com.sydigit.yzwater.module.dal.mysql.problem.dto.ProblemFeedbackStatusCountRow;
import com.sydigit.yzwater.module.dal.mysql.problem.dto.ProblemFeedbackTypeCountRow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 问题反馈表 Mapper
 */
@Mapper
public interface YzProblemFeedbackMapper extends BaseMapperX<YzProblemFeedbackDO> {

    @Select({
            "select feedback_type as feedbackType, count(1) as count",
            "from yz_problem_feedback",
            "where COALESCE(deleted, 0) = 0",
            "  and longitude is not null",
            "  and latitude is not null",
            "  and ST_DWithin(",
            "    ST_SetSRID(ST_MakePoint(longitude, latitude), 4490)::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "group by feedback_type"
    })
    List<ProblemFeedbackTypeCountRow> selectTypeCountInRange(@Param("longitude") BigDecimal longitude,
                                                             @Param("latitude") BigDecimal latitude,
                                                             @Param("radius") BigDecimal radius);

    @Select({
            "select status as status, count(1) as count",
            "from yz_problem_feedback",
            "where COALESCE(deleted, 0) = 0",
            "  and status is not null",
            "  and longitude is not null",
            "  and latitude is not null",
            "  and ST_DWithin(",
            "    ST_SetSRID(ST_MakePoint(longitude, latitude), 4490)::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "group by status"
    })
    List<ProblemFeedbackStatusCountRow> selectStatusCountInRange(@Param("longitude") BigDecimal longitude,
                                                                 @Param("latitude") BigDecimal latitude,
                                                                 @Param("radius") BigDecimal radius);

    // publicNoticeId 旧查询已废弃，保留注释便于追溯
    // @Select({
    //         "<script>",
    //         "select public_notice_id as signboard_id, count(1) as cnt",
    //         "from yz_problem_feedback",
    //         "where COALESCE(deleted, 0) = 0",
    //         "  and status not in (#{finishedStatus}, #{rejectedStatus})",
    //         "  and public_notice_id in",
    //         "  <foreach collection='signboardIds' item='id' open='(' separator=',' close=')'>",
    //         "    #{id}",
    //         "  </foreach>",
    //         "group by public_notice_id",
    //         "</script>"
    // })
    // List<Map<String, Object>> selectUnfinishedCountGroupBySignboardIds(@Param("signboardIds") Collection<Long> signboardIds,
    //                                                                    @Param("finishedStatus") int finishedStatus,
    //                                                                    @Param("rejectedStatus") int rejectedStatus);

    /**
     * 按关联对象分组统计未办结问题数量（以问题反馈表状态为准）。
     */
    @Select({
            "<script>",
            "select reference_id as reference_id, count(1) as cnt",
            "from yz_problem_feedback",
            "where COALESCE(deleted, 0) = 0",
            "  and status not in (#{finishedStatus}, #{rejectedStatus})",
            "  and reference_type = #{referenceType}",
            "  and reference_id in",
            "  <foreach collection='referenceIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "group by reference_id",
            "</script>"
    })
    List<Map<String, Object>> selectUnfinishedCountGroupByReferenceIds(@Param("referenceType") String referenceType,
                                                                       @Param("referenceIds") Collection<Long> referenceIds,
                                                                       @Param("finishedStatus") int finishedStatus,
                                                                       @Param("rejectedStatus") int rejectedStatus);

    default LambdaQueryWrapper<YzProblemFeedbackDO> buildQueryWrapper(ProblemFeedbackPageReqVO reqVO) {
        Integer status = reqVO.getStatus();
        // status 为空或小于 0 时表示“全部”，不做状态过滤
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .eq(status != null && status >= 0, YzProblemFeedbackDO::getStatus, status)
                .eq(StrUtil.isNotBlank(reqVO.getFeedbackType()), YzProblemFeedbackDO::getFeedbackType, reqVO.getFeedbackType())
                .orderByDesc(YzProblemFeedbackDO::getUpdateTime);
        LocalDate[] createTime = parseCreateDateRange(reqVO.getCreateTime());
        if (createTime != null && createTime.length == 2 && (createTime[0] != null || createTime[1] != null)) {
            // 入参 createTime 精确到“天”，这里统一转成当天起止范围（包含结束日）进行过滤
            LocalDate startDate = createTime[0];
            LocalDate endDate = createTime[1];
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("开始时间不能晚于结束时间");
                }
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
                wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDateTime)
                        .lt(YzProblemFeedbackDO::getCreateTime, endExclusive);
            } else if (startDate != null) {
                wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDate.atStartOfDay());
            } else if (endDate != null) {
                wrapper.lt(YzProblemFeedbackDO::getCreateTime, endDate.plusDays(1).atStartOfDay());
            }
        }
        return wrapper;
    }

    default LambdaQueryWrapper<YzProblemFeedbackDO> buildQueryWrapper(ProblemFeedbackListReqVO reqVO) {
        Integer status = reqVO.getStatus();
        // status 为空或小于 0 时表示“全部”，不做状态过滤
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .eq(status != null && status >= 0, YzProblemFeedbackDO::getStatus, status)
                .eq(StrUtil.isNotBlank(reqVO.getFeedbackType()), YzProblemFeedbackDO::getFeedbackType, reqVO.getFeedbackType())
                .orderByDesc(YzProblemFeedbackDO::getUpdateTime);
        LocalDate[] createTime = parseCreateDateRange(reqVO.getCreateTime());
        if (createTime != null && createTime.length == 2 && (createTime[0] != null || createTime[1] != null)) {
            // 入参 createTime 精确到“天”，这里统一转成当天起止范围（包含结束日）进行过滤
            LocalDate startDate = createTime[0];
            LocalDate endDate = createTime[1];
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("开始时间不能晚于结束时间");
                }
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
                wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDateTime)
                        .lt(YzProblemFeedbackDO::getCreateTime, endExclusive);
            } else if (startDate != null) {
                wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDate.atStartOfDay());
            } else if (endDate != null) {
                wrapper.lt(YzProblemFeedbackDO::getCreateTime, endDate.plusDays(1).atStartOfDay());
            }
        }
        return wrapper;
    }

    /**
     * 解析按天范围参数，兼容以下传参方式：
     * <ul>
     *     <li>createTime=2025-12-02&createTime=2025-12-03</li>
     *     <li>createTime[0]=2025-12-02&createTime[1]=2025-12-03</li>
     *     <li>createTime[]=2025-12-02&createTime[]=2025-12-03</li>
     *     <li>createTime=[2025-12-02,2025-12-03]</li>
     * </ul>
     *
     * @param rawCreateTime 原始入参
     * @return 固定长度为 2 的日期数组（开始、结束），元素允许为空
     */
    private LocalDate[] parseCreateDateRange(String[] rawCreateTime) {
        if (rawCreateTime == null || rawCreateTime.length == 0) {
            return null;
        }

        List<String> candidates = new ArrayList<>();
        for (String item : rawCreateTime) {
            if (StrUtil.isBlank(item)) {
                continue;
            }
            String value = StrUtil.trim(item);
            // 兼容：createTime=[2025-12-02,2025-12-03] 或 createTime=["2025-12-02","2025-12-03"]
            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1);
            }
            value = value.replace("\"", "");
            if (value.contains(",")) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    if (StrUtil.isNotBlank(part)) {
                        candidates.add(StrUtil.trim(part));
                    }
                }
            } else {
                candidates.add(value);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        LocalDate start = null;
        LocalDate end = null;
        if (candidates.size() >= 1) {
            start = parseDateOrNull(candidates.get(0));
        }
        if (candidates.size() >= 2) {
            end = parseDateOrNull(candidates.get(1));
        }
        return new LocalDate[]{start, end};
    }

    private LocalDate parseDateOrNull(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String value = StrUtil.trim(text);
        // 仅取日期部分，避免前端误传 2025-12-02 00:00:00 导致解析失败
        if (value.length() > 10) {
            value = value.substring(0, 10);
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("创建时间格式不正确，要求 yyyy-MM-dd，例如 2025-12-02", ex);
        }
    }
}
