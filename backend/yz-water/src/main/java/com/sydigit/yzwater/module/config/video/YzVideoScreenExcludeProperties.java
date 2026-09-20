package com.sydigit.yzwater.module.config.video;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 大屏等业务中不展示、不参与统计的海康视频区域（按区域维度排除其下全部摄像头）。
 */
@Data
@ConfigurationProperties(prefix = "yz.video.screen")
public class YzVideoScreenExcludeProperties {

    /**
     * 海康区域 indexCode，与 {@link #excludedRegionNames} 解析结果合并后生效。
     */
    private List<String> excludedRegionIndexCodes = new ArrayList<>(List.of("bcae4eb36bd745dcb3e034bb6c8c16d5"));

    /**
     * 海康区域名称（与 yz_video_regions.name 精确匹配），解析为 indexCode 后与上项合并。
     */
    private List<String> excludedRegionNames = new ArrayList<>(List.of("录像存储"));
}
