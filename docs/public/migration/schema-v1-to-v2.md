# schema 迁移：v1 → v2

## 变更说明

v2 在 `lock_guard` 表中新增 `hold_token` 列，用于修复 `release` 操作的竞态问题。

**背景**：原实现 `DELETE WHERE id = ?` 不校验持有者，当请求 A 超时后请求 B 抢锁，A 执行完毕时仍会删除 B 的锁。新增 `hold_token` 后，`release` 只删除自己持有的锁实例。

---

## 升级步骤

### 1. 执行 DDL

**MySQL**

```sql
ALTER TABLE lock_guard
    ADD COLUMN hold_token VARCHAR(36) NOT NULL DEFAULT '';
```

**PostgreSQL**

```sql
ALTER TABLE lock_guard
    ADD COLUMN hold_token VARCHAR(36) NOT NULL DEFAULT '';
```

### 2. 升级应用

替换 jar 包后重启，新版本会在每次 `tryHold` 时自动生成并写入 `hold_token`。

### 3. 清理默认值（可选）

存量行的 `hold_token` 为空字符串，不影响新请求。若需清理，可在业务低峰期执行：

```sql
DELETE FROM lock_guard WHERE hold_token = '';
```

---

## 注意事项

- 升级前无需停机，DDL 为非破坏性变更（仅加列）
- 滚动发布期间，旧版本写入的行 `hold_token` 为空，`release` 时 `DELETE WHERE hold_token = ''` 不会匹配到任何行，旧锁将依赖过期自然清理
- 建议在全量升级完成后再执行可选清理步骤

---

## 升级顺序（重要）

v2 默认开启启动自检（`idempotent-guard.jdbc.schema-check=true`），应用启动时会执行探针查询验证 `hold_token` 列是否存在。

**必须先执行 DDL，再部署新版本应用**，否则应用将在启动时快速失败并输出如下错误：

```
GuardInfrastructureException: Schema check failed for table 'lock_guard': ...
Please run the migration script.
```

如需在 DDL 执行前临时部署（例如蓝绿发布过渡期），可临时关闭自检：

```yaml
idempotent-guard:
  jdbc:
    schema-check: false
```

待 DDL 全量完成后恢复为 `true`。
