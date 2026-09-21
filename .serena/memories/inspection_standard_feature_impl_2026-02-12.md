# 巡检标准功能实现记录（2026-02-12）

## 本次完成
- 后端：强化 `inspectionstandard` 入参校验，新增巡检枚举并通过 `@InEnum` 约束。
- 前端：新增巡检标准 API 与页面 `views/inspection/standard/index.vue`，支持标准主信息、多检查项、多记录项模板维护。
- 菜单：确认 `sql/postgresql/iot_inspection_init.sql` 已包含巡检标准菜单（component=`inspection/standard/index`）及查询/创建/更新/删除权限点。

## 关键校验策略
- 标准主表：对象类型、建议周期单位、状态均做枚举校验；建议周期值最小 1。
- 检查项：默认检查结果必须为 excellent/good/qualified/unqualified。
- 记录项：属性名称、具体数值、单位必填。
- 前端提交前进行分层校验，明确错误提示定位到“第 N 个检查项目/第 M 条记录项”。

## 质量验证
- 后端编译：`mvn -pl yz-module-iot/yz-module-iot-biz -am -DskipTests compile` 通过。
- 前端局部 lint：`pnpm eslint src/views/inspection/standard/index.vue src/api/iot/inspection/standard/index.ts` 通过。
- 全量 `pnpm ts:check` 失败，但为仓库既有大量历史 TS 问题，非本次改动引入。

## 编码与乱码控制
- 新增前端文件曾带 BOM，已清理为 UTF-8 无 BOM。
- 本次新增/修改文件中文内容可正常读取，避免了乱码。