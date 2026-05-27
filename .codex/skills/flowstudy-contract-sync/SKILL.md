---
name: flowstudy-contract-sync
description: Use this skill when checking or updating FlowStudy code against project contracts, especially REST API, Result<T>, ErrorCode, database schema, RabbitMQ message formats, environment variables, ports, or docker-compose conventions.

---

# FlowStudy Contract Sync Skill

Use this skill to keep FlowStudy implementation aligned with the project contract documents.

## Trigger examples

Use this skill when the user says things like:

- "检查这个接口是否符合契约"
- "根据接口文档实现这个 API"
- "同步 docs 和代码"
- "检查 Result<T> / ErrorCode / MQ 消息是否一致"
- "新增接口后更新文档"
- "对齐 FlowStudy contract"

## Scope

This skill applies to FlowStudy repositories, especially:

- `flowstudy-core`
- `flowstudy-judge`
- `flowstudy-ai`
- `flowstudy-frontend`
- `flowstudy-infra`

It is mainly for contract consistency, not for broad refactoring.

## Contract documents to read first

Before changing code, locate and read the relevant files. Prefer repo-local docs first.

Required when available:

- `docs/05-restful-api-contract.md`
- `docs/06-result-error-code-contract.md`
- `docs/07-database-design.md`
- `docs/08-rabbitmq-message-contract.md`
- `docs/15-deployment-docker-compose.md`
- `docs/flowstudy-core-plan.md`

If the files are not in the current repository, search common locations:

- `../flowstudy-infra/docs/`
- `../docs/`
- `.agents/references/`
- `docs/`

Do not invent contract details when the contract file exists. If the contract is missing or ambiguous, state the gap before modifying code.

## Main checks

### REST API contract

For HTTP APIs, verify:

- Path starts with `/api/v1` unless explicitly documented otherwise.
- HTTP method matches the contract.
- Request body, path variables, and query parameters match the contract.
- Protected APIs require authentication.
- Admin APIs require admin authorization.
- Controllers do not return raw `Entity` objects directly.
- Response objects use `VO` / response DTO objects.
- Input objects use request DTOs with validation annotations.

### Result and error code contract

Verify:

- Normal REST APIs return `Result<T>`.
- Paginated APIs return `Result<PageResult<T>>` or the project equivalent.
- Business errors use `BusinessException` and project `ErrorCode`.
- Error responses include `code`, `message`, `data`, `traceId`, and `timestamp`.
- `traceId` is generated, logged, and returned.
- SSE endpoints are not wrapped in `Result<T>`, but SSE error events still use `code`, `message`, and `traceId`.

### Database contract

Verify:

- Entity fields match database column names and types.
- `created_at`, `updated_at`, and logical delete fields follow project conventions.
- Unique constraints are enforced in service logic where needed.
- Query methods respect `deleted = 0` if logical deletion is used.
- Controllers do not expose sensitive fields such as `passwordHash`.

### RabbitMQ contract

Verify:

- Exchange, queue, and routing key names exactly match the MQ contract.
- Message envelope includes `schemaVersion`, `messageId`, `traceId`, `eventType`, `producer`, `occurredAt`, and `payload`.
- Consumers are idempotent.
- Failed messages are handled with retry or DLQ where applicable.
- MQ payload fields use the documented names and types.

### Environment and infrastructure contract

Verify:

- Secrets are read from environment variables or config files, not hardcoded.
- `.env` is ignored by Git.
- `.env.example` is safe to commit.
- Service ports match the docs.
- Docker service names, ports, usernames, and passwords match local dev docs.

## Modification rules

1. Read the relevant contract before editing code.
2. Prefer minimal changes that make implementation conform to the contract.
3. Do not silently change public API paths, response structures, MQ field names, or database column names.
4. If code and contract disagree, report the mismatch and ask whether to update code or docs unless the user explicitly asked for one direction.
5. Preserve existing behavior unless it violates the documented contract.
6. Add or update tests when changing contract-sensitive behavior.
7. Update docs only when the user asks to update docs or when the task explicitly includes documentation synchronization.

## Suggested workflow

1. Identify which contract area is involved: REST, Result/ErrorCode, DB, MQ, env, ports, or docker-compose.
2. Read the matching contract document.
3. Inspect the existing implementation.
4. Produce a short mismatch list.
5. Apply the smallest safe patch.
6. Run relevant checks when available:
    - Java: `mvn test`
    - Go: `go test ./...`
    - Python: `pytest`
    - Frontend: `npm test` or `npm run lint`
7. Summarize:
    - contract files consulted
    - files changed
    - mismatches fixed
    - checks run
    - remaining risks

## Output format

When reporting results, use this structure:

```md
## Contract sync summary

### Consulted docs
- ...

### Changes made
- ...

### Checks run
- ...

### Remaining issues
- ...
```

If no files are changed, still provide the mismatch analysis and recommended next steps.