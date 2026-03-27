.. _chapter3:

========================
第 3 章 微服务度量设计
========================

.. admonition:: 本章你将学到

   - 不同通信协议（HTTP/gRPC/WebSocket/LLM API）的度量要点
   - 度量存储系统的选型方法
   - SLI/SLO/SLA 的设计与实践
   - 高可用性设计中的容错模式与度量
   - 土豆微服务的度量设计方案


开篇故事
========

   *团队花了 3 个月开发了一套"完美"的度量系统——采集了上百个指标，
   搭建了漂亮的 Grafana 面板。上线后，CTO 问了一个简单的问题：
   "上周我们的 SLO 达标了吗？"*

   *没人能回答。因为我们采集了大量基础设施指标，
   却没有定义过 SLI 和 SLO。我们度量了"系统在做什么"，
   却没有度量"系统做得好不好"。*

度量设计的第一步不是选工具、写代码，而是回答一个问题：
**"我们要度量什么，为什么？"**


3.1 微服务协议的选择与度量
==========================

3.1.1 协议概述
--------------

微服务之间的通信协议是度量的基础。选择合适的协议不仅影响性能，
也决定了我们能采集到什么样的度量数据。

常见的微服务通信协议:

.. list-table:: 微服务通信协议
   :header-rows: 1
   :widths: 15 20 30 35

   * - 协议
     - 类型
     - 特点
     - 度量要点
   * - HTTP/REST
     - 同步、请求-响应
     - 简单、通用、无状态
     - 状态码、延迟、吞吐量
   * - gRPC
     - 同步/流式、RPC
     - 高性能、强类型、HTTP/2
     - 调用次数、延迟、错误码
   * - GraphQL
     - 同步、查询语言
     - 灵活查询、减少过度获取
     - 查询复杂度、延迟、错误
   * - WebSocket
     - 全双工、长连接
     - 实时通信、低延迟
     - 连接数、消息延迟、断线率
   * - AMQP/Kafka
     - 异步、消息传递
     - 解耦、缓冲、可靠投递
     - 队列深度、消费延迟、吞吐量

3.1.2 协议分析
--------------

分析协议的关键维度:

- **性能**: 序列化/反序列化效率、传输效率
- **可靠性**: 消息投递保证、重试机制
- **可观测性**: 是否易于采集度量数据
- **互操作性**: 跨语言、跨平台支持
- **安全性**: 加密、认证、授权支持


3.2 HTTP 协议及其度量
=====================

3.2.1 HTTP 协议简介
--------------------

HTTP 是微服务中最常用的通信协议，基于请求-响应模式。

HTTP/1.1 到 HTTP/2 再到 HTTP/3 的演进:

- **HTTP/1.1**: 文本协议，持久连接，管线化
- **HTTP/2**: 二进制分帧，多路复用，头部压缩，服务端推送
- **HTTP/3**: 基于 QUIC，减少连接延迟

3.2.2 REST API 度量要点
------------------------

对于 RESTful API，核心度量指标:

.. code-block:: text

   ┌─────────────────────────────────────────────────┐
   │              REST API 度量维度                    │
   ├─────────────────────────────────────────────────┤
   │                                                  │
   │  延迟 (Latency)                                  │
   │  ├── 请求处理时间 (P50, P90, P95, P99)           │
   │  ├── 上游依赖延迟                                │
   │  └── 队列等待时间                                │
   │                                                  │
   │  流量 (Traffic)                                  │
   │  ├── QPS (每秒查询数)                            │
   │  ├── 并发请求数                                  │
   │  └── 请求/响应大小                               │
   │                                                  │
   │  错误 (Errors)                                   │
   │  ├── HTTP 状态码分布 (4xx, 5xx)                  │
   │  ├── 超时率                                      │
   │  └── 业务错误码                                  │
   │                                                  │
   │  饱和度 (Saturation)                             │
   │  ├── 线程池使用率                                │
   │  ├── 连接池使用率                                │
   │  └── 队列深度                                    │
   └─────────────────────────────────────────────────┘

**HTTP 度量的多语言实现**

**Go (Gin + Prometheus)**

.. code-block:: go

   // Go 中使用 Prometheus 度量 HTTP 请求
   func PrometheusMiddleware() gin.HandlerFunc {
       httpRequestsTotal := promauto.NewCounterVec(
           prometheus.CounterOpts{
               Name: "http_requests_total",
               Help: "Total HTTP requests",
           },
           []string{"method", "path", "status"},
       )
       httpDuration := promauto.NewHistogramVec(
           prometheus.HistogramOpts{
               Name:    "http_request_duration_seconds",
               Help:    "HTTP request duration",
               Buckets: []float64{0.01, 0.05, 0.1, 0.5, 1, 5},
           },
           []string{"method", "path"},
       )

       return func(c *gin.Context) {
           start := time.Now()
           c.Next()
           duration := time.Since(start).Seconds()
           status := strconv.Itoa(c.Writer.Status())
           httpRequestsTotal.WithLabelValues(c.Request.Method, c.FullPath(), status).Inc()
           httpDuration.WithLabelValues(c.Request.Method, c.FullPath()).Observe(duration)
       }
   }

**Python (FastAPI + Prometheus)**

.. code-block:: python

   # Python 中使用 prometheus_client 度量 HTTP 请求
   from prometheus_client import Counter, Histogram
   from fastapi import FastAPI, Request
   import time

   app = FastAPI()

   REQUEST_COUNT = Counter(
       "http_requests_total", "Total HTTP requests",
       ["method", "path", "status"]
   )
   REQUEST_DURATION = Histogram(
       "http_request_duration_seconds", "HTTP request duration",
       ["method", "path"],
       buckets=[0.01, 0.05, 0.1, 0.5, 1.0, 5.0]
   )

   @app.middleware("http")
   async def metrics_middleware(request: Request, call_next):
       start = time.time()
       response = await call_next(request)
       duration = time.time() - start
       REQUEST_COUNT.labels(request.method, request.url.path, response.status_code).inc()
       REQUEST_DURATION.labels(request.method, request.url.path).observe(duration)
       return response

**Java (Spring Boot + Micrometer)**

.. code-block:: java

   // Spring Boot 自动集成，只需配置
   @Bean
   public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
       return registry -> registry.config()
           .commonTags("application", "potato-server");
   }

**C++ (prometheus-cpp)**

.. code-block:: cpp

   // C++ 中使用 prometheus-cpp 度量 HTTP 请求
   #include <prometheus/counter.h>
   #include <prometheus/histogram.h>
   #include <prometheus/registry.h>

   auto& http_requests = prometheus::BuildCounter()
       .Name("http_requests_total")
       .Help("Total HTTP requests")
       .Register(*registry);
   auto& request_family = http_requests.Add({{"method", "GET"}, {"path", "/api"}});

   auto& http_duration = prometheus::BuildHistogram()
       .Name("http_request_duration_seconds")
       .Help("HTTP request duration")
       .Register(*registry);
   auto& duration_metric = http_duration.Add(
       {{"method", "GET"}, {"path", "/api"}},
       prometheus::Histogram::BucketBoundaries{0.01, 0.05, 0.1, 0.5, 1.0, 5.0});


3.3 gRPC 协议及其度量
======================

3.3.1 gRPC 概述
-----------------

gRPC 是 Google 开发的高性能 RPC 框架，基于 HTTP/2 和 Protocol Buffers。
在微服务架构中，gRPC 因其高性能和强类型特性被广泛使用。

gRPC 支持四种通信模式：

- **Unary RPC**：一请求一响应（最常见）
- **Server Streaming**：一请求多响应
- **Client Streaming**：多请求一响应
- **Bidirectional Streaming**：双向流

3.3.2 gRPC 度量要点
---------------------

gRPC 的度量与 HTTP/REST 类似，但有几个关键区别：

- 使用 **gRPC 状态码** 而非 HTTP 状态码（OK、CANCELLED、DEADLINE_EXCEEDED 等）
- 流式调用需要度量 **消息数** 和 **流持续时间**
- HTTP/2 多路复用使得 **连接级度量** 更加重要

**Go: gRPC 度量中间件**

.. code-block:: go

   import (
       "github.com/grpc-ecosystem/go-grpc-prometheus"
       "google.golang.org/grpc"
   )

   func NewGRPCServer() *grpc.Server {
       // 创建带度量的 gRPC 服务器
       grpcMetrics := grpc_prometheus.NewServerMetrics()
       server := grpc.NewServer(
           grpc.StreamInterceptor(grpcMetrics.StreamServerInterceptor()),
           grpc.UnaryInterceptor(grpcMetrics.UnaryServerInterceptor()),
       )
       grpcMetrics.InitializeMetrics(server)
       // 启用延迟直方图
       grpcMetrics.EnableHandlingTimeHistogram()
       return server
   }

**Python: gRPC 度量**

.. code-block:: python

   from prometheus_client import Counter, Histogram

   grpc_requests_total = Counter(
       "grpc_requests_total", "Total gRPC requests",
       ["method", "status"]
   )
   grpc_duration = Histogram(
       "grpc_request_duration_seconds", "gRPC request duration",
       ["method"],
       buckets=[0.005, 0.01, 0.05, 0.1, 0.5, 1.0, 5.0]
   )

gRPC 自动暴露的核心指标：

.. list-table::
   :header-rows: 1
   :widths: 40 60

   * - 指标
     - 说明
   * - ``grpc_server_handled_total``
     - 按方法和状态码分组的请求总数
   * - ``grpc_server_handling_seconds``
     - 请求处理延迟直方图
   * - ``grpc_server_msg_received_total``
     - 接收的消息总数（流式场景）
   * - ``grpc_server_msg_sent_total``
     - 发送的消息总数（流式场景）


3.4 WebSocket 与实时通信度量
==============================

3.4.1 WebSocket 度量要点
--------------------------

WebSocket 是全双工长连接协议，常用于实时通信场景。
其度量与请求-响应模式有本质区别：

.. list-table::
   :header-rows: 1
   :widths: 25 35 40

   * - 指标
     - 说明
     - 度量方式
   * - 活跃连接数
     - 当前打开的 WebSocket 连接数
     - Gauge
   * - 消息延迟
     - 消息从发送到接收的延迟
     - Histogram
   * - 消息吞吐量
     - 每秒发送/接收的消息数
     - Counter + rate()
   * - 断线率
     - 非正常断开的连接比例
     - Counter
   * - 重连次数
     - 客户端重连的次数
     - Counter

.. note::

   **实时通信协议（SIP/RTP）的度量**

   对于音视频通信场景，SIP（信令协议）和 RTP（媒体传输协议）有专门的度量维度：
   呼叫建立成功率、丢包率、抖动、端到端延迟、MOS 值等。
   这些内容较为专业，详见本书 GitHub 仓库中的补充材料。


3.5 LLM API 协议及其度量（新增）
===================================

3.5.1 LLM API 协议概述
------------------------

LLM API 已经成为现代应用中最重要的协议之一。
主流 LLM 提供商（OpenAI、Anthropic、Google）都采用 HTTP/REST + JSON 的协议格式，
但有两种调用模式:

.. list-table:: LLM API 调用模式
   :header-rows: 1
   :widths: 20 40 40

   * - 模式
     - 特点
     - 度量要点
   * - 非流式 (Non-Streaming)
     - 等待完整响应后一次性返回
     - E2E 延迟、Token 数、成本
   * - 流式 (Streaming / SSE)
     - 逐 Token 返回（Server-Sent Events）
     - TTFT、TPS、流中断率

3.5.2 LLM API 度量要点
------------------------

.. code-block:: text

   ┌─────────────────────────────────────────────────┐
   │              LLM API 度量维度                     │
   ├─────────────────────────────────────────────────┤
   │                                                  │
   │  延迟 (Latency)                                  │
   │  ├── TTFT: 首 Token 延迟                         │
   │  ├── TPS: Token 生成速率                         │
   │  ├── E2E: 端到端延迟                             │
   │  └── Queue Time: 排队等待                        │
   │                                                  │
   │  Token (用量)                                    │
   │  ├── Input Tokens: 输入 Token 数                 │
   │  ├── Output Tokens: 输出 Token 数                │
   │  ├── Cache Hit Tokens: 缓存命中 Token 数         │
   │  └── Total Cost: 总成本（美元）                   │
   │                                                  │
   │  错误 (Errors)                                   │
   │  ├── Rate Limit (429): 限流                      │
   │  ├── Overloaded (529): 过载                      │
   │  ├── Timeout: 超时                               │
   │  └── Content Filter: 内容过滤触发                 │
   │                                                  │
   │  质量 (Quality)                                  │
   │  ├── 幻觉率: 生成不存在信息的比例                  │
   │  ├── 相关性: 回答与问题的相关程度                  │
   │  └── 忠实度: 回答是否忠于提供的上下文              │
   └─────────────────────────────────────────────────┘

**Streaming vs Non-Streaming 度量差异:**

.. code-block:: python

   import time
   import anthropic
   from prometheus_client import Histogram, Counter

   llm_ttft = Histogram(
       "llm_ttft_seconds", "Time to first token",
       ["model"], buckets=[0.1, 0.2, 0.5, 1.0, 2.0, 5.0]
   )
   llm_tps = Histogram(
       "llm_tokens_per_second", "Token generation rate",
       ["model"], buckets=[10, 20, 30, 50, 80, 100]
   )

   def query_llm_streaming(prompt: str, model: str):
       """流式调用 LLM 并采集度量"""
       client = anthropic.Anthropic()
       start = time.time()
       first_token_time = None
       token_count = 0

       with client.messages.stream(
           model=model,
           max_tokens=1024,
           messages=[{"role": "user", "content": prompt}]
       ) as stream:
           for text in stream.text_stream:
               if first_token_time is None:
                   first_token_time = time.time()
                   llm_ttft.labels(model=model).observe(
                       first_token_time - start)
               token_count += 1
               yield text

       total_time = time.time() - (first_token_time or start)
       if total_time > 0 and token_count > 0:
           llm_tps.labels(model=model).observe(
               token_count / total_time)

.. tip::

   LLM API 的错误码与传统 HTTP API 不同。429 (Rate Limit) 和 529 (Overloaded)
   是 LLM API 特有的高频错误，需要专门监控和处理（指数退避重试）。
   详见第 9 章 LLM 应用度量。


3.6 基于度量的存储系统选型
==========================

选择存储系统时，度量数据是重要的决策依据:

.. list-table:: 数据存储选型
   :header-rows: 1
   :widths: 20 20 30 30

   * - 存储类型
     - 代表产品
     - 适用场景
     - 关键度量
   * - 关系型数据库
     - MySQL, PostgreSQL
     - 事务性数据、关系查询
     - QPS、连接数、慢查询
   * - 文档数据库
     - MongoDB
     - 灵活 Schema、文档存储
     - 写入吞吐、查询延迟
   * - 缓存
     - Redis, Memcached
     - 高频读取、会话存储
     - 命中率、内存使用、延迟
   * - 消息队列
     - Kafka, RabbitMQ
     - 异步通信、事件流
     - 消费延迟、吞吐量
   * - 时序数据库
     - InfluxDB, TimescaleDB
     - 度量数据、监控数据
     - 写入速率、查询延迟
   * - 搜索引擎
     - Elasticsearch
     - 全文搜索、日志分析
     - 索引速率、搜索延迟


3.7 基于度量实现高可用性
========================

高可用性 (High Availability) 的目标是通过消除单点故障来保证系统的连续运行。

3.7.1 可用性度量
----------------

.. code-block:: text

   可用性 = (总时间 - 停机时间) / 总时间 × 100%

常见的可用性级别:

.. list-table:: 可用性级别
   :header-rows: 1
   :widths: 20 25 25 30

   * - 级别
     - 可用性
     - 年停机时间
     - 适用场景
   * - 两个 9
     - 99%
     - 3.65 天
     - 内部工具
   * - 三个 9
     - 99.9%
     - 8.76 小时
     - 一般业务系统
   * - 四个 9
     - 99.99%
     - 52.6 分钟
     - 核心业务系统
   * - 五个 9
     - 99.999%
     - 5.26 分钟
     - 金融、通信

3.7.2 SLI/SLO/SLA
------------------

.. tip::

   Google SRE 提出的可靠性度量框架:

   - **SLI (Service Level Indicator)**: 服务级别指标，如可用性、延迟、吞吐量
   - **SLO (Service Level Objective)**: 服务级别目标，如 99.9% 可用性
   - **SLA (Service Level Agreement)**: 服务级别协议，违反 SLO 时的补偿条款

3.7.3 容错模式
--------------

- **重试 (Retry)**: 遇到临时错误时自动重试
- **超时 (Timeout)**: 设置合理的超时时间，避免长时间等待
- **断路器 (Circuit Breaker)**: 在下游服务故障时快速失败
- **降级 (Degradation)**: 在部分功能不可用时提供降级服务
- **限流 (Rate Limiting)**: 保护服务不被过多请求压垮
- **隔舱 (Bulkhead)**: 隔离不同的调用，防止故障扩散

**Go: 使用标准库实现带度量的断路器**

.. code-block:: go

   // Go 中使用 sony/gobreaker 实现断路器
   import "github.com/sony/gobreaker"

   var cb *gobreaker.CircuitBreaker

   func init() {
       settings := gobreaker.Settings{
           Name:        "potato-service",
           MaxRequests: 3,
           Interval:    10 * time.Second,
           Timeout:     30 * time.Second,
           ReadyToTrip: func(counts gobreaker.Counts) bool {
               failureRatio := float64(counts.TotalFailures) / float64(counts.Requests)
               return counts.Requests >= 3 && failureRatio >= 0.6
           },
           OnStateChange: func(name string, from, to gobreaker.State) {
               circuitBreakerState.WithLabelValues(name).Set(float64(to))
               log.Printf("Circuit breaker %s: %s -> %s", name, from, to)
           },
       }
       cb = gobreaker.NewCircuitBreaker(settings)
   }

   func GetPotato(id int64) (*Potato, error) {
       result, err := cb.Execute(func() (interface{}, error) {
           return potatoClient.GetPotato(id)
       })
       if err != nil {
           return defaultPotato(), nil // fallback
       }
       return result.(*Potato), nil
   }

**Python: 使用 tenacity 实现带度量的重试**

.. code-block:: python

   from tenacity import retry, stop_after_attempt, wait_exponential
   from prometheus_client import Counter

   retry_count = Counter("http_retry_total", "Retry attempts", ["service"])

   @retry(
       stop=stop_after_attempt(3),
       wait=wait_exponential(multiplier=1, min=1, max=10),
       before_sleep=lambda info: retry_count.labels("potato").inc()
   )
   def get_potato(potato_id: int):
       response = httpx.get(f"http://potato-server/api/potatoes/{potato_id}")
       response.raise_for_status()
       return response.json()


3.8 土豆微服务度量设计
======================

结合以上理论，土豆微服务的度量设计方案:

.. code-block:: text

   ┌──────────────────────────────────────────────────┐
   │             土豆微服务度量设计                      │
   ├──────────────────────────────────────────────────┤
   │                                                   │
   │  业务层度量                                       │
   │  ├── 待办事项创建数/完成数                         │
   │  ├── 提醒发送成功率                               │
   │  └── 用户活跃度                                   │
   │                                                   │
   │  应用层度量                                       │
   │  ├── API 响应时间 (P50, P90, P99)                 │
   │  ├── API 错误率                                   │
   │  ├── 并发请求数                                   │
   │  └── JVM 指标 (GC, 堆内存, 线程)                  │
   │                                                   │
   │  中间件度量                                       │
   │  ├── MySQL 慢查询、连接池                         │
   │  ├── Consul 健康检查                              │
   │  └── InfluxDB 写入速率                            │
   │                                                   │
   │  基础设施度量                                     │
   │  ├── CPU, Memory, Disk                            │
   │  ├── Docker 容器状态                              │
   │  └── 网络连通性                                   │
   └──────────────────────────────────────────────────┘


3.9 本章小结
============

.. note::

   **关键要点：**

   - 不同通信协议有不同的度量要点：HTTP 看状态码，gRPC 看状态码+流消息数，WebSocket 看连接数+断线率
   - LLM API 是一种新型协议，需要度量 Token、成本、TTFT 等特有指标
   - 存储系统选型应以度量数据为依据
   - SLI/SLO/SLA 是度量设计的顶层框架——先定义"好"的标准，再设计度量方案
   - 容错模式（重试、断路器、限流等）必须配合度量才能发挥作用


.. admonition:: 📝 思考题

   1. 你的系统使用了哪些通信协议？每种协议的核心度量指标是什么？
   2. 如果让你为一个新服务定义 SLO，你会选择哪些 SLI？阈值如何设定？
   3. 你的系统中是否有断路器？它的触发条件是基于度量数据的吗？
