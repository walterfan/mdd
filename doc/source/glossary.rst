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

   AI Agent
      AI 智能体。能够自主感知环境、做出决策并执行操作的 AI 系统。
      在运维场景中，Agent 可以自主处理告警、执行修复操作。

   Chunking
      分块。将长文档切分为较小片段的过程，是 RAG 系统中的关键步骤。
      分块策略直接影响检索质量。

   Context Window
      上下文窗口。LLM 单次推理能处理的最大 Token 数量。
      如 GPT-4 Turbo 为 128K，Claude 3 为 200K。

   DeepEval
      开源的 LLM 评估框架，提供 Faithfulness、Answer Relevancy 等
      预定义评估指标，支持自动化回归测试。

   E2E Latency
      端到端延迟。从用户发出请求到收到完整响应的总时间。
      在 LLM 应用中包括 Prompt 处理、推理、Token 生成等全部耗时。

   Embedding
      嵌入/向量化。将文本、图像等非结构化数据转换为高维向量的过程。
      是 RAG 系统中语义检索的基础。

   Eval-Driven Development
      评估驱动开发。类似 TDD，先定义评估数据集和质量指标，
      再迭代优化 AI 系统的开发方法论。

   Faithfulness
      忠实度。RAG 系统中衡量生成答案是否忠实于检索到的上下文的指标。
      高忠实度意味着低幻觉率。

   Function Calling
      函数调用。LLM 根据用户意图选择并调用外部工具/API 的能力。
      是 AI Agent 的核心机制之一。

   Guardrail
      安全护栏。对 AI 系统输入输出的安全检查机制，
      防止有害内容生成、敏感信息泄露等。

   Hallucination
      幻觉。LLM 生成看似合理但实际上不正确或无依据的内容。
      是 LLM 应用中最需要度量和控制的质量问题。

   LangFuse
      开源的 LLM 可观测性平台，提供 Trace、评估、Prompt 管理等功能。

   LangSmith
      LangChain 提供的 LLM 应用可观测性和评估平台。
      支持 Trace 可视化、数据集管理、在线评估。

   LLM
      Large Language Model，大语言模型。
      基于 Transformer 架构的大规模预训练语言模型，如 GPT-4、Claude、Llama。

   LLM-as-Judge
      以 LLM 作为评判者。使用一个 LLM 来评估另一个 LLM 的输出质量，
      是自动化质量评估的常用方法。

   MRR
      Mean Reciprocal Rank，平均倒数排名。
      衡量检索系统中第一个相关结果排名的指标。

   NDCG
      Normalized Discounted Cumulative Gain，归一化折损累积增益。
      衡量检索结果排序质量的指标，考虑了相关性和位置。

   Precision@K
      前 K 个检索结果中相关结果的比例。RAG 检索质量的核心指标。

   Prompt Engineering
      提示工程。设计和优化 LLM 输入提示以获得更好输出的技术。

   RAG
      Retrieval-Augmented Generation，检索增强生成。
      结合信息检索和文本生成的技术，先从知识库检索相关文档，
      再基于检索结果生成回答，有效减少 LLM 幻觉。

   Ragas
      RAG Assessment，开源的 RAG 系统评估框架。
      提供 Faithfulness、Context Relevancy 等标准化评估指标。

   Recall@K
      知识库中所有相关文档被检索到前 K 个结果中的比例。

   ReAct
      Reasoning + Acting。一种 AI Agent 架构模式，
      交替进行推理（思考下一步）和行动（调用工具）。

   Token
      词元。LLM 处理文本的基本单位。英文约 1 Token ≈ 0.75 个单词，
      中文约 1 Token ≈ 0.5-1 个汉字。Token 数量直接影响 API 成本和延迟。

   Tool Calling
      工具调用。AI Agent 调用外部工具（API、数据库、代码执行器等）
      来完成任务的能力。度量工具调用的准确率和延迟是 Agent 评估的关键。

   TTFT
      Time To First Token，首 Token 时间。
      从发送请求到收到第一个生成 Token 的时间，是 LLM 流式响应的关键延迟指标。

   TPS (LLM)
      Tokens Per Second，每秒生成 Token 数。
      衡量 LLM 推理速度的指标，影响用户感知的流畅度。

   USED
      Usage-Saturation-Error-Delay。
      本书提出的度量核心维度框架，适用于从基础设施到 AI 应用的各个层次。

   USE
      Utilization-Saturation-Errors。
      Brendan Gregg 提出的系统资源分析方法。

   Vector Database
      向量数据库。专门用于存储和检索高维向量的数据库，
      如 ChromaDB、Pinecone、Milvus、Weaviate。是 RAG 系统的核心组件。

   EDD
      Eval-Driven Development，评估驱动开发。
      AI 应用的 TDD——先定义评估数据集和质量阈值，再迭代优化系统。
      核心循环：定义评估集 → 建立基线 → 迭代优化 → 回归验证。

   Error Budget
      错误预算。SLO 允许的不可用时间或错误量。
      例如 99.9% 可用性的 SLO 意味着每月有约 43 分钟的错误预算。

   FinOps
      Financial Operations，云财务管理。
      FinOps for AI 专注于 LLM API 成本的可见性、归因和优化。

   gRPC
      Google Remote Procedure Call。基于 HTTP/2 和 Protocol Buffers 的高性能 RPC 框架。
      支持 Unary、Server Streaming、Client Streaming、Bidirectional Streaming 四种模式。

   Prompt Caching
      提示缓存。将重复使用的 System Prompt 前缀缓存起来，
      减少重复计算，降低 LLM API 输入成本 (最高可降 90%)。

   Runbook
      运维手册。告警触发后的标准操作流程文档，
      包含排查步骤、处理方法和升级路径。每条告警规则都应附带 Runbook。

   Three Guardians
      三大护法。AI 时代软件质量的三个支柱：
      可验证性 (TDD)、可观测性 (MDD)、可理解性 (活文档)。
