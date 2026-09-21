# 常用命令（Windows / PowerShell）

> 默认在项目根目录 `D:/code/RuoYi-Vue-Dev/yzwater` 执行。

## 1. 后端（Maven）
- 全量编译（跳过测试）：
  - `mvn clean package -DskipTests`
- 编译指定服务并联动依赖模块：
  - `mvn -pl yz-server -am clean package -DskipTests`
- 运行单元测试（全量）：
  - `mvn test`
- 运行单个测试类（推荐，控制在 60s 内）：
  - `mvn -pl yz-framework/yz-spring-boot-starter-web -Dtest=ApiEncryptTest test`
- 本地启动后端（Maven 方式）：
  - `mvn -pl yz-server -am spring-boot:run -Dspring-boot.run.profiles=dev`
- Jar 方式启动后端（需先打包）：
  - `java -jar yz-server/target/yz-server.jar --spring.profiles.active=dev`

## 2. 前端（pnpm）
- 安装依赖：
  - `pnpm install`
- 本地开发：
  - `pnpm dev`
- 指定 dev-server 模式：
  - `pnpm dev-server`
- 类型检查：
  - `pnpm ts:check`
- 生产构建：
  - `pnpm build:prod`
- 代码检查/格式化：
  - `pnpm lint:eslint`
  - `pnpm lint:style`
  - `pnpm lint:format`

> 前端命令执行目录：`yz-ui/yz-boot-mini-front-master`

## 3. 常用仓库操作
- 查看变更：`git status`
- 查看差异：`git diff`
- 查看分支：`git branch`
- 历史提交：`git log --oneline -n 20`

## 4. Windows 实用命令映射
- 列目录：`Get-ChildItem`（别名 `ls`）
- 切目录：`Set-Location <path>`（别名 `cd`）
- 搜索文本（推荐）：`rg "关键字" "路径"`
- PowerShell 文本搜索（备选）：`Select-String -Path "**/*" -Pattern "关键字"`
- 查找文件（推荐）：`rg --files | rg "文件名片段"`

## 5. 容器化（按需）
- 查看说明：`script/docker/Docker-HOWTO.md`
- 启动 compose：`docker compose --env-file script/docker/docker.env up -d`

> 注意：`script/docker` 中部分目录名示例与当前仓库结构可能不一致，需先核对路径。