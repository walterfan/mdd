.. _chapter11:

==============================
第 11 章 评估驱动开发（EDD）
==============================

.. admonition:: 本章你将学到

   - 什么是评估驱动开发（EDD），它与 TDD 的关系
   - EDD 的核心循环：定义评估集 → 建立基线 → 迭代优化 → 回归验证
   - 如何构建高质量的评估数据集
   - 评估框架实战：Ragas、DeepEval、promptfoo 的对比与使用
   - 度量金字塔：从 MDD 到 EDD 的方法论升华


开篇故事
========

   *设想这样一个场景：为了降低成本，你精简了 System Prompt——
   从 500 Token 缩减到 200 Token。上线后，LLM 成本确实降了不少。*

   *但过了一段时间，用户投诉明显增加。排查发现，精简 Prompt 的过程中，
   不小心删掉了一条关键的约束指令，导致 LLM 在某些场景下开始"编造"答案。*

   *如果在上线前跑过一轮评估——用几十个标准问答对测试一下——
   就能在几分钟内发现这个问题。*

这个场景揭示了 AI 应用开发中的一个核心挑战：
**你无法用传统的单元测试来验证 LLM 的输出质量。**
你需要一种新的方法论——评估驱动开发（Eval-Driven Development, EDD）。


11.1 什么是评估驱动开发
=========================

11.1.1 从 TDD 到 EDD
----------------------

传统软件有 TDD（测试驱动开发），AI 应用需要 EDD（评估驱动开发）：

.. code-block:: text

   TDD 循环:  写测试 → 写代码 → 跑测试 → 重构
   EDD 循环:  写评估集 → 写 Prompt/RAG → 跑评估 → 优化

两者的核心思想是一样的：**先定义"什么是正确的"，再去实现它。**
区别在于：

.. list-table:: TDD vs EDD
   :header-rows: 1
   :widths: 20 40 40

   * - 维度
     - TDD
     - EDD
   * - 验证对象
     - 代码逻辑
     - AI 输出质量
   * - 测试用例
     - 确定性断言（assertEqual）
     - 统计性评估（分数 > 阈值）
   * - 通过标准
     - 100% 通过
     - 平均分 > 阈值（如 0.8）
   * - 运行成本
     - 几乎为零
     - 需要 LLM API 调用（有成本）
   * - 运行时间
     - 秒级
     - 分钟级（取决于评估集大小）
   * - 适用场景
     - 确定性逻辑
     - 概率性 AI 输出

11.1.2 EDD 的核心循环
-----------------------

.. code-block:: text

   ┌─────────────────────────────────────────────────┐
   │              EDD 核心循环                         │
   │                                                   │
   │   ① 定义评估集          ② 建立基线               │
   │   ┌──────────┐         ┌──────────┐              │
   │   │ 收集真实  │ ──────> │ 运行评估  │              │
   │   │ 问题 +    │         │ 记录当前  │              │
   │   │ 标准答案  │         │ 系统分数  │              │
   │   └──────────┘         └──────────┘              │
   │        ▲                     │                    │
   │        │                     ▼                    │
   │   ┌──────────┐         ┌──────────┐              │
   │   │ 确认改进  │ <────── │ 修改系统  │              │
   │   │ 无退化    │         │ (Prompt/  │              │
   │   │ 合并上线  │         │  检索/    │              │
   │   │          │         │  模型)    │              │
   │   └──────────┘         └──────────┘              │
   │   ④ 回归验证           ③ 迭代优化               │
   └─────────────────────────────────────────────────┘

每一步的具体操作：

1. **定义评估集**：收集 50-200 个真实问题 + 标准答案（黄金数据集）
2. **建立基线**：用当前系统跑一轮评估，记录各维度的分数
3. **迭代优化**：修改 Prompt、检索策略、模型等
4. **回归验证**：重新跑评估，确认改进没有引入退化

.. tip::

   EDD 的关键原则：**每次修改都要跑评估。**
   就像 TDD 中每次改代码都要跑测试一样。
   没有评估的 Prompt 修改，就像没有测试的代码重构——你不知道改坏了什么。


11.2 评估数据集的构建
=======================

评估数据集（Evaluation Dataset）是 EDD 的基础。
它的质量直接决定了评估结果的可信度。

11.2.1 黄金数据集的设计
------------------------

一个好的评估数据集应该满足：

- **覆盖性**：覆盖系统的主要使用场景
- **多样性**：包含简单和复杂的问题
- **边界情况**：包含容易出错的边界场景
- **可维护性**：有版本管理，定期更新

**评估样本的结构：**

.. code-block:: python

   @dataclass
   class EvalCase:
       question: str           # 用户问题
       expected_answer: str    # 标准答案
       expected_sources: list  # 期望检索到的文档
       category: str           # 分类（简单/复杂/边界）
       tags: list              # 标签（用于筛选）

**评估集规模建议：**

.. list-table::
   :header-rows: 1
   :widths: 30 30 40

   * - 阶段
     - 评估集大小
     - 说明
   * - 原型验证
     - 20-50 个
     - 快速验证基本功能
   * - 开发迭代
     - 50-100 个
     - 覆盖主要场景
   * - 上线前
     - 100-200 个
     - 全面覆盖 + 边界情况
   * - 持续监控
     - 200+ 个
     - 定期采样 + 新增场景

11.2.2 自动生成评估数据
-------------------------

手动构建评估数据集耗时耗力。可以用 LLM 辅助生成：

.. code-block:: python

   GENERATE_EVAL_PROMPT = """基于以下文档内容，生成 5 个问答对。

   文档内容:
   {document}

   要求:
   1. 问题应该是用户可能真实提出的
   2. 答案必须完全基于文档内容
   3. 包含不同难度级别
   4. 以 JSON 格式返回

   格式:
   [
     {{"question": "...", "answer": "...",
       "difficulty": "easy|medium|hard"}}
   ]
   """

   def generate_eval_cases(documents: list) -> list:
       """用 LLM 从文档中自动生成评估数据"""
       cases = []
       for doc in documents:
           response = llm.invoke(
               GENERATE_EVAL_PROMPT.format(document=doc.text)
           )
           cases.extend(json.loads(response))
       return cases

.. warning::

   自动生成的评估数据 **必须经过人工审核**。
   LLM 生成的"标准答案"本身可能有错误。
   建议流程：LLM 生成 → 人工审核 → 纳入评估集。

11.2.3 评估数据的版本管理
---------------------------

评估数据集应该像代码一样进行版本管理：

.. code-block:: text

   eval_data/
   ├── v1.0/
   │   ├── general.json      # 通用问答 (50 个)
   │   ├── edge_cases.json   # 边界情况 (20 个)
   │   └── metadata.json     # 版本信息
   ├── v1.1/
   │   ├── general.json      # 新增 10 个
   │   ├── edge_cases.json   # 新增 5 个
   │   ├── regression.json   # 回归测试集
   │   └── metadata.json
   └── latest -> v1.1/

每次发现新的失败案例，都应该加入评估集，
确保同样的问题不会再次出现（类似于 TDD 中的"bug-driven testing"）。


11.3 评估框架实战
==================

11.3.1 框架对比
----------------

.. list-table:: 评估框架对比
   :header-rows: 1
   :widths: 20 25 25 30

   * - 框架
     - 特点
     - 适用场景
     - 集成方式
   * - Ragas
     - RAG 专用，指标全面
     - RAG 系统评估
     - Python SDK
   * - DeepEval
     - 类 pytest 语法，CI/CD 友好
     - LLM 单元测试
     - pytest 插件
   * - promptfoo
     - 多模型对比，CLI 工具
     - Prompt 优化
     - CLI + YAML 配置
   * - LangSmith
     - 全链路追踪 + 评估
     - LangChain 生态
     - SDK + Web UI

11.3.2 DeepEval 实战
----------------------

DeepEval 的语法类似 pytest，对开发者非常友好：

.. code-block:: python

   # test_rag.py — 用 DeepEval 做 RAG 评估
   from deepeval import assert_test
   from deepeval.test_case import LLMTestCase
   from deepeval.metrics import (
       FaithfulnessMetric,
       AnswerRelevancyMetric,
   )

   def test_rag_faithfulness():
       test_case = LLMTestCase(
           input="什么是度量驱动开发？",
           actual_output="度量驱动开发是一种以度量为核心...",
           retrieval_context=[
               "MDD 是指在软件开发过程中..."
           ]
       )
       metric = FaithfulnessMetric(threshold=0.8)
       assert_test(test_case, [metric])

   def test_rag_relevancy():
       test_case = LLMTestCase(
           input="如何用 Prometheus 监控微服务？",
           actual_output="可以使用 Prometheus 客户端库...",
           retrieval_context=[
               "Prometheus 是一个开源的监控系统..."
           ]
       )
       metric = AnswerRelevancyMetric(threshold=0.8)
       assert_test(test_case, [metric])

运行方式：

.. code-block:: bash

   # 像运行 pytest 一样运行评估
   deepeval test run test_rag.py

   # 输出:
   # test_rag_faithfulness PASSED (score: 0.92)
   # test_rag_relevancy PASSED (score: 0.88)
   # 2 passed in 15.3s

11.3.3 集成到 CI/CD
---------------------

将评估集成到 CI/CD 流水线，确保每次 Prompt 或检索策略的修改都经过评估：

.. code-block:: yaml

   # .github/workflows/rag-eval.yml
   name: RAG Evaluation
   on:
     pull_request:
       paths:
         - 'prompts/**'
         - 'rag/**'
         - 'knowledge_base/**'

   jobs:
     evaluate:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v4

         - name: Run evaluation
           env:
             OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
           run: |
             pip install deepeval
             deepeval test run tests/test_rag.py

         - name: Check thresholds
           run: |
             python -c "
             import json
             r = json.load(open('eval_results.json'))
             assert r['avg_faithfulness'] >= 0.80
             assert r['avg_relevancy'] >= 0.85
             print('✅ All thresholds passed')
             "

这样，任何可能影响 RAG 质量的代码变更都会自动触发评估，
防止质量退化悄悄上线。


11.4 度量金字塔：从 MDD 到 EDD
================================

将 MDD 和 EDD 结合起来，我们可以构建一个完整的 **度量金字塔**：

.. code-block:: text

   ┌─────────────────────────────────┐
   │     AI 质量评估（EDD）           │  Faithfulness、Relevancy
   │     "AI 做得对不对"              │  幻觉率、准确率
   │     Ragas / DeepEval            │  评估数据集 + CI/CD
   ├─────────────────────────────────┤
   │     应用度量（MDD）              │  延迟、吞吐量、错误率
   │     "系统跑得好不好"             │  TTFT、TPS、Token 成本
   │     Prometheus / OTel           │  实时监控 + 告警
   ├─────────────────────────────────┤
   │     基础设施度量                  │  CPU、内存、GPU 利用率
   │     "机器还活着吗"               │  磁盘、网络、向量数据库
   │     Node Exporter / cAdvisor    │  基础监控
   └─────────────────────────────────┘

**三层的关系：**

- **基础设施层** （底层）：确保系统活着。这是最基本的，没有它一切免谈。
- **应用度量层** （中层）：确保系统跑得好。延迟、错误率、成本都在可接受范围内。
- **AI 质量层** （顶层）：确保系统做得对。这是 AI 应用特有的，也是最难但最重要的。

.. tip::

   很多团队只做了底层和中层，忽略了顶层。
   结果就是：系统一切正常（绿色仪表盘），但用户不满意（投诉增加）。
   **AI 应用的度量体系，必须包含质量评估层。**


11.5 EDD 实践检查清单
=======================

**LLM 应用上线前：**

- [ ] 有评估数据集（至少 50 个样本）
- [ ] 有基线评估分数
- [ ] Token 使用量有监控（input/output 分开）
- [ ] 成本有追踪和告警（日/周/月预算）
- [ ] 有质量评估机制（定期采样 + LLM-as-Judge）

**RAG 系统上线前：**

- [ ] 检索质量有评估（Precision@K、Hit Rate）
- [ ] 生成质量有评估（Faithfulness、Relevancy）
- [ ] 评估集成到 CI/CD
- [ ] 向量数据库有监控
- [ ] 有评估数据集和回归测试

**AI Agent 上线前：**

- [ ] 任务完成率有追踪
- [ ] 工具调用有度量
- [ ] 循环检测机制已就位
- [ ] 成本有上限控制（per-task budget）
- [ ] 安全护栏有监控


.. admonition:: 💡 避坑指南：EDD 实践中的 3 个常见陷阱
   :class: warning

   **陷阱 1：评估集太小或不具代表性**

   20 个精心挑选的样本可能全部通过，但上线后用户的真实问题千奇百怪。
   解决方案：持续从生产环境中收集失败案例，加入评估集。

   **陷阱 2：只看平均分，忽略分布**

   平均 Faithfulness 0.85 看起来不错，但如果有 10% 的查询分数低于 0.5，
   那就意味着每 10 个用户中有 1 个会得到严重不准确的回答。
   解决方案：关注分数分布，设置最低分阈值。

   **陷阱 3：评估成本失控**

   每次评估都要调用 LLM API，200 个样本 × 4 个指标 = 800 次 LLM 调用。
   解决方案：用低成本模型（如 GPT-4o-mini）做日常评估，
   用高质量模型（如 Claude Sonnet）做关键节点评估。


11.6 本章小结
==============

.. note::

   **关键要点：**

   - EDD 是 AI 应用的 TDD：先定义"什么是正确的"，再去实现它
   - 评估数据集是 EDD 的基础，需要版本管理和持续更新
   - DeepEval 提供了类 pytest 的评估体验，易于集成到 CI/CD
   - 度量金字塔：基础设施度量 → 应用度量 → AI 质量评估，三层缺一不可
   - **没有评估的 Prompt 修改，就像没有测试的代码重构**


.. admonition:: 📝 思考题

   1. 你的 AI 应用目前有评估数据集吗？如果没有，你会如何开始构建？
   2. 如果你的评估分数在一次 Prompt 修改后下降了 5%，你会如何决策——上线还是回滚？
   3. EDD 和 TDD 能否结合使用？在什么场景下两者可以互补？
