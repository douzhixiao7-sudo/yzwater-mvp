# 代码风格与约定

## 1. 全局协作约束（来自 AGENTS.md）
- 输出与注释优先中文。
- 复杂任务先做结构化规划（Sequential Thinking），再实施修改。
- 优先使用 Serena 的符号级检索/编辑工具，避免盲改。
- 高风险操作（删除、强制重置、推送、生产变更）需先确认。

## 2. 后端分层与命名
- 基础包前缀：`com.sydigit.yzwater`。
- 典型分层：`controller` / `service` / `dal(dataobject + mysql)` / `util` / `constants`。
- Controller 常见注解：`@RestController` + `@RequestMapping` + `@Validated` + Swagger 注解。
- 返回体统一：`CommonResult<T>`。
- DO 命名：`*DO`，Mapper 命名：`*Mapper`，VO 位于 `controller/.../vo`。

## 3. 后端实现习惯（从仓库抽样）
- 广泛使用 Lombok（`@Data`、`@EqualsAndHashCode`、`@Slf4j`）。
- MyBatis Plus + `BaseMapperX` + `LambdaQueryWrapper` 为主。
- 代码注释风格偏中文，业务语义和边界逻辑通常会注释。
- 依赖注入方式有构造注入与字段注入并存；新代码建议优先构造注入。
- `lombok.config` 启用链式访问器（`lombok.accessors.chain=true`）。

## 4. 前端风格（yz-ui）
- 技术：Vue3 + TS + Vite + Pinia + Element Plus。
- `.editorconfig`：UTF-8、2 空格缩进、`max_line_length=100`。
- `prettier.config.js`：单引号、无分号、行宽 100。
- ESLint 规则相对宽松（部分 TS/Vue 规则关闭），结合 IDE + Prettier 使用。
- 目录组织：`src/api`、`src/views`、`src/store`、`src/router`、`src/components` 等。

## 5. 质量实践建议
- 新增接口时保持 VO/DO 分离，不直接暴露 DO。
- 优先复用模块内既有常量、工具类、转换流程，减少重复实现。
- 修改 SQL/Mapper 时同步检查租户、逻辑删除、权限过滤等横切影响。
- 提交前至少完成：后端编译 + 目标测试 + 前端类型检查/ESLint。