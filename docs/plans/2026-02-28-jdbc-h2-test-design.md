# Design: JDBC 模块 H2 集成测试

Date: 2026-02-28
Branch: fac

## 背景

`idempotent-guard-jdbc` 现有测试全部使用 Mockito mock 数据库层，属于纯单元测试。
`GeneralJdbcOperator` 的 SQL 逻辑（INSERT/UPDATE 时间条件/DELETE token 匹配/CLEAR 阈值）
从未在真实数据库上验证过。本设计补充基于 H2 内存数据库的集成测试，覆盖完整 JDBC 功能路径。

## 目标

- 验证 `GeneralJdbcOperator` 的 SQL 逻辑正确性
- 验证 `SchemaChecker` 对真实 schema 的检查行为
- 验证 `GeneralJdbcGuard` 的端到端加锁/释放流程
- 不引入并发测试（并发安全由数据库约束保证，应在真实 DB 上验证）

## 方案

每个被测类建立独立的集成测试类（`*IT`），共享公共基类管理 H2 生命周期。
每个测试方法在独立的 H2 实例上运行，测试间完全隔离。

## 文件变更

```
idempotent-guard-jdbc/
├── pom.xml                                        ← 新增 h2 test 依赖
└── src/
    ├── test/
    │   ├── java/com/power4j/idempotentguard/jdbc/
    │   │   ├── H2TestSupport.java                 ← 公共基类
    │   │   ├── GeneralJdbcOperatorIT.java
    │   │   ├── SchemaCheckerIT.java
    │   │   └── GeneralJdbcGuardIT.java
    │   └── resources/db/
    │       └── schema-h2.sql                      ← H2 兼容 schema
```

## 依赖变更

`idempotent-guard-jdbc/pom.xml` 新增：

```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>test</scope>
</dependency>
```

版本由 `spring-boot-dependencies` BOM 管理。

## H2 Schema

```sql
CREATE TABLE lock_guard (
    id              VARCHAR(128)  NOT NULL,
    start_time_utc  TIMESTAMP     NOT NULL,
    expire_time_utc TIMESTAMP     NOT NULL,
    hold_by         VARCHAR(255)  NOT NULL,
    hold_hint       VARCHAR(255)  NULL,
    hold_token      VARCHAR(36)   NOT NULL DEFAULT '',
    CONSTRAINT PK_LOCK_GUARD PRIMARY KEY (id)
);
```

## H2TestSupport 基类

- `@BeforeEach`：`EmbeddedDatabaseBuilder` 创建内存 H2，加载 `schema-h2.sql`，构建 `JdbcTemplate` 和 `GeneralJdbcOperator`
- `@AfterEach`：关闭 `EmbeddedDatabase`
- 提供 `protected JdbcTemplate jdbcTemplate` 和 `protected GeneralJdbcOperator operator`

## 测试覆盖

### GeneralJdbcOperatorIT

| 测试方法 | 验证点 |
|---|---|
| `create_shouldInsertRow` | INSERT 成功，返回含 token 的 Holder |
| `create_duplicateKey_shouldThrow` | 主键冲突抛 DuplicateKeyException |
| `update_whenExpired_shouldReplaceRow` | 过期记录能被 UPDATE 覆盖 |
| `update_whenNotExpired_shouldReturnEmpty` | 未过期记录 UPDATE 无效，返回 empty |
| `delete_withCorrectToken_shouldRemoveRow` | token 匹配时删除成功 |
| `delete_withWrongToken_shouldReturnFalse` | token 不匹配时不删除 |
| `clear_shouldRemoveExpiredRows` | 过期行被清除，未过期行保留 |

### SchemaCheckerIT

| 测试方法 | 验证点 |
|---|---|
| `check_withCorrectSchema_shouldPass` | 正确 schema 下无异常 |
| `check_withMissingColumn_shouldThrow` | DROP 某列后抛 GuardInfrastructureException |

### GeneralJdbcGuardIT

| 测试方法 | 验证点 |
|---|---|
| `tryHold_newKey_shouldSucceed` | 新 key 加锁成功 |
| `tryHold_sameKey_notExpired_shouldFail` | 未过期时第二次 tryHold 返回 empty |
| `tryHold_sameKey_afterExpiry_shouldSucceed` | 过期后能重新加锁 |
| `release_shouldAllowRehold` | release 后同 key 可立即重新加锁 |

## 不在范围内

- 并发/多线程竞争测试（应在真实 DB 上进行）
- Spring Boot 上下文集成测试
- MySQL / PostgreSQL 方言兼容性测试
