package com.sydigit.yzwater.module.iot.service.openapi;

import java.util.List;
import java.util.Map;

/**
 * 幸福河湖 MySQL 查询服务。
 */
public interface XfhhMysqlQueryService {

    /**
     * 按旧脚本编码执行列表查询。
     */
    List<Map<String, Object>> list(String code, Map<String, Object> params);
}
