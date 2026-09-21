# 巡检标准改造：设备优先配置（2026-02-12）

## 需求改造点
- 新增/编辑巡检标准流程调整为：先选择设备，再配置检查项目，再配置记录项。
- 数据关系调整为：标准与设备多对多。
- 删除“异常标准”字段（abnormal_rule）及其前后端映射。

## 主要代码改动
- 后端新增 `IotInspectionStandardDeviceDO` 与 `IotInspectionStandardDeviceMapper`，用于标准-设备关联维护。
- `IotInspectionStandardSaveReqVO` 新增 `deviceIds` 必填；`IotInspectionStandardRespVO` 增加 `deviceIds` 回显。
- `IotInspectionStandardServiceImpl` 增加设备关联保存/替换/清理与设备存在性校验。
- 检查项 VO/DO/Resp 移除 `abnormalRule` 字段。
- 前端巡检标准页面新增“关联设备”多选，按闸站动态加载设备；提交时携带 `deviceIds`。
- 前端去掉“异常标准”输入与 payload 字段。

## 文档与SQL
- `doc/xunjian-pg-design.md` 已更新：
  - 新增 `yz_equipment_inspection_standard_device` 表设计与 ER 图关系。
  - 删除检查项 `abnormal_rule` 字段。
- 新增可执行升级脚本：
  - `sql/postgresql/iot_inspection_standard_upgrade_20260212.sql`
  - 包含新增关联表和删除 `abnormal_rule`。

## 验证
- 后端编译通过：`mvn -pl yz-module-iot/yz-module-iot-biz -am -DskipTests compile`。
- 前端局部 lint 通过：`pnpm eslint src/views/inspection/standard/index.vue src/api/iot/inspection/standard/index.ts`。
- 全量 `pnpm ts:check` 仍有仓库历史问题，非本次改造引入。