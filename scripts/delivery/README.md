# 河长制交付库

交付方式：对方拿精简代码 + 本目录中的库备份继续开发。

## 文件

| 文件 | 说明 |
|---|---|
| `hezhangzhi-lite-db-cleanup.sql` | 在现有库上清理菜单 / 定时任务 / 短信的脚本（已对本地 `yzwater_isolated` 执行过） |
| `yzwater_isolated_hezhangzhi-lite.dump` | 清理后的库备份（PostgreSQL custom 格式） |

## 恢复备份

```bash
# 新建空库后恢复
createdb -h localhost -U postgres yzwater_delivery
pg_restore -h localhost -U postgres -d yzwater_delivery --no-owner --no-acl \
  scripts/delivery/yzwater_isolated_hezhangzhi-lite.dump
```

后端 `application-*.yaml` 里把库名改成 `yzwater_delivery` 即可。

## 短信

库内启用渠道 `DEBUG_CONSOLE`。验证码和通知只打后端日志并写入 `system_sms_log`，不发真实短信。

需要后端包含 `DebugConsoleSmsClient`（交付分支代码），重启后生效。
