# 项目概览（yzwater）

## 1. 项目定位
- 基于 RuoYi-Vue-Pro 的二次开发多模块平台，后端为 Spring Boot 3 多模块 Maven 工程，前端为 Vue3 + Vite。
- 根 `pom.xml` 描述为“芋道项目基础脚手架”，当前仓库已集成 `yz-water` 水利/GIS 业务模块（PostGIS + GeoTools）。
- `yz-server` 是后端启动聚合模块，负责装配各业务模块并对外提供 REST API。

## 2. 关键模块
- `yz-dependencies`：BOM 依赖版本管理。
- `yz-framework`：公共基础设施与 starter（web/security/mybatis/redis/job/mq 等）。
- `yz-server`：Spring Boot 启动入口（`YzServerApplication`）。
- `yz-module-system`、`yz-module-infra`、`yz-module-bpm`、`yz-module-report`、`yz-module-iot`：通用业务模块。
- `yz-water`：水利业务扩展模块（河道、水库、堤防、巡检、GIS 等）。
- `yz-ui/yz-boot-mini-front-master`：前端管理端项目（Vue3 + TS + Element Plus）。

## 3. 启动入口与运行形态
- 后端主入口：`yz-server/src/main/java/com/sydigit/yzwater/server/YzServerApplication.java`。
- 后端配置：`yz-server/src/main/resources/application*.yaml`，默认 `spring.profiles.active=dev`。
- 前端入口：`yz-ui/yz-boot-mini-front-master/src/main.ts`。
- 部署脚本：`script/shell/deploy.sh`（Linux 部署脚本，含备份、停服、发版、健康检查流程）。

## 4. 技术栈（以仓库配置为准）
- Java 17、Spring Boot（root pom 声明 `3.5.5`）、MyBatis Plus、Lombok、MapStruct。
- 数据侧包含 PostgreSQL/PostGIS、Redis，且可扩展 RocketMQ/Kafka/RabbitMQ/Quartz。
- 前端：Vue 3.5、Vite 5、TypeScript、Pinia、Vue Router、Element Plus、UnoCSS。

## 5. 仓库状态提示
- 项目文档存在新旧并存现象（例如 `script/docker` 与当前前端目录名不完全一致），执行命令前需以实际目录为准。
- `application*.yaml` 含大量示例密钥/账号配置，开发时应做环境变量化与敏感信息脱敏，不在日志/提交中传播。