package com.sydigit.yzwater.module.iot.enums;

import com.sydigit.yzwater.framework.common.exception.ErrorCode;

/**
 * iot 错误码枚举类
 * <p>
 * iot 系统，使用 1-050-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 产品相关 1-050-001-000 ============
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_001_000, "产品不存在");
    ErrorCode PRODUCT_KEY_EXISTS = new ErrorCode(1_050_001_001, "产品标识已经存在");
    ErrorCode PRODUCT_STATUS_NOT_DELETE = new ErrorCode(1_050_001_002, "产品状是发布状态，不允许删除");
    ErrorCode PRODUCT_STATUS_NOT_ALLOW_THING_MODEL = new ErrorCode(1_050_001_003, "产品状是发布状态，不允许操作物模型");
    ErrorCode PRODUCT_DELETE_FAIL_HAS_DEVICE = new ErrorCode(1_050_001_004, "产品下存在设备，不允许删除");

    // ========== 产品物模型 1-050-002-000 ============
    ErrorCode THING_MODEL_NOT_EXISTS = new ErrorCode(1_050_002_000, "产品物模型不存在");
    ErrorCode THING_MODEL_EXISTS_BY_PRODUCT_KEY = new ErrorCode(1_050_002_001, "ProductKey 对应的产品物模型已存在");
    ErrorCode THING_MODEL_IDENTIFIER_EXISTS = new ErrorCode(1_050_002_002, "存在重复的功能标识符。");
    ErrorCode THING_MODEL_NAME_EXISTS = new ErrorCode(1_050_002_003, "存在重复的功能名称。");
    ErrorCode THING_MODEL_IDENTIFIER_INVALID = new ErrorCode(1_050_002_003, "产品物模型标识无效");

    // ========== 设备 1-050-003-000 ============
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_050_003_000, "设备不存在");
    ErrorCode DEVICE_NAME_EXISTS = new ErrorCode(1_050_003_001, "设备名称在同一产品下必须唯一");
    ErrorCode DEVICE_HAS_CHILDREN = new ErrorCode(1_050_003_002, "有子设备，不允许删除");
    ErrorCode DEVICE_KEY_EXISTS = new ErrorCode(1_050_003_003, "设备标识已经存在");
    ErrorCode DEVICE_GATEWAY_NOT_EXISTS = new ErrorCode(1_050_003_004, "网关设备不存在");
    ErrorCode DEVICE_NOT_GATEWAY = new ErrorCode(1_050_003_005, "设备不是网关设备");
    ErrorCode DEVICE_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_050_003_006, "导入设备数据不能为空！");
    ErrorCode DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL = new ErrorCode(1_050_003_007, "下行设备消息失败，原因：设备未连接网关");
    ErrorCode DEVICE_SERIAL_NUMBER_EXISTS = new ErrorCode(1_050_003_008, "设备序列号已存在，序列号必须全局唯一");
    ErrorCode DEVICE_PROPERTY_TAG_PROPERTY_EMPTY = new ErrorCode(1_050_003_009, "设备物模型属性不能为空");
    ErrorCode DEVICE_PROPERTY_TAG_COUNT_MISMATCH = new ErrorCode(1_050_003_010, "标签名称数量必须与设备物模型属性一致");
    ErrorCode DEVICE_PROPERTY_TAG_IDENTIFIER_INVALID = new ErrorCode(1_050_003_011, "标签标识符与设备物模型属性不匹配");
    ErrorCode DEVICE_PROPERTY_TAG_NAME_EMPTY = new ErrorCode(1_050_003_012, "标签名称不能为空");
    ErrorCode DEVICE_PROPERTY_TAG_NAME_DUPLICATE = new ErrorCode(1_050_003_013, "标签名称在设备内必须唯一");
    ErrorCode DEVICE_HAS_RELATION_DATA = new ErrorCode(1_050_003_014, "存在关联数据，无法删除");
    ErrorCode DEVICE_DELETE_ONLY_ADMIN = new ErrorCode(1_050_003_015, "仅管理员可删除");
    ErrorCode DEVICE_QR_CODE_EXISTS = new ErrorCode(1_050_003_017, "设备二维码已存在");
    ErrorCode DEVICE_QR_CODE_GENERATE_FAIL = new ErrorCode(1_050_003_018, "设备二维码生成失败");
    ErrorCode DEVICE_QR_CODE_NOT_ALLOW_UPDATE = new ErrorCode(1_050_003_019, "设备二维码生成后不允许修改");
    ErrorCode DEVICE_GENESIS_CONFIG_NOT_EXISTS = new ErrorCode(1_050_003_020, "GENESIS64 采集配置不存在");
    ErrorCode DEVICE_GENESIS_POINT_NOT_EXISTS = new ErrorCode(1_050_003_021, "GENESIS64 点位配置不存在");
    ErrorCode DEVICE_GENESIS_POINT_EXISTS = new ErrorCode(1_050_003_022, "GENESIS64 点位配置已存在");
    ErrorCode DEVICE_MQTT_CONFIG_NOT_EXISTS = new ErrorCode(1_050_003_023, "MQTT 采集配置不存在");
    ErrorCode DEVICE_MQTT_MAPPING_NOT_EXISTS = new ErrorCode(1_050_003_024, "MQTT 属性映射不存在");
    ErrorCode DEVICE_MQTT_MAPPING_EXISTS = new ErrorCode(1_050_003_025, "MQTT 属性映射已存在");
    // ========== 产品分类 1-050-004-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_050_004_000, "产品分类不存在");

    // ========== 设备分组 1-050-005-000 ==========
    ErrorCode DEVICE_GROUP_NOT_EXISTS = new ErrorCode(1_050_005_000, "设备分组不存在");
    ErrorCode DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS = new ErrorCode(1_050_005_001, "设备分组下存在设备，不允许删除");

    // ========== 设备位置 1-050-006-000 ==========
    ErrorCode DEVICE_LOCATION_NOT_EXISTS = new ErrorCode(1_050_006_000, "设备位置不存在");
    ErrorCode DEVICE_LOCATION_PARENT_NOT_EXISTS = new ErrorCode(1_050_006_001, "父级位置不存在");
    ErrorCode DEVICE_LOCATION_PARENT_ERROR = new ErrorCode(1_050_006_002, "父级位置不合法");
    ErrorCode DEVICE_LOCATION_EXISTS_CHILDREN = new ErrorCode(1_050_006_003, "存在下级位置，无法删除");

    // ========== 备件库存 1-050-016-000 ==========
    ErrorCode SPARE_NOT_EXISTS = new ErrorCode(1_050_016_000, "备件不存在");
    ErrorCode SPARE_DELETE_FAIL_HAS_IO = new ErrorCode(1_050_016_001, "备件存在出入库记录，无法删除");
    ErrorCode SPARE_DELETE_FAIL_HAS_CHECK = new ErrorCode(1_050_016_002, "备件存在盘点记录，无法删除");
    ErrorCode SPARE_IO_NOT_EXISTS = new ErrorCode(1_050_016_010, "出入库记录不存在");
    ErrorCode SPARE_IO_STATUS_NOT_PENDING = new ErrorCode(1_050_016_011, "出入库记录已审批，无法修改或删除");
    ErrorCode SPARE_IO_STOCK_NOT_ENOUGH = new ErrorCode(1_050_016_012, "库存不足，无法出库");
    ErrorCode SPARE_IO_AUDIT_STATUS_NOT_PENDING = new ErrorCode(1_050_016_013, "出入库记录不是待审批状态");
    ErrorCode SPARE_IO_REVERSE_STOCK_NOT_ENOUGH = new ErrorCode(1_050_016_014, "库存不足，无法反审批");
    ErrorCode SPARE_IO_AUDIT_STATUS_NOT_APPROVED = new ErrorCode(1_050_016_015, "出入库记录不是已通过状态");
    ErrorCode SPARE_CHECK_NOT_EXISTS = new ErrorCode(1_050_016_020, "盘点记录不存在");
    ErrorCode SPARE_CHECK_ALREADY_APPLIED = new ErrorCode(1_050_016_021, "盘点结果已审批");
    ErrorCode SPARE_CHECK_NOT_APPLIED = new ErrorCode(1_050_016_022, "盘点结果未审批");

    // ========== 故障维修 1-050-017-000 ==========
    ErrorCode FAULT_REPAIR_NOT_EXISTS = new ErrorCode(1_050_017_000, "故障维修工单不存在");
    ErrorCode FAULT_REPAIR_STATUS_NOT_ASSIGNABLE = new ErrorCode(1_050_017_001, "当前工单状态不允许派工");
    ErrorCode FAULT_REPAIR_STATUS_NOT_FEEDBACKABLE = new ErrorCode(1_050_017_002, "当前工单状态不允许反馈");
    ErrorCode FAULT_REPAIR_DELETE_FAIL_HAS_SPARE_IO = new ErrorCode(1_050_017_003, "工单已关联备件出库记录，无法删除");

    // ========== 养护计划 1-050-018-000 ==========
    ErrorCode MAINTENANCE_PLAN_NOT_EXISTS = new ErrorCode(1_050_018_000, "养护计划不存在");
    ErrorCode MAINTENANCE_PLAN_ALREADY_COMPLETED = new ErrorCode(1_050_018_001, "养护计划已完成，无法再次提交");
    ErrorCode MAINTENANCE_PLAN_ALREADY_EXISTS = new ErrorCode(1_050_018_002, "该设备当天已存在养护计划");

    // ========== 技术资料库 1-050-019-000 ==========
    ErrorCode DEVICE_DOC_NOT_EXISTS = new ErrorCode(1_050_019_000, "技术资料不存在");

    // ========== 设备评级 1-050-020-000 ==========
    ErrorCode DEVICE_RATING_NOT_EXISTS = new ErrorCode(1_050_020_000, "设备评级不存在");
    ErrorCode DEVICE_RATING_RECTIFY_REQUIRED = new ErrorCode(1_050_020_001, "不合格评级需填写整改建议和整改期限");
    ErrorCode DEVICE_RATING_ATTACHMENT_REQUIRED = new ErrorCode(1_050_020_002, "不合格评级需上传附件");

    // ========== 设备事故 1-050-021-000 ==========
    ErrorCode DEVICE_ACCIDENT_NOT_EXISTS = new ErrorCode(1_050_021_000, "设备事故不存在");

    // ========== OTA 固件相关 1-050-008-000 ==========

    ErrorCode OTA_FIRMWARE_NOT_EXISTS = new ErrorCode(1_050_008_000, "固件信息不存在");
    ErrorCode OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE = new ErrorCode(1_050_008_001, "产品版本号重复");

    // ========== OTA 升级任务相关 1-050-008-100 ==========

    ErrorCode OTA_TASK_NOT_EXISTS = new ErrorCode(1_050_008_100, "升级任务不存在");
    ErrorCode OTA_TASK_CREATE_FAIL_NAME_DUPLICATE = new ErrorCode(1_050_008_101, "创建 OTA 任务失败，原因：任务名称重复");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_FIRMWARE_EXISTS = new ErrorCode(1_050_008_102,
            "创建 OTA 任务失败，原因：设备({})已经是该固件版本");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_OTA_IN_PROCESS = new ErrorCode(1_050_008_102,
            "创建 OTA 任务失败，原因：设备({})已经在升级中...");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_EMPTY = new ErrorCode(1_050_008_103, "创建 OTA 任务失败，原因：没有可升级的设备");
    ErrorCode OTA_TASK_CANCEL_FAIL_STATUS_END = new ErrorCode(1_050_008_104, "取消 OTA 任务失败，原因：任务状态不是进行中");

    // ========== OTA 升级任务记录相关 1-050-008-200 ==========

    ErrorCode OTA_TASK_RECORD_NOT_EXISTS = new ErrorCode(1_050_008_200, "升级记录不存在");
    ErrorCode OTA_TASK_RECORD_CANCEL_FAIL_STATUS_ERROR = new ErrorCode(1_050_008_201, "取消 OTA 升级记录失败，原因：记录状态不是进行中");
    ErrorCode OTA_TASK_RECORD_UPDATE_PROGRESS_FAIL_NO_EXISTS = new ErrorCode(1_050_008_202, "更新 OTA 升级记录进度失败，原因：该设备没有进行中的升级记录");

    // ========== IoT 数据流转规则 1-050-010-000 ==========
    ErrorCode DATA_RULE_NOT_EXISTS = new ErrorCode(1_050_010_000, "数据流转规则不存在");

    // ========== IoT 数据流转目的 1-050-011-000 ==========
    ErrorCode DATA_SINK_NOT_EXISTS = new ErrorCode(1_050_011_000, "数据桥梁不存在");
    ErrorCode DATA_SINK_DELETE_FAIL_USED_BY_RULE = new ErrorCode(1_050_011_001, "数据流转目的正在被数据流转规则使用，无法删除");

    // ========== IoT 场景联动 1-050-012-000 ==========
    ErrorCode RULE_SCENE_NOT_EXISTS = new ErrorCode(1_050_012_000, "场景联动不存在");

    // ========== IoT 告警配置 1-050-013-000 ==========
    ErrorCode ALERT_CONFIG_NOT_EXISTS = new ErrorCode(1_050_013_000, "IoT 告警配置不存在");

    // ========== IoT 告警记录 1-050-014-000 ==========
    ErrorCode ALERT_RECORD_NOT_EXISTS = new ErrorCode(1_050_014_000, "IoT 告警记录不存在");

    // ========== IoT 实时数据 1-050-015-000 ==========
    ErrorCode REALTIME_DATA_SOURCE_NOT_EXISTS = new ErrorCode(1_050_015_000, "实时数据采集源不存在");
    ErrorCode REALTIME_DATA_SOURCE_CODE_EXISTS = new ErrorCode(1_050_015_001, "实时数据采集源编码已存在");
    ErrorCode REALTIME_DATA_MAPPING_NOT_EXISTS = new ErrorCode(1_050_015_010, "实时数据点位映射不存在");
    ErrorCode REALTIME_DATA_MAPPING_POINT_EXISTS = new ErrorCode(1_050_015_011, "实时数据点位在当前采集源下已存在");
    ErrorCode REALTIME_DATA_IMPORT_CONFIG_INVALID = new ErrorCode(1_050_015_020, "实时数据配置不完整");
    ErrorCode REALTIME_DATA_IMPORT_MAPPING_EMPTY = new ErrorCode(1_050_015_021, "实时数据点位配置为空");
    ErrorCode REALTIME_DATA_IMPORT_TENANT_MISMATCH = new ErrorCode(1_050_015_022, "实时数据点位配置存在多个租户，无法导入");
    ErrorCode REALTIME_DATA_IMPORT_MAPPING_INVALID = new ErrorCode(1_050_015_023, "实时数据点位配置不完整，pointName={}");
    ErrorCode MQTT_SOURCE_NOT_EXISTS = new ErrorCode(1_050_015_030, "MQTT 数据源不存在");
    ErrorCode MQTT_SOURCE_CODE_EXISTS = new ErrorCode(1_050_015_031, "MQTT 数据源编码已存在");


    // ========== 巡检标准 1-050-022-000 ==========
    ErrorCode INSPECTION_STANDARD_NOT_EXISTS = new ErrorCode(1_050_022_000, "巡检标准不存在");
    ErrorCode INSPECTION_STANDARD_NAME_EXISTS = new ErrorCode(1_050_022_001, "巡检标准名称已存在");
    ErrorCode INSPECTION_STANDARD_TARGET_NOT_EXISTS = new ErrorCode(1_050_022_002, "巡检标准适用对象不存在");
    ErrorCode INSPECTION_STANDARD_TARGET_TYPE_INVALID = new ErrorCode(1_050_022_003, "巡检标准适用对象类型不合法");
    ErrorCode INSPECTION_STANDARD_DELETE_FORBIDDEN_REFERENCED = new ErrorCode(1_050_022_004, "\u5de1\u68c0\u6807\u51c6\u5df2\u88ab\u8ba1\u5212\u6216\u4efb\u52a1\u5f15\u7528\uff0c\u65e0\u6cd5\u5220\u9664");

    // ========== 巡检线路 1-050-023-000 ==========
    ErrorCode INSPECTION_LINE_NOT_EXISTS = new ErrorCode(1_050_023_000, "巡检线路不存在");
    ErrorCode INSPECTION_LINE_NAME_EXISTS = new ErrorCode(1_050_023_001, "巡检线路名称已存在");
    ErrorCode INSPECTION_LINE_DELETE_FORBIDDEN_REFERENCED = new ErrorCode(1_050_023_002, "\u5de1\u68c0\u7ebf\u8def\u5df2\u88ab\u8ba1\u5212\u6216\u4efb\u52a1\u5f15\u7528\uff0c\u65e0\u6cd5\u5220\u9664");

    // ========== 巡检计划 1-050-024-000 ==========
    ErrorCode INSPECTION_PLAN_NOT_EXISTS = new ErrorCode(1_050_024_000, "巡检计划不存在");
    ErrorCode INSPECTION_PLAN_NAME_EXISTS = new ErrorCode(1_050_024_001, "巡检计划名称已存在");
    ErrorCode INSPECTION_PLAN_TARGET_EMPTY = new ErrorCode(1_050_024_002, "巡检对象不能为空");
    ErrorCode INSPECTION_PLAN_TARGET_NOT_EXISTS = new ErrorCode(1_050_024_003, "巡检对象不存在");
    ErrorCode INSPECTION_PLAN_OBJECT_TYPE_INVALID = new ErrorCode(1_050_024_004, "巡检对象类型不合法");
    ErrorCode INSPECTION_PLAN_STANDARD_NOT_EXISTS = new ErrorCode(1_050_024_005, "巡检标准不存在");
    ErrorCode INSPECTION_PLAN_CYCLE_UNIT_INVALID = new ErrorCode(1_050_024_006, "计划周期不合法");
    ErrorCode INSPECTION_PLAN_DATE_RANGE_INVALID = new ErrorCode(1_050_024_007, "计划结束日期不能早于开始日期");
    ErrorCode INSPECTION_PLAN_CYCLE_MONTH_INVALID = new ErrorCode(1_050_024_008, "时间周期格式不合法");
    ErrorCode INSPECTION_PLAN_LINE_NOT_EXISTS = new ErrorCode(1_050_024_009, "巡检线路不存在");
    ErrorCode INSPECTION_PLAN_DELETE_FORBIDDEN_TASK_PROCESSED = new ErrorCode(1_050_024_010, "关联巡检任务已处理，不能删除巡检计划");
    ErrorCode INSPECTION_PLAN_UPDATE_FORBIDDEN_TASK_FINISHED = new ErrorCode(1_050_024_011, "巡检任务已完成且流程已结束，巡检计划不允许编辑");
    ErrorCode INSPECTION_PLAN_UPDATE_FORBIDDEN_TASK_STATUS = new ErrorCode(1_050_024_012, "仅当关联巡检任务状态为未开始或未完成时，才允许编辑巡检计划");

    // ========== 巡检任务 1-050-025-000 ==========
    ErrorCode INSPECTION_TASK_NOT_EXISTS = new ErrorCode(1_050_025_000, "巡检任务不存在");
    ErrorCode INSPECTION_TASK_TARGET_EMPTY = new ErrorCode(1_050_025_001, "巡检对象不能为空");
    ErrorCode INSPECTION_TASK_TARGET_NOT_EXISTS = new ErrorCode(1_050_025_002, "巡检对象不存在");
    ErrorCode INSPECTION_TASK_OBJECT_TYPE_INVALID = new ErrorCode(1_050_025_003, "巡检对象类型不合法");
    ErrorCode INSPECTION_TASK_DATE_RANGE_INVALID = new ErrorCode(1_050_025_004, "计划完成时间不能早于计划开始时间");
    ErrorCode INSPECTION_TASK_STANDARD_NOT_EXISTS = new ErrorCode(1_050_025_005, "巡检标准不存在");
    ErrorCode INSPECTION_TASK_LINE_NOT_EXISTS = new ErrorCode(1_050_025_006, "巡检线路不存在");
    ErrorCode INSPECTION_TASK_PLAN_READ_ONLY = new ErrorCode(1_050_025_007, "计划自动生成任务不支持编辑");
    ErrorCode INSPECTION_TASK_SUBMIT_FORBIDDEN = new ErrorCode(1_050_025_008, "仅任务执行人可提交巡检结果");
    ErrorCode INSPECTION_TASK_SUBMIT_STATUS_INVALID = new ErrorCode(1_050_025_009, "当前任务状态不支持提交巡检结果");
    ErrorCode INSPECTION_TASK_WORKFLOW_NOT_STARTED = new ErrorCode(1_050_025_010, "任务流程未启动，无法提交巡检结果");
    ErrorCode INSPECTION_TASK_WORKFLOW_RUNNING_TASK_NOT_EXISTS = new ErrorCode(1_050_025_011, "流程待办任务不存在，无法提交巡检结果");
    ErrorCode INSPECTION_TASK_WORKFLOW_START_FAILED = new ErrorCode(1_050_025_012, "巡检任务发起流程失败");
    ErrorCode INSPECTION_TASK_SUBMIT_ABNORMAL_REMARK_REQUIRED = new ErrorCode(1_050_025_013, "检查结果为异常时，检查备注不能为空");
    ErrorCode INSPECTION_TASK_SUBMIT_FAULT_DEVICE_NOT_EXISTS = new ErrorCode(1_050_025_014, "异常项未匹配到设备，无法生成故障记录");

    // ========== 调度方案 1-050-026-000 ==========
    ErrorCode DISPATCH_PLAN_NOT_EXISTS = new ErrorCode(1_050_026_000, "调度方案不存在");
    ErrorCode DISPATCH_PLAN_NAME_EXISTS = new ErrorCode(1_050_026_001, "调度方案名称已存在");
    ErrorCode DISPATCH_PLAN_TYPE_INVALID = new ErrorCode(1_050_026_002, "调度方案类型不合法");
    ErrorCode DISPATCH_PLAN_STATUS_INVALID = new ErrorCode(1_050_026_003, "调度方案状态不合法");
    ErrorCode DISPATCH_PLAN_PREPARE_USER_EMPTY = new ErrorCode(1_050_026_004, "编制人不能为空");
    ErrorCode DISPATCH_PLAN_OBJECT_EMPTY = new ErrorCode(1_050_026_005, "操作对象不能为空");
    ErrorCode DISPATCH_PLAN_OBJECT_TYPE_INVALID = new ErrorCode(1_050_026_006, "操作对象类型不合法");
    ErrorCode DISPATCH_PLAN_OBJECT_DEVICE_NOT_EXISTS = new ErrorCode(1_050_026_007, "操作对象设备不存在");
    ErrorCode DISPATCH_PLAN_OBJECT_LOCATION_NOT_EXISTS = new ErrorCode(1_050_026_008, "操作对象安装位置不存在");
    ErrorCode DISPATCH_PLAN_OBJECT_CUSTOM_NAME_EMPTY = new ErrorCode(1_050_026_009, "自定义操作对象名称不能为空");
    ErrorCode DISPATCH_PLAN_OBJECT_PARAM_EMPTY = new ErrorCode(1_050_026_010, "控制参数不能为空");
    ErrorCode DISPATCH_PLAN_ARCHIVE_STATUS_INVALID = new ErrorCode(1_050_026_011, "当前状态不允许归档");
    ErrorCode DISPATCH_PLAN_USED_LOCKED = new ErrorCode(1_050_026_012, "调度方案已被使用，不允许编辑或删除");

    // ========== 调度管理 1-050-027-000 ==========
    ErrorCode DISPATCH_MANAGE_NOT_EXISTS = new ErrorCode(1_050_027_000, "调度指令不存在");
    ErrorCode DISPATCH_MANAGE_PLAN_EMPTY = new ErrorCode(1_050_027_001, "调度方案不能为空");
    ErrorCode DISPATCH_MANAGE_PLAN_NOT_EXISTS = new ErrorCode(1_050_027_002, "调度方案不存在");
    ErrorCode DISPATCH_MANAGE_RECEIVER_DEPT_EMPTY = new ErrorCode(1_050_027_003, "接收单位不能为空");
    ErrorCode DISPATCH_MANAGE_RECEIVER_USER_EMPTY = new ErrorCode(1_050_027_004, "接收人不能为空");
    ErrorCode DISPATCH_MANAGE_RECEIVER_USER_INVALID = new ErrorCode(1_050_027_005, "接收人不属于所选接收单位");
    ErrorCode DISPATCH_MANAGE_STATUS_INVALID = new ErrorCode(1_050_027_006, "执行状态不合法");
    ErrorCode DISPATCH_MANAGE_SUBMIT_FORBIDDEN = new ErrorCode(1_050_027_007, "仅执行人本人可提交调度反馈");
    ErrorCode DISPATCH_MANAGE_SUBMIT_STATUS_INVALID = new ErrorCode(1_050_027_008, "当前状态不支持提交调度反馈");
    ErrorCode DISPATCH_MANAGE_UPDATE_DELETE_FORBIDDEN = new ErrorCode(1_050_027_009, "仅管理员可编辑或删除调度指令");
    ErrorCode DISPATCH_MANAGE_EXECUTED_LOCKED = new ErrorCode(1_050_027_010, "已提交反馈的调度指令不允许编辑或删除");
    ErrorCode DISPATCH_MANAGE_QUERY_FORBIDDEN = new ErrorCode(1_050_027_011, "仅可查看分配给自己的调度指令");
    ErrorCode DISPATCH_MANAGE_EXECUTOR_USER_EMPTY = new ErrorCode(1_050_027_012, "执行人不能为空");
    ErrorCode DISPATCH_MANAGE_EXECUTOR_USER_INVALID = new ErrorCode(1_050_027_013, "执行人必须为启用状态且不包含游客角色");

    // ========== 调令接受 1-050-028-000 ==========
    ErrorCode DISPATCH_RECEIVE_NOT_EXISTS = new ErrorCode(1_050_028_000, "调令不存在");
    ErrorCode DISPATCH_RECEIVE_FORBIDDEN = new ErrorCode(1_050_028_001, "仅接收人或执行人可访问该调令");
    ErrorCode DISPATCH_RECEIVE_STATUS_INVALID = new ErrorCode(1_050_028_002, "当前调令状态不支持该操作");
    ErrorCode DISPATCH_RECEIVE_ATTACHMENT_EMPTY = new ErrorCode(1_050_028_003, "请至少上传一个附件");
    ErrorCode DISPATCH_RECEIVE_EXECUTE_FLAG_INVALID = new ErrorCode(1_050_028_004, "执行情况取值不合法");
    ErrorCode DISPATCH_RECEIVE_ACCEPT_FORBIDDEN = new ErrorCode(1_050_028_005, "仅执行人可接收该调令");
    ErrorCode DISPATCH_RECEIVE_SUBMIT_FORBIDDEN = new ErrorCode(1_050_028_006, "仅执行人可提交调令执行结果");

    // ========== 运行日志 1-050-029-000 ==========
    ErrorCode RUN_LOG_NOT_EXISTS = new ErrorCode(1_050_029_000, "运行日志不存在");
    ErrorCode RUN_LOG_PERIOD_INVALID = new ErrorCode(1_050_029_001, "运行时段不合法");
    ErrorCode RUN_LOG_FORBIDDEN = new ErrorCode(1_050_029_002, "仅可操作本人或本班组创建的运行日志");
    ErrorCode RUN_LOG_DUTY_TEAM_EMPTY = new ErrorCode(1_050_029_003, "值班班组不能为空");
    ErrorCode RUN_LOG_DISPATCH_INSTRUCTION_NOT_EXISTS = new ErrorCode(1_050_029_004, "关联调令不存在");
    ErrorCode RUN_LOG_ABNORMAL_REMARK_REQUIRED = new ErrorCode(1_050_029_005, "检查结果为异常时，检查备注不能为空");

    // ========== 班次管理 1-050-030-000 ==========
    ErrorCode SHIFT_CONFIG_NOT_EXISTS = new ErrorCode(1_050_030_000, "班次不存在");
    ErrorCode SHIFT_CONFIG_NAME_EXISTS = new ErrorCode(1_050_030_001, "班次名称已存在");
    ErrorCode SHIFT_CONFIG_TIME_INVALID = new ErrorCode(1_050_030_002, "结束时间必须晚于起始时间");
    ErrorCode SHIFT_CONFIG_CROSS_DAY_REQUIRED = new ErrorCode(1_050_030_003, "结束时间早于或等于起始时间时，必须勾选跨天标识");
    ErrorCode SHIFT_CONFIG_DELETE_FORBIDDEN_REFERENCED = new ErrorCode(1_050_030_004, "该班次已关联排班记录，不允许删除");

    // ========== 班组管理 1-050-031-000 ==========
    ErrorCode SHIFT_TEAM_NOT_EXISTS = new ErrorCode(1_050_031_000, "班组不存在");
    ErrorCode SHIFT_TEAM_NAME_EXISTS = new ErrorCode(1_050_031_001, "班组名称已存在");
    ErrorCode SHIFT_TEAM_LEADER_REQUIRED = new ErrorCode(1_050_031_002, "班组长不能为空");
    ErrorCode SHIFT_TEAM_LEADER_NOT_MEMBER = new ErrorCode(1_050_031_003, "班组长必须是该班组成员");
    ErrorCode SHIFT_TEAM_DELETE_FORBIDDEN_REFERENCED = new ErrorCode(1_050_031_004, "该班组已关联排班记录，不允许删除");

    // ========== 员工排班 1-050-032-000 ==========
    ErrorCode SHIFT_SCHEDULE_NOT_EXISTS = new ErrorCode(1_050_032_000, "员工排班记录不存在");
    ErrorCode SHIFT_SCHEDULE_SHIFT_NOT_EXISTS = new ErrorCode(1_050_032_001, "值班班次不存在");
    ErrorCode SHIFT_SCHEDULE_TEAM_NOT_EXISTS = new ErrorCode(1_050_032_002, "值班班组不存在");
    ErrorCode SHIFT_SCHEDULE_USER_NOT_EXISTS = new ErrorCode(1_050_032_003, "值班人员不存在");
    ErrorCode SHIFT_SCHEDULE_MOBILE_INVALID = new ErrorCode(1_050_032_004, "值班人员联系方式缺失或手机号格式不正确");
    ErrorCode SHIFT_SCHEDULE_POST_REQUIRED = new ErrorCode(1_050_032_005, "值班人员岗位信息未维护");
    ErrorCode SHIFT_SCHEDULE_CONFLICT = new ErrorCode(1_050_032_006, "同一员工在该时间段已存在排班，禁止重复排班");
    ErrorCode SHIFT_SCHEDULE_USER_NOT_IN_TEAM = new ErrorCode(1_050_032_007, "值班人员不属于所选班组");
    ErrorCode SHIFT_SCHEDULE_DELETE_FORBIDDEN_REFERENCED = new ErrorCode(1_050_032_008, "该排班已关联交接班记录，不允许删除");
    ErrorCode SHIFT_SCHEDULE_DUTY_TIME_REQUIRED = new ErrorCode(1_050_032_009, "值班日期开始时间和结束时间不能为空");

    // ========== 交接班管理 1-050-033-000 ==========
    ErrorCode SHIFT_HANDOVER_NOT_EXISTS = new ErrorCode(1_050_033_000, "交接班记录不存在");
    ErrorCode SHIFT_HANDOVER_SCHEDULE_NOT_EXISTS = new ErrorCode(1_050_033_001, "关联排班不存在");
    ErrorCode SHIFT_HANDOVER_SCHEDULE_EXISTS = new ErrorCode(1_050_033_002, "该排班已完成交接，不可重复提交");
    ErrorCode SHIFT_HANDOVER_USER_NOT_EXISTS = new ErrorCode(1_050_033_003, "交班人或接班人不存在");
    ErrorCode SHIFT_HANDOVER_TAKEOVER_USER_NOT_FOUND = new ErrorCode(1_050_033_004, "未匹配到下一班次接班人，请手动选择");
    ErrorCode SHIFT_HANDOVER_AUTO_MATCH_REQUIRED = new ErrorCode(1_050_033_005, "当前未匹配到可交接班次，请在交接窗口内提交值班日志");
    ErrorCode SHIFT_HANDOVER_CURRENT_SCHEDULE_NOT_FOUND = new ErrorCode(1_050_033_006, "未匹配到当前值班排班：需存在当前时间命中的本人排班");
    ErrorCode SHIFT_HANDOVER_CURRENT_SCHEDULE_TIME_INVALID = new ErrorCode(1_050_033_007, "当前值班排班缺少开始或结束时间，请先完善排班值班日期");
    ErrorCode SHIFT_HANDOVER_NEXT_SCHEDULE_NOT_FOUND = new ErrorCode(1_050_033_008, "未匹配到下一可交接排班：需存在开始时间晚于当前班次的下一排班（当前班有站点时需同站点）");
    ErrorCode SHIFT_HANDOVER_NEXT_SCHEDULE_TIME_INVALID = new ErrorCode(1_050_033_009, "下一排班缺少开始时间，请先完善排班值班日期");
    ErrorCode SHIFT_HANDOVER_CURRENT_DUTY_USER_NOT_SET = new ErrorCode(1_050_033_010, "当前排班未配置交班人，请先在排班中配置值班人员");
    ErrorCode SHIFT_HANDOVER_NEXT_DUTY_USER_NOT_SET = new ErrorCode(1_050_033_011, "下一排班未配置接班人，请先在排班中配置值班人员");
    ErrorCode SHIFT_HANDOVER_SUBMIT_TOO_EARLY = new ErrorCode(1_050_033_012, "未到交接提交时间：仅允许在下一班开始前15分钟内提交");
    ErrorCode SHIFT_HANDOVER_SUBMIT_WINDOW_EXPIRED = new ErrorCode(1_050_033_013, "已超过交接提交时限：仅允许在当前班结束前提交");
    ErrorCode SHIFT_HANDOVER_SCHEDULE_MISMATCH = new ErrorCode(1_050_033_014, "提交的排班与系统自动匹配排班不一致，请在交接提醒弹窗中提交");

}
