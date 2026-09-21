package com.sydigit.yzwater.module.dal.mysql.problem;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.problem.YzProblemStatusTaskDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 问题状态表 Mapper
 */
@Mapper
public interface YzProblemStatusTaskMapper extends BaseMapperX<YzProblemStatusTaskDO> {

    // publicNoticeId 旧查询已废弃，保留注释便于追溯
    // @Select({
    //         "<script>",
    //         "select pf.public_notice_id as signboard_id, count(distinct t.problem_feedback) as cnt",
    //         "from yz_problem_status_task t",
    //         "join yz_problem_feedback pf on pf.id = t.problem_feedback",
    //         "where t.status != #{finishedStatus}",
    //         "  and pf.public_notice_id in",
    //         "  <foreach collection='signboardIds' item='id' open='(' separator=',' close=')'>",
    //         "    #{id}",
    //         "  </foreach>",
    //         "group by pf.public_notice_id",
    //         "</script>"
    // })
    // List<Map<String, Object>> selectUnfinishedCountGroupBySignboardIds(@Param("signboardIds") Collection<Long> signboardIds,
    //                                                                   @Param("finishedStatus") int finishedStatus);

    /**
     * 按关联对象分组统计未办结问题数量（以问题状态任务表为准：status != finishedStatus）。
     */
    @Select({
            "<script>",
            "select pf.reference_id as reference_id, count(distinct t.problem_feedback) as cnt",
            "from yz_problem_status_task t",
            "join yz_problem_feedback pf on pf.id = t.problem_feedback",
            "where t.status != #{finishedStatus}",
            "  and pf.reference_type = #{referenceType}",
            "  and pf.reference_id in",
            "  <foreach collection='referenceIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "group by pf.reference_id",
            "</script>"
    })
    List<Map<String, Object>> selectUnfinishedCountGroupByReferenceIds(@Param("referenceType") String referenceType,
                                                                      @Param("referenceIds") Collection<Long> referenceIds,
                                                                      @Param("finishedStatus") int finishedStatus);
}
