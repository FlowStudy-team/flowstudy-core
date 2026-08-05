# flowstudy-core Agent Guide

## Service Responsibility

`flowstudy-core` 是 FlowStudy 核心业务服务，负责用户注册登录、JWT 鉴权、教程/博客/题目/文档 API、代码运行和提交记录、RabbitMQ 判题任务投递以及判题结果查询。它不负责前端渲染、代码沙箱执行、AI 模型调用或生产基础设施编排。

## Technology Stack

- Java 17：见 `pom.xml` 的 `<java.version>`
- Spring Boot 3.4.1：见 `pom.xml`
- Spring MVC、Spring Security、Spring AMQP、Validation
- MyBatis Spring Boot Starter 3.0.5
- MySQL Connector、H2 test database
- JWT：`io.jsonwebtoken:jjwt-* 0.12.6`

## Important Entry Points

- 启动入口：`src/main/java/com/flowstudy/core/FlowstudyCoreApplication.java`
- 配置：`src/main/resources/application.properties`、`application-example.yml`、`.env.example`
- 构建：`pom.xml`、`mvnw.cmd`、`mvnw`
- 测试：`src/test/java`、`src/test/resources/application-test.properties`、`schema.sql`
- 核心目录：`src/main/java/com/flowstudy/core`

## Key Modules

- `common/result`：`Result`、`PageResponse`
- `common/exception`：业务异常与全局异常处理
- `common/trace`：TraceId filter/context
- `security`：JWT、SecurityFilterChain、当前用户
- `module/auth`、`module/user`：注册、登录、用户信息
- `module/tutorial`、`module/blog`：教程和博客内容
- `module/problem`：题目、测试用例、代码模板
- `module/submission`：运行、提交、结果查询
- `module/submission/judge`：判题消息构造和 RabbitMQ 投递
- `module/document`：文档中心

## External Dependencies

- MySQL：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
- RabbitMQ：`RABBITMQ_HOST`、`RABBITMQ_PORT`、`RABBITMQ_USERNAME`、`RABBITMQ_PASSWORD`、`RABBITMQ_VHOST`
- JWT：`JWT_SECRET`、`JWT_EXPIRE_SECONDS`
- Judge 队列：`JUDGE_SUBMISSION_QUEUE`
- Redis：文档有规划，本仓库当前未发现 Redis 依赖或实现。

## Contracts

- REST API：[../flowstudy-infra/docs/05-restful-api-contract.md](../flowstudy-infra/docs/05-restful-api-contract.md)
- OpenAPI：[../flowstudy-infra/docs/api/FlowStudy_Apifox_OpenAPI.yaml](../flowstudy-infra/docs/api/FlowStudy_Apifox_OpenAPI.yaml)
- 错误码：[../flowstudy-infra/docs/06-result-error-code-contract.md](../flowstudy-infra/docs/06-result-error-code-contract.md)
- RabbitMQ：[../flowstudy-infra/docs/08-rabbitmq-message-contract.md](../flowstudy-infra/docs/08-rabbitmq-message-contract.md)
- 数据库：[../flowstudy-infra/docs/07-database-design.md](../flowstudy-infra/docs/07-database-design.md)
- 鉴权：[../flowstudy-infra/docs/13-auth-security-rate-limit.md](../flowstudy-infra/docs/13-auth-security-rate-limit.md)

当前已知：OpenAPI 路径和实际 `/api/v1` Controller 存在差异，修改 API 前必须先确认契约。

## Environment Variables

入口：`.env.example` 和 `src/main/resources/application.properties`。不要复制真实 `.env`。

必需项包括 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、RabbitMQ 连接变量、`JWT_SECRET`、`JWT_EXPIRE_SECONDS`、`JUDGE_SUBMISSION_QUEUE`。

## Validation Commands

```bash
./mvnw test
./mvnw spring-boot:run
```

Windows PowerShell 可使用：

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## Modification Rules

- API 修改必须同步 infra REST 文档、OpenAPI 和 frontend 调用。
- RabbitMQ 消息修改必须同步 Judge 的 `SubmissionMessage` 解析和 infra 消息契约。
- 数据库修改必须新增 `../flowstudy-infra/mysql/migration/` 脚本，并同步测试 schema。
- 鉴权修改必须覆盖登录、401、当前用户和越权测试。
- 不要引入真实密码、JWT secret 或生产连接串。
- 不要删除失败测试规避问题。

## Task Completion Checklist

完成任务时说明：修改了什么、为什么修改、运行了哪些命令、哪些验证通过、哪些未验证、是否影响契约、是否需要更新 infra 文档。
