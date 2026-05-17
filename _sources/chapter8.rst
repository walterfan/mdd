.. _chapter8:

======================================
第 8 章 全程度量与可观测性
======================================

.. admonition:: 本章你将学到

   - AI 辅助编程时代 MDD 为什么比以往更重要
   - 三大护法（TDD + MDD + 活文档）的协同实践
   - 可观测性三支柱：Metrics、Traces、Logs
   - 全链路追踪的实现方法
   - AIOps：AI 驱动的智能运维


开篇故事
========

   *AI 能让你写代码快 10 倍，但如果配套方法跟不上，
   你可能在以 10 倍速度制造技术债。*

   *设想这样一个场景：一个工程师用 AI 助手在一天内生成了大量代码。
   代码能跑，测试也通过了。但上线一段时间后，服务开始周期性崩溃。
   排查发现，AI 生成的代码在循环中创建了 HTTP 客户端但没有关闭连接，
   导致连接池耗尽。*

   *如果有连接池使用率的度量和告警，这个问题在上线第一天就能发现。
   这就是 MDD 在 AI 时代的价值——AI 写的代码不一定比人写的更可靠，
   但度量能帮你尽早发现问题。*

本章讨论全链路度量和可观测性三支柱，
回答一个关键问题：**在 AI 辅助编程时代，MDD 为什么比以往任何时候都更重要？**


8.1 AI 辅助编程的悖论
======================

8.1.1 速度与质量的矛盾
-----------------------

AI 工具 (Claude, Copilot, ChatGPT) 改变了编程的方式:

.. list-table:: 传统开发 vs AI 辅助开发
   :header-rows: 1
   :widths: 20 40 40

   * - 维度
     - 传统开发
     - AI 辅助开发
   * - 代码来源
     - 你自己写，边写边想
     - AI 生成，你可能看都没仔细看
   * - 理解程度
     - 每行代码都在你脑子里
     - 可能只理解了大概逻辑
   * - 验证方式
     - 边写边测，出问题立刻知道
     - 生成完了才发现，调试成本高
   * - 质量控制
     - 靠经验和 Code Review
     - 靠测试用例和指标监控
   * - 维护成本
     - 自己写的，改起来有底
     - AI 写的，改时怕牵一发动全身

**核心问题**: AI 生成代码的速度远超人类阅读和理解的速度。
你可能还没搞清楚第 100 行在干什么，AI 已经给你写完 500 行了。

8.1.2 三大护法
--------------

要驯服 AI 这匹烈马，你需要三大护法:

.. code-block:: text

              可验证性（TDD）
             "确保写对了"
                   ▲
                  / \
                 /   \
                / 质量 \
               / 保障   \
              ▼         ▼
     可观测性（MDD）    可理解性（活文档）
    "确保跑得好"       "确保看得懂"

.. list-table:: 三大护法
   :header-rows: 1
   :widths: 15 20 20 45

   * - 护法
     - 核心问题
     - 方法
     - 在 AI 时代如何应用
   * - 可验证性
     - AI 写对了吗？
     - TDD
     - 先让 AI 写测试用例，Review 后再实现
   * - **可观测性**
     - **代码跑得怎样？**
     - **MDD**
     - **在 Prompt 中明确度量需求，代码自带监控**
   * - 可理解性
     - 人能看懂吗？
     - 活文档
     - 要求注释、架构图、运维手册

缺少任何一个:

- **缺可验证性**: 不知道对不对，线上出问题才发现
- **缺可观测性**: 出了问题定位不了，只能瞪眼
- **缺可理解性**: 改不动，越改越乱，最后只能重写


8.2 MDD: AI 时代的可观测性护法
==============================

8.2.1 为什么 MDD 在 AI 时代更重要
-----------------------------------

传统开发中，开发者对每一行代码都了如指掌，出了问题往往能凭经验快速定位。
但 AI 生成的代码:

- **你不完全理解它的内部逻辑** -- 度量数据成为你了解运行时行为的唯一窗口
- **边界条件可能没有被考虑** -- 度量报警帮你发现 AI 遗漏的异常场景
- **性能特征未知** -- 你需要度量来确认 AI 选择的算法和数据结构是否合理

MDD 让你从"我觉得代码应该没问题"变成"数据告诉我代码确实没问题"。

8.2.2 让 AI Build in MDD
--------------------------

**错误示范:**

.. code-block:: text

   帮我写一个 HTTP 请求重试逻辑。

AI 会给你一个能用的实现，但你不知道:
重试了几次？哪些请求在重试？重试成功率多少？

**正确示范:**

.. code-block:: text

   我需要一个带指标监控的 HTTP 请求重试逻辑。

   功能要求：
   - 支持指数退避重试（最多 3 次）
   - 可配置重试条件

   度量要求（用 Prometheus metrics）：
   - http_request_total: 总请求数（按 method, path, status_code 分类）
   - http_request_retry_total: 重试次数
   - http_request_duration_seconds: 请求耗时分布（Histogram）

   请同时生成：
   1. Go 实现代码（包含度量埋点）
   2. 对应的测试用例
   3. PromQL 告警规则（重试成功率 < 50% 时告警）

**AI 会给你什么？**

- 一个带埋点的实现
- 可直接运行的测试
- 现成的告警规则

**你的收益？**

代码上线第一天，就能看到关键指标。
出问题时，5 分钟定位，而不是 5 小时。


8.3 完整的 AI 辅助开发流程
==========================

将三大护法融入 AI 辅助开发的完整流程:

.. code-block:: text

   Step 1: 定义验收测试 (TDD)
      │   - 正常流程、边界条件、异常场景
      │   - 让 AI 先写测试，你 Review
      ▼
   Step 2: 设计度量方案 (MDD)
      │   - Golden Signals (Latency, Traffic, Errors, Saturation)
      │   - SLO 定义
      │   - 告警规则
      ▼
   Step 3: 让 AI 生成实现
      │   - 必须满足所有测试用例
      │   - 必须包含度量埋点
      │   - 必须有清晰的注释
      ▼
   Step 4: 验证和迭代
      │   - 运行测试
      │   - 启动服务，检查度量面板
      │   - 运行负载测试
      ▼
   Step 5: 部署和监控
       - 金丝雀发布
       - 对比基线度量
       - 自动回滚

8.3.1 实战示例: 消息推送服务
-----------------------------

**Step 1: 定义验收测试**

.. code-block:: text

   ## 验收测试用例

   ### 正常流程
   - 用户 A 发送消息，用户 B 能在 100ms 内收到
   - 支持 10000 个并发连接

   ### 边界条件
   - 用户离线时，消息缓存 24 小时
   - 单用户消息队列 > 1000 条时，触发限流

   ### 异常场景
   - WebSocket 连接中断，自动重连（最多 3 次）
   - Redis 挂掉，降级到内存队列

**Step 2: 定义度量指标**

.. code-block:: text

   ## RED Metrics
   - Request Rate: 每秒推送消息数
   - Error Rate: 推送失败率（目标 < 0.1%）
   - Duration: P99 推送延迟（目标 < 100ms）

   ## 业务指标
   - active_connections: 当前活跃连接数
   - message_queue_depth: 待推送消息队列深度
   - offline_message_count: 离线消息数量

**Step 3: 发给 AI 的 Prompt**

.. code-block:: text

   请实现实时消息推送服务，要求：
   1. 按照测试用例，确保所有测试通过
   2. 按照度量方案，加入 Prometheus 指标埋点
   3. 使用 Go + gorilla/websocket + Redis
   4. 代码要包含：
      - 核心实现 (message_service.go)
      - 测试代码 (message_service_test.go)
      - 度量定义 (metrics.go)
      - Docker Compose (包含 Prometheus + Grafana)
      - README (如何运行、如何查看监控)


8.4 可观测性三支柱
==================

8.4.1 Metrics + Logs + Traces
------------------------------

.. code-block:: text

   ┌──────────────────────────────────────────────────┐
   │              可观测性 (Observability)              │
   ├────────────┬────────────────┬────────────────────┤
   │  Metrics   │    Logging     │     Tracing        │
   │  度量指标   │    日志         │     链路追踪       │
   │            │                │                    │
   │ 告诉你      │ 告诉你          │ 告诉你             │
   │ "出了什么   │ "为什么         │ "在哪里            │
   │  问题"     │  出问题"        │  出的问题"         │
   │            │                │                    │
   │ Counter    │ Structured     │ Spans              │
   │ Gauge      │ JSON logs      │ Trace ID           │
   │ Histogram  │ Log levels     │ Context            │
   │            │                │ Propagation        │
   └────────────┴────────────────┴────────────────────┘

三者通过 **Trace ID** 关联:

.. code-block:: text

   告警: P99 延迟 > 2s
       │
       ▼ (Metrics → Trace)
   追踪: trace_id = abc123, 总耗时 2.5s
       │ └── potato-server: 2.3s
       │     └── MySQL query: 2.1s
       │
       ▼ (Trace → Logs)
   日志: [abc123] Slow query detected:
         SELECT * FROM potato WHERE status = 'PENDING'
         AND created_at > '2026-01-01'  -- missing index!

8.4.2 OpenTelemetry: 统一框架
------------------------------

OpenTelemetry 将三大信号统一在一个框架中:

.. code-block:: text

   应用程序 (Go/Python/Java/C++)
       │ OpenTelemetry SDK
       │ ┌────────┬───────┬────────┐
       │ │Metrics │Traces │ Logs   │
       │ └────────┴───────┴────────┘
       │         OTLP
       ▼
   OTel Collector
       │ ┌─────────┬──────────┬───────────┐
       │ │Receivers│Processors│ Exporters │
       │ └─────────┴──────────┴───────────┘
       │
   ┌───┴──────────┬──────────────┐
   ▼              ▼              ▼
   Prometheus     Jaeger         Loki
   (Metrics)      (Traces)       (Logs)
       │              │              │
       └──────────────┴──────────────┘
                      │
                      ▼
                   Grafana
                 (统一展示)

8.4.3 结构化日志
-----------------

.. code-block:: json

   {
     "timestamp": "2026-02-08T10:30:00Z",
     "level": "ERROR",
     "service": "potato-server",
     "trace_id": "abc123",
     "span_id": "def456",
     "message": "Failed to create potato",
     "error": "DuplicateKeyException",
     "potato_name": "Learn MDD",
     "user_id": "user-789",
     "duration_ms": 150
   }

**Go 结构化日志 (slog):**

.. code-block:: go

   import "log/slog"

   slog.Error("Failed to create potato",
       "trace_id", span.SpanContext().TraceID(),
       "error", err,
       "potato_name", potato.Name,
       "duration_ms", duration.Milliseconds(),
   )

**Python 结构化日志 (structlog):**

.. code-block:: python

   import structlog

   logger = structlog.get_logger()
   logger.error("Failed to create potato",
       trace_id=span.get_span_context().trace_id,
       error=str(e),
       potato_name=potato.name,
       duration_ms=duration_ms,
   )


8.5 客户端度量
==============

8.5.1 Web 前端: Core Web Vitals
---------------------------------

.. list-table:: Core Web Vitals
   :header-rows: 1
   :widths: 15 35 25 25

   * - 指标
     - 说明
     - 良好
     - 需改善
   * - LCP
     - 最大内容绘制时间
     - <= 2.5s
     - > 4.0s
   * - INP
     - 交互到下一次绘制
     - <= 200ms
     - > 500ms
   * - CLS
     - 累积布局偏移
     - <= 0.1
     - > 0.25

8.5.2 移动端度量
-----------------

- **启动时间**: 冷启动/热启动
- **崩溃率**: 应用崩溃频率
- **网络请求**: 延迟、失败率
- **电池消耗**: 应用耗电量


8.6 AIOps: AI 驱动的智能运维
==============================

8.6.1 从规则告警到智能告警
--------------------------

.. code-block:: text

   传统告警:  if error_rate > 5%  → 报警
   智能告警:  if 当前模式偏离历史基线 → 报警

   传统根因:  人工逐一排查
   智能根因:  AI 自动关联多个异常指标

AIOps 的核心能力:

- **异常检测**: 自动学习正常模式，检测偏差
- **根因分析**: 自动关联多个异常指标，定位根因
- **容量预测**: 基于历史趋势预测未来资源需求
- **智能告警**: 减少误报，自动分级和路由

8.6.2 大数据与度量
-------------------

度量数据的 4V 特征:

- **Volume (量大)**: 每秒数百万数据点
- **Variety (多样)**: 指标、日志、追踪
- **Velocity (高速)**: 实时产生和处理
- **Value (低密度)**: 大量数据中蕴含少量有价值信息

8.6.3 AI Agent 自主运维
-------------------------

随着 AI Agent 技术的成熟，运维正在从"AI 辅助人"走向"Agent 自主执行":

.. code-block:: text

   传统运维:  告警 → 人工排查 → 人工操作 → 人工验证
   AI 辅助:   告警 → AI 建议 → 人工操作 → 人工验证
   Agent 运维: 告警 → Agent 分析 → Agent 执行 → Agent 验证 → 人工审批

**Agent 运维的度量框架:**

.. list-table:: AI Agent 运维度量
   :header-rows: 1
   :widths: 25 35 40

   * - 度量维度
     - 指标
     - 说明
   * - 自主处理率
     - ``agent_auto_resolved_total / incidents_total``
     - Agent 无需人工介入即可解决的故障比例
   * - 决策准确率
     - ``agent_correct_actions / agent_total_actions``
     - Agent 采取的操作中正确的比例
   * - 响应时间
     - ``agent_response_seconds``
     - 从告警触发到 Agent 开始处理的时间
   * - 恢复时间
     - ``agent_mttr_seconds``
     - Agent 处理故障的平均恢复时间
   * - 误操作率
     - ``agent_wrong_actions / agent_total_actions``
     - Agent 执行了错误操作的比例（安全红线）
   * - 人工升级率
     - ``agent_escalated_total / incidents_total``
     - Agent 无法处理需要升级给人工的比例
   * - 成本节省
     - ``manual_hours_saved``
     - Agent 替代人工节省的工时

**Agent 运维的安全护栏:**

.. code-block:: python

   # Agent 运维的安全度量
   from prometheus_client import Counter, Histogram, Gauge

   # 操作审计
   agent_actions_total = Counter(
       "agent_actions_total",
       "Total actions taken by ops agent",
       ["action_type", "result"]  # restart/scale/rollback, success/failed/blocked
   )

   # 安全护栏触发
   agent_guardrail_triggered = Counter(
       "agent_guardrail_triggered_total",
       "Times agent action was blocked by guardrail",
       ["guardrail_type"]  # blast_radius/approval_required/rate_limit
   )

   # 爆炸半径控制
   agent_blast_radius = Gauge(
       "agent_blast_radius_percentage",
       "Percentage of infrastructure affected by agent action"
   )

.. warning::

   AI Agent 运维的核心原则: **宁可漏处理，不可误操作。**

   - 所有破坏性操作（重启、回滚、缩容）必须有人工审批
   - 爆炸半径超过 10% 的操作自动阻断
   - 所有 Agent 操作必须有完整的审计日志
   - 误操作率超过 1% 时自动降级为"建议模式"


8.7 度量驱动开发的回顾与展望
==============================

8.7.1 MDD 方法论全景图
-----------------------

.. code-block:: text

   ┌──────────────────────────────────────────────────────┐
   │              度量驱动开发 (MDD) 全景图                 │
   │                                                       │
   │  设计阶段                                             │
   │  ├── 定义 SLI/SLO                                    │
   │  ├── 设计度量方案 (USED)                              │
   │  └── 选择技术栈                                       │
   │                                                       │
   │  开发阶段                                             │
   │  ├── 度量代码质量 (静态分析)                           │
   │  ├── 度量开发进度 (DORA)                              │
   │  ├── 内建度量埋点                                     │
   │  └── AI 辅助: Prompt 中前置度量需求                    │
   │                                                       │
   │  测试阶段                                             │
   │  ├── 性能基准测试                                     │
   │  ├── 负载/压力测试                                    │
   │  └── 验证度量数据采集                                  │
   │                                                       │
   │  部署阶段                                             │
   │  ├── 金丝雀发布 + 度量对比                             │
   │  ├── GitOps 自动化                                    │
   │  └── 自动回滚 (指标异常)                               │
   │                                                       │
   │  运维阶段                                             │
   │  ├── 可观测性三支柱                                    │
   │  ├── 告警与响应 (SOP)                                 │
   │  ├── 容量规划与自动伸缩                                │
   │  └── AIOps 智能运维                                   │
   └──────────────────────────────────────────────────────┘

8.7.2 未来趋势
--------------

1. **eBPF**: 内核级别的无侵入式度量采集
2. **OpenTelemetry GenAI**: OTel 的 GenAI 语义约定，标准化 LLM/Agent 度量
3. **评估驱动开发 (EDD)**: 像 TDD 一样，先写评估集再优化 AI 系统
4. **AI Agent 自主运维**: Agent 不仅辅助运维，而是自主执行修复操作
5. **LLM 可观测性平台**: LangSmith/LangFuse 等专用工具成为标配
6. **Chaos Engineering**: 基于度量验证系统韧性
7. **Platform Engineering**: 平台团队提供"度量即服务"
8. **成本工程 (FinOps for AI)**: LLM 成本优化成为专门的工程实践

8.7.3 一句话总结
-----------------

   *在 AI 时代，测试用例是你的"验收报告"，度量指标是你的"仪表盘"，*
   *活文档是你的"地图"。三者缺一，你就是在用 10 倍速度制造技术债。*


8.8 实战检查清单
================

下次让 AI 帮你写代码时，带上这份清单:

**可验证性 (TDD) -- 先做:**

- [ ] 明确验收标准（正常流程、边界条件、异常场景）
- [ ] 让 AI 先写测试用例，你 Review 后再实现
- [ ] 确保测试覆盖率 > 80%
- [ ] 提供一键运行测试的脚本

**可观测性 (MDD) -- 同步做:**

- [ ] 定义关键指标（RED Metrics + 业务指标 + 资源指标）
- [ ] 在代码中加入埋点（Prometheus / OpenTelemetry）
- [ ] 生成监控面板（Grafana）
- [ ] 配置告警规则（SLO 违反时触发）
- [ ] 写出指标查询示例（PromQL）

**可理解性 (活文档) -- 同步做:**

- [ ] 关键函数有清晰的 docstring
- [ ] 复杂逻辑有"为什么这样设计"的注释
- [ ] 有系统架构图（Mermaid）
- [ ] 有运维手册 RUNBOOK.md

**AI 应用额外检查项 (如果涉及 LLM/RAG/Agent):**

- [ ] Token 使用量有监控（input/output 分开）
- [ ] 成本有追踪和预算告警
- [ ] LLM 延迟有 Histogram（TTFT + E2E）
- [ ] 有质量评估机制（采样 + LLM-as-Judge）
- [ ] RAG 检索质量有评估（Precision@K、Hit Rate）
- [ ] Agent 工具调用有度量（次数、准确率、延迟）
- [ ] 有评估数据集和回归测试
- [ ] Prompt 版本有管理和灰度机制


8.9 本章小结
============

本章讨论了 AI 时代的全程度量驱动:

- AI 辅助编程时代，MDD 作为"可观测性护法"比以往更重要
- 三大护法 (TDD + MDD + 活文档) 是质量保障的黄金三角
- 可观测性三支柱 (Metrics + Logs + Traces) 通过 OpenTelemetry 统一
- AIOps 让度量分析从规则驱动走向智能驱动
- 不要先让 AI 写完代码再补度量，而是 **从一开始就 Build in 可观测性**

.. note::

   **最后一个建议:**

   不要一上来就让 AI 生成 500 行代码。先让它:

   1. 写 10 个测试用例（你 Review）—— 可验证性
   2. 定义 5 个关键指标（你确认）—— 可观测性
   3. 画一张架构图（你理解）—— 可理解性
   4. 再开始写实现

   **花 15 分钟前期准备，能省下 3 小时后期调试和维护时间。**


.. admonition:: 🔬 动手实验：为 AI 生成的代码添加可观测性
   :class: tip

   **目标**：让 AI 生成一个带完整可观测性的 HTTP 服务，验证三大护法的协同效果。

   **步骤**：

   1. 用 AI 生成一个简单的 TODO API (CRUD)
   2. 要求 AI 同时生成：单元测试 (TDD)、度量代码 (MDD)、API 文档 (活文档)
   3. 运行测试，确认通过
   4. 启动服务，访问 ``/metrics``，确认度量正常
   5. 用 k6 做一次简单的负载测试，观察度量变化

   **完整代码**：见 GitHub 仓库 ``examples/ch8-three-guardians/``


.. admonition:: 📝 思考题

   1. 在你的团队中，TDD、MDD、活文档三大护法的实践程度如何？哪个最弱？
   2. 如果你的系统要接入 OpenTelemetry，迁移成本有多大？最大的障碍是什么？
   3. AIOps 能否完全替代人工告警分析？它的局限性在哪里？
