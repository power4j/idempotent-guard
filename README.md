## Idempotent Guard

保护后端业务,防止前端重复提交。

## 使用方式

在你的应用中注册一个异常转换器,重复请求被拦截后会触发。

```java
@Bean
GuardExceptionTranslator guardExceptionTranslator(){
    return e -> Results.of(-1,"请勿重复提交");
}
```

> 通常情况下,你还需要写一个`@RestControllerAdvice`,否则前端会得到500错误。

例子

```java
@LockGuard(operation = "create",time = 15,resources = {
        @ResDescribe(name = "username",expr = "#dto.username")
})
@PostMapping("/users")
public Result<Void> createUser(@RequestBody UserDTO dto) {
    return Results.ok();
}
```

效果: 根据请求中的`username`值和操作ID`create`,创建一个锁,如果此方法尚未执行结束,再次请求这个接口会被服务端拒绝。


## 模块

### idempotent-guard-jdbc

使用数据库来模拟分布式锁,功能:

- 支持锁超时,即使程序发生bug未能释放锁,在到超时时间后,自动解除
- 自带后台清除线程,自动清除过期的锁

局限性

- 锁不可重入,因此不要在服务层使用,应该在Controller方法上使用


## 配置

```yaml
idempotent-guard:
  enabled: true # 开关,默认true
  # 使用数据库时的配置
  jdbc:
    table-name: lock_guard # 表名称,默认 lock_guard 
    clear-job-delay: 30000 # 后台清理任务的执行间隔,默认30s
    expire-delay: 30000 # 清理过期锁的额外时间,默认30s

```
