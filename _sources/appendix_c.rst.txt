.. _appendix_c:

==========================================
附录 C 告警规则与 Grafana 面板模板
==========================================

本附录提供可直接复制使用的 Prometheus 告警规则和 Grafana 面板设计参考。
完整的 JSON 面板文件请访问 GitHub 仓库：https://github.com/walterfan/mdd


C.1 Prometheus 告警规则
========================

C.1.1 基础设施告警
--------------------

.. code-block:: yaml

   groups:
     - name: infrastructure-alerts
       rules:
         # 服务宕机
         - alert: ServiceDown
           expr: up == 0
           for: 1m
           labels:
             severity: critical
           annotations:
             summary: "服务 {{ $labels.instance }} 已宕机"

         # CPU 使用率过高
         - alert: HighCPUUsage
           expr: |
             100 - (avg by(instance)
               (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 85
           for: 10m
           labels:
             severity: warning
           annotations:
             summary: "CPU 使用率 > 85%: {{ $value }}%"

         # 内存使用率过高
         - alert: HighMemoryUsage
           expr: |
             (1 - node_memory_MemAvailable_bytes
               / node_memory_MemTotal_bytes) * 100 > 90
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "内存使用率 > 90%: {{ $value }}%"

         # 磁盘空间不足
         - alert: DiskSpaceLow
           expr: |
             (1 - node_filesystem_avail_bytes
               / node_filesystem_size_bytes) * 100 > 85
           for: 10m
           labels:
             severity: warning
           annotations:
             summary: "磁盘使用率 > 85%: {{ $value }}%"

C.1.2 应用层告警
------------------

.. code-block:: yaml

   groups:
     - name: application-alerts
       rules:
         # 错误率升高
         - alert: HighErrorRate
           expr: |
             sum(rate(http_requests_total{status=~"5.."}[5m]))
             / sum(rate(http_requests_total[5m])) > 0.05
           for: 5m
           labels:
             severity: critical
           annotations:
             summary: "HTTP 5xx 错误率 > 5%: {{ $value | humanizePercentage }}"

         # P99 延迟过高
         - alert: HighLatency
           expr: |
             histogram_quantile(0.99,
               rate(http_request_duration_seconds_bucket[5m])) > 2
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "P99 延迟 > 2s: {{ $value }}s"

         # 连接池耗尽
         - alert: ConnectionPoolExhausted
           expr: |
             db_pool_active / db_pool_max > 0.9
           for: 5m
           labels:
             severity: critical
           annotations:
             summary: "数据库连接池使用率 > 90%: {{ $value | humanizePercentage }}"

C.1.3 AI 应用告警
-------------------

.. code-block:: yaml

   groups:
     - name: ai-application-alerts
       rules:
         # LLM 成本激增
         - alert: LLMCostSpike
           expr: increase(llm_cost_dollars_total[1h]) > 10
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "LLM 成本激增: 过去 1 小时 ${{ $value }}"

         # LLM 日成本超限
         - alert: LLMDailyCostExceeded
           expr: increase(llm_cost_dollars_total[24h]) > 100
           for: 10m
           labels:
             severity: critical
           annotations:
             summary: "LLM 日成本超限: ${{ $value }}/天"

         # RAG 错误率升高
         - alert: RAGHighErrorRate
           expr: |
             sum(rate(rag_queries_total{status="error"}[5m]))
             / sum(rate(rag_queries_total[5m])) > 0.05
           for: 5m
           labels:
             severity: critical
           annotations:
             summary: "RAG 错误率 > 5%"

         # RAG 延迟过高
         - alert: RAGHighLatency
           expr: |
             histogram_quantile(0.99,
               rate(rag_e2e_latency_seconds_bucket[5m])) > 10
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "RAG P99 延迟 > 10s: {{ $value }}s"

         # RAG 质量下降
         - alert: RAGLowFaithfulness
           expr: rag_faithfulness_score < 0.7
           for: 15m
           labels:
             severity: warning
           annotations:
             summary: "RAG 忠实度下降: {{ $value }}"

         # Agent 循环检测
         - alert: AgentLoopDetected
           expr: increase(agent_loops_detected_total[1h]) > 0
           for: 1m
           labels:
             severity: warning
           annotations:
             summary: "检测到 Agent 循环: {{ $value }} 次/小时"


C.2 告警通知模板
=================

C.2.1 飞书/Lark 通知模板
--------------------------

.. code-block:: json

   {
     "msg_type": "interactive",
     "card": {
       "header": {
         "title": {
           "tag": "plain_text",
           "content": "🚨 {{ .CommonLabels.alertname }}"
         },
         "template": "red"
       },
       "elements": [
         {
           "tag": "div",
           "text": {
             "tag": "lark_md",
             "content": "**严重级别**: {{ .CommonLabels.severity }}\n**摘要**: {{ .CommonAnnotations.summary }}\n**时间**: {{ .StartsAt }}"
           }
         }
       ]
     }
   }

C.2.2 Slack 通知模板
----------------------

.. code-block:: json

   {
     "channel": "#alerts",
     "username": "Prometheus",
     "icon_emoji": ":rotating_light:",
     "attachments": [
       {
         "color": "{{ if eq .Status \"firing\" }}danger{{ else }}good{{ end }}",
         "title": "{{ .CommonLabels.alertname }}",
         "text": "{{ .CommonAnnotations.summary }}",
         "fields": [
           {"title": "Severity", "value": "{{ .CommonLabels.severity }}", "short": true},
           {"title": "Status", "value": "{{ .Status }}", "short": true}
         ]
       }
     ]
   }


C.3 Grafana 面板设计参考
==========================

C.3.1 服务概览面板
--------------------

.. code-block:: text

   ┌─────────────────────────────────────────────────────────┐
   │              服务概览面板                                  │
   ├──────────┬──────────┬──────────┬────────────────────────┤
   │ 总请求量  │ 成功率    │ P99 延迟  │ 活跃连接数             │
   │ Stat     │ Stat     │ Stat     │ Stat                  │
   ├──────────┴──────────┴──────────┴────────────────────────┤
   │  ┌────────────────────────────────────────────────────┐  │
   │  │  请求速率 + 错误率 (双轴折线图)                      │  │
   │  │  左轴: rate(http_requests_total[5m])                │  │
   │  │  右轴: error_rate                                   │  │
   │  └────────────────────────────────────────────────────┘  │
   │  ┌──────────────────────┐  ┌──────────────────────────┐  │
   │  │ 延迟分布 (热力图)     │  │ 状态码分布 (饼图)         │  │
   │  │ P50 / P90 / P99      │  │ 2xx / 4xx / 5xx          │  │
   │  └──────────────────────┘  └──────────────────────────┘  │
   └─────────────────────────────────────────────────────────┘

C.3.2 AI 应用面板
-------------------

.. code-block:: text

   ┌─────────────────────────────────────────────────────────┐
   │              AI 应用监控面板                               │
   ├──────────┬──────────┬──────────┬────────────────────────┤
   │ 查询总量  │ 成功率    │ E2E P99  │ 今日 LLM 成本          │
   ├──────────┴──────────┴──────────┴────────────────────────┤
   │  ┌────────────────────────────────────────────────────┐  │
   │  │  LLM 成本趋势 (面积图，按模型分组)                   │  │
   │  └────────────────────────────────────────────────────┘  │
   │  ┌──────────────────────┐  ┌──────────────────────────┐  │
   │  │ Token 使用趋势        │  │ 延迟分布                  │  │
   │  │ Input vs Output      │  │ 检索 vs 生成              │  │
   │  └──────────────────────┘  └──────────────────────────┘  │
   │  ┌──────────────────────┐  ┌──────────────────────────┐  │
   │  │ 质量评分趋势          │  │ 检索命中率               │  │
   │  │ Faithfulness          │  │ Hit Rate / Precision@5   │  │
   │  │ Relevancy             │  │                          │  │
   │  └──────────────────────┘  └──────────────────────────┘  │
   └─────────────────────────────────────────────────────────┘

.. note::

   完整的 Grafana 面板 JSON 文件可从 GitHub 仓库下载：

   - ``grafana/service-overview.json`` — 服务概览面板
   - ``grafana/ai-application.json`` — AI 应用面板
   - ``grafana/rag-monitoring.json`` — RAG 系统面板

   导入方式：Grafana → Dashboards → Import → Upload JSON file
