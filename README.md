# flowstudy-core

学习行为与 AI 画像接口：

- `POST /api/v1/learning/events`：记录当前登录用户的学习行为。
- `POST /api/v1/learning/profile/analyze`：将近期行为发送给 AI 服务，更新 `fs_user_profile` 并生成 `fs_learning_note`。
- `GET /api/v1/learning/profile`：读取当前用户画像。
- `GET /api/v1/learning/notes`：读取当前用户的 AI 学习总结。
The core business and gateway service fFlowStudy. Built with Spring Boot to handle content distribution, API rate-limiting, and async task dispatching.
