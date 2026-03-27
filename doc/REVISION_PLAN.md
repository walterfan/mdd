# 《度量驱动开发》修订计划

## 修订状态：✅ 全部完成

## 修订统计

| 项目 | 修订前 | 修订后 | 变化 |
|------|--------|--------|------|
| 总文件数 | 12 | 13 | +1 (chapter9) |
| 总字节数 | ~131KB | ~206KB | +75KB (+57%) |
| 总字符数 | ~95K | ~145K | +50K |
| 章节数 | 8 | 9 | +1 |
| 术语表条目 | ~30 | ~60 | +30 |
| 附录工具 | ~25 | ~50 | +25 |

## 修订内容清单

### 1. ✅ 新增：第 9 章 AI 应用的度量体系（45KB，核心新增）

- 9.1 LLM 应用度量（性能/成本/质量）
- 9.2 RAG 系统度量（检索质量/生成质量/向量数据库）
- 9.3 AI Agent 度量（任务完成率/工具调用/循环检测/多Agent）
- 9.4 AI 应用的可观测性实践（LangSmith/LangFuse/OTel GenAI/EDD）
- 9.5 实战: RAG 知识库的度量仪表盘（完整代码+Grafana+告警）
- 9.6 AI 应用度量的最佳实践（度量金字塔+检查清单）
- 9.7 本章小结

### 2. ✅ 增补：现有章节

#### 第 1 章：新增 1.6 AI 时代的服务架构演进
- 传统微服务 vs AI 应用服务对比表
- AI 应用典型架构图
- 度量在 AI 应用中的重要性

#### 第 2 章：新增 2.3.3 AI 应用度量层次
- 在"按度量层次划分"中增加 AI/ML 层

#### 第 3 章：新增 3.5 LLM API 协议及其度量
- LLM API 协议概述（Streaming vs Non-streaming）
- LLM API 度量要点（Token/延迟/成本/错误码）

#### 第 4 章：新增 4.6.4 AI 应用的度量代码模式
- LLM 调用包装器（带度量的 Python 示例）

#### 第 5 章：新增 5.5.1 AI 应用的度量聚合特殊考量
- 高基数标签问题
- 评估数据的异步采集
- 成本度量的多维聚合

#### 第 6 章：新增 6.4 AI 应用的分析与告警策略
- AI 应用 USED 告警矩阵
- 成本告警（LLM 特有需求）
- 质量告警（从"能用"到"好用"）

#### 第 7 章：新增 7.8 AI 应用的运维度量
- 模型版本管理与灰度
- 向量数据库运维
- Prompt 版本管理

#### 第 8 章：
- 新增 8.6.3 AI Agent 自主运维（度量框架+安全护栏+代码示例）
- 更新 8.7.2 未来趋势（加入 OTel GenAI/EDD/FinOps for AI）
- 新增 8.8 AI 应用额外检查项

### 3. ✅ 更新：术语表
- 新增 ~30 个 AI 相关术语：AI Agent, Chunking, Context Window, DeepEval,
  Embedding, Eval-Driven Development, Faithfulness, Function Calling,
  Guardrail, Hallucination, LangFuse, LangSmith, LLM, LLM-as-Judge,
  MRR, NDCG, Precision@K, Prompt Engineering, RAG, Ragas, Recall@K,
  ReAct, Token, Tool Calling, TTFT, TPS (LLM), Vector Database, E2E Latency

### 4. ✅ 更新：附录
- 新增 A.3 AI 应用度量工具
  - A.3.1 LLM 可观测性平台（LangSmith/LangFuse/Phoenix/Helicone/W&B/Braintrust）
  - A.3.2 RAG/LLM 评估框架（Ragas/DeepEval/TruLens/promptfoo/ARES）
  - A.3.3 AI Agent 度量工具（AgentOps/Smith/OTel GenAI）
  - A.3.4 向量数据库监控（ChromaDB/Pinecone/Milvus/Weaviate/Qdrant）
- 原 A.3 度量类库 → A.4 度量类库

### 5. ✅ 更新：index.rst
- 加入 chapter9

### 6. ✅ 更新：前言
- 本书主线加入第 9 章描述
- 修订要点加入"AI 应用度量"

### 7. ✅ 构建验证
- `make html` 成功，0 warnings
- 所有 13 个 RST 文件正确处理
