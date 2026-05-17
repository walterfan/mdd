.. _appendix_b:

========================
附录 B 度量指标速查表
========================

本附录按场景分类列出常用的度量指标，包含指标名称、类型、PromQL 查询和参考阈值。
可作为日常开发和运维的快速参考。


B.1 Web API 度量
=================

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - 请求速率 (QPS)
     - Counter
     - ``rate(http_requests_total[5m])``
     - 根据容量规划
   * - 错误率
     - Counter
     - ``rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m])``
     - < 1%
   * - P50 延迟
     - Histogram
     - ``histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m]))``
     - < 100ms
   * - P99 延迟
     - Histogram
     - ``histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))``
     - < 1s
   * - 请求大小
     - Histogram
     - ``histogram_quantile(0.50, rate(http_request_size_bytes_bucket[5m]))``
     - 监控趋势
   * - 并发连接数
     - Gauge
     - ``http_connections_active``
     - < 连接池上限 80%


B.2 数据库度量
===============

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - 连接池使用率
     - Gauge
     - ``db_pool_active / db_pool_max``
     - < 80%
   * - 查询延迟
     - Histogram
     - ``histogram_quantile(0.99, rate(db_query_duration_seconds_bucket[5m]))``
     - < 100ms
   * - 慢查询数
     - Counter
     - ``rate(db_slow_queries_total[5m])``
     - 趋近于 0
   * - 事务失败率
     - Counter
     - ``rate(db_transactions_total{status="failed"}[5m])``
     - < 0.1%
   * - 复制延迟
     - Gauge
     - ``db_replication_lag_seconds``
     - < 1s


B.3 缓存度量
==============

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - 命中率
     - Counter
     - ``rate(cache_hits_total[5m]) / (rate(cache_hits_total[5m]) + rate(cache_misses_total[5m]))``
     - > 90%
   * - 延迟
     - Histogram
     - ``histogram_quantile(0.99, rate(cache_operation_duration_seconds_bucket[5m]))``
     - < 5ms
   * - 内存使用
     - Gauge
     - ``cache_memory_used_bytes / cache_memory_max_bytes``
     - < 80%
   * - 驱逐率
     - Counter
     - ``rate(cache_evictions_total[5m])``
     - 监控趋势


B.4 消息队列度量
=================

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - 消息积压
     - Gauge
     - ``mq_messages_pending``
     - < 1000（视业务而定）
   * - 消费延迟
     - Gauge
     - ``mq_consumer_lag_seconds``
     - < 30s
   * - 生产速率
     - Counter
     - ``rate(mq_messages_produced_total[5m])``
     - 监控趋势
   * - 消费速率
     - Counter
     - ``rate(mq_messages_consumed_total[5m])``
     - ≥ 生产速率
   * - 消费失败率
     - Counter
     - ``rate(mq_messages_failed_total[5m])``
     - < 0.1%


B.5 LLM 度量
==============

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - TTFT
     - Histogram
     - ``histogram_quantile(0.50, rate(llm_ttft_seconds_bucket[5m]))``
     - < 500ms
   * - TPS
     - Histogram
     - ``histogram_quantile(0.50, rate(llm_tokens_per_second_bucket[5m]))``
     - > 30 TPS
   * - Token 使用速率
     - Counter
     - ``sum(rate(llm_tokens_total[5m])) by (model, direction)``
     - 监控趋势
   * - 每小时成本
     - Counter
     - ``increase(llm_cost_dollars_total[1h])``
     - < 预算上限
   * - 错误率
     - Counter
     - ``rate(llm_requests_total{status="error"}[5m])``
     - < 1%
   * - 质量评分
     - Gauge
     - ``llm_quality_score``
     - > 3.5 (1-5 分)


B.6 RAG 度量
==============

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - 检索延迟
     - Histogram
     - ``histogram_quantile(0.99, rate(rag_retrieval_latency_seconds_bucket[5m]))``
     - < 200ms
   * - E2E 延迟
     - Histogram
     - ``histogram_quantile(0.99, rate(rag_e2e_latency_seconds_bucket[5m]))``
     - < 10s
   * - 检索命中率
     - Gauge
     - ``rag_retrieval_hit_rate``
     - > 85%
   * - Faithfulness
     - Gauge
     - ``rag_faithfulness_score``
     - > 0.80
   * - Answer Relevancy
     - Gauge
     - ``rag_answer_relevancy_score``
     - > 0.85
   * - 查询成功率
     - Counter
     - ``rate(rag_queries_total{status="success"}[5m]) / rate(rag_queries_total[5m])``
     - > 98%


B.7 AI Agent 度量
==================

.. list-table::
   :header-rows: 1
   :widths: 25 12 38 25

   * - 指标
     - 类型
     - PromQL 查询
     - 参考阈值
   * - 任务完成率
     - Counter
     - ``rate(agent_tasks_total{status="success"}[1h]) / rate(agent_tasks_total[1h])``
     - > 80%
   * - 平均步骤数
     - Histogram
     - ``histogram_quantile(0.50, rate(agent_task_steps_bucket[1h]))``
     - 越少越好
   * - 工具调用准确率
     - Counter
     - ``rate(agent_tool_calls_total{status="correct"}[1h]) / rate(agent_tool_calls_total[1h])``
     - > 90%
   * - 每任务成本
     - Histogram
     - ``histogram_quantile(0.50, rate(agent_task_cost_dollars_bucket[1h]))``
     - < 业务价值
   * - 循环检测率
     - Counter
     - ``rate(agent_loops_detected_total[1h])``
     - 趋近于 0
