# 巡检模块工作流接入设计要点（2026-02-12）

- 巡检工作流采用业务表单模式：业务表 `yz_equipment_inspection_task` 保存 `process_instance_id`，流程实例以 `BUSINESS_KEY_ = task.id` 反向关联。
- 表设计改造重点：任务表新增流程字段（`workflow_status`、`process_instance_id`、`process_definition_key`、当前待办节点与处理人等）并补充索引。
- 业务状态与流程状态分离：`task_status` 表示执行进度，`workflow_status` 表示审批进度，避免状态耦合冲突。
- 接入时序建议：先提交巡检结果，再调用 `BpmProcessInstanceApi#createProcessInstance(...)` 发起审批流程。
- 流程结果回写建议：通过 `BpmProcessInstanceStatusEventListener` 统一监听并回写任务表，避免在 Controller 使用 Flowable 原生 API。