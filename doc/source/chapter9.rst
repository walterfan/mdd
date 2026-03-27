.. _chapter9:

==============================
第 9 章 LLM 应用的度量体系
==============================

.. admonition:: 本章你将学到

   - LLM 应用与传统 API 在度量上的本质区别
   - LLM 性能度量：TTFT、TPS、E2E Latency 的含义和实现
   - LLM 成本度量：Token 成本模型、成本归因与预算控制
   - LLM 质量度量：幻觉检测、LLM-as-Judge、自动评估方法
   - 如何用 Prometheus 为 LLM 调用添加完整的度量


开篇故事
========

   *这是一个很多团队都会遇到的场景：LLM 服务上线后，
   某天财务部门发来邮件——API 费用突然翻了好几倍。*

   *打开监控面板一看，API 调用量没有异常增长，错误率也正常。
   但问题是——根本没有 Token 用量和成本的监控面板。
   深入排查后发现，某个功能在每次调用时把大量上下文塞进了 Prompt，
   导致每次请求的 Token 消耗远超预期。一个简单的 Prompt 优化就能把成本降大半。*

   *但如果没有度量，你可能要到月底账单出来才会发现这个问题。*

这个场景揭示了 LLM 应用度量的核心挑战：传统的 HTTP 状态码和响应时间
已经无法反映 LLM 服务的真实状态。你需要一套全新的度量体系。


9.1 为什么 LLM 需要专门的度量
===============================

传统 API 调用是确定性的：同样的输入，同样的输出。
LLM 调用是概率性的：同样的 Prompt，每次输出可能不同。

这意味着：

- **正确性无法用单元测试完全覆盖** —— 需要统计性的质量评估
- **成本与输入长度直接相关** —— 一个 Prompt 可能花 $0.001，也可能花 $0.1
- **延迟波动大** —— 取决于输出长度、模型负载、是否 Streaming

.. list-table:: 传统 API vs LLM API 度量对比
   :header-rows: 1
   :widths: 25 35 40

   * - 维度
     - 传统 API
     - LLM API
   * - 正确性
     - 状态码 200 = 成功
     - 200 但答案可能是错的（幻觉）
   * - 延迟
     - 相对稳定（毫秒级）
     - 与输出 Token 数成正比（秒级）
   * - 成本
     - 固定（按请求或资源计费）
     - 按 Token 计费，差异可达 100 倍
   * - 可重复性
     - 确定性
     - 概率性（temperature > 0）
   * - 质量评估
     - 功能测试即可
     - 需要人工评估 + 自动评估

因此，LLM 应用的度量需要覆盖三个传统 API 不需要关心的维度：
**成本**、**质量** 和 **Token 效率**。


9.2 LLM 性能度量
==================

9.2.1 延迟指标详解
-------------------

LLM 的延迟与传统 API 有本质区别。传统 API 的延迟主要取决于服务端处理时间，
而 LLM 的延迟取决于 **输出 Token 的数量**——输出越长，延迟越高。

.. code-block:: text

   ┌──────────────────────────────────────────────────┐
   │              LLM 延迟分解                          │
   │                                                    │
   │  ┌─────────┐  ┌──────────────────┐  ┌──────────┐ │
   │  │ 排队等待 │→│ 首 Token 生成     │→│ 后续生成  │ │
   │  │ Queue    │  │ TTFT             │  │ 逐Token  │ │
   │  │ Time     │  │ Time to First    │  │ TPS      │ │
   │  │          │  │ Token            │  │ Tokens   │ │
   │  │          │  │                  │  │ Per Sec  │ │
   │  └─────────┘  └──────────────────┘  └──────────┘ │
   │                                                    │
   │  E2E Latency = Queue + TTFT + (Output Tokens / TPS)│
   └──────────────────────────────────────────────────┘

关键延迟指标：

.. list-table:: LLM 延迟指标
   :header-rows: 1
   :widths: 20 40 40

   * - 指标
     - 说明
     - 参考值
   * - TTFT
     - Time to First Token，首 Token 延迟
     - < 500ms（交互场景）
   * - TPS
     - Tokens Per Second，生成速率
     - 30-100 TPS（取决于模型）
   * - E2E Latency
     - 端到端延迟（从请求到完整响应）
     - 取决于输出长度
   * - Queue Time
     - 排队等待时间
     - < 100ms（正常负载）

9.2.2 流式 vs 非流式的度量差异
-------------------------------

LLM 调用通常有两种模式，度量方式不同：

- **非流式（Non-streaming）**：等待完整响应后返回。度量简单，直接记录 E2E 延迟。
- **流式（Streaming）**：逐 Token 返回。需要分别度量 TTFT 和 TPS。

流式模式下，用户感知到的延迟是 TTFT（首 Token 出现的时间），
而非 E2E 延迟。因此 **TTFT 是流式场景下最重要的延迟指标**。

9.2.3 性能度量代码实现
-----------------------

.. code-block:: python

   from prometheus_client import Counter, Histogram, Gauge

   # Token 使用量
   llm_tokens_total = Counter(
       "llm_tokens_total",
       "Total tokens consumed",
       ["model", "direction"]  # direction: input/output
   )

   # 请求延迟
   llm_request_duration = Histogram(
       "llm_request_duration_seconds",
       "LLM request duration",
       ["model", "endpoint"],
       buckets=[0.1, 0.5, 1, 2, 5, 10, 30, 60]
   )

   # TTFT
   llm_ttft_seconds = Histogram(
       "llm_ttft_seconds",
       "Time to first token",
       ["model"],
       buckets=[0.1, 0.2, 0.5, 1.0, 2.0, 5.0]
   )

   # 生成速率
   llm_tps = Histogram(
       "llm_tokens_per_second",
       "Token generation rate",
       ["model"],
       buckets=[10, 20, 30, 50, 80, 100, 150]
   )

下面是一个带完整度量的 LLM 调用包装器：

.. code-block:: python

   import time
   import anthropic

   class MetricsLLMClient:
       """带度量的 LLM 客户端包装器"""

       def __init__(self, model: str = "claude-sonnet-4-20250514"):
           self.client = anthropic.Anthropic()
           self.model = model

       def invoke(self, messages: list, **kwargs) -> dict:
           start = time.time()
           try:
               response = self.client.messages.create(
                   model=self.model,
                   messages=messages,
                   **kwargs
               )
               # 记录 Token 使用
               input_tk = response.usage.input_tokens
               output_tk = response.usage.output_tokens
               llm_tokens_total.labels(
                   model=self.model, direction="input"
               ).inc(input_tk)
               llm_tokens_total.labels(
                   model=self.model, direction="output"
               ).inc(output_tk)

               # 记录延迟和生成速率
               duration = time.time() - start
               llm_request_duration.labels(
                   model=self.model, endpoint="messages"
               ).observe(duration)
               if output_tk > 0 and duration > 0:
                   tps = output_tk / duration
                   llm_tps.labels(model=self.model).observe(tps)

               return {
                   "content": response.content[0].text,
                   "input_tokens": input_tk,
                   "output_tokens": output_tk,
                   "duration": duration,
               }
           except Exception as e:
               llm_request_duration.labels(
                   model=self.model, endpoint="messages"
               ).observe(time.time() - start)
               raise


9.3 LLM 成本度量
==================

9.3.1 Token 成本模型
---------------------

LLM 的成本与 Token 使用量直接挂钩。不同模型的定价差异巨大：

.. list-table:: 主流 LLM 定价（2026 年参考价，$/百万 Token）
   :header-rows: 1
   :widths: 30 25 25 20

   * - 模型
     - 输入价格
     - 输出价格
     - 性价比
   * - Claude Sonnet 4
     - $3.00
     - $15.00
     - 高质量
   * - Claude Haiku 4
     - $0.80
     - $4.00
     - 高性价比
   * - GPT-4o
     - $2.50
     - $10.00
     - 通用
   * - GPT-4o-mini
     - $0.15
     - $0.60
     - 极低成本
   * - DeepSeek V3
     - $0.27
     - $1.10
     - 开源替代

9.3.2 成本追踪实现
--------------------

.. code-block:: python

   MODEL_PRICING = {  # $/百万 Token
       "claude-sonnet-4-20250514": {"input": 3.0, "output": 15.0},
       "claude-haiku-4-20250514": {"input": 0.80, "output": 4.0},
       "gpt-4o": {"input": 2.5, "output": 10.0},
       "gpt-4o-mini": {"input": 0.15, "output": 0.60},
   }

   llm_cost_dollars = Counter(
       "llm_cost_dollars_total",
       "Total LLM cost in USD",
       ["model", "use_case"]
   )

   def track_cost(model: str, input_tokens: int,
                   output_tokens: int, use_case: str):
       """追踪 LLM 调用成本"""
       pricing = MODEL_PRICING.get(model, {})
       cost = (
           input_tokens * pricing.get("input", 0)
           + output_tokens * pricing.get("output", 0)
       ) / 1_000_000
       llm_cost_dollars.labels(
           model=model, use_case=use_case
       ).inc(cost)

9.3.3 成本归因与预算控制
--------------------------

在实际系统中，LLM 成本需要按 **用途** 归因，才能知道钱花在了哪里：

.. code-block:: text

   总成本 $5,000/月
   ├── 客服聊天机器人    $2,000 (40%)  ← 高频低成本
   ├── 文档 RAG 问答     $1,500 (30%)  ← 中频中成本
   ├── 代码审查助手      $1,000 (20%)  ← 低频高成本（长上下文）
   └── 内部工具          $500  (10%)

**成本告警规则：**

.. code-block:: yaml

   groups:
     - name: llm-cost-alerts
       rules:
         - alert: LLMCostSpike
           expr: increase(llm_cost_dollars_total[1h]) > 10
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "LLM 成本激增: 过去 1 小时 ${{ $value }}"

         - alert: LLMDailyCostExceeded
           expr: increase(llm_cost_dollars_total[24h]) > 100
           for: 10m
           labels:
             severity: critical
           annotations:
             summary: "LLM 日成本超限: ${{ $value }}/天"

9.3.4 FinOps for AI
--------------------

AI 应用的成本管理（FinOps for AI）是一个新兴领域。核心实践包括：

1. **成本可见性**：每个 API 调用都追踪 Token 和成本
2. **成本归因**：按功能/团队/用户归因成本
3. **预算告警**：设置日/周/月预算上限
4. **优化策略**：Prompt 压缩、模型降级、缓存、批处理

.. tip::

   一个简单但有效的优化：**Prompt 缓存**。
   如果多个请求使用相同的 System Prompt，可以利用 Anthropic 的 Prompt Caching
   功能，将重复的 Prompt 前缀缓存起来，输入成本降低 90%。


9.4 LLM 质量度量
==================

LLM 输出的质量评估是最具挑战性的部分。
HTTP 200 不代表答案正确——这是 LLM 度量与传统 API 度量的根本区别。

9.4.1 自动评估指标
-------------------

.. list-table:: LLM 质量自动评估方法
   :header-rows: 1
   :widths: 25 35 40

   * - 方法
     - 说明
     - 适用场景
   * - BLEU / ROUGE
     - 与参考答案的文本相似度
     - 翻译、摘要
   * - BERTScore
     - 基于 BERT 嵌入的语义相似度
     - 通用文本生成
   * - LLM-as-Judge
     - 用另一个 LLM 评分
     - 开放式问答、创意写作
   * - Exact Match
     - 精确匹配率
     - 分类、实体提取
   * - Hallucination Rate
     - 幻觉率
     - RAG、知识问答

9.4.2 LLM-as-Judge 实现
-------------------------

LLM-as-Judge 是目前最通用的自动评估方法——用一个 LLM 来评估另一个 LLM 的输出：

.. code-block:: python

   JUDGE_PROMPT = """请评估以下 AI 回答的质量。

   用户问题: {question}
   AI 回答: {answer}
   参考答案: {reference}

   请从以下维度评分（1-5 分）:
   1. 准确性: 答案是否正确
   2. 完整性: 是否回答了所有要点
   3. 相关性: 是否切题
   4. 清晰度: 表达是否清晰

   请以 JSON 格式返回:
   {{"accuracy": X, "completeness": X,
     "relevancy": X, "clarity": X, "overall": X}}
   """

   llm_quality_score = Histogram(
       "llm_quality_score",
       "LLM output quality score (1-5)",
       ["model", "dimension"],
       buckets=[1, 2, 3, 4, 5]
   )

   async def evaluate_output(question, answer, reference, model):
       """用 LLM-as-Judge 评估输出质量"""
       client = anthropic.Anthropic()
       response = client.messages.create(
           model="claude-haiku-4-20250514",  # 用低成本模型做评估
           max_tokens=200,
           messages=[{
               "role": "user",
               "content": JUDGE_PROMPT.format(
                   question=question,
                   answer=answer,
                   reference=reference
               )
           }]
       )
       scores = json.loads(response.content[0].text)
       for dim, score in scores.items():
           llm_quality_score.labels(
               model=model, dimension=dim
           ).observe(score)
       return scores

9.4.3 幻觉检测
----------------

幻觉（Hallucination）是 LLM 最危险的问题之一——模型生成了看起来合理但实际上错误的信息。

常见的幻觉检测方法：

1. **NLI（自然语言推理）模型**：判断答案是否被上下文"蕴含"
2. **LLM 交叉验证**：用另一个 LLM 判断答案是否被上下文支持
3. **实体提取 + 事实核查**：提取答案中的实体和事实，与知识库对比

.. code-block:: python

   llm_hallucination_total = Counter(
       "llm_hallucination_total",
       "Detected hallucinations",
       ["model", "severity"]  # minor/major/critical
   )

在生产环境中，建议对 LLM 输出进行 **采样评估**：
不需要评估每一个响应，而是按一定比例（如 5-10%）抽样进行质量评估，
将结果写入 Prometheus，在 Grafana 上监控质量趋势。

9.4.4 人工评估 vs 自动评估
----------------------------

.. list-table:: 人工评估 vs 自动评估
   :header-rows: 1
   :widths: 20 40 40

   * - 维度
     - 人工评估
     - 自动评估
   * - 准确性
     - 高（黄金标准）
     - 中等（与人工评估相关性 ~0.7-0.8）
   * - 成本
     - 高（需要人力）
     - 低（API 调用费用）
   * - 速度
     - 慢（小时/天级）
     - 快（秒/分钟级）
   * - 可扩展性
     - 差
     - 好
   * - 适用场景
     - 基线建立、定期校准
     - 日常监控、CI/CD 集成

**推荐实践**：用人工评估建立基线和校准自动评估，
用自动评估（LLM-as-Judge）做日常监控和 CI/CD 集成。


.. admonition:: 🔬 动手实验：为 LLM 调用添加完整度量
   :class: tip

   **目标**：在 15 分钟内为一个 OpenAI/Anthropic API 调用添加延迟、Token、成本度量。

   **步骤**：

   1. 安装依赖：``pip install prometheus-client anthropic``
   2. 复制本章的 ``MetricsLLMClient`` 和 ``track_cost`` 代码
   3. 添加一个 ``/metrics`` 端点暴露 Prometheus 指标
   4. 调用几次 LLM API，观察指标变化
   5. （可选）用 Docker 启动 Prometheus + Grafana，导入面板

   **完整代码**：见 GitHub 仓库 ``examples/ch9-llm-metrics/``


9.5 本章小结
=============

.. note::

   **关键要点：**

   - LLM 应用的度量 = 传统度量（延迟/错误率）+ AI 特有度量（成本/质量/幻觉）
   - TTFT 是流式场景下最重要的延迟指标
   - Token 成本需要按用途归因，设置预算告警
   - LLM-as-Judge 是目前最通用的自动质量评估方法
   - 生产环境建议采样评估（5-10%），而非全量评估


.. admonition:: 📝 思考题

   1. 你的 LLM 应用目前追踪了哪些度量指标？是否覆盖了性能、成本、质量三个维度？
   2. 如果你的 LLM 成本突然翻倍，你能在多长时间内发现并定位原因？
   3. LLM-as-Judge 方法有什么局限性？在什么场景下它可能不可靠？
