package com.sydigit.yzwater.module.dal.mysql.rivers;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 公示牌信息 Mapper
 */
@Mapper
public interface YzSignboardMapper extends BaseMapperX<YzSignboardDO> {

    default LambdaQueryWrapper<YzSignboardDO> buildQueryWrapper(SignboardPageReqVO reqVO) {
        LambdaQueryWrapper<YzSignboardDO> wrapper = new LambdaQueryWrapper<YzSignboardDO>()
                .like(StrUtil.isNotBlank(reqVO.getSignboardName()),
                        YzSignboardDO::getSignboardName, reqVO.getSignboardName())
                .like(StrUtil.isNotBlank(reqVO.getSignboardCode()),
                        YzSignboardDO::getSignboardCode, reqVO.getSignboardCode())
                .in(reqVO.getSignboardLevel() != null && !reqVO.getSignboardLevel().isEmpty(),
                        YzSignboardDO::getSignboardLevel, reqVO.getSignboardLevel())
                .eq(reqVO.getIsScreenDisplay() != null,
                        YzSignboardDO::getIsScreenDisplay, reqVO.getIsScreenDisplay())
                .orderByDesc(YzSignboardDO::getUpdateTime, YzSignboardDO::getCreateTime);

        if (StrUtil.isNotBlank(reqVO.getMaintenanceUnit())) {
            // 维护单位改为数组后，这里将数组拼接成字符串再做模糊匹配
            wrapper.apply("array_to_string(maintenance_unit, ',') like {0}", "%" + reqVO.getMaintenanceUnit() + "%");
        }

        if (StrUtil.isNotBlank(reqVO.getRiverKeyword())) {
            wrapper.and(w -> w.like(YzSignboardDO::getRiverChannelName, reqVO.getRiverKeyword())
                    .or()
                    .like(YzSignboardDO::getRiverSectionName, reqVO.getRiverKeyword()));
        }
        return wrapper;
    }

    /**
     * 按行政区划（admin_region）分组统计公示牌数量。
     *
     * <p>兼容 admin_region 为单值（varchar）或包含分隔符的多值文本（提取其中的数字 ID）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select regexp_split_to_table(admin_region, '[^0-9]+') as area_code",
            "  from yz_signboard",
            "  where admin_region is not null",
            "    and btrim(admin_region) <> ''",
            "    and COALESCE(deleted, 0) = 0",
            ") t",
            "where t.area_code is not null and btrim(t.area_code) <> ''",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectAdminRegionCountGroup();

    /**
     * 按公示牌等级统计公示牌数量。
     */
    @Select({
            "select signboard_level as signboard_level, count(1) as cnt",
            "from yz_signboard",
            "where signboard_level is not null",
            "  and btrim(signboard_level) <> ''",
            "  and COALESCE(deleted, 0) = 0",
            "group by signboard_level"
    })
    List<Map<String, Object>> selectSignboardLevelCount();

    /**
     * 按行政区划编码集合查询公示牌列表（包含子级匹配）。
     *
     * <p>兼容 admin_region 为单值（varchar）或包含分隔符的多值文本（提取其中的数字 ID）。</p>
     */
    @Select({
            "select",
            "  id,",
            "  signboard_name,",
            "  signboard_type,",
            "  reference_type,",
            "  reference_id,",
            "  longitude,",
            "  latitude,",
            "  specific_location,",
            "  maintenance_unit",
            "from yz_signboard",
            "where admin_region is not null",
            "  and btrim(admin_region) <> ''",
            "  and COALESCE(deleted, 0) = 0",
            "  and exists (",
            "    select 1",
            "    from regexp_split_to_table(admin_region, '[^0-9]+') as r(area_code)",
            "    where r.area_code is not null",
            "      and btrim(r.area_code) <> ''",
            "      and r.area_code = any(#{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler})",
            "  )",
            "order by signboard_name asc, create_time desc"
    })
    List<YzSignboardDO> selectListByAreaCodes(@Param("areaCodes") String[] areaCodes);
}
