# 河长制交付目录说明

本目录文档已拆成两套主文档：

| 文档 | 用途 |
|---|---|
| **[软件安装流程.md](./软件安装流程.md)** | Windows + macOS 软件安装与命令 |
| **[河长制交付说明.md](./河长制交付说明.md)** | 交付范围、**未交付名录**、还原库、启动、配置、验收 |
| [现场安装操作清单.md](./现场安装操作清单.md) | 去现场时的短勾选流程 |

## 库文件

| 文件 | 说明 |
|---|---|
| `yzwater_isolated_hezhangzhi-lite.dump` | 主交付库（结构+数据，PostgreSQL custom 格式） |
| `hezhangzhi-lite-db-cleanup.sql` | 可选：在已有库上收口 |

### 快速还原

```bash
createdb -h localhost -U postgres yzwater_delivery
psql -h localhost -U postgres -d yzwater_delivery -c "CREATE EXTENSION IF NOT EXISTS postgis;"
pg_restore -h localhost -U postgres -d yzwater_delivery --no-owner --no-acl \
  scripts/delivery/yzwater_isolated_hezhangzhi-lite.dump
```

后端使用 `--spring.profiles.active=dev`，修改 `application-dev.yaml` 中的库名与账号。

### 二维码等配置（交付默认 localhost）

详见《河长制交付说明》第 7 节；生产改 `infra_config` 前缀即可，无需改业务代码。
