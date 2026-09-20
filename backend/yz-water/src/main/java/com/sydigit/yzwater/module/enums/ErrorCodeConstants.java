package com.sydigit.yzwater.module.enums;

import com.sydigit.yzwater.framework.common.exception.ErrorCode;

/**
 * YZ 模块错误码（水利设施相关）
 */
public interface ErrorCodeConstants {

    ErrorCode YZ_FILE_EMPTY = new ErrorCode(1_009_000_001, "上传文件不能为空");
    ErrorCode YZ_FILE_TYPE_INVALID = new ErrorCode(1_009_000_002, "仅支持 zip/shp/geojson/kml 文件");
    ErrorCode YZ_SHP_PARSE_ERROR = new ErrorCode(1_009_000_005, "Shapefile 解析失败");
    ErrorCode YZ_FILE_CONVERT_FAIL = new ErrorCode(1_009_000_010, "文件转换失败");
    ErrorCode YZ_DATASET_IMPORT_FAIL = new ErrorCode(1_009_000_017, "GeoJSON 数据集批量导入失败：{}");
    ErrorCode YZ_WATER_FACILITY_NOT_EXISTS = new ErrorCode(1_009_000_025, "设施不存在或已被删除");
    ErrorCode YZ_RIVER_CHANNEL_NOT_EXISTS = new ErrorCode(1_009_000_026, "河道不存在或已被删除");
    ErrorCode YZ_EXCEL_TYPE_INVALID = new ErrorCode(1_009_000_030, "仅支持上传 xls/xlsx 文件");
    ErrorCode YZ_DICT_VALUE_NOT_FOUND = new ErrorCode(1_009_000_031, "字典 {}/{} 未匹配到有效值");
    ErrorCode YZ_EXCEL_PARSE_ERROR = new ErrorCode(1_009_000_032, "Excel 解析失败：{}");
    ErrorCode YZ_RIVER_CHANNEL_QRCODE_FAIL = new ErrorCode(1_009_000_033, "生成河道二维码失败");
    ErrorCode YZ_PROBLEM_FEEDBACK_NOT_EXISTS = new ErrorCode(1_009_000_040, "问题反馈不存在或已被删除");
    ErrorCode YZ_PROBLEM_PERMISSION_DENIED = new ErrorCode(1_009_000_041, "当前用户无权限执行此操作");
    ErrorCode YZ_PROBLEM_ASSIGNEE_REQUIRED = new ErrorCode(1_009_000_042, "指派处理人不能为空");
    ErrorCode YZ_PROBLEM_ASSIGNEE_PURE_GUEST_NOT_ALLOWED = new ErrorCode(1_009_000_069, "不能指派纯游客用户为处理人");
    ErrorCode YZ_PROBLEM_PLANNED_COMPLETION_TIME_REQUIRED = new ErrorCode(1_009_000_062, "计划完成时间不能为空");
    ErrorCode YZ_PROBLEM_PLANNED_COMPLETION_TIME_INVALID = new ErrorCode(1_009_000_063, "计划完成时间必须晚于当前时间");
    ErrorCode YZ_PROBLEM_NOT_ASSIGNED = new ErrorCode(1_009_000_043, "当前用户未被指派处理该问题");
    ErrorCode YZ_PROBLEM_STATUS_INVALID = new ErrorCode(1_009_000_044, "当前问题状态不支持此操作");
    ErrorCode YZ_SIGNBOARD_NOT_EXISTS = new ErrorCode(1_009_000_045, "公示牌不存在或已被删除");
    ErrorCode YZ_RIVER_SECTION_NOT_EXISTS = new ErrorCode(1_009_000_046, "河段不存在或已被删除");
    ErrorCode YZ_SIGNBOARD_QRCODE_FAIL = new ErrorCode(1_009_000_047, "生成公示牌二维码失败");
    ErrorCode YZ_SIGNBOARD_QR_CODE_GENERATE_FAIL = new ErrorCode(1_009_000_048, "生成公示牌二维码标识失败");
    ErrorCode YZ_SIGNBOARD_RELATION_REQUIRED = new ErrorCode(1_009_000_049, "公示牌必须关联河道或河段");
    ErrorCode YZ_SIGNBOARD_RELATION_INVALID = new ErrorCode(1_009_000_050, "公示牌关联河段与河道不匹配");
    ErrorCode YZ_SIGNBOARD_IMAGES_TOO_MANY = new ErrorCode(1_009_000_051, "公示牌图片最多只能上传 5 张");

    ErrorCode YZ_EMBANKMENT_RELATION_REQUIRED = new ErrorCode(1_009_000_052, "堤防必须关联河道或河段");
    ErrorCode YZ_EMBANKMENT_RELATION_INVALID = new ErrorCode(1_009_000_053, "堤防关联河段与河道不匹配");
    ErrorCode YZ_EMBANKMENT_IMAGES_TOO_MANY = new ErrorCode(1_009_000_054, "堤防图片最多只能上传 5 张");
    ErrorCode YZ_EMBANKMENT_NOT_EXISTS = new ErrorCode(1_009_000_055, "堤防不存在或已被删除");

    ErrorCode YZ_RESERVOIR_NOT_EXISTS = new ErrorCode(1_009_000_060, "水库不存在或已被删除");
    ErrorCode YZ_GEOJSON_PARSE_ERROR = new ErrorCode(1_009_000_061, "GeoJSON 解析失败：{}");

    ErrorCode YZ_RIVER_CHANNEL_CODE_DUPLICATE = new ErrorCode(1_009_000_064, "已有对应河道编码信息存在");
    ErrorCode YZ_RESERVOIR_CODE_DUPLICATE = new ErrorCode(1_009_000_065, "已有对应水库编码信息存在");
    ErrorCode YZ_PROBLEM_REVIEW_IMAGES_REQUIRED = new ErrorCode(1_009_000_066, "请上传问题确认图片");
    ErrorCode YZ_PROBLEM_REVIEW_IMAGES_TOO_MANY = new ErrorCode(1_009_000_067, "问题确认图片最多只能上传 5 张");

    ErrorCode YZ_RIVER_CHIEF_NOT_EXISTS = new ErrorCode(1_009_000_068, "河长信息不存在或已失效");
}
