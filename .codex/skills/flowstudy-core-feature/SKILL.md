---
name: flowstudy-core-feature
description: Use this skill when implementing or modifying a feature in flowstudy-core, including Spring Boot Controller, Service, Mapper, DTO, VO, Result<T>, ErrorCode, JWT security, MyBatis-Plus, Redis limit, or RabbitMQ integration.

---

# FlowStudy Core Feature Skill

Use this skill to implement features in the `flowstudy-core` repository safely and consistently.

## Trigger examples

Use this skill when the user says things like:

- "实现登录接口"
- "开发文章列表接口"
- "实现代码提交接口"
- "接入 Judge 结果消费"
- "在 flowstudy-core 里加一个功能"
- "按照 core plan 开发下一阶段"

## Core service responsibility

`flowstudy-core` is the main business backend and orchestration service. It should handle:

- user registration, login, and authentication
- article, chapter, problem, and testcase metadata
- submission creation and query
- Redis-based rate limiting
- RabbitMQ task publishing and result consuming
- learning behavior event collection
- context APIs for `flowstudy-ai`

It should not directly run user code, implement Linux sandboxing, or perform complex LLM agent workflows. Those belong to `flowstudy-judge` and `flowstudy-ai`.

## Read before coding

Before changing code, inspect relevant docs and current code.

Recommended docs:

- `docs/flowstudy-core-plan.md`
- `docs/05-restful-api-contract.md`
- `docs/06-result-error-code-contract.md`
- `docs/07-database-design.md`
- `docs/08-rabbitmq-message-contract.md`
- `docs/13-auth-security-rate-limit.md`

If docs are not in the current repo, search sibling folders such as `../flowstudy-infra/docs/`.

## Preferred early-stage project structure

For early MVP development, the project may use a traditional layered structure:

```text
com.flowstudy.core
├── common
├── config
├── security
├── controller
├── service
│   └── impl
├── mapper
├── entity
├── dto
├── vo
├── enums
├── mq
└── admin
```

Do not force a large package refactor unless the user asks for it. Keep changes compatible with this early-stage structure.

## Implementation rules

### Controller rules

- Keep Controllers thin.
- Controllers should only handle routing, validation, and calling services.
- Controllers must return `Result<T>` for normal REST APIs.
- Controllers must not return database `Entity` objects directly.
- Use request DTOs for inputs and VO / response DTOs for outputs.
- Validate request DTOs with annotations such as `@NotBlank`, `@NotNull`, `@Size`, and `@Email` where appropriate.

### Service rules

- Put business logic in Service classes.
- Throw `BusinessException` for expected business failures.
- Use project `ErrorCode` values.
- Use transactions when multiple database writes must succeed or fail together.
- Never store plaintext passwords.
- Do not hardcode secrets, API keys, tokens, or passwords.

### Mapper and entity rules

- Use MyBatis-Plus conventions already present in the project.
- Match entity fields with database contract.
- Exclude sensitive fields from response VO objects.
- Respect logical deletion if `deleted` is used.
- Use indexed columns in query conditions where practical.

### Auth and security rules

- Passwords must be hashed, typically with BCrypt or the existing project encoder.
- Protected APIs should use the current user from the security context / user context.
- Admin APIs must check role or authority.
- Unauthenticated requests should map to error code `40100` if that code exists.
- Forbidden requests should map to error code `40300` if that code exists.

### Submission and Judge rules

For code submission features:

1. Validate user login.
2. Validate `problemId` exists.
3. Validate language is supported.
4. Apply Redis rate limit if available.
5. Create `fs_submission` with status `PENDING`.
6. Load testcases or testcase set reference according to the current contract.
7. Publish `judge.submit.created` message to RabbitMQ.
8. Return `submitId` and `PENDING` status.

Do not run user code inside `flowstudy-core`.

### Tracking rules

For behavior tracking features:

- Accept batch events where the API contract supports it.
- Validate event types.
- Save events to `fs_behavior_event`.
- Publish behavior messages to RabbitMQ if configured.
- Do not put AI analysis logic in Core.

## Development phases reference

When the user asks what to implement next, prefer this order:

1. project initialization and health check
2. common contract: `Result<T>`, `ErrorCode`, exception handling, `traceId`
3. database entities and mappers
4. auth and user APIs
5. article, chapter, problem query APIs
6. submission creation and RabbitMQ publish
7. judge result consumer and result persistence
8. Redis rate limiting
9. behavior tracking
10. AI context internal APIs
11. admin content management

## Testing rules

Add or update tests where the project already has a test setup.

Recommended tests:

- Controller tests with MockMvc for API behavior.
- Service tests for business rules.
- Mapper tests for database queries when test DB is available.
- MQ producer / consumer tests using mocks or integration tests.

Before finishing, run the most relevant available checks:

```bash
mvn test
```

If tests cannot run because the environment is missing dependencies, say so clearly and explain what was checked instead.

## Output summary

At the end of the task, summarize:

- feature implemented
- interfaces added or changed
- database tables touched
- MQ messages added or changed
- tests run
- known limitations or follow-up work