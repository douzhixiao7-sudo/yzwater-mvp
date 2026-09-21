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

## 公示牌二维码地址

后台公示牌页展示的二维码 URL，来自参数配置（表 `infra_config`），不是写死在代码里：

| config_key | 用途 | 当前交付库默认值 |
|---|---|---|
| `river_info_qr_code_address` | 河道/公示牌信息页 | `https://yzriver.sy-digit.com/h5/user_BaseInfo` |
| `river_issue_qr_code_address` | 问题反馈入口（登录） | `https://yzriver.sy-digit.com/h5/login` |

最终二维码内容形如：`{上述地址}?qrcode={公示牌qrCode}`。

对方部署自己的 H5 后，在管理端「基础设施 → 配置管理」改这两项，或执行：

```sql
UPDATE infra_config
SET value = 'http://<你们的H5域名>/h5/user_BaseInfo', update_time = NOW()
WHERE config_key = 'river_info_qr_code_address';

UPDATE infra_config
SET value = 'http://<你们的H5域名>/h5/login', update_time = NOW()
WHERE config_key = 'river_issue_qr_code_address';
```

本地联调可改为 `http://localhost:3000/h5/user_BaseInfo` 与 `http://localhost:3000/h5/login`。  
改配置后需在后台重新打开/下载二维码；已打印的旧码仍指向旧域名，需重打。

短链跳转相关配置同样在 `infra_config`：`feedback_person_redirect_url`、`handle_person_redirect_url`。
