
---
name: flowstudy-code-review
description: Use this skill when reviewing FlowStudy changes before commit or PR, especially checking API contracts, Result<T>, ErrorCode, traceId, security, RabbitMQ, database consistency, tests, secrets, and maintainability.

---

# FlowStudy Code Review Skill

Use this skill to review FlowStudy code changes before commit, merge, or pull request.

## Trigger examples

Use this skill when the user says things like:

- "帮我 review 当前改动"
- "提交 PR 前检查一下"
- "检查这段代码有没有问题"
- "检查是否符合 FlowStudy 规范"
- "review core 代码"

## Review mode

Default to review-only. Do not modify files unless the user explicitly asks to fix issues.

When asked to fix issues, make minimal patches and explain the change.

## Context to inspect

Start by inspecting the changed files:

```bash
git status --short
git diff --stat
git diff
```

If the user asks to review a branch against a base branch, use the requested base. If no base is provided, use the current working tree changes.

Read relevant docs when needed:

- `docs/05-restful-api-contract.md`
- `docs/06-result-error-code-contract.md`
- `docs/07-database-design.md`
- `docs/08-rabbitmq-message-contract.md`
- `docs/flowstudy-core-plan.md`
- `docs/16-git-workflow-engineering-rules.md`

## Review checklist

### Contract compliance

Check:

- API paths and HTTP methods match docs.
- Normal REST APIs return `Result<T>`.
- Paginated APIs use the project `PageResult` convention.
- Error responses use project `ErrorCode` and `BusinessException`.
- Response includes or preserves `traceId`.
- SSE endpoints use proper event format and error events.
- No controller returns raw database `Entity` objects.

### Architecture and layering

Check:

- Controller is thin.
- Business logic is in Service.
- Mapper is only for persistence queries.
- DTO and VO are used correctly.
- No circular or unnecessary dependencies.
- No broad refactor unrelated to the task.
- Package structure follows the current project style.

### Security

Check:

- No secrets, API keys, JWT secrets, passwords, or tokens are committed.
- Passwords are hashed, never stored or logged in plaintext.
- Protected APIs require authentication.
- Admin APIs enforce admin authorization.
- Internal APIs require internal service token or equivalent.
- User input is validated.
- Sensitive fields such as `passwordHash` are not returned.

### Database and transaction safety

Check:

- Entity fields match schema.
- Unique constraints are handled gracefully.
- Multi-write operations use transactions.
- Logical deletion conventions are followed.
- Index-friendly queries are used where possible.
- Submission and judge result writes are idempotent where needed.

### RabbitMQ

Check:

- Exchange, queue, and routing key names match contract.
- Message envelope includes required fields.
- `messageId` and `traceId` are present.
- Consumers are idempotent.
- Failures do not create infinite retry loops.
- Malformed messages are handled safely.

### Observability

Check:

- Logs contain useful context without leaking secrets.
- Logs include `traceId` where possible.
- Exceptions are not swallowed silently.
- Error messages are clear enough for debugging.

### Testing

Check:

- New business logic has tests where project conventions exist.
- Controller tests cover success and failure paths.
- Service tests cover important business rules.
- MQ tests or mock-based checks exist for message producers/consumers when practical.
- The test suite or relevant checks were run.

### Documentation

Check:

- New or changed public APIs are reflected in API docs.
- New error codes are documented.
- New MQ messages are documented.
- New env vars are added to `.env.example`.
- README or dev docs are updated when startup behavior changes.

## Severity levels

Use these severity labels:

- `BLOCKER`: must fix before merge; breaks build, security, data correctness, or public contract.
- `MAJOR`: should fix; likely bug or maintainability issue.
- `MINOR`: nice to fix; small clarity, style, or consistency issue.
- `QUESTION`: needs clarification.
- `PRAISE`: good implementation worth keeping.

## Output format

Use this format:

```md
## FlowStudy code review

### Summary
- Overall risk: LOW / MEDIUM / HIGH
- Main area: ...

### Findings

#### BLOCKER
- [file:line] Issue
  - Why it matters:
  - Suggested fix:

#### MAJOR
- ...

#### MINOR
- ...

#### QUESTIONS
- ...

### Contract checks
- REST API: pass / issues found
- Result/ErrorCode: pass / issues found
- Database: pass / issues found
- RabbitMQ: pass / issues found
- Security: pass / issues found

### Tests
- Tests observed:
- Tests run:
- Missing tests:
```

If no serious issues are found, say so clearly, but still mention any risks or missing tests.

## Fix mode

If the user asks to fix review findings:

1. Fix only the agreed issues.
2. Keep patches small.
3. Do not change public contracts unless explicitly requested.
4. Re-run relevant tests when possible.
5. Summarize files changed and remaining risks.