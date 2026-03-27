.. _chapter10:

====================================
第 10 章 RAG 与 Agent 的度量体系
====================================

.. admonition:: 本章你将学到

   - RAG 系统的度量架构：检索质量和生成质量的分别度量
   - 检索质量核心指标：Precision@K、Recall@K、MRR、NDCG
   - 生成质量核心指标：Faithfulness、Answer Relevancy
   - AI Agent 的度量挑战：任务完成率、工具调用、循环检测
   - AI 可观测性工具生态：LangSmith、LangFuse、OpenTelemetry GenAI


开篇故事
========

   *一个常见的困境：RAG 知识库问答系统上线后，用户投诉越来越多——
   "回答不准确"、"答非所问"、"明明文档里有，但系统说不知道"。*

   *但监控面板上一切正常——API 成功率 99.8%，P99 延迟在可接受范围内，
   LLM Token 使用量稳定。所有指标都是绿色的。*

   *问题出在哪里？出在只度量了"系统是否正常运行"，
   而没有度量"系统回答得对不对"。HTTP 200 不等于答案正确。*

这个场景揭示了 AI 应用度量的核心盲区：传统度量只能告诉你系统"活着"，
但无法告诉你系统"做得好不好"。RAG 和 Agent 需要专门的质量度量。


10.1 RAG 系统度量
==================

RAG（Retrieval-Augmented Generation）是当前最常见的 AI 应用模式。
它的度量需要覆盖 **检索** 和 **生成** 两个阶段——任何一个阶段出问题，
最终的回答质量都会受影响。

10.1.1 RAG 架构与度量点
-------------------------

.. code-block:: text

   ┌──────────────────────────────────────────────────────────┐
   │                    RAG 系统度量点                          │
   │                                                           │
   │  用户查询                                                 │
   │     │                                                     │
   │     ▼  ① Query 处理延迟                                   │
   │  ┌──────────┐                                             │
   │  │ Query    │  度量: 查询改写次数、扩展词数                  │
   │  │ 处理     │                                             │
   │  └────┬─────┘                                             │
   │       │                                                   │
   │       ▼  ② Embedding 延迟                                 │
   │  ┌──────────┐                                             │
   │  │ Embedding│  度量: 向量化延迟、Token 数                   │
   │  │ 生成     │                                             │
   │  └────┬─────┘                                             │
   │       │                                                   │
   │       ▼  ③ 检索延迟 + 检索质量                             │
   │  ┌──────────┐                                             │
   │  │ 向量检索  │  度量: 检索延迟、Top-K 命中率                 │
   │  │ + 重排序  │        Precision@K、Recall@K、MRR           │
   │  └────┬─────┘                                             │
   │       │                                                   │
   │       ▼  ④ 上下文质量                                     │
   │  ┌──────────┐                                             │
   │  │ Context  │  度量: 上下文相关性、上下文长度                │
   │  │ 组装     │        上下文利用率                           │
   │  └────┬─────┘                                             │
   │       │                                                   │
   │       ▼  ⑤ 生成延迟 + 生成质量                             │
   │  ┌──────────┐                                             │
   │  │ LLM 生成 │  度量: TTFT、TPS、Token 成本                 │
   │  │          │        忠实度、答案相关性、幻觉率              │
   │  └────┬─────┘                                             │
   │       │                                                   │
   │       ▼  ⑥ 端到端                                        │
   │  ┌──────────┐                                             │
   │  │ 返回用户  │  度量: E2E 延迟、用户满意度                   │
   │  └──────────┘                                             │
   └──────────────────────────────────────────────────────────┘

关键洞察：**检索是 RAG 的基础。检索不好，生成再好也没用。**
因此，检索质量度量是 RAG 度量体系中最重要的部分。

10.1.2 检索质量度量
--------------------

**核心指标：**

.. list-table:: RAG 检索质量指标
   :header-rows: 1
   :widths: 20 40 40

   * - 指标
     - 说明
     - 计算方式
   * - Precision@K
     - Top-K 结果中相关文档的比例
     - 相关文档数 / K
   * - Recall@K
     - Top-K 结果覆盖了多少相关文档
     - 命中的相关文档 / 总相关文档
   * - MRR
     - Mean Reciprocal Rank，第一个相关结果的排名倒数
     - 1 / 第一个相关结果的排名
   * - NDCG@K
     - 考虑排名位置的相关性评分
     - 越靠前的相关结果权重越高
   * - Hit Rate
     - Top-K 中至少有一个相关结果的查询比例
     - 有命中的查询数 / 总查询数

**Python 实现：**

.. code-block:: python

   from prometheus_client import Histogram, Counter, Gauge

   rag_retrieval_latency = Histogram(
       "rag_retrieval_latency_seconds",
       "RAG retrieval latency",
       ["index_name"],
       buckets=[0.01, 0.05, 0.1, 0.2, 0.5, 1.0]
   )

   rag_retrieval_precision = Histogram(
       "rag_retrieval_precision_at_k",
       "Precision@K for RAG retrieval",
       ["k"],
       buckets=[0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
   )

   def calculate_precision_at_k(retrieved_ids: list,
                                 relevant_ids: set, k: int) -> float:
       """计算 Precision@K"""
       top_k = retrieved_ids[:k]
       relevant_count = sum(
           1 for doc_id in top_k if doc_id in relevant_ids
       )
       return relevant_count / k

   def calculate_mrr(retrieved_ids: list,
                      relevant_ids: set) -> float:
       """计算 MRR (Mean Reciprocal Rank)"""
       for i, doc_id in enumerate(retrieved_ids):
           if doc_id in relevant_ids:
               return 1.0 / (i + 1)
       return 0.0

10.1.3 生成质量度量（RAG 专用）
--------------------------------

RAG 的生成质量不仅要看"答案好不好"，还要看"答案是否忠于检索到的上下文"。
这是 RAG 与普通 LLM 应用的关键区别。

**Ragas 框架的核心指标：**

.. list-table:: RAG 生成质量指标（Ragas）
   :header-rows: 1
   :widths: 25 40 35

   * - 指标
     - 说明
     - 评估方式
   * - Faithfulness（忠实度）
     - 答案是否被检索到的上下文支持
     - LLM-as-Judge
   * - Answer Relevancy（答案相关性）
     - 答案是否回答了用户的问题
     - LLM-as-Judge
   * - Context Relevancy（上下文相关性）
     - 检索到的上下文是否与问题相关
     - LLM-as-Judge
   * - Context Recall（上下文召回）
     - 上下文是否包含了回答所需的信息
     - 与参考答案对比
   * - Answer Correctness（答案正确性）
     - 答案与参考答案的一致程度
     - 语义相似度 + 事实核查

**使用 Ragas 评估：**

.. code-block:: python

   from ragas import evaluate
   from ragas.metrics import (
       faithfulness, answer_relevancy,
       context_precision, context_recall,
   )
   from datasets import Dataset

   eval_data = Dataset.from_dict({
       "question": ["什么是度量驱动开发？"],
       "answer": ["度量驱动开发(MDD)是一种以度量数据为核心..."],
       "contexts": [["MDD 是指在软件开发过程中..."]],
       "ground_truth": ["度量驱动开发是一种软件工程方法论..."]
   })

   result = evaluate(
       eval_data,
       metrics=[faithfulness, answer_relevancy,
                context_precision, context_recall]
   )
   # {'faithfulness': 0.85, 'answer_relevancy': 0.92,
   #  'context_precision': 0.78, 'context_recall': 0.80}

10.1.4 向量数据库度量
----------------------

向量数据库是 RAG 的核心组件，需要专门的度量：

.. list-table:: 向量数据库度量
   :header-rows: 1
   :widths: 25 35 40

   * - 指标
     - 说明
     - 告警阈值
   * - 查询延迟
     - 向量相似度搜索的延迟
     - P99 > 200ms
   * - 索引大小
     - 向量索引占用的存储空间
     - 接近磁盘容量 80%
   * - 文档数量
     - 索引中的文档/向量数量
     - 监控增长趋势
   * - 写入吞吐
     - 每秒写入的向量数
     - 低于预期吞吐
   * - 召回率
     - 近似搜索 vs 精确搜索的一致性
     - < 95%

10.1.5 端到端度量与 Grafana 面板
----------------------------------

一个完整的 RAG 监控面板应该包含以下区域：

.. code-block:: text

   ┌─────────────────────────────────────────────────────────┐
   │              RAG 系统监控面板                              │
   ├──────────┬──────────┬──────────┬────────────────────────┤
   │ 查询总量  │ 成功率    │ E2E P99  │ 今日 LLM 成本          │
   │ 1,234    │ 98.5%    │ 3.2s     │ $12.50                │
   ├──────────┴──────────┴──────────┴────────────────────────┤
   │  ┌────────────────────────────────────────────────────┐  │
   │  │  查询量趋势 + 错误率 (折线图)                        │  │
   │  └────────────────────────────────────────────────────┘  │
   │  ┌──────────────────────┐  ┌──────────────────────────┐  │
   │  │ 延迟分布 (热力图)     │  │ Token 使用趋势 (面积图)   │  │
   │  │ 检索 vs 生成          │  │ Input vs Output          │  │
   │  └──────────────────────┘  └──────────────────────────┘  │
   │  ┌──────────────────────┐  ┌──────────────────────────┐  │
   │  │ 质量评分趋势          │  │ 检索命中率               │  │
   │  │ Faithfulness          │  │ Hit Rate / Precision@5   │  │
   │  └──────────────────────┘  └──────────────────────────┘  │
   └─────────────────────────────────────────────────────────┘

**关键 PromQL 查询：**

.. code-block:: text

   # RAG 查询成功率
   sum(rate(rag_queries_total{status="success"}[5m]))
   / sum(rate(rag_queries_total[5m]))

   # E2E P99 延迟
   histogram_quantile(0.99,
     rate(rag_e2e_latency_seconds_bucket[5m]))

   # 每小时 LLM 成本
   increase(llm_cost_dollars_total[1h])


10.2 AI Agent 度量
===================

AI Agent 是比 RAG 更复杂的 AI 应用模式。Agent 能自主决策、调用工具、
多步推理，这带来了全新的度量挑战。

10.2.1 Agent 架构与度量点
---------------------------

.. code-block:: text

   ┌──────────────────────────────────────────────────────┐
   │                  AI Agent 度量点                       │
   │                                                       │
   │  用户任务                                             │
   │     │                                                 │
   │     ▼  ① 任务理解                                     │
   │  ┌──────────┐                                         │
   │  │ Planner  │  度量: 规划延迟、规划步骤数               │
   │  └────┬─────┘                                         │
   │       │                                               │
   │       ▼  ② 工具选择与调用（可能多轮）                   │
   │  ┌──────────┐                                         │
   │  │ Tool     │  度量: 调用次数、准确率、延迟、失败率      │
   │  │ Executor │                                         │
   │  └────┬─────┘                                         │
   │       │                                               │
   │       ▼  ③ 中间推理                                   │
   │  ┌──────────┐                                         │
   │  │ Reasoner │  度量: 推理步骤数、Token 消耗、循环检测    │
   │  └────┬─────┘                                         │
   │       │                                               │
   │       ▼  ④ 结果输出                                   │
   │  ┌──────────┐                                         │
   │  │ Output   │  度量: 任务完成率、结果质量、总成本       │
   │  └──────────┘                                         │
   └──────────────────────────────────────────────────────┘

10.2.2 Agent 核心度量指标
---------------------------

.. list-table:: AI Agent 核心度量
   :header-rows: 1
   :widths: 25 35 40

   * - 指标
     - 说明
     - 参考值
   * - Task Success Rate
     - 任务完成率
     - > 80%（简单任务 > 95%）
   * - Steps per Task
     - 每个任务的平均步骤数
     - 越少越好（效率）
   * - Tool Call Accuracy
     - 工具调用准确率
     - > 90%
   * - Tool Call Success Rate
     - 工具执行成功率
     - > 95%
   * - Cost per Task
     - 每个任务的 LLM 成本
     - 取决于业务价值
   * - Loop Detection Rate
     - 循环/卡死检测率
     - 应接近 0%

.. code-block:: python

   from enum import Enum

   class TaskStatus(Enum):
       SUCCESS = "success"
       FAILED = "failed"
       TIMEOUT = "timeout"
       LOOP_DETECTED = "loop_detected"
       GUARDRAIL_BLOCKED = "guardrail_blocked"

   agent_tasks_total = Counter(
       "agent_tasks_total",
       "Total agent tasks",
       ["agent_name", "status"]
   )

   agent_task_steps = Histogram(
       "agent_task_steps",
       "Number of steps per task",
       ["agent_name"],
       buckets=[1, 2, 3, 5, 8, 10, 15, 20, 50]
   )

   agent_tool_calls_total = Counter(
       "agent_tool_calls_total",
       "Total tool calls",
       ["agent_name", "tool_name", "status"]
   )

10.2.3 循环与卡死检测
-----------------------

Agent 最危险的问题之一是 **无限循环** ——Agent 反复调用同一个工具，
或者在两个状态之间来回切换，消耗大量 Token 却没有进展。

.. code-block:: python

   class AgentLoopDetector:
       """检测 Agent 是否陷入循环"""

       def __init__(self, max_steps=20,
                    max_repeated_tools=3, max_cost=1.0):
           self.max_steps = max_steps
           self.max_repeated_tools = max_repeated_tools
           self.max_cost = max_cost
           self.tool_history: list[str] = []
           self.total_cost: float = 0.0

       def check(self, tool_name: str, step_cost: float) -> bool:
           """返回 True 表示检测到异常，应该终止"""
           self.tool_history.append(tool_name)
           self.total_cost += step_cost

           # 检查 1: 步骤数超限
           if len(self.tool_history) > self.max_steps:
               return True

           # 检查 2: 连续重复调用同一工具
           recent = self.tool_history[-self.max_repeated_tools:]
           if (len(recent) == self.max_repeated_tools
               and len(set(recent)) == 1):
               return True

           # 检查 3: 成本超限
           if self.total_cost > self.max_cost:
               return True

           return False

10.2.4 多 Agent 系统度量
--------------------------

当多个 Agent 协作时，度量维度进一步扩展：

.. list-table:: 多 Agent 系统度量
   :header-rows: 1
   :widths: 25 35 40

   * - 指标
     - 说明
     - 度量方式
   * - Agent 间通信延迟
     - Agent 之间消息传递的延迟
     - Histogram
   * - 任务分配准确率
     - Supervisor 分配任务到正确 Agent 的比例
     - Counter (correct/total)
   * - 协作效率
     - 多 Agent vs 单 Agent 的效率比
     - 对比实验
   * - 瓶颈 Agent
     - 哪个 Agent 是整体延迟的瓶颈
     - 各 Agent 耗时占比


10.3 AI 应用的可观测性工具
============================

10.3.1 专用可观测性工具
-------------------------

传统的 Prometheus + Grafana 可以覆盖基础度量，
但 AI 应用还需要专用的可观测性工具来追踪 LLM 调用链：

.. list-table:: AI 可观测性工具
   :header-rows: 1
   :widths: 20 30 50

   * - 工具
     - 类型
     - 特点
   * - LangSmith
     - LangChain 官方
     - Trace 可视化、Prompt 管理、评估套件
   * - LangFuse
     - 开源
     - 自托管、OTel 集成、成本追踪
   * - Phoenix (Arize)
     - 开源
     - Embedding 可视化、RAG 评估、LLM Trace
   * - Helicone
     - SaaS
     - LLM 代理层、自动度量、成本分析
   * - DeepEval
     - 开源测试框架
     - LLM 单元测试、RAG 评估、CI/CD 集成

10.3.2 OpenTelemetry GenAI 语义约定
--------------------------------------

OpenTelemetry 社区正在开发 LLM 语义约定（Semantic Conventions），
为 LLM 调用定义标准化的 Span 属性：

.. code-block:: python

   from opentelemetry import trace

   tracer = trace.get_tracer("rag-service")

   def query_rag(question: str):
       with tracer.start_as_current_span("rag.query") as span:
           # 检索阶段
           with tracer.start_as_current_span("rag.retrieval"):
               docs = vector_store.similarity_search(
                   question, k=5)

           # 生成阶段
           with tracer.start_as_current_span("rag.generation") as gen:
               response = llm.invoke(prompt.format(
                   context=docs, question=question))
               gen.set_attribute("gen_ai.system", "anthropic")
               gen.set_attribute("gen_ai.request.model",
                                  "claude-sonnet-4-20250514")
               gen.set_attribute("gen_ai.usage.input_tokens",
                                  response.usage.input_tokens)
               gen.set_attribute("gen_ai.usage.output_tokens",
                                  response.usage.output_tokens)
           return response

这种标准化的 Trace 格式使得不同的 AI 应用可以使用统一的可观测性后端，
降低了工具碎片化的问题。


.. admonition:: 🔬 动手实验：用 Ragas 评估 RAG 系统
   :class: tip

   **目标**：用 Ragas 框架评估一个简单 RAG 系统的检索和生成质量。

   **步骤**：

   1. 安装依赖：``pip install ragas datasets langchain-openai``
   2. 准备 5-10 个评估样本（问题 + 参考答案 + 上下文）
   3. 运行 Ragas 评估，获取 Faithfulness、Answer Relevancy 等分数
   4. 分析结果：哪个维度最弱？如何改进？

   **完整代码**：见 GitHub 仓库 ``examples/ch10-rag-eval/``


10.4 本章小结
==============

.. note::

   **关键要点：**

   - RAG 系统必须分别度量检索和生成两个阶段
   - 检索质量（Precision@K、MRR）是 RAG 质量的基础
   - Faithfulness（忠实度）是 RAG 生成质量最重要的指标
   - Agent 的循环检测和成本控制是生产环境的必备能力
   - OpenTelemetry GenAI 语义约定正在成为 AI 可观测性的统一标准


.. admonition:: 📝 思考题

   1. 你的 RAG 系统的检索质量和生成质量，哪个是瓶颈？你如何判断？
   2. 如果你的 Agent 在生产环境中陷入了无限循环，你的系统能在多长时间内检测到？
   3. LangSmith 和 LangFuse 各有什么优缺点？你会选择哪个？为什么？
