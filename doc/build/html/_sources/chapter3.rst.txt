.. _chapter3:

========================
第 3 章 微服务度量设计
========================

本章从微服务的协议入手，介绍如何选用和分析微服务的协议，
然后讨论基于度量的存储系统选型和高可用性设计。


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


3.3 SIP 协议及其度量
=====================

3.3.1 SIP 协议简介
-------------------

SIP (Session Initiation Protocol) 是一种信令协议，
用于创建、修改和终止多媒体会话（如语音和视频通话）。

SIP 消息类型:

- **请求方法**: INVITE, ACK, BYE, CANCEL, REGISTER, OPTIONS
- **响应状态**: 1xx (临时), 2xx (成功), 3xx (重定向), 4xx (客户端错误), 5xx (服务器错误), 6xx (全局错误)

3.3.2 SIP 度量要点
-------------------

- **呼叫建立成功率**: INVITE 请求成功完成的比例
- **呼叫建立时间**: 从发送 INVITE 到收到 200 OK 的耗时
- **注册成功率**: REGISTER 请求成功的比例
- **并发会话数**: 当前活跃的 SIP 会话数


3.4 RTP 协议及其度量
=====================

3.4.1 RTP 协议简介
-------------------

RTP (Real-time Transport Protocol) 是用于传输实时音视频数据的协议。
RTCP (RTP Control Protocol) 是 RTP 的控制协议，用于传递度量和控制信息。

3.4.2 RTP 度量要点
-------------------

- **丢包率 (Packet Loss)**: 传输过程中丢失的数据包比例
- **抖动 (Jitter)**: 数据包到达时间间隔的变化
- **延迟 (Latency)**: 端到端的传输延迟
- **MOS 值 (Mean Opinion Score)**: 语音质量评分 (1-5)

.. code-block:: text

   ┌──────────────────────────────────────┐
   │     RTP 质量度量模型                  │
   │                                      │
   │  丢包率 < 1%     → 优秀              │
   │  丢包率 1-3%     → 良好              │
   │  丢包率 3-5%     → 一般              │
   │  丢包率 > 5%     → 差                │
   │                                      │
   │  抖动 < 30ms     → 优秀              │
   │  抖动 30-50ms    → 良好              │
   │  抖动 50-100ms   → 一般              │
   │  抖动 > 100ms    → 差                │
   │                                      │
   │  延迟 < 150ms    → 优秀              │
   │  延迟 150-300ms  → 良好              │
   │  延迟 300-450ms  → 一般              │
   │  延迟 > 450ms    → 差                │
   └──────────────────────────────────────┘


3.5 基于度量的存储系统选型
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


3.6 基于度量实现高可用性
========================

高可用性 (High Availability) 的目标是通过消除单点故障来保证系统的连续运行。

3.6.1 可用性度量
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

3.6.2 SLI/SLO/SLA
------------------

.. tip::

   Google SRE 提出的可靠性度量框架:

   - **SLI (Service Level Indicator)**: 服务级别指标，如可用性、延迟、吞吐量
   - **SLO (Service Level Objective)**: 服务级别目标，如 99.9% 可用性
   - **SLA (Service Level Agreement)**: 服务级别协议，违反 SLO 时的补偿条款

3.6.3 容错模式
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


3.7 土豆微服务度量设计
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


3.8 本章小结
============

本章讨论了微服务度量设计的各个方面:

- 微服务通信协议的选择影响可采集的度量数据
- HTTP/REST、SIP、RTP 各有不同的度量要点
- 存储系统选型应以度量数据为依据
- 高可用性设计需要 SLI/SLO/SLA 框架
- 容错模式（重试、断路器、限流等）是度量驱动设计的重要组成部分
