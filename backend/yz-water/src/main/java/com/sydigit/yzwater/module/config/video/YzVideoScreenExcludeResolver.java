package com.sydigit.yzwater.module.config.video;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoRegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 解析大屏等场景需排除的海康视频区域 indexCode 集合（配置编码 + 按名称查库）。
 */
@Component
@RequiredArgsConstructor
public class YzVideoScreenExcludeResolver {

    private final YzVideoScreenExcludeProperties properties;
    private final YzVideoRegionMapper regionMapper;

    /**
     * 合并配置的 indexCode 与按名称匹配到的区域 indexCode，去重后返回只读集合。
     */
    public Set<String> excludedRegionIndexCodes() {
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        if (CollUtil.isNotEmpty(properties.getExcludedRegionIndexCodes())) {
            for (String c : properties.getExcludedRegionIndexCodes()) {
                if (StrUtil.isNotBlank(c)) {
                    codes.add(StrUtil.trim(c));
                }
            }
        }
        List<String> names = CollUtil.emptyIfNull(properties.getExcludedRegionNames()).stream()
                .map(StrUtil::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(names)) {
            List<YzVideoRegionDO> matched = regionMapper.selectList(new LambdaQueryWrapper<YzVideoRegionDO>()
                    .select(YzVideoRegionDO::getIndexCode)
                    .in(YzVideoRegionDO::getName, names));
            for (YzVideoRegionDO region : matched) {
                if (StrUtil.isNotBlank(region.getIndexCode())) {
                    codes.add(StrUtil.trim(region.getIndexCode()));
                }
            }
        }
        return Collections.unmodifiableSet(codes);
    }
}
