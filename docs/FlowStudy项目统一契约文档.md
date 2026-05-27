# FlowStudy 项目统一契约文档

> 版本：v1.0  
> 适用范围：`flowstudy-core`、`flowstudy-judge`、`flowstudy-ai`、`flowstudy-frontend`  
> 目标：统一 RESTful API、RabbitMQ 消息、数据库表结构、环境变量、Docker Compose、本地端口和统一返回格式，保证多仓库、多语言、多服务并行开发时的接口一致性。

---

## 0. 项目服务划分

FlowStudy 采用多仓库、多服务架构，当前推荐服务划分如下：

```text
flowstudy-frontend   前端项目，负责页面展示、Markdown 阅读、Monaco Editor、AI 侧边栏
flowstudy-core       Java Spring Boot 核心业务服务，负责用户、文章、题目、提交、限流、MQ 投递
flowstudy-judge      Go / C++ 判题服务，负责消费判题任务、运行用户代码、返回判题结果
flowstudy-ai         Python FastAPI AI 服务，负责流式问答、上下文拼接、行为分析、笔记生成
.github              GitHub 组织级配置仓库，负责 Issue 模板、PR 模板、贡献指南等
```

建议后续根据复杂度新增：

```text
flowstudy-infra      基础设施仓库，统一存放 docker-compose、SQL、部署脚本、接口契约、MQ 契约
```

其中 `flowstudy-infra` 不是业务服务，而是所有服务共同依赖的基础设施与契约仓库。

---

# 一、RESTful API 接口定义

## 1.1 API 基本规范

统一 API 前缀：

```text
/api/v1
```

统一认证方式：

```http
Authorization: Bearer <access_token>
```

统一请求与响应数据格式：

```http
Content-Type: application/json
```

除 AI SSE 流式接口外，所有接口均返回统一结构：

```text
Result<T>
```

分页接口统一使用：

```text
page: 当前页，从 1 开始
size: 每页数量
total: 总记录数
records: 当前页数据列表
```

---

## 1.2 认证与用户模块

### 1.2.1 用户注册

```http
POST /api/v1/auth/register
```

请求体：

```json
{
  "username": "wdd",
  "email": "wdd@example.com",
  "password": "12345678",
  "nickname": "Flow Learner"
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 10001
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.2.2 用户登录

```http
POST /api/v1/auth/login
```

请求体：

```json
{
  "account": "wdd",
  "password": "12345678"
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 10001,
      "username": "wdd",
      "nickname": "Flow Learner",
      "role": "USER"
    }
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.2.3 获取当前用户信息

```http
GET /api/v1/users/me
```

请求头：

```http
Authorization: Bearer <access_token>
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 10001,
    "username": "wdd",
    "email": "wdd@example.com",
    "nickname": "Flow Learner",
    "avatarUrl": null,
    "role": "USER"
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

## 1.3 文章与章节模块

### 1.3.1 获取文章列表

```http
GET /api/v1/articles?page=1&size=10&keyword=java
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Java 并发编程基础",
        "summary": "从线程、锁、线程池到并发容器",
        "coverUrl": "",
        "authorName": "admin",
        "chapterCount": 8,
        "problemCount": 12,
        "viewCount": 1024
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.3.2 获取文章详情

```http
GET /api/v1/articles/{articleId}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Java 并发编程基础",
    "summary": "从线程、锁、线程池到并发容器",
    "coverUrl": "",
    "authorName": "admin",
    "viewCount": 1024,
    "chapterCount": 8,
    "problemCount": 12,
    "createdAt": "2026-05-27T10:30:00+08:00"
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.3.3 获取文章下的章节列表

```http
GET /api/v1/articles/{articleId}/chapters
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 10,
      "articleId": 1,
      "title": "线程池的基本原理",
      "sortOrder": 1,
      "estimatedMinutes": 20
    }
  ],
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.3.4 获取章节详情

```http
GET /api/v1/chapters/{chapterId}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 10,
    "articleId": 1,
    "title": "线程池的基本原理",
    "contentMd": "## 线程池\n这里是 Markdown 内容...",
    "sortOrder": 1,
    "estimatedMinutes": 20,
    "problems": [
      {
        "id": 100,
        "title": "实现一个简单线程池",
        "difficulty": "MEDIUM"
      }
    ]
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

## 1.4 题目与代码提交模块

### 1.4.1 获取题目列表

```http
GET /api/v1/problems?chapterId=10&page=1&size=10
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 100,
        "chapterId": 10,
        "title": "两数之和",
        "difficulty": "EASY",
        "timeLimitMs": 1000,
        "memoryLimitMb": 256
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.4.2 获取题目详情

```http
GET /api/v1/problems/{problemId}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 100,
    "chapterId": 10,
    "title": "两数之和",
    "descriptionMd": "给定一个整数数组 nums...",
    "difficulty": "EASY",
    "inputDescription": "第一行输入 n...",
    "outputDescription": "输出结果...",
    "sampleCases": [
      {
        "input": "4\n2 7 11 15\n9",
        "output": "0 1"
      }
    ],
    "supportLanguages": ["java", "cpp", "go", "python"],
    "timeLimitMs": 1000,
    "memoryLimitMb": 256
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.4.3 获取代码模板

```http
GET /api/v1/problems/{problemId}/template?language=java
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "language": "java",
    "code": "public class Main {\n    public static void main(String[] args) {\n        \n    }\n}"
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

### 1.4.4 提交代码

```http
POST /api/v1/problems/{problemId}/submissions
```

请求体：

```json
{
  "language": "java",
  "code": "public class Main { public static void main(String[] args) { } }"
}
```

返回：

```json
{
  "code": 0,
  "message": "submit success",
  "data": {
    "submitId": 90001,
    "status": "PENDING"
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

说明：前端提交代码后，`flowstudy-core` 不直接判题，而是先将提交记录写入数据库，再向 RabbitMQ 投递判题任务，由 `flowstudy-judge` 异步消费。

---

### 1.4.5 查询提交结果

```http
GET /api/v1/submissions/{submitId}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "submitId": 90001,
    "problemId": 100,
    "language": "java",
    "status": "ACCEPTED",
    "timeUsedMs": 12,
    "memoryUsedKb": 20480,
    "score": 100,
    "errorMessage": null,
    "caseResults": [
      {
        "caseIndex": 1,
        "status": "ACCEPTED",
        "timeUsedMs": 4,
        "memoryUsedKb": 10240
      }
    ]
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

判题状态枚举：

```text
PENDING
RUNNING
ACCEPTED
WRONG_ANSWER
COMPILE_ERROR
RUNTIME_ERROR
TIME_LIMIT_EXCEEDED
MEMORY_LIMIT_EXCEEDED
SYSTEM_ERROR
```

---

## 1.5 学习行为埋点接口

### 1.5.1 批量上报行为事件

```http
POST /api/v1/tracking/events
```

请求体：

```json
{
  "events": [
    {
      "eventType": "CHAPTER_VIEW",
      "articleId": 1,
      "chapterId": 10,
      "problemId": null,
      "durationSeconds": 35,
      "extra": {
        "scrollPercent": 80
      },
      "occurredAt": "2026-05-27T10:30:00+08:00"
    }
  ]
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accepted": 1
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

事件类型枚举：

```text
ARTICLE_VIEW
CHAPTER_VIEW
CHAPTER_LEAVE
CODE_EDIT
CODE_SUBMIT
JUDGE_ERROR_VIEW
AI_QUESTION
AI_ANSWER_VIEW
NOTE_GENERATE
```

---

## 1.6 AI 侧边栏接口

AI 服务独立部署，但生产环境建议通过 Nginx 或网关统一转发：

```text
/api/v1/ai/**
```

---

### 1.6.1 AI 流式问答

```http
POST /api/v1/ai/chat/stream
```

请求体：

```json
{
  "conversationId": null,
  "articleId": 1,
  "chapterId": 10,
  "problemId": 100,
  "submitId": 90001,
  "question": "为什么我这里会数组越界？"
}
```

返回类型：

```http
Content-Type: text/event-stream
```

SSE 数据格式：

```text
event: delta
data: {"content":"你这里的问题是..."}

event: delta
data: {"content":"数组下标从 0 开始..."}

event: done
data: {"conversationId":30001}
```

说明：AI 服务在回答前需要根据 `articleId`、`chapterId`、`problemId`、`submitId` 获取上下文。MVP 阶段推荐由 AI 服务调用 Core 内部接口获取上下文；后续也可以由 Core 拼接上下文后转发给 AI。

---

### 1.6.2 生成个性化学习笔记

```http
POST /api/v1/ai/notes/generate
```

请求体：

```json
{
  "articleId": 1,
  "chapterId": 10
}
```

返回：

```json
{
  "code": 0,
  "message": "note generation task submitted",
  "data": {
    "taskId": "note-task-001",
    "status": "PENDING"
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

# 二、RabbitMQ 消息格式定义

## 2.1 RabbitMQ 命名规范

统一使用 topic exchange。

```text
Exchange 命名：fs.{domain}.exchange
Queue 命名：fs.{service}.{purpose}.queue
RoutingKey 命名：{domain}.{action}.{status}
```

示例：

```text
fs.judge.exchange
fs.judge.submit.queue
judge.submit.created
```

---

## 2.2 Exchange 与 Queue 设计

| Exchange | 类型 | 作用 |
|---|---|---|
| `fs.judge.exchange` | topic | Core 投递判题任务 |
| `fs.judge.result.exchange` | topic | Judge 回传判题结果 |
| `fs.behavior.exchange` | topic | Core 投递用户行为事件 |
| `fs.ai.exchange` | topic | AI 画像、笔记等异步任务 |
| `fs.dlx.exchange` | topic | 死信交换机 |

| Queue | 绑定 Exchange | RoutingKey | 消费者 |
|---|---|---|---|
| `fs.judge.submit.queue` | `fs.judge.exchange` | `judge.submit.created` | `flowstudy-judge` |
| `fs.core.judge-result.queue` | `fs.judge.result.exchange` | `judge.result.finished` | `flowstudy-core` |
| `fs.ai.behavior.queue` | `fs.behavior.exchange` | `behavior.#` | `flowstudy-ai` |
| `fs.ai.note.queue` | `fs.ai.exchange` | `ai.note.generate` | `flowstudy-ai` |
| `fs.dlq.queue` | `fs.dlx.exchange` | `dlq.#` | 人工排查 |

---

## 2.3 通用消息外壳

所有 MQ 消息统一使用以下外壳：

```json
{
  "schemaVersion": "1.0",
  "messageId": "msg-20260527-000001",
  "traceId": "9f2c1a7e",
  "eventType": "judge.submit.created",
  "producer": "flowstudy-core",
  "occurredAt": "2026-05-27T10:30:00+08:00",
  "payload": {}
}
```

字段说明：

| 字段 | 含义 |
|---|---|
| `schemaVersion` | 消息结构版本 |
| `messageId` | 消息唯一 ID，用于幂等 |
| `traceId` | 链路追踪 ID |
| `eventType` | 事件类型 |
| `producer` | 生产者服务 |
| `occurredAt` | 事件发生时间 |
| `payload` | 具体业务数据 |

---

## 2.4 判题任务消息

Exchange：

```text
fs.judge.exchange
```

RoutingKey：

```text
judge.submit.created
```

消息体：

```json
{
  "schemaVersion": "1.0",
  "messageId": "msg-judge-000001",
  "traceId": "9f2c1a7e",
  "eventType": "judge.submit.created",
  "producer": "flowstudy-core",
  "occurredAt": "2026-05-27T10:30:00+08:00",
  "payload": {
    "submitId": 90001,
    "userId": 10001,
    "problemId": 100,
    "language": "java",
    "code": "public class Main { }",
    "timeLimitMs": 1000,
    "memoryLimitMb": 256,
    "testCases": [
      {
        "caseId": 1,
        "input": "4\n2 7 11 15\n9",
        "expectedOutput": "0 1",
        "isSample": true
      }
    ]
  }
}
```

说明：MVP 阶段可以直接把测试用例放进 MQ。后期测试用例变大后，建议改成只传 `testCaseSetId`，由 Judge 服务从数据库、对象存储或本地只读挂载目录中读取完整测试用例。

---

## 2.5 判题结果消息

Exchange：

```text
fs.judge.result.exchange
```

RoutingKey：

```text
judge.result.finished
```

消息体：

```json
{
  "schemaVersion": "1.0",
  "messageId": "msg-result-000001",
  "traceId": "9f2c1a7e",
  "eventType": "judge.result.finished",
  "producer": "flowstudy-judge",
  "occurredAt": "2026-05-27T10:30:05+08:00",
  "payload": {
    "submitId": 90001,
    "status": "ACCEPTED",
    "score": 100,
    "timeUsedMs": 12,
    "memoryUsedKb": 20480,
    "compileMessage": null,
    "runtimeMessage": null,
    "caseResults": [
      {
        "caseId": 1,
        "caseIndex": 1,
        "status": "ACCEPTED",
        "timeUsedMs": 4,
        "memoryUsedKb": 10240,
        "actualOutput": "0 1",
        "expectedOutput": "0 1"
      }
    ]
  }
}
```

---

## 2.6 用户行为消息

Exchange：

```text
fs.behavior.exchange
```

RoutingKey 示例：

```text
behavior.chapter.view
behavior.code.submit
behavior.ai.question
behavior.judge.error
```

消息体：

```json
{
  "schemaVersion": "1.0",
  "messageId": "msg-behavior-000001",
  "traceId": "9f2c1a7e",
  "eventType": "behavior.chapter.view",
  "producer": "flowstudy-core",
  "occurredAt": "2026-05-27T10:30:00+08:00",
  "payload": {
    "userId": 10001,
    "articleId": 1,
    "chapterId": 10,
    "problemId": null,
    "eventType": "CHAPTER_VIEW",
    "durationSeconds": 35,
    "extra": {
      "scrollPercent": 80
    }
  }
}
```

---

## 2.7 AI 笔记生成任务消息

Exchange：

```text
fs.ai.exchange
```

RoutingKey：

```text
ai.note.generate
```

消息体：

```json
{
  "schemaVersion": "1.0",
  "messageId": "msg-note-000001",
  "traceId": "9f2c1a7e",
  "eventType": "ai.note.generate",
  "producer": "flowstudy-core",
  "occurredAt": "2026-05-27T10:30:00+08:00",
  "payload": {
    "userId": 10001,
    "articleId": 1,
    "chapterId": 10,
    "noteTaskId": "note-task-001"
  }
}
```

---

# 三、数据库表结构定义

数据库名：

```sql
CREATE DATABASE IF NOT EXISTS flowstudy
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;
```

建议统一使用：

```text
数据库：MySQL 8.x
字符集：utf8mb4
排序规则：utf8mb4_unicode_ci
存储引擎：InnoDB
```

---

## 3.1 用户表：sys_user

```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    avatar_url VARCHAR(512) DEFAULT NULL COMMENT '头像',
    role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

---

## 3.2 文章表：fs_article

```sql
CREATE TABLE fs_article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
    title VARCHAR(255) NOT NULL COMMENT '文章标题',
    summary VARCHAR(512) DEFAULT NULL COMMENT '文章摘要',
    cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
    view_count BIGINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_author_id (author_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
```

---

## 3.3 章节表：fs_chapter

```sql
CREATE TABLE fs_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    title VARCHAR(255) NOT NULL COMMENT '章节标题',
    content_md MEDIUMTEXT NOT NULL COMMENT 'Markdown 内容',
    sort_order INT NOT NULL DEFAULT 0,
    estimated_minutes INT DEFAULT NULL COMMENT '预计学习分钟数',
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';
```

---

## 3.4 题目表：fs_problem

```sql
CREATE TABLE fs_problem (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    chapter_id BIGINT NOT NULL COMMENT '所属章节ID',
    title VARCHAR(255) NOT NULL COMMENT '题目标题',
    description_md MEDIUMTEXT NOT NULL COMMENT '题目描述',
    difficulty VARCHAR(32) NOT NULL DEFAULT 'EASY' COMMENT 'EASY/MEDIUM/HARD',
    input_description TEXT DEFAULT NULL,
    output_description TEXT DEFAULT NULL,
    support_languages VARCHAR(255) NOT NULL DEFAULT 'java,cpp,go,python',
    time_limit_ms INT NOT NULL DEFAULT 1000,
    memory_limit_mb INT NOT NULL DEFAULT 256,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_chapter_id (chapter_id),
    KEY idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';
```

---

## 3.5 题目测试用例表：fs_problem_testcase

```sql
CREATE TABLE fs_problem_testcase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '测试用例ID',
    problem_id BIGINT NOT NULL COMMENT '题目ID',
    input_text MEDIUMTEXT NOT NULL COMMENT '输入',
    expected_output MEDIUMTEXT NOT NULL COMMENT '期望输出',
    is_sample TINYINT NOT NULL DEFAULT 0 COMMENT '是否样例',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_problem_id (problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目测试用例表';
```

---

## 3.6 代码提交记录表：fs_submission

```sql
CREATE TABLE fs_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    problem_id BIGINT NOT NULL COMMENT '题目ID',
    language VARCHAR(32) NOT NULL COMMENT '语言',
    code MEDIUMTEXT NOT NULL COMMENT '提交代码',
    status VARCHAR(64) NOT NULL DEFAULT 'PENDING' COMMENT '判题状态',
    score INT DEFAULT 0 COMMENT '得分',
    time_used_ms INT DEFAULT NULL COMMENT '运行耗时',
    memory_used_kb INT DEFAULT NULL COMMENT '内存占用',
    compile_message MEDIUMTEXT DEFAULT NULL COMMENT '编译信息',
    runtime_message MEDIUMTEXT DEFAULT NULL COMMENT '运行错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_problem_id (problem_id),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码提交记录表';
```

---

## 3.7 单个测试点结果表：fs_judge_case_result

```sql
CREATE TABLE fs_judge_case_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '测试点结果ID',
    submission_id BIGINT NOT NULL COMMENT '提交ID',
    testcase_id BIGINT DEFAULT NULL COMMENT '测试用例ID',
    case_index INT NOT NULL COMMENT '测试点序号',
    status VARCHAR(64) NOT NULL COMMENT 'ACCEPTED/WRONG_ANSWER/TLE/RE',
    time_used_ms INT DEFAULT NULL,
    memory_used_kb INT DEFAULT NULL,
    actual_output MEDIUMTEXT DEFAULT NULL,
    expected_output MEDIUMTEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_submission_id (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试点结果表';
```

---

## 3.8 用户行为事件表：fs_behavior_event

```sql
CREATE TABLE fs_behavior_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行为事件ID',
    user_id BIGINT NOT NULL,
    article_id BIGINT DEFAULT NULL,
    chapter_id BIGINT DEFAULT NULL,
    problem_id BIGINT DEFAULT NULL,
    submission_id BIGINT DEFAULT NULL,
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    duration_seconds INT DEFAULT NULL COMMENT '停留时间',
    extra_json JSON DEFAULT NULL COMMENT '扩展信息',
    occurred_at DATETIME NOT NULL COMMENT '事件发生时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_event_type (event_type),
    KEY idx_article_chapter (article_id, chapter_id),
    KEY idx_occurred_at (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为事件表';
```

---

## 3.9 AI 会话表：fs_ai_conversation

```sql
CREATE TABLE fs_ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL,
    article_id BIGINT DEFAULT NULL,
    chapter_id BIGINT DEFAULT NULL,
    problem_id BIGINT DEFAULT NULL,
    title VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';
```

---

## 3.10 AI 消息表：fs_ai_message

```sql
CREATE TABLE fs_ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL COMMENT 'user/assistant/system',
    content MEDIUMTEXT NOT NULL,
    model_name VARCHAR(128) DEFAULT NULL,
    token_count INT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_conversation_id (conversation_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';
```

---

## 3.11 用户画像表：fs_user_profile

```sql
CREATE TABLE fs_user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ability_json JSON DEFAULT NULL COMMENT '能力画像',
    weak_points_json JSON DEFAULT NULL COMMENT '易错点',
    coding_style_json JSON DEFAULT NULL COMMENT '代码风格',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习画像表';
```

---

## 3.12 学习笔记表：fs_learning_note

```sql
CREATE TABLE fs_learning_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    article_id BIGINT DEFAULT NULL,
    chapter_id BIGINT DEFAULT NULL,
    title VARCHAR(255) NOT NULL,
    content_md MEDIUMTEXT NOT NULL,
    source VARCHAR(32) NOT NULL DEFAULT 'AI' COMMENT 'AI/MANUAL',
    status VARCHAR(32) NOT NULL DEFAULT 'GENERATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_article_chapter (article_id, chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习笔记表';
```

---

# 四、环境变量规范

所有仓库都必须提交 `.env.example`，但禁止提交真实 `.env`。

`.gitignore` 必须包含：

```text
.env
.env.local
.env.production
```

---

## 4.1 flowstudy-core 环境变量

```env
APP_NAME=flowstudy-core
APP_PORT=8080

SPRING_PROFILES_ACTIVE=dev

MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=flowstudy
MYSQL_USERNAME=flowstudy
MYSQL_PASSWORD=flowstudy123

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=flowstudy
RABBITMQ_PASSWORD=flowstudy123
RABBITMQ_VHOST=/

JWT_SECRET=please-change-this-secret
JWT_EXPIRE_SECONDS=7200

AI_SERVICE_BASE_URL=http://localhost:8000
JUDGE_SUBMIT_EXCHANGE=fs.judge.exchange
JUDGE_SUBMIT_ROUTING_KEY=judge.submit.created

CORS_ALLOWED_ORIGINS=http://localhost:5173
SUBMIT_RATE_LIMIT_PER_MINUTE=20
```

---

## 4.2 flowstudy-judge 环境变量

```env
APP_NAME=flowstudy-judge
APP_PORT=9000

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=flowstudy
RABBITMQ_PASSWORD=flowstudy123
RABBITMQ_VHOST=/

JUDGE_SUBMIT_QUEUE=fs.judge.submit.queue
JUDGE_RESULT_EXCHANGE=fs.judge.result.exchange
JUDGE_RESULT_ROUTING_KEY=judge.result.finished

SANDBOX_WORK_DIR=/tmp/flowstudy-sandbox
SANDBOX_MAX_CONCURRENCY=4
SANDBOX_DEFAULT_TIME_LIMIT_MS=1000
SANDBOX_DEFAULT_MEMORY_LIMIT_MB=256

ENABLE_DOCKER_SANDBOX=true
DOCKER_JAVA_IMAGE=flowstudy/java-runner:17
DOCKER_CPP_IMAGE=flowstudy/cpp-runner:latest
DOCKER_GO_IMAGE=flowstudy/go-runner:1.22
DOCKER_PYTHON_IMAGE=flowstudy/python-runner:3.11
```

---

## 4.3 flowstudy-ai 环境变量

```env
APP_NAME=flowstudy-ai
APP_PORT=8000

CORE_SERVICE_BASE_URL=http://localhost:8080
INTERNAL_API_TOKEN=please-change-this-internal-token

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=flowstudy
RABBITMQ_PASSWORD=flowstudy123
RABBITMQ_VHOST=/

AI_BEHAVIOR_QUEUE=fs.ai.behavior.queue
AI_NOTE_QUEUE=fs.ai.note.queue

LLM_PROVIDER=openai
LLM_BASE_URL=https://api.openai.com/v1
LLM_API_KEY=your-api-key
LLM_CHAT_MODEL=gpt-4o-mini
LLM_EMBEDDING_MODEL=text-embedding-3-small

VECTOR_STORE_TYPE=local
VECTOR_STORE_DIR=./data/vector_store

CORS_ALLOWED_ORIGINS=http://localhost:5173
```

---

## 4.4 flowstudy-frontend 环境变量

```env
VITE_APP_NAME=FlowStudy
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_AI_BASE_URL=http://localhost:8000/api/v1
VITE_ENABLE_AI_SIDEBAR=true
VITE_ENABLE_TRACKING=true
```

---

# 五、docker-compose 启动方式

建议放置位置：

```text
flowstudy-infra/docker-compose.yml
```

MVP 阶段先只启动中间件：MySQL、Redis、RabbitMQ。

```yaml
version: "3.9"

services:
  mysql:
    image: mysql:8.4
    container_name: flowstudy-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: flowstudy
      MYSQL_USER: flowstudy
      MYSQL_PASSWORD: flowstudy123
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - flowstudy_mysql_data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    networks:
      - flowstudy-net

  redis:
    image: redis:7.2-alpine
    container_name: flowstudy-redis
    restart: always
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - flowstudy_redis_data:/data
    networks:
      - flowstudy-net

  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: flowstudy-rabbitmq
    restart: always
    environment:
      RABBITMQ_DEFAULT_USER: flowstudy
      RABBITMQ_DEFAULT_PASS: flowstudy123
      RABBITMQ_DEFAULT_VHOST: /
      TZ: Asia/Shanghai
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - flowstudy_rabbitmq_data:/var/lib/rabbitmq
    networks:
      - flowstudy-net

volumes:
  flowstudy_mysql_data:
  flowstudy_redis_data:
  flowstudy_rabbitmq_data:

networks:
  flowstudy-net:
    driver: bridge
```

启动命令：

```bash
cd flowstudy-infra
docker compose up -d
```

查看容器：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f rabbitmq
```

停止服务：

```bash
docker compose down
```

清空所有数据并重新启动：

```bash
docker compose down -v
docker compose up -d
```

RabbitMQ 管理后台：

```text
http://localhost:15672
username: flowstudy
password: flowstudy123
```

---

# 六、各服务端口号定义

| 服务 | 本地端口 | 说明 |
|---|---:|---|
| `flowstudy-frontend` | `5173` | Vite 前端开发服务 |
| `flowstudy-core` | `8080` | Java 核心业务服务 |
| `flowstudy-ai` | `8000` | Python AI 服务 |
| `flowstudy-judge` | `9000` | Go 判题服务健康检查端口 |
| MySQL | `3306` | 数据库 |
| Redis | `6379` | 缓存、限流 |
| RabbitMQ | `5672` | MQ 通信端口 |
| RabbitMQ Management | `15672` | MQ 管理后台 |
| Nginx | `80 / 443` | 生产环境统一入口 |

本地访问关系：

```text
frontend  -> core      http://localhost:8080/api/v1
frontend  -> ai        http://localhost:8000/api/v1
core      -> rabbitmq  localhost:5672
judge     -> rabbitmq  localhost:5672
ai        -> rabbitmq  localhost:5672
core      -> mysql     localhost:3306
core      -> redis     localhost:6379
```

生产环境建议：

```text
https://flowstudy.com/api/v1/**       -> flowstudy-core
https://flowstudy.com/api/v1/ai/**    -> flowstudy-ai
https://flowstudy.com/                -> flowstudy-frontend
```

Judge 服务不直接暴露公网，只通过 RabbitMQ 与 Core 服务通信。

---

# 七、统一返回格式 Result<T>

## 7.1 标准结构

Java 后端统一返回：

```java
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    private String traceId;

    private Long timestamp;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
```

---

## 7.2 成功返回

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

## 7.3 失败返回

```json
{
  "code": 40000,
  "message": "invalid request parameter",
  "data": null,
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

## 7.4 分页返回结构

Java 分页结构：

```java
public class PageResult<T> {

    private List<T> records;

    private Long total;

    private Integer page;

    private Integer size;
}
```

JSON 示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  },
  "traceId": "9f2c1a7e",
  "timestamp": 1710000000000
}
```

---

## 7.5 错误码定义

| 错误码 | 含义 |
|---:|---|
| `0` | 成功 |
| `40000` | 请求参数错误 |
| `40100` | 未登录或 Token 无效 |
| `40300` | 无权限 |
| `40400` | 资源不存在 |
| `40900` | 数据冲突，例如用户名重复 |
| `42900` | 请求过于频繁，被限流 |
| `50000` | 系统内部错误 |
| `53000` | 判题服务异常 |
| `54000` | AI 服务异常 |

---

# 八、推荐开发顺序

## 8.1 第一阶段：基础设施

目标仓库：

```text
flowstudy-infra
```

主要任务：

```text
定义 docker-compose.yml
定义 mysql/init/init.sql
启动 MySQL / Redis / RabbitMQ
编写 docs/flowstudy-contract.md
```

---

## 8.2 第二阶段：Core 主链路

目标仓库：

```text
flowstudy-core
```

主要任务：

```text
用户注册登录
文章列表 / 章节详情 / 题目详情
代码提交接口
提交记录入库
投递 judge.submit.created 消息
```

---

## 8.3 第三阶段：Judge 最小可运行版本

目标仓库：

```text
flowstudy-judge
```

主要任务：

```text
消费 fs.judge.submit.queue
运行最简单的 Java / C++ / Python 代码
生成判题结果
投递 judge.result.finished 消息
```

---

## 8.4 第四阶段：Core 消费判题结果

目标仓库：

```text
flowstudy-core
```

主要任务：

```text
消费 fs.core.judge-result.queue
更新 fs_submission 状态
写入 fs_judge_case_result
提供提交结果查询接口
```

---

## 8.5 第五阶段：Frontend 页面闭环

目标仓库：

```text
flowstudy-frontend
```

主要任务：

```text
登录页面
文章阅读页面
题目详情页面
Monaco Editor
代码提交
轮询展示判题结果
```

---

## 8.6 第六阶段：AI 能力接入

目标仓库：

```text
flowstudy-ai
```

主要任务：

```text
AI 侧边栏问答
SSE 流式输出
根据文章 / 章节 / 提交记录拼接上下文
消费行为数据
生成个性化学习笔记
```

---

# 九、MVP 范围建议

## 9.1 V1 MVP 必做

```text
用户注册登录
文章列表与章节详情
题目详情
代码提交
RabbitMQ 异步判题
判题结果查询
前端基础页面闭环
```

---

## 9.2 V2 增强功能

```text
AI 侧边栏问答
根据当前章节和报错代码上下文回答问题
保存 AI 对话历史
用户行为埋点
```

---

## 9.3 V3 创新功能

```text
用户能力画像
易错点分析
代码风格标签
个性化 Markdown 学习笔记
RAG 检索
Agent 工作流
```

---

# 十、落库与仓库建议

建议将本文档放入：

```text
flowstudy-infra/docs/flowstudy-contract.md
```

同时在以下仓库的 README 中引用该文档：

```text
flowstudy-core/README.md
flowstudy-judge/README.md
flowstudy-ai/README.md
flowstudy-frontend/README.md
```

建议每个服务仓库都至少包含：

```text
README.md
.env.example
Dockerfile
src/
docs/
```

对于接口契约、MQ 契约和数据库结构，原则上只允许在 `flowstudy-infra` 中维护主版本，其他仓库只引用，不重复维护，避免多份文档不一致。

---

# 十一、关键约定总结

本文档当前确定以下核心约定：

```text
API 前缀：/api/v1
统一返回：Result<T>
用户认证：JWT Bearer Token
数据库：MySQL 8.x / utf8mb4 / InnoDB
缓存限流：Redis
异步消息：RabbitMQ topic exchange
Core 服务端口：8080
AI 服务端口：8000
Judge 服务端口：9000
Frontend 服务端口：5173
RabbitMQ 管理后台：15672
```

后续所有服务开发都应优先遵守本文档。如需变更 API、MQ 字段、数据库表结构或端口规范，应先修改本文档，再同步修改各仓库代码。
