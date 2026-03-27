.. _chapter5:

==============================
第 5 章 度量数据的聚合与展示
==============================

.. admonition:: 本章你将学到

   - 度量数据的聚合方式：时间聚合、空间聚合、层次聚合
   - 时序数据库的选型：InfluxDB、Prometheus、TimescaleDB
   - 数据清洗与降采样策略
   - Grafana 仪表盘设计的最佳实践
   - 常用技术栈：TIG、ELKK、Prometheus、OpenTelemetry


开篇故事
========

   *老板问："上周我们的系统可用性是多少？"
   我打开 Grafana，面对 50 个面板，不知道该看哪个。
   有 CPU 使用率、有内存趋势、有网络流量……
   但就是没有一个面板直接回答"可用性是多少"这个问题。*

   *那天我学到了一个教训：仪表盘不是越多越好，
   而是要能回答业务关心的问题。*

本章讲述如何聚合和存储度量数据，如何进行清洗和处理，
然后重点讲解度量的可视化技术和常用的技术栈。


5.1 度量数据的聚合和存储
========================

5.1.1 聚合方式
--------------

度量数据的聚合方式:

- **时间聚合**: 按时间窗口聚合 (1 分钟、5 分钟、1 小时)
- **空间聚合**: 按维度聚合 (按服务、按实例、按区域)
- **层次聚合**: 从底层到高层逐步汇总

.. code-block:: text

   原始数据 (每秒)
       │
       ▼ 时间聚合
   1 分钟粒度
       │
       ▼ 时间聚合
   5 分钟粒度
       │
       ▼ 降采样
   1 小时粒度
       │
       ▼ 降采样
   1 天粒度

5.1.2 时序数据库
----------------

度量数据本质上是 **时间序列数据 (Time Series Data)**，
推荐使用专门的时序数据库:

.. list-table:: 时序数据库对比
   :header-rows: 1
   :widths: 20 20 30 30

   * - 数据库
     - 语言
     - 特点
     - 适用场景
   * - InfluxDB
     - Go
     - 类 SQL 查询、易用
     - 中小规模度量
   * - Prometheus
     - Go
     - Pull 模型、告警集成
     - Kubernetes 生态
   * - TimescaleDB
     - C (PostgreSQL 扩展)
     - SQL 兼容、成熟
     - 需要关系查询的场景
   * - VictoriaMetrics
     - Go
     - 高压缩、高性能
     - 大规模度量存储
   * - ClickHouse
     - C++
     - 列式存储、极高吞吐
     - 分析型查询

5.1.3 数据保留策略
------------------

.. code-block:: text

   保留策略示例:

   高精度数据 (1s 粒度)  → 保留 7 天
   中精度数据 (1min 粒度) → 保留 30 天
   低精度数据 (1h 粒度)  → 保留 1 年
   汇总数据 (1d 粒度)    → 永久保留


5.2 度量数据的清洗和处理
========================

5.2.1 数据清洗
--------------

原始度量数据可能包含噪声和异常，需要清洗:

- **去重**: 去除重复的度量数据点
- **补值**: 填充缺失的数据点 (线性插值、前值填充)
- **去噪**: 使用移动平均或中位数滤波去除噪声
- **异常检测**: 识别并标记异常数据点

5.2.2 数据处理
--------------

常用的数据处理方法:

- **移动平均 (Moving Average)**: 平滑数据波动
- **指数加权移动平均 (EWMA)**: 近期数据权重更高
- **差分 (Derivative)**: 计算变化率
- **积分 (Integral)**: 计算累积量
- **百分位计算**: 计算 P50, P90, P95, P99

**Python (Pandas)**

.. code-block:: python

   import pandas as pd

   # 移动平均
   df['latency_ma'] = df['latency'].rolling(window=5).mean()

   # 指数加权移动平均
   df['latency_ewma'] = df['latency'].ewm(span=5).mean()

   # 百分位计算
   p50 = df['latency'].quantile(0.50)
   p90 = df['latency'].quantile(0.90)
   p99 = df['latency'].quantile(0.99)

**Go (实时流处理)**

.. code-block:: go

   // Go 中使用滑动窗口计算 P99
   type LatencyTracker struct {
       mu      sync.Mutex
       window  []float64
       maxSize int
   }

   func (t *LatencyTracker) Record(latency float64) {
       t.mu.Lock()
       defer t.mu.Unlock()
       t.window = append(t.window, latency)
       if len(t.window) > t.maxSize {
           t.window = t.window[1:]
       }
   }

   func (t *LatencyTracker) Percentile(p float64) float64 {
       t.mu.Lock()
       sorted := make([]float64, len(t.window))
       copy(sorted, t.window)
       t.mu.Unlock()
       sort.Float64s(sorted)
       idx := int(float64(len(sorted)-1) * p)
       return sorted[idx]
   }

**PromQL 查询示例**

.. code-block:: text

   # 计算 P99 延迟
   histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))

   # 计算错误率
   sum(rate(http_requests_total{status=~"5.."}[5m]))
   / sum(rate(http_requests_total[5m]))

   # 计算 QPS
   sum(rate(http_requests_total[1m]))


5.3 度量数据的可视化
====================

5.3.1 常用图表类型
------------------

.. list-table:: 度量可视化图表
   :header-rows: 1
   :widths: 20 35 45

   * - 图表类型
     - 适用场景
     - 示例
   * - 折线图 (Line Chart)
     - 趋势变化
     - CPU 使用率随时间的变化
   * - 柱状图 (Bar Chart)
     - 分类比较
     - 各服务的错误数比较
   * - 面积图 (Area Chart)
     - 累积量变化
     - 各状态码请求的堆叠分布
   * - 饼图 (Pie Chart)
     - 占比分布
     - 错误类型分布
   * - 热力图 (Heatmap)
     - 二维分布
     - 请求延迟的时间-频率分布
   * - 仪表盘 (Gauge)
     - 单一指标
     - 当前 CPU 使用率
   * - 表格 (Table)
     - 精确数值
     - Top N 慢查询

5.3.2 仪表盘设计原则
--------------------

一个好的度量仪表盘应该:

1. **一目了然**: 关键指标一眼可见
2. **层次清晰**: 从总览到详情逐层钻取
3. **实时更新**: 数据自动刷新
4. **可交互**: 支持时间范围选择、维度筛选
5. **告警集成**: 异常指标高亮显示

.. code-block:: text

   ┌─────────────────────────────────────────────────────┐
   │                 系统总览 Dashboard                    │
   ├─────────────┬─────────────┬─────────────────────────┤
   │  可用性      │  错误率      │  平均响应时间            │
   │  99.95%     │   0.3%      │  125ms                  │
   │  ●(绿)      │  ●(绿)      │  ●(绿)                  │
   ├─────────────┴─────────────┴─────────────────────────┤
   │                                                      │
   │  ┌──────────────────────────────────────────────┐   │
   │  │  请求量趋势 (折线图)                           │   │
   │  │  ~~~~~~~~~~~~~~~~~~~~~~~~                     │   │
   │  └──────────────────────────────────────────────┘   │
   │                                                      │
   │  ┌──────────────────┐  ┌────────────────────────┐   │
   │  │ 延迟分布 (热力图) │  │ 错误类型分布 (饼图)     │   │
   │  │                   │  │                         │   │
   │  └──────────────────┘  └────────────────────────┘   │
   └─────────────────────────────────────────────────────┘


5.4 常用度量聚合与展示方案
==========================

5.4.1 TIG 技术栈
-----------------

**Telegraf + InfluxDB + Grafana**

.. code-block:: text

   ┌──────────┐    ┌───────────┐    ┌──────────┐
   │ Telegraf  │───>│ InfluxDB  │───>│ Grafana  │
   │ (采集)    │    │ (存储)     │    │ (展示)   │
   └──────────┘    └───────────┘    └──────────┘

**Telegraf 配置示例:**

.. code-block:: toml

   # telegraf.conf
   [agent]
     interval = "10s"
     round_interval = true

   # 输入: 系统指标
   [[inputs.cpu]]
     percpu = true
     totalcpu = true

   [[inputs.mem]]

   [[inputs.disk]]
     ignore_fs = ["tmpfs", "devtmpfs"]

   # 输入: Docker 容器指标
   [[inputs.docker]]
     endpoint = "unix:///var/run/docker.sock"

   # 输出: InfluxDB
   [[outputs.influxdb]]
     urls = ["http://influxdb:8086"]
     database = "telegraf"

5.4.2 ELKK 技术栈
------------------

**Elasticsearch + Logstash + Kibana + Kafka**

.. code-block:: text

   ┌──────────┐    ┌──────────┐    ┌───────────────┐    ┌──────────┐
   │ Filebeat  │───>│  Kafka   │───>│  Logstash     │───>│  Elastic │
   │ (采集)    │    │ (缓冲)   │    │ (处理/转换)    │    │  search  │
   └──────────┘    └──────────┘    └───────────────┘    └────┬─────┘
                                                              │
                                                              ▼
                                                        ┌──────────┐
                                                        │  Kibana  │
                                                        │  (展示)   │
                                                        └──────────┘

**Logstash 配置示例:**

.. code-block:: ruby

   # logstash.conf
   input {
     kafka {
       bootstrap_servers => "kafka:9092"
       topics => ["app-logs"]
       codec => json
     }
   }

   filter {
     if [type] == "access_log" {
       grok {
         match => { "message" => "%{COMBINEDAPACHELOG}" }
       }
       date {
         match => [ "timestamp", "dd/MMM/yyyy:HH:mm:ss Z" ]
       }
       mutate {
         convert => { "response" => "integer" }
         convert => { "bytes" => "integer" }
       }
     }
   }

   output {
     elasticsearch {
       hosts => ["elasticsearch:9200"]
       index => "app-logs-%{+YYYY.MM.dd}"
     }
   }

5.4.3 Prometheus 技术栈
------------------------

**Prometheus + Grafana + Alertmanager**

.. code-block:: text

   ┌──────────────────────────────┐
   │        微服务集群             │
   │  ┌─────┐ ┌─────┐ ┌─────┐   │
   │  │svc-1│ │svc-2│ │svc-3│   │
   │  │/met │ │/met │ │/met │   │
   │  └──┬──┘ └──┬──┘ └──┬──┘   │
   └─────┼───────┼───────┼──────┘
         │       │       │  Pull
         ▼       ▼       ▼
   ┌──────────────────────────┐
   │      Prometheus          │
   │   (抓取、存储、查询)       │
   └──────┬──────────┬────────┘
          │          │
          ▼          ▼
   ┌──────────┐  ┌──────────────┐
   │ Grafana  │  │ Alertmanager │
   │ (展示)    │  │ (告警)        │
   └──────────┘  └──────────────┘

**Prometheus 配置示例:**

.. code-block:: yaml

   # prometheus.yml
   global:
     scrape_interval: 15s
     evaluation_interval: 15s

   rule_files:
     - "rules/*.yml"

   alerting:
     alertmanagers:
       - static_configs:
           - targets: ['alertmanager:9093']

   scrape_configs:
     - job_name: 'potato-server'
       metrics_path: '/actuator/prometheus'
       static_configs:
         - targets: ['potato-server:9003']

     - job_name: 'potato-scheduler'
       metrics_path: '/actuator/prometheus'
       static_configs:
         - targets: ['potato-scheduler:9002']

5.4.4 OpenTelemetry (新增)
---------------------------

OpenTelemetry 是 CNCF 的统一可观测性框架，
将 Metrics、Traces、Logs 三大信号统一收集:

.. code-block:: text

   ┌──────────────────────────────────────┐
   │           应用程序                     │
   │  ┌──────────────────────────────┐    │
   │  │   OpenTelemetry SDK          │    │
   │  │  ┌────────┬───────┬────────┐ │    │
   │  │  │Metrics │Traces │ Logs   │ │    │
   │  │  └────────┴───────┴────────┘ │    │
   │  └──────────────┬───────────────┘    │
   └─────────────────┼────────────────────┘
                     │ OTLP
                     ▼
   ┌──────────────────────────────────────┐
   │      OTel Collector                   │
   │  ┌─────────┬──────────┬───────────┐  │
   │  │Receivers│Processors│ Exporters │  │
   │  └─────────┴──────────┴───────────┘  │
   └──────┬──────────┬──────────┬─────────┘
          │          │          │
          ▼          ▼          ▼
   ┌──────────┐ ┌────────┐ ┌────────┐
   │Prometheus│ │ Jaeger │ │  Loki  │
   │(Metrics) │ │(Traces)│ │ (Logs) │
   └──────────┘ └────────┘ └────────┘


5.5 土豆微服务的度量聚合与展示
==============================

在土豆微服务中，我们使用以下方案:

- **系统指标**: Telegraf 采集 → InfluxDB 存储 → Grafana 展示
- **应用日志**: Filebeat 采集 → Kafka 缓冲 → Logstash 处理 → Elasticsearch 存储 → Kibana 展示
- **业务度量**: Micrometer → InfluxDB → Grafana

.. code-block:: yaml

   # docker-compose.yml (度量相关组件)
   services:
     influxdb:
       image: influxdb:2.7
       ports:
         - "8086:8086"
       volumes:
         - influxdb-data:/var/lib/influxdb2

     grafana:
       image: grafana/grafana:10.0
       ports:
         - "3000:3000"
       depends_on:
         - influxdb

     telegraf:
       image: telegraf:1.27
       volumes:
         - ./telegraf.conf:/etc/telegraf/telegraf.conf
       depends_on:
         - influxdb


5.5.1 AI 应用的度量聚合特殊考量
---------------------------------

AI 应用的度量数据与传统微服务有几个显著差异，需要特殊处理：

**1. 高基数标签问题**

LLM 应用中，Prompt 内容、模型版本、用户 ID 等维度组合可能产生极高的基数，
导致时序数据库存储爆炸。

.. code-block:: text

   ❌ 错误: llm_request{prompt="帮我写一篇关于..."} — 每个 Prompt 一个时间序列
   ✅ 正确: llm_request{model="gpt-4o", task_type="writing"} — 按任务类型聚合

**2. 评估数据的异步采集**

LLM 输出质量评估（Faithfulness、Relevancy 等）通常需要异步计算，
不能在请求路径上同步完成：

.. code-block:: text

   请求路径 (同步):  延迟、Token 数、错误码 → Prometheus
   评估路径 (异步):  采样请求 → 评估队列 → LLM-as-Judge → 评估结果 → Prometheus

**3. 成本度量的多维聚合**

LLM 成本需要按模型、用户、功能、时间等多个维度聚合：

.. code-block:: yaml

   # Grafana 面板: LLM 成本分析
   # 按模型的日成本
   sum by (model) (increase(llm_cost_dollars_total[24h]))
   # 按功能的月成本
   sum by (task_type) (increase(llm_cost_dollars_total[30d]))
   # 单次请求平均成本
   rate(llm_cost_dollars_total[1h]) / rate(llm_requests_total[1h])


5.6 本章小结
============

本章介绍了度量数据从采集到展示的完整流程:

- 度量数据的聚合方式和时序数据库选型
- 数据清洗和处理的常用方法
- 度量可视化的图表类型和仪表盘设计原则
- 四种主流的度量技术栈: TIG、ELKK、Prometheus、OpenTelemetry

.. note::

   **关键要点:**

   - 时序数据库是度量数据存储的首选
   - 使用百分位数而非均值来展示延迟
   - 仪表盘应该层次清晰、一目了然
   - OpenTelemetry 正在成为可观测性的统一标准


.. admonition:: 💡 避坑指南：仪表盘设计的 3 个原则
   :class: warning

   **原则 1：每个面板回答一个问题**

   不要把所有指标堆在一个图上。每个面板应该能回答一个明确的问题，
   比如"当前错误率是多少？"或"过去 24 小时的延迟趋势如何？"

   **原则 2：从上到下，从概览到细节**

   面板顶部放 Stat 面板（关键数字），中间放趋势图，底部放详细表格。
   让人一眼就能看到系统的整体状态。

   **原则 3：颜色有含义**

   绿色 = 正常，黄色 = 警告，红色 = 严重。不要随意使用颜色。
   设置阈值让面板自动变色，比手动检查数值更高效。


.. admonition:: 🔬 动手实验：搭建 Prometheus + Grafana 监控栈
   :class: tip

   **目标**：用 Docker Compose 在 10 分钟内搭建一个完整的监控栈。

   **步骤**：

   1. 创建 ``docker-compose.yml``，包含 Prometheus + Grafana + Node Exporter
   2. 配置 Prometheus 抓取 Node Exporter 指标
   3. 在 Grafana 中导入 Node Exporter 面板 (Dashboard ID: 1860)
   4. 观察 CPU、内存、磁盘、网络的实时数据

   **完整配置**：见 GitHub 仓库 ``examples/ch5-monitoring-stack/``


.. admonition:: 📝 思考题

   1. 你的团队目前使用什么监控技术栈？它的优缺点是什么？
   2. 如果让你设计一个"一屏看全局"的仪表盘，你会放哪 5 个面板？
   3. 度量数据应该保留多久？保留策略如何平衡存储成本和分析需求？
