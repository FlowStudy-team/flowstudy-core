---
name: flowstudy-rabbitmq
description: Use this skill when implementing, reviewing, or debugging RabbitMQ in FlowStudy, including judge.submit.created, judge.result.finished, behavior events, note generation tasks, message envelope fields, retry, idempotency, and dead-letter queues.

---

# FlowStudy RabbitMQ Skill

Use this skill for all RabbitMQ-related work in FlowStudy.

## Trigger examples

Use this skill when the user says things like:

- "实现 judge.submit.created 消息"
- "实现 JudgeResultConsumer"
- "RabbitMQ 消费不到消息"
- "检查 MQ 消息格式"
- "给行为埋点投递 MQ"
- "设计死信队列和重试"

## Read before coding

Always read the current MQ contract first when available:

- `docs/08-rabbitmq-message-contract.md`
- `docs/flowstudy-core-plan.md`
- `docs/06-result-error-code-contract.md`

If docs are not in the current repository, search sibling repositories such as `../flowstudy-infra/docs/`.

## Core MQ responsibilities

In `flowstudy-core`, RabbitMQ is used for:

- publishing code judge tasks to `flowstudy-judge`
- consuming judge results from `flowstudy-judge`
- publishing learning behavior events to `flowstudy-ai`
- publishing AI note generation tasks when needed

## Message envelope standard

All application-level MQ messages should use a consistent envelope unless the project contract explicitly says otherwise:

```json
{
  "schemaVersion": "1.0",
  "messageId": "msg-unique-id",
  "traceId": "trace-id",
  "eventType": "judge.submit.created",
  "producer": "flowstudy-core",
  "occurredAt": "2026-05-27T10:30:00+08:00",
  "payload": {}
}
```

Do not omit `messageId` or `traceId`.

## Common FlowStudy messages

### Judge submit task

Expected semantic event:

```text
judge.submit.created
```

Producer:

```text
flowstudy-core
```

Consumer:

```text
flowstudy-judge
```

Payload usually includes:

- `submitId`
- `userId`
- `problemId`
- `language`
- `code`
- `timeLimitMs`
- `memoryLimitMb`
- `testCases` or `testCaseSetId`

### Judge result

Expected semantic event:

```text
judge.result.finished
```

Producer:

```text
flowstudy-judge
```

Consumer:

```text
flowstudy-core
```

Payload usually includes:

- `submitId`
- `status`
- `score`
- `timeUsedMs`
- `memoryUsedKb`
- `compileMessage`
- `runtimeMessage`
- `caseResults`

### Behavior events

Expected semantic event examples:

```text
behavior.chapter.view
behavior.code.submit
behavior.ai.question
behavior.judge.error
```

Producer:

```text
flowstudy-core
```

Consumer:

```text
flowstudy-ai
```

## Implementation rules

### Producer rules

- Generate a globally unique `messageId`.
- Reuse current request `traceId` when available.
- Set a clear `eventType` matching the routing key semantics.
- Set `producer` to the service name.
- Use ISO-8601 time for `occurredAt` where practical.
- Serialize payload with stable field names matching the contract.
- Log publish success and failure with `messageId` and `traceId`.
- Do not publish messages before the related database state is durable.

For submission publishing, prefer:

1. write `fs_submission` as `PENDING`
2. commit transaction or use reliable publish strategy
3. publish `judge.submit.created`

If strict reliability is needed, recommend an outbox pattern instead of best-effort publish.

### Consumer rules

- Consumers must be idempotent.
- Check `messageId` or use natural idempotency keys such as `submitId` + `caseIndex`.
- Never assume a message is delivered exactly once.
- Validate required fields before processing.
- Log `messageId`, `traceId`, and `eventType`.
- Reject or dead-letter malformed messages according to the project convention.
- Avoid infinite retry loops.

### Judge result consumer rules

When consuming `judge.result.finished` in Core:

1. Validate `submitId` exists.
2. Ensure status transition is valid.
3. Update `fs_submission`.
4. Upsert or replace case results safely.
5. Avoid duplicate inserts on repeated delivery.
6. Acknowledge only after durable persistence succeeds.

### Error handling

Classify failures:

- malformed message: reject / DLQ
- temporary DB failure: retry if supported
- duplicate message: treat as success after verifying state
- unknown submitId: log and route to DLQ or ignored queue depending on contract

## Debugging workflow

When debugging RabbitMQ issues:

1. Check broker connection config: host, port, username, password, vhost.
2. Check exchange exists and type matches contract.
3. Check queue exists.
4. Check binding routing key.
5. Check producer logs for publish confirm or exceptions.
6. Check RabbitMQ management UI for message counts.
7. Check consumer startup logs.
8. Check serialization/deserialization errors.
9. Check manual ack / auto ack behavior.
10. Check DLQ for failed messages.

Useful local checks when available:

```bash
docker compose ps
docker compose logs -f rabbitmq
```

## Output summary

After MQ work, report:

- exchanges / queues / routing keys involved
- producer or consumer classes changed
- message payload fields
- idempotency strategy
- retry / DLQ handling
- tests or manual checks run