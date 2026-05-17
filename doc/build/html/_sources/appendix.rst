.. _appendix:

==============================
附录 常用度量工具与软件库
==============================

市场上的度量方案"百花齐放"，度量相关的开源和商业软件汗牛充栋。
本附录对常用工具做简要介绍。


A.1 开源软件
============

A.1.1 数据采集
--------------

**Telegraf**

Telegraf 是 InfluxData 开发的服务器代理，用于采集、处理和写入度量数据。
支持数百种输入输出插件。

- 语言: Go
- 官网: https://www.influxdata.com/time-series-platform/telegraf/

**Filebeat**

Elastic 的轻量级日志采集器，用于将日志数据转发到 Logstash 或 Elasticsearch。

- 语言: Go
- 官网: https://www.elastic.co/beats/filebeat

**OpenTelemetry Collector**

CNCF 的统一可观测性数据采集器，支持 Metrics、Traces 和 Logs。

- 语言: Go
- 官网: https://opentelemetry.io/docs/collector/

**Prometheus Node Exporter**

用于暴露 Unix/Linux 系统级别的度量数据。

- 语言: Go
- 官网: https://github.com/prometheus/node_exporter

A.1.2 数据存储
--------------

**Elasticsearch**

基于 Apache Lucene 的高度可扩展的开源全文搜索和分析引擎。
可以快速、近实时地存储、搜索和分析大量数据。

- 语言: Java
- 官网: https://www.elastic.co/elasticsearch/

**InfluxDB**

开源的时间序列数据库，为快速并且高可靠性的时间序列数据存取做了优化。

- 语言: Go
- 官网: https://www.influxdata.com

**Prometheus**

开源的系统监控和报警工具包，采用 Pull 模型采集度量数据，
内置强大的查询语言 PromQL。

- 语言: Go
- 官网: https://prometheus.io

**VictoriaMetrics**

高性能、低成本的时序数据库，兼容 Prometheus 协议。

- 语言: Go
- 官网: https://victoriametrics.com

**ClickHouse**

列式存储数据库管理系统，适用于在线分析处理 (OLAP)。

- 语言: C++
- 官网: https://clickhouse.com

A.1.3 数据处理
--------------

**Logstash**

开源的服务器端数据处理管道工具，可以同时从多个源中提取数据，
对其进行转换，然后发送到目的地。

- 语言: Java (JRuby)
- 官网: https://www.elastic.co/logstash/

**Apache Kafka**

分布式流处理平台，用于构建实时数据管道和流式应用。

- 语言: Java/Scala
- 官网: https://kafka.apache.org

**Apache Flink**

分布式流处理框架，支持有状态的实时计算。

- 语言: Java/Scala
- 官网: https://flink.apache.org

A.1.4 数据展示
--------------

**Grafana**

开源的度量分析与可视化平台，支持多种数据源。

- 语言: Go + TypeScript
- 官网: https://grafana.com

**Kibana**

Elastic 的数据可视化套件，为 Elasticsearch 提供图表和仪表盘。

- 语言: TypeScript
- 官网: https://www.elastic.co/kibana/

A.1.5 监控告警
--------------

**Alertmanager**

Prometheus 配套的告警管理工具，负责去重、分组、路由和抑制告警。

- 语言: Go
- 官网: https://prometheus.io/docs/alerting/alertmanager/

**Grafana Alerting**

Grafana 内置的告警功能，支持多种通知渠道。

A.1.6 分布式追踪
-----------------

**Jaeger**

Uber 开源的端到端分布式追踪系统，源于 Dapper 和 OpenZipkin。

- 语言: Go
- 官网: https://www.jaegertracing.io

**Zipkin**

Twitter 开源的分布式追踪系统。

- 语言: Java
- 官网: https://zipkin.io

**SkyWalking**

Apache 开源的 APM 系统，支持分布式追踪、度量分析和日志关联。

- 语言: Java
- 官网: https://skywalking.apache.org

A.1.7 其他工具
--------------

**Graphite**

Python 编写的 Web 应用，用于收集和展示时间序列数据。

- 语言: Python
- 官网: https://graphiteapp.org

**Nagios**

经典的系统和网络监控工具。

- 语言: C
- 官网: https://www.nagios.org

**Zabbix**

企业级的分布式监控平台。

- 语言: C
- 官网: https://www.zabbix.com

**collectd**

系统统计信息收集守护进程。

- 语言: C
- 官网: https://collectd.org


A.2 商业软件
============

.. list-table:: 商业度量软件
   :header-rows: 1
   :widths: 20 30 50

   * - 产品
     - 厂商
     - 特点
   * - Datadog
     - Datadog
     - 全栈可观测性平台，SaaS
   * - New Relic
     - New Relic
     - APM 和基础设施监控
   * - Dynatrace
     - Dynatrace
     - AI 驱动的全栈监控
   * - Splunk
     - Cisco (Splunk)
     - 日志分析和安全信息
   * - AWS CloudWatch
     - Amazon
     - AWS 原生监控服务
   * - Azure Monitor
     - Microsoft
     - Azure 原生监控服务
   * - Google Cloud Monitoring
     - Google
     - GCP 原生监控服务
   * - PagerDuty
     - PagerDuty
     - 事件管理和告警路由


A.3 AI 应用度量工具
====================

随着 LLM、RAG 和 AI Agent 应用的普及，一批专门针对 AI 应用的度量和可观测性工具应运而生。

A.3.1 LLM 可观测性平台
-----------------------

.. list-table:: LLM 可观测性平台
   :header-rows: 1
   :widths: 18 15 67

   * - 工具
     - 类型
     - 特点
   * - LangSmith
     - 商业 (免费层)
     - LangChain 官方平台。Trace 可视化、数据集管理、在线评估、Prompt Playground。
       与 LangChain/LangGraph 深度集成。
   * - LangFuse
     - 开源 + 商业
     - 独立的 LLM 可观测性平台。支持 Trace、Session、评估、Prompt 版本管理。
       可自托管，兼容 OpenAI/Anthropic/LangChain 等多种框架。
   * - Phoenix (Arize)
     - 开源
     - Arize AI 的开源可观测性工具。支持 LLM Trace、Embedding 可视化、
       检索质量分析。与 LlamaIndex/LangChain 集成。
   * - Helicone
     - 开源 + 商业
     - LLM API 代理层，零代码集成。请求日志、成本追踪、缓存、速率限制。
       通过修改 API base URL 即可接入。
   * - Weights & Biases
     - 商业 (免费层)
     - ML 实验追踪平台，扩展支持 LLM 应用。Prompt 追踪、模型评估、
       数据集版本管理。
   * - Braintrust
     - 商业
     - AI 产品评估平台。在线/离线评估、Prompt Playground、数据集管理。
       支持自定义评估函数。

A.3.2 RAG/LLM 评估框架
-----------------------

.. list-table:: AI 评估框架
   :header-rows: 1
   :widths: 18 15 67

   * - 框架
     - 语言
     - 特点
   * - Ragas
     - Python
     - RAG 系统评估的事实标准。提供 Faithfulness、Answer Relevancy、
       Context Precision/Recall 等指标。支持自动化评估流水线。
   * - DeepEval
     - Python
     - 通用 LLM 评估框架。14+ 预定义指标，支持 pytest 集成，
       可作为 CI/CD 中的回归测试。支持自定义评估指标。
   * - TruLens
     - Python
     - 基于反馈函数的 LLM 评估。支持 Groundedness、Relevance、
       Harmfulness 等维度。与 LlamaIndex/LangChain 集成。
   * - promptfoo
     - TypeScript
     - Prompt 测试和评估工具。支持多模型对比、红队测试、
       自定义断言。CLI 驱动，适合 CI/CD 集成。
   * - ARES
     - Python
     - 自动化 RAG 评估系统。使用少量人工标注 + LLM 判断，
       生成统计置信区间。学术界广泛使用。

A.3.3 AI Agent 度量工具
------------------------

.. list-table:: AI Agent 度量工具
   :header-rows: 1
   :widths: 18 15 67

   * - 工具
     - 类型
     - 特点
   * - AgentOps
     - 开源 + 商业
     - 专门的 Agent 可观测性平台。Session 回放、工具调用追踪、
       成本追踪、错误分析。支持 LangChain/CrewAI/AutoGen。
   * - Smith (LangSmith)
     - 商业
     - LangGraph Agent 的原生追踪。可视化 Agent 决策树、
       工具调用链、状态转换。
   * - OpenTelemetry + GenAI
     - 开源标准
     - OTel 的 GenAI 语义约定 (Semantic Conventions)。
       标准化 LLM/Agent 的 Span 属性和度量名称。

A.3.4 向量数据库监控
---------------------

.. list-table:: 向量数据库及其监控
   :header-rows: 1
   :widths: 18 15 67

   * - 数据库
     - 类型
     - 监控特点
   * - ChromaDB
     - 开源 (嵌入式)
     - 轻量级，适合开发和小规模部署。通过自定义 Prometheus Exporter 监控。
   * - Pinecone
     - 商业 (SaaS)
     - 内置监控面板：索引大小、查询延迟、QPS、错误率。
   * - Milvus
     - 开源
     - 原生 Prometheus 指标导出。支持查询延迟、内存使用、索引构建进度等。
   * - Weaviate
     - 开源 + 商业
     - 内置 Prometheus 端点。支持对象数量、查询延迟、向量维度等度量。
   * - Qdrant
     - 开源 + 商业
     - 内置 Prometheus 指标。支持集合大小、搜索延迟、gRPC/REST 请求度量。


A.4 度量类库
============

.. list-table:: 度量类库汇总
   :header-rows: 1
   :widths: 20 15 65

   * - 类库
     - 语言
     - 说明
   * - Micrometer
     - Java
     - 厂商中立的度量门面，Spring Boot 默认集成
   * - Dropwizard Metrics
     - Java
     - 成熟的 Java 度量库
   * - prometheus_client
     - Python
     - Prometheus 官方 Python 客户端
   * - opentelemetry-python
     - Python
     - OpenTelemetry Python SDK
   * - client_golang
     - Go
     - Prometheus 官方 Go 客户端
   * - opentelemetry-go
     - Go
     - OpenTelemetry Go SDK
   * - prom-client
     - Node.js
     - Prometheus Node.js 客户端
   * - opentelemetry-js
     - JavaScript
     - OpenTelemetry JavaScript SDK
