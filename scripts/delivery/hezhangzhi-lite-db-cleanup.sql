-- 河长制交付版：清理现有库（菜单 / 定时任务 / 短信）
-- 在 yzwater_isolated 上执行；可重复执行（幂等）。

BEGIN;

-- ========== 1. 补齐保留菜单的 component ==========
UPDATE system_menu SET component = 'yz/headLevel/riverChiefHistory', update_time = NOW()
WHERE id = 2006239023273791490 AND deleted = 0;

UPDATE system_menu SET component = 'yz/problem/problemFeedback', update_time = NOW()
WHERE id = 2001917010387972097 AND deleted = 0;

UPDATE system_menu SET component = 'yz/problem/problemStatistics', update_time = NOW()
WHERE id = 2006238396573470721 AND deleted = 0;

-- ========== 2. 逻辑删除不交付菜单（整棵子树）==========
-- 顶层整组：基础设施、河长管理、泵站设备、报表、备件、故障、防汛、养护、数据查询、技术资料、巡检、调度、工作流
WITH RECURSIVE doomed AS (
  SELECT id FROM system_menu
  WHERE deleted = 0 AND id IN (
    1997973786857152514, -- 基础设施
    2006207100975448065, -- 河长管理
    4000,                -- 泵站设备
    1281,                -- 报表管理
    4060,                -- 库存备件
    4080,                -- 故障维修
    2013131574932848642, -- 防汛抗旱
    4090,                -- 养护计划
    202603191001,        -- 数据查询
    4120,                -- 技术资料库
    4200,                -- 巡检管理
    2021826773735881721, -- 调度管理
    1185,                -- 工作流程
    -- 河长制下删除项
    2052329968570077185, -- 正式公示牌管理
    2052946724376416258, -- 提防管理
    2052947049724383233, -- 泵站管理
    2052947684934946817, -- 灌区管理
    -- 系统管理下删除项
    1224,                -- 租户管理
    108,                 -- 审计日志
    1261,                -- OAuth 2.0
    2447,                -- 三方登录
    2130,                -- 邮箱管理
    2144,                -- 站内信管理
    107,                 -- 通知公告
    -- 系统监控下删除项（保留文件管理、配置管理）
    115,                 -- 代码生成
    1070,                -- 代码生成案例
    1255,                -- 数据源配置
    114,                 -- 表单构建
    116,                 -- API 接口
    1083,                -- API 日志
    2525,                -- WebSocket
    110,                 -- 定时任务
    2740                 -- 监控中心
  )
  UNION ALL
  SELECT c.id FROM system_menu c
  JOIN doomed d ON c.parent_id = d.id
  WHERE c.deleted = 0
)
UPDATE system_menu m
SET deleted = 1, update_time = NOW()
FROM doomed
WHERE m.id = doomed.id AND m.deleted = 0;

-- ========== 3. 停用并逻辑删除无效定时任务 ==========
UPDATE infra_job
SET status = 2, deleted = 1, update_time = NOW()
WHERE deleted = 0
  AND handler_name IN (
    'yzWeatherCollectJob',
    'yzWeatherStationCollectJob',
    'yzWeatherFyCollectJob',
    'iotRealtimeDataPullJob',
    'xfhhVideoSyncJob'
  );

-- ========== 4. 短信：控制台渠道 + 清空真实密钥 ==========
-- 字典增加 DEBUG_CONSOLE
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 202609210001, 6, '调试(控制台日志)', 'DEBUG_CONSOLE', 'system_sms_channel_code', 0, 'info', '', '交付模拟短信，只打日志', '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'system_sms_channel_code' AND value = 'DEBUG_CONSOLE' AND deleted = 0
);

-- 控制台短信渠道（启用）
INSERT INTO system_sms_channel (id, signature, code, status, remark, api_key, api_secret, callback_url, creator, create_time, updater, update_time, deleted)
SELECT 202609210002, '交付控制台', 'DEBUG_CONSOLE', 0, '验证码与通知只打后端日志，不发真实短信', 'console', 'console', NULL, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (
  SELECT 1 FROM system_sms_channel WHERE id = 202609210002
);

UPDATE system_sms_channel
SET signature = '交付控制台',
    code = 'DEBUG_CONSOLE',
    status = 0,
    remark = '验证码与通知只打后端日志，不发真实短信',
    api_key = 'console',
    api_secret = 'console',
    deleted = 0,
    update_time = NOW()
WHERE id = 202609210002;

-- 所有模板改挂控制台渠道
UPDATE system_sms_template
SET channel_id = 202609210002,
    channel_code = 'DEBUG_CONSOLE',
    update_time = NOW()
WHERE deleted = 0;

-- 清空并禁用其它真实/调试钉钉渠道
UPDATE system_sms_channel
SET status = 1,
    api_key = '',
    api_secret = '',
    update_time = NOW()
WHERE deleted = 0
  AND id <> 202609210002;

-- 交付样例：若水库表为空，补 1 座水库并挂到已有公示牌 id=39（月塘水库公示牌）
INSERT INTO yz_water_facility_base_bf (
  id, facility_code, facility_name, facility_type, status, creator, create_time, updater, update_time, deleted, source_type, srid
) SELECT 2101942000000000001, 'DEMO-RES-001', '月塘水库(交付样例)', 'reservoir', 'normal', '1', NOW(), '1', NOW(), 0, 'system', 4490
WHERE NOT EXISTS (SELECT 1 FROM yz_water_facility_base_bf WHERE id = 2101942000000000001);

INSERT INTO yz_water_facility_base (
  id, facility_code, facility_name, facility_type, status, creator, create_time, updater, update_time, deleted, source_type, srid
) SELECT 2101942000000000001, 'DEMO-RES-001', '月塘水库(交付样例)', 'reservoir', 'normal', '1', NOW(), '1', NOW(), 0, 'system', 4490
WHERE NOT EXISTS (SELECT 1 FROM yz_water_facility_base WHERE id = 2101942000000000001);

INSERT INTO yz_water_reservoir_bf (
  id, facility_id, reservoir_code, reservoir_name, creator, create_time, updater, update_time, deleted
) SELECT 2001237187037638657, 2101942000000000001, 'DEMO-RES-001', '月塘水库(交付样例)', '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM yz_water_reservoir_bf WHERE id = 2001237187037638657);

INSERT INTO yz_water_reservoir (
  id, facility_id, reservoir_code, reservoir_name, creator, create_time, updater, update_time, deleted
) SELECT 2001237187037638657, 2101942000000000001, 'DEMO-RES-001', '月塘水库(交付样例)', '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM yz_water_reservoir WHERE id = 2001237187037638657);



-- 河长办 BF：对齐河道 facility_id，并同步主表几何，保证手机端 buffer-query 可检索
UPDATE yz_river_channel_bf bf
SET facility_id = m.facility_id,
    update_time = NOW()
FROM yz_river_channel m
WHERE m.id = bf.id
  AND m.facility_id IS NOT NULL
  AND (bf.facility_id IS DISTINCT FROM m.facility_id);

INSERT INTO yz_water_facility_base_bf (
  id, facility_code, facility_name, facility_type, status, admin_region_code,
  admin_region, manage_unit, attributes, basin_code, safety_level, design_standard,
  geom, geom_type, srid, source_type, creator, create_time, updater, update_time, deleted
)
SELECT
  b.id, b.facility_code, b.facility_name, b.facility_type, b.status, b.admin_region_code,
  b.admin_region, b.manage_unit, b.attributes, b.basin_code, b.safety_level, b.design_standard,
  b.geom, b.geom_type, b.srid, b.source_type, b.creator, b.create_time, b.updater, NOW(), COALESCE(b.deleted, 0)
FROM yz_water_facility_base b
WHERE b.id IN (
  SELECT facility_id FROM yz_river_channel_bf WHERE facility_id IS NOT NULL
  UNION
  SELECT facility_id FROM yz_water_reservoir_bf WHERE facility_id IS NOT NULL
)
  AND NOT EXISTS (SELECT 1 FROM yz_water_facility_base_bf x WHERE x.id = b.id);

UPDATE yz_water_facility_base_bf bf
SET geom = b.geom,
    geom_type = b.geom_type,
    srid = COALESCE(b.srid, 4490),
    update_time = NOW(),
    deleted = 0
FROM yz_water_facility_base b
WHERE b.id = bf.id
  AND b.geom IS NOT NULL
  AND bf.geom IS NULL;

UPDATE yz_river_section_bf s
SET start_longitude = m.start_longitude,
    start_latitude = m.start_latitude,
    end_longitude = m.end_longitude,
    end_latitude = m.end_latitude,
    update_time = NOW()
FROM yz_river_section m
WHERE m.id = s.id
  AND m.start_longitude IS NOT NULL
  AND (s.start_longitude IS NULL OR s.end_longitude IS NULL);

UPDATE yz_water_reservoir_bf r
SET longitude = m.longitude,
    latitude = m.latitude,
    update_time = NOW()
FROM yz_water_reservoir m
WHERE m.id = r.id
  AND (r.longitude IS NULL OR r.latitude IS NULL);

-- 文件存储：交付/本地联调默认用数据库存储，避免依赖内网 MinIO
UPDATE infra_file_config SET master = false, update_time = NOW() WHERE master = true AND id <> 4;
UPDATE infra_file_config
SET deleted = 0,
    master = true,
    config = '{"@class":"com.sydigit.yzwater.module.infra.framework.file.core.client.db.DBFileClientConfig","domain":"http://127.0.0.1:48082"}',
    update_time = NOW()
WHERE id = 4;

COMMIT;
