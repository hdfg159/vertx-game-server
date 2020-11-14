# vertx-game-server
Vert.x Game Server

# 简介

Vert.x Game Server 是一个基于 Vert.x 封装的游戏开发框架，核心设计目标是简易使用和学习，基于 Vert.x 优点结合 Netty 可以快速进行游戏开发。

# 技术

| -- | 说明|
|---|---|
| [Netty](https://github.com/netty/netty)| 构建游戏基础网络通信(Websocket连接)
| [Vert.x](https://github.com/eclipse-vertx/vert.x) | 构建 Web 服务器 / EventBus
| [RxJava](https://github.com/ReactiveX/RxJava) | 搭配异步编程 / 线程处理
| [Protobuf](https://github.com/protocolbuffers/protobuf) | 游戏传输协议
| [MongoDB](https://github.com/mongodb/mongo) | 游戏服务端持久化数据库(过期缓存数据持久化)
| [Caffeine Cache](https://github.com/ben-manes/caffeine) | 游戏数据内存缓存

# 贡献

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 Issue 讨论新特性或者变更。

# License

wechat-work-robot is under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) - see the [LICENSE](LICENSE) file for details.