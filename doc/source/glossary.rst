.. _glossary:

======
术语表
======

.. glossary::

   APDEX
      Application Performance Index，应用性能指数。
      一个衡量用户满意度的标准指标，基于响应时间计算。

   AIOps
      Artificial Intelligence for IT Operations，智能运维。
      利用 AI/ML 技术自动化 IT 运维流程。

   APM
      Application Performance Management，应用性能管理。
      监控和管理应用程序性能和可用性的实践。

   CI/CD
      Continuous Integration / Continuous Delivery，持续集成/持续交付。
      自动化的软件构建、测试和部署流水线。

   Circuit Breaker
      断路器模式。当下游服务故障时快速失败，防止级联故障。

   CLS
      Cumulative Layout Shift，累积布局偏移。
      Core Web Vitals 之一，衡量页面视觉稳定性。

   Counter
      计数器。一种单调递增的度量类型，用于记录累计值。

   DAU
      Daily Active Users，日活跃用户数。

   DORA
      DevOps Research and Assessment。
      Google 提出的四个关键指标: 部署频率、变更前置时间、变更失败率、服务恢复时间。

   eBPF
      Extended Berkeley Packet Filter。
      Linux 内核技术，允许在内核中运行沙盒程序进行无侵入式监控。

   EWMA
      Exponentially Weighted Moving Average，指数加权移动平均。

   Gauge
      测量值/仪表。一种可增可减的度量类型，表示某个瞬时值。

   Golden Signals
      黄金信号。Google SRE 提出的四大关键指标: 延迟、流量、错误、饱和度。

   Histogram
      直方图。一种度量类型，用于统计数据的分布情况。

   HPA
      Horizontal Pod Autoscaler。
      Kubernetes 中基于度量的自动水平伸缩机制。

   INP
      Interaction to Next Paint，交互到下一次绘制。
      Core Web Vitals 之一，衡量页面交互响应性。

   JMT
      Join Meeting Time，参会时间。
      网络会议系统中用户加入会议所需的时间。

   LCP
      Largest Contentful Paint，最大内容绘制时间。
      Core Web Vitals 之一，衡量页面加载性能。

   MDD
      Metrics Driven Development，度量驱动开发。
      以度量为核心的软件开发方法论。

   Meter
      速率计。一种度量类型，用于记录事件的平均速率。

   MTTR
      Mean Time To Recovery，平均恢复时间。
      从故障发生到服务恢复所需的平均时间。

   NPS
      Net Promoter Score，净推荐值。
      衡量客户忠诚度的指标。

   OpenTelemetry
      CNCF 的统一可观测性框架，将 Metrics、Traces、Logs 三大信号标准化。

   OTLP
      OpenTelemetry Protocol，OpenTelemetry 协议。
      用于传输遥测数据的标准协议。

   P50/P90/P95/P99
      百分位数。P99 = 99th percentile，表示 99% 的请求的延迟低于该值。

   PDCA
      Plan-Do-Check-Act，戴明循环。
      质量管理和持续改进的方法论。

   PromQL
      Prometheus Query Language，Prometheus 查询语言。

   QPS
      Queries Per Second，每秒查询数。

   RED
      Rate-Errors-Duration。Tom Wilkie 提出的微服务度量方法。

   RUM
      Real User Monitoring，真实用户监控。
      采集真实用户的体验数据进行分析。

   SLA
      Service Level Agreement，服务级别协议。
      服务提供者与客户之间关于服务质量的约定。

   SLI
      Service Level Indicator，服务级别指标。
      衡量服务质量的具体指标。

   SLO
      Service Level Objective，服务级别目标。
      服务提供者对 SLI 设定的目标值。

   SRE
      Site Reliability Engineering，站点可靠性工程。
      Google 提出的运维方法论。

   Timer
      计时器。一种度量类型，结合了直方图和速率计。

   TPS
      Transactions Per Second，每秒事务数。

   USE
      Utilization-Saturation-Errors。
      Brendan Gregg 提出的系统资源分析方法。
