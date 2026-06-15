---
name: flowstudy-core-feature
description: Use this skill when implementing or modifying a feature in flowstudy-core, including Spring Boot Controller, Service, Mapper, DTO, VO, Result<T>, ErrorCode, JWT security, MyBatis-Plus, Redis limit, RabbitMQ integration, branch naming, and final commit message recommendation.

---

# FlowStudy Core Feature Skill

Use this skill to implement features in the `flowstudy-core` repository safely and consistently.

This skill is for coding assistance and workflow guidance. It may recommend a branch name and a commit message, but it must not run `git commit`, `git push`, or create a Pull Request unless the user explicitly asks for that action.

## Trigger examples

Use this skill when the user says things like:

- "实现登录接口"
- "开发文章列表接口"
- "实现代码提交接口"
- "接入 Judge 结果消费"
- "在 flowstudy-core 里加一个功能"
- "按照 core plan 开发下一阶段"
- "帮我为这个功能创建合适分支名"
- "这个功能完成后 commit message 应该怎么写"

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

## Git workflow guidance

Follow FlowStudy Git Flow, but do not perform commit or push automatically.

### Before editing

1. Inspect the current branch and working tree when possible:

```bash
git status
git branch --show-current
```

2. If the current branch is `main`, recommend creating a feature branch before coding.
3. If there are unrelated uncommitted changes, do not overwrite them. Ask the user before editing files that may conflict.
4. Use a branch name based on the task scope.

### Branch naming rules

Use this format:

```text
<type>/<scope>-<short-description>
```

Recommended `type` values:

- `feat` for new features or APIs
- `fix` for bug fixes
- `refactor` for internal restructuring without behavior change
- `docs` for documentation-only changes
- `test` for test-only changes
- `chore` for build, dependency, config, or maintenance tasks

Recommended branch names for FlowStudy Core phases:

```text
feat/core-init
feat/common-contract
feat/database-base
feat/auth-user
feat/auth-register-login
feat/article-chapter-query
feat/problem-query-api
feat/submission-mq-publish
feat/judge-result-consumer
feat/redis-rate-limit
feat/tracking-events
feat/ai-context-api
feat/admin-content-manage
```

If the user asks to start a new feature, output a recommended branch creation command, for example:

```bash
git switch main
git pull origin main
git switch -c feat/auth-register-login
```

Do not run these commands unless the user explicitly asks.

### Commit message recommendation rules

At the end of a task, output a suggested Angular Conventional Commit message only. Do not run `git commit`.

Use this format:

```text
<type>: <summary>
```

Examples:

```text
feat: add user authentication APIs
feat: add article and chapter query APIs
feat: add code submission and judge task publishing
feat: add judge result consumer
fix: handle expired JWT authentication errors
refactor: simplify global exception handling
```

If the task changes multiple areas, prefer the main user-visible change in the summary.

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

## Implementation workflow

When implementing a feature, follow this order:

1. Clarify the task scope.
2. Identify the matching development phase.
3. Recommend a branch name if the user has not already created one.
4. Read relevant docs and existing code.
5. Implement the smallest vertical slice that works end-to-end.
6. Keep API behavior aligned with the REST contract.
7. Keep database fields aligned with the database contract.
8. Keep MQ messages aligned with the RabbitMQ contract.
9. Add or update tests where the project already has a test setup.
10. Run the most relevant available checks.
11. Summarize changes and provide a suggested commit message.

Do not perform these actions unless explicitly requested:

- `git commit`
- `git push`
- creating a Pull Request
- deleting branches
- force pushing
- rebasing public branches

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

- recommended branch name, if a new branch is appropriate
- feature implemented
- interfaces added or changed
- database tables touched
- MQ messages added or changed
- tests run
- known limitations or follow-up work
- suggested commit message

The final suggested commit message should be shown as plain text, for example:

```text
feat: add user authentication APIs
```