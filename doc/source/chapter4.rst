.. _chapter4:

==============================
第 4 章 度量驱动的多语言实现
==============================

现代微服务系统通常涉及多种编程语言。本章介绍如何在 Go、Python、Java、C++ 中
实现度量驱动开发，并探讨 AI 辅助编程时代的度量实践。


4.1 度量代码质量
================

4.1.1 静态分析指标
------------------

.. list-table:: 代码质量指标
   :header-rows: 1
   :widths: 25 35 40

   * - 指标
     - 说明
     - 参考阈值
   * - 圈复杂度 (Cyclomatic Complexity)
     - 代码路径的复杂度
     - < 10 为佳, > 20 需重构
   * - 代码覆盖率 (Code Coverage)
     - 测试覆盖的代码比例
     - > 80% 为佳
   * - 代码重复率 (Duplication)
     - 重复代码的比例
     - < 5% 为佳
   * - 技术债务 (Technical Debt)
     - 需要修复的代码问题
     - 持续降低

4.1.2 各语言的代码质量工具
--------------------------

.. list-table:: 多语言代码质量工具
   :header-rows: 1
   :widths: 15 40 45

   * - 语言
     - 工具
     - 用法
   * - Go
     - golangci-lint, go vet, go test -cover
     - ``golangci-lint run ./...``
   * - Python
     - pylint, flake8, mypy, pytest-cov
     - ``pytest --cov=src --cov-report=html``
   * - Java
     - SonarQube, Checkstyle, SpotBugs
     - ``mvn sonar:sonar``
   * - C++
     - clang-tidy, cppcheck, gcov, lcov
     - ``cmake -DCMAKE_BUILD_TYPE=Coverage``

.. tip::

   **AI 时代的代码质量度量更重要了。**

   AI 生成的代码可能通过了基本的语法检查，但可能存在:

   - 未处理的边界条件
   - 资源泄漏 (文件句柄、连接、内存)
   - 不合理的算法复杂度
   - 缺失的错误处理

   用静态分析工具扫描 AI 生成的代码，是质量保障的第一道防线。


4.2 度量进度
============

4.2.1 DORA 指标
----------------

DORA 四个关键指标是度量软件交付效能的黄金标准:

.. list-table:: DORA 指标
   :header-rows: 1
   :widths: 25 25 25 25

   * - 指标
     - 精英
     - 高效
     - 低效
   * - 部署频率
     - 每天多次
     - 每天到每周
     - 每月到每半年
   * - 变更前置时间
     - 不到一天
     - 一天到一周
     - 一个月到半年
   * - 变更失败率
     - 0-15%
     - 16-30%
     - 46-60%
   * - 服务恢复时间
     - 不到一小时
     - 不到一天
     - 超过半年


4.3 度量性能
============

4.3.1 性能度量金三角
--------------------

.. code-block:: text

           延迟 (Latency)
              /\
             /  \
            /    \
           /      \
          /________\
   吞吐量           资源使用率
   (Throughput)     (Utilization)

4.3.2 多语言性能测试
--------------------

**Go: 内置 Benchmark**

.. code-block:: go

   // Go 内置的 benchmark 是度量性能的利器
   func BenchmarkCreatePotato(b *testing.B) {
       svc := NewPotatoService(testDB)
       b.ResetTimer()
       for i := 0; i < b.N; i++ {
           svc.Create(&Potato{
               Name:     fmt.Sprintf("potato-%d", i),
               Priority: 1,
           })
       }
   }

   func BenchmarkQueryPotato(b *testing.B) {
       svc := NewPotatoService(testDB)
       b.ResetTimer()
       b.RunParallel(func(pb *testing.PB) {
           for pb.Next() {
               svc.FindAll()
           }
       })
   }

   // 运行: go test -bench=. -benchmem -count=3

**Python: pytest-benchmark**

.. code-block:: python

   import pytest

   def test_create_potato_perf(benchmark, potato_service):
       """度量创建土豆的性能"""
       result = benchmark(
           potato_service.create,
           name="benchmark-potato",
           priority=1
       )
       assert result.id is not None

   def test_query_potato_perf(benchmark, potato_service):
       """度量查询土豆的性能"""
       result = benchmark(potato_service.find_all)
       assert isinstance(result, list)

   # 运行: pytest --benchmark-only --benchmark-json=output.json

**C++: Google Benchmark**

.. code-block:: cpp

   #include <benchmark/benchmark.h>

   static void BM_CreatePotato(benchmark::State& state) {
       PotatoService svc(test_db);
       for (auto _ : state) {
           Potato p{"benchmark-potato", 1};
           svc.create(p);
       }
       state.SetItemsProcessed(state.iterations());
   }
   BENCHMARK(BM_CreatePotato)->Threads(4);

   static void BM_QueryPotato(benchmark::State& state) {
       PotatoService svc(test_db);
       for (auto _ : state) {
           auto results = svc.find_all();
           benchmark::DoNotOptimize(results);
       }
   }
   BENCHMARK(BM_QueryPotato);

   BENCHMARK_MAIN();

**负载测试: k6 (语言无关)**

.. code-block:: javascript

   // k6 负载测试脚本 -- 适用于任何语言的 HTTP 服务
   import http from 'k6/http';
   import { check, sleep } from 'k6';
   import { Rate, Trend } from 'k6/metrics';

   const errorRate = new Rate('errors');
   const latencyTrend = new Trend('latency_p99');

   export const options = {
     stages: [
       { duration: '30s', target: 50 },   // ramp up
       { duration: '1m',  target: 100 },  // steady
       { duration: '30s', target: 0 },    // ramp down
     ],
     thresholds: {
       http_req_duration: ['p(99)<500'],   // P99 < 500ms
       errors: ['rate<0.01'],              // error rate < 1%
     },
   };

   export default function () {
     const res = http.get('http://localhost:9003/api/potatoes');
     check(res, { 'status is 200': (r) => r.status === 200 });
     errorRate.add(res.status !== 200);
     latencyTrend.add(res.timings.duration);
     sleep(1);
   }


4.4 多语言度量实现
==================

4.4.1 Go 度量实现
------------------

Go 是云原生时代的主力语言，Prometheus 客户端是最常用的度量库。

.. code-block:: go

   package metrics

   import (
       "net/http"
       "github.com/prometheus/client_golang/prometheus"
       "github.com/prometheus/client_golang/prometheus/promauto"
       "github.com/prometheus/client_golang/prometheus/promhttp"
   )

   var (
       // 业务度量
       PotatoCreated = promauto.NewCounter(prometheus.CounterOpts{
           Name: "potato_created_total",
           Help: "Total number of potatoes created",
       })

       PotatoCompleted = promauto.NewCounter(prometheus.CounterOpts{
           Name: "potato_completed_total",
           Help: "Total number of potatoes completed",
       })

       // 应用度量
       RequestDuration = promauto.NewHistogramVec(
           prometheus.HistogramOpts{
               Name:    "http_request_duration_seconds",
               Help:    "HTTP request duration",
               Buckets: prometheus.DefBuckets,
           },
           []string{"method", "path", "status"},
       )

       // 资源度量
       ActiveConnections = promauto.NewGauge(prometheus.GaugeOpts{
           Name: "active_connections",
           Help: "Number of active connections",
       })
   )

   // ExposeMetrics 暴露 /metrics 端点
   func ExposeMetrics(addr string) {
       http.Handle("/metrics", promhttp.Handler())
       go http.ListenAndServe(addr, nil)
   }

**Go 健康检查:**

.. code-block:: go

   // 标准的健康检查端点
   type HealthStatus struct {
       Status    string            `json:"status"`
       Checks   map[string]string `json:"checks"`
       Version  string            `json:"version"`
       Uptime   string            `json:"uptime"`
   }

   func (s *Server) healthHandler(w http.ResponseWriter, r *http.Request) {
       status := HealthStatus{
           Status:  "UP",
           Version: Version,
           Uptime:  time.Since(s.startTime).String(),
           Checks:  make(map[string]string),
       }

       // 检查数据库连接
       if err := s.db.Ping(); err != nil {
           status.Status = "DOWN"
           status.Checks["database"] = "FAIL: " + err.Error()
       } else {
           status.Checks["database"] = "OK"
       }

       // 检查 Redis 连接
       if _, err := s.redis.Ping(r.Context()).Result(); err != nil {
           status.Checks["redis"] = "FAIL: " + err.Error()
       } else {
           status.Checks["redis"] = "OK"
       }

       w.Header().Set("Content-Type", "application/json")
       json.NewEncoder(w).Encode(status)
   }

4.4.2 Python 度量实现
---------------------

Python 常用 FastAPI + Prometheus Client:

.. code-block:: python

   from prometheus_client import Counter, Histogram, Gauge, generate_latest
   from fastapi import FastAPI, Request, Response
   import time
   import psutil

   app = FastAPI(title="Potato Service")

   # 业务度量
   POTATO_CREATED = Counter("potato_created_total", "Potatoes created")
   POTATO_COMPLETED = Counter("potato_completed_total", "Potatoes completed")

   # 应用度量
   REQUEST_DURATION = Histogram(
       "http_request_duration_seconds",
       "Request duration",
       ["method", "endpoint"],
       buckets=[0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0]
   )

   # 资源度量
   CPU_USAGE = Gauge("process_cpu_usage_percent", "CPU usage percentage")
   MEMORY_USAGE = Gauge("process_memory_usage_bytes", "Memory usage in bytes")

   @app.middleware("http")
   async def metrics_middleware(request: Request, call_next):
       start = time.time()
       response = await call_next(request)
       duration = time.time() - start
       REQUEST_DURATION.labels(
           method=request.method,
           endpoint=request.url.path
       ).observe(duration)
       return response

   @app.get("/metrics")
   async def metrics():
       # 更新资源度量
       CPU_USAGE.set(psutil.cpu_percent())
       MEMORY_USAGE.set(psutil.Process().memory_info().rss)
       return Response(
           content=generate_latest(),
           media_type="text/plain"
       )

   @app.post("/api/potatoes")
   async def create_potato(potato: PotatoCreate):
       result = await potato_service.create(potato)
       POTATO_CREATED.inc()
       return result

4.4.3 C++ 度量实现
------------------

C++ 使用 prometheus-cpp 或 OpenTelemetry C++ SDK:

.. code-block:: cpp

   #include <prometheus/counter.h>
   #include <prometheus/exposer.h>
   #include <prometheus/histogram.h>
   #include <prometheus/registry.h>
   #include <chrono>

   class MetricsManager {
   public:
       MetricsManager(const std::string& bind_address)
           : exposer_(bind_address)
           , registry_(std::make_shared<prometheus::Registry>()) {

           // 业务度量
           auto& potato_family = prometheus::BuildCounter()
               .Name("potato_created_total")
               .Help("Total potatoes created")
               .Register(*registry_);
           potato_created_ = &potato_family.Add({});

           // 请求延迟
           auto& duration_family = prometheus::BuildHistogram()
               .Name("http_request_duration_seconds")
               .Help("HTTP request duration")
               .Register(*registry_);
           request_duration_ = &duration_family.Add(
               {{"handler", "api"}},
               prometheus::Histogram::BucketBoundaries{
                   0.01, 0.05, 0.1, 0.5, 1.0, 5.0
               });

           exposer_.RegisterCollectable(registry_);
       }

       // RAII 计时器
       class ScopedTimer {
       public:
           ScopedTimer(prometheus::Histogram* h)
               : histogram_(h), start_(std::chrono::steady_clock::now()) {}
           ~ScopedTimer() {
               auto duration = std::chrono::steady_clock::now() - start_;
               auto seconds = std::chrono::duration<double>(duration).count();
               histogram_->Observe(seconds);
           }
       private:
           prometheus::Histogram* histogram_;
           std::chrono::steady_clock::time_point start_;
       };

       ScopedTimer time_request() { return ScopedTimer(request_duration_); }
       void inc_potato_created() { potato_created_->Increment(); }

   private:
       prometheus::Exposer exposer_;
       std::shared_ptr<prometheus::Registry> registry_;
       prometheus::Counter* potato_created_;
       prometheus::Histogram* request_duration_;
   };

   // 使用示例
   void handle_create_potato(MetricsManager& metrics) {
       auto timer = metrics.time_request();  // RAII 自动计时
       // ... 处理请求 ...
       metrics.inc_potato_created();
   }

4.4.4 Java 度量实现
-------------------

Spring Boot + Micrometer 仍然是 Java 生态的首选:

.. code-block:: java

   @Service
   public class PotatoService {
       private final Counter potatoCreated;
       private final Timer queryTimer;

       public PotatoService(MeterRegistry registry) {
           this.potatoCreated = Counter.builder("potato.created.total")
               .description("Total potatoes created")
               .register(registry);
           this.queryTimer = Timer.builder("potato.query.duration")
               .description("Potato query duration")
               .publishPercentiles(0.5, 0.9, 0.95, 0.99)
               .register(registry);
       }

       public Potato create(Potato potato) {
           Potato result = repository.save(potato);
           potatoCreated.increment();
           return result;
       }

       public List<Potato> findAll() {
           return queryTimer.record(() -> repository.findAll());
       }
   }


4.5 OpenTelemetry: 统一的度量标准
==================================

OpenTelemetry 是 CNCF 的统一可观测性框架，支持所有主流语言:

.. code-block:: text

   ┌──────────────────────────────────────────────┐
   │         OpenTelemetry SDK (多语言)            │
   │  ┌────────┐  ┌────────┐  ┌────────┐         │
   │  │Metrics │  │Traces  │  │ Logs   │         │
   │  └───┬────┘  └───┬────┘  └───┬────┘         │
   └──────┼───────────┼───────────┼───────────────┘
          │           │           │  OTLP
          ▼           ▼           ▼
   ┌──────────────────────────────────────────────┐
   │         OTel Collector                        │
   │  ┌──────────┐ ┌──────────┐ ┌──────────────┐ │
   │  │Receivers │ │Processors│ │  Exporters   │ │
   │  └──────────┘ └──────────┘ └──────────────┘ │
   └──────┬──────────────┬──────────────┬─────────┘
          │              │              │
          ▼              ▼              ▼
   ┌──────────┐   ┌──────────┐   ┌──────────┐
   │Prometheus│   │  Jaeger  │   │   Loki   │
   └──────────┘   └──────────┘   └──────────┘

**Go + OpenTelemetry:**

.. code-block:: go

   import (
       "go.opentelemetry.io/otel"
       "go.opentelemetry.io/otel/metric"
   )

   var meter = otel.Meter("potato-service")

   var potatoCreated, _ = meter.Int64Counter(
       "potato.created",
       metric.WithDescription("Number of potatoes created"),
   )

   var requestDuration, _ = meter.Float64Histogram(
       "http.request.duration",
       metric.WithDescription("HTTP request duration in seconds"),
       metric.WithUnit("s"),
   )

**Python + OpenTelemetry:**

.. code-block:: python

   from opentelemetry import metrics
   from opentelemetry.sdk.metrics import MeterProvider

   meter = metrics.get_meter("potato-service")

   potato_created = meter.create_counter(
       "potato.created",
       description="Number of potatoes created"
   )

   request_duration = meter.create_histogram(
       "http.request.duration",
       description="HTTP request duration",
       unit="s"
   )


4.6 AI 辅助编程中的度量实践
============================

.. important::

   **在 AI 时代，让 AI 生成代码时就要求"自带度量"。**

   不要先让 AI 写完功能代码，再补度量。
   而是在 Prompt 中明确度量需求，让 AI 从一开始就 Build in 可观测性。

4.6.1 正确的 AI Prompt 模式
----------------------------

**错误示范:**

.. code-block:: text

   帮我写一个 HTTP 请求重试逻辑。

**正确示范:**

.. code-block:: text

   我需要一个带指标监控的 HTTP 请求重试逻辑。

   功能要求：
   - 支持指数退避重试（最多 3 次）
   - 可配置重试条件

   度量要求（用 Prometheus metrics）：
   - http_request_total: 总请求数（按 method, path, status_code 分类）
   - http_request_retry_total: 重试次数
   - http_request_duration_seconds: 请求耗时分布

   请生成：
   1. 实现代码（包含埋点）
   2. 对应的测试用例
   3. Grafana 面板查询（PromQL）

4.6.2 度量设计先于代码实现
--------------------------

对于复杂系统，让 AI 先设计度量方案:

.. code-block:: text

   请先给出度量设计方案，包括：
   1. 关键指标清单（Golden Signals: Latency, Traffic, Errors, Saturation）
   2. 每个指标的类型（Gauge / Counter / Histogram）
   3. SLO 定义（可用性、性能目标）
   然后再实现代码。

**为什么度量设计比代码实现更重要？**

因为度量方向错了，埋再多点也没用。
而有了正确的度量设计，AI 可以很好地生成实现代码。

4.6.3 AI 生成代码的度量检查清单
--------------------------------

.. code-block:: text

   ✅ 可观测性检查项（MDD）
   - [ ] 定义了关键度量指标（RED Metrics + 业务指标）
   - [ ] 代码包含度量埋点
   - [ ] 健康检查端点可用
   - [ ] 关键操作有日志记录
   - [ ] 配置了告警规则
   - [ ] 提供了 Grafana 面板配置或 PromQL 查询示例


4.7 本章小结
============

本章介绍了多语言环境下的度量驱动实现:

- Go、Python、Java、C++ 各有成熟的度量生态
- OpenTelemetry 正在统一各语言的度量标准
- DORA 指标是度量团队交付效能的黄金标准
- **AI 时代，度量需求应该在 Prompt 中前置，而非事后补充**

.. note::

   **关键要点:**

   - 每种语言都有 Prometheus 客户端，API 风格一致
   - OpenTelemetry 是未来统一的方向
   - 让 AI 生成带度量的代码，比生成后再补度量更高效
   - 度量设计先于代码实现（先定义指标，再写实现）
