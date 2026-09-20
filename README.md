# 仪征水务独立交付版

本目录由现有上线项目抽离，当前阶段以完整保留源码和隔离运行环境为目标，不针对下游项目裁剪业务。

## 目录

- `backend`：Java 17 / Spring Boot 后端
- `admin-web`：管理后台 Vue 3 前端
- `mobile-web`：移动端 Vue 3 前端
- `database`：数据库结构、基础数据和模拟数据
- `deploy`：独立环境变量及启动辅助文件
- `docs`：抽离、配置和验收说明

## 当前状态

源码已经从原项目复制，Git 历史、依赖目录、构建产物、日志及旧压缩包未纳入。后端请使用 `isolated` profile；数据库脚本到位后再执行建库、数据清理和完整联调。

## 运行原则

1. 不使用原项目的 `dev`、`prod` 等环境配置启动交付版。
2. 复制 `deploy/.env.example` 为本机私有配置，并填写独立数据库和 Redis 信息。
3. 所有第三方能力默认关闭，配置独立账号后再启用。
4. 禁止把生产数据库、Redis、文件目录或生产密钥写回本项目。

## 预定启动方式

后端：

```powershell
$env:SPRING_PROFILES_ACTIVE = "isolated"
mvn -pl yz-server -am spring-boot:run
```

管理后台：

```powershell
pnpm install
pnpm dev:isolated
```

移动端：

```powershell
npm install
npm run dev
```

移动端运行时配置位于 `mobile-web/public/config.js`，默认请求本机 `48082` 端口。
