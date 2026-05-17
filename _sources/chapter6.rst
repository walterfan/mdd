.. _chapter6:

==============================
第 6 章 度量数据的分析与报警
==============================

.. admonition:: 本章你将学到

   - 度量数据的分析方法：趋势分析、对比分析、关联分析、异常检测
   - USE/RED/黄金信号方法论的实际应用
   - 告警策略设计：分级、聚合、抑制
   - APDEX 用户满意度评分
   - 如何避免告警疲劳


开篇故事
========

   *一个告警规则配错了阈值——把 CPU 告警从 90% 改成了 70%。
   结果团队一晚上收到了 200 条告警通知。
   第二天早上，所有人都把告警通知静音了。*

   *一周后，一个真正的 P0 故障发生了。告警正常触发了，
   但没有人看到——因为所有人都已经对告警麻木了。*

   *这就是"告警疲劳"——它比没有告警更危险。*

本章讲解如何分析度量数据，揭示隐藏在图表背后所蕴含的意义，
以及如何构建一个有效的（而非令人疲劳的）报警系统。


6.1 度量数据的分析
==================

6.1.1 分析方法
--------------

.. list-table:: 度量分析方法
   :header-rows: 1
   :widths: 20 30 50

   * - 方法
     - 说明
     - 应用场景
   * - 趋势分析
     - 观察指标随时间的变化趋势
     - 容量规划、性能退化检测
   * - 对比分析
     - 与基线、历史或其他维度对比
     - 版本发布影响评估
   * - 关联分析
     - 分析多个指标之间的关联关系
     - 根因分析
   * - 异常检测
     - 自动识别异常的数据模式
     - 故障预警
   * - TopN 分析
     - 找出排名前 N 的项目
     - 性能瓶颈定位

6.1.2 USE 方法
--------------

Brendan Gregg 提出的 USE 方法，适用于分析系统资源:

- **U (Utilization)**: 资源的使用率 (如 CPU 使用率 90%)
- **S (Saturation)**: 资源的饱和度 (如队列长度 > 100)
- **E (Errors)**: 错误事件 (如磁盘 I/O 错误)

对每种系统资源 (CPU、内存、磁盘、网络) 逐一检查 USE 指标。

6.1.3 RED 方法
--------------

Tom Wilkie 提出的 RED 方法，适用于分析微服务:

- **R (Rate)**: 请求速率 (每秒请求数)
- **E (Errors)**: 错误率 (失败请求的比例)
- **D (Duration)**: 请求持续时间 (延迟)

.. code-block:: text

   系统资源 → USE 方法 (Utilization, Saturation, Errors)
   微服务   → RED 方法 (Rate, Errors, Duration)
   业务指标 → 自定义 (DAU, 转化率, 收入等)

6.1.4 黄金信号 (Golden Signals)
--------------------------------

Google SRE 提出的四大黄金信号:

1. **延迟 (Latency)**: 服务请求所需时间
2. **流量 (Traffic)**: 系统的需求量
3. **错误 (Errors)**: 请求失败的速率
4. **饱和度 (Saturation)**: 系统的负载程度

.. tip::

   在实践中，建议结合使用:

   - 基础设施层: USE 方法
   - 应用服务层: RED 方法 / 黄金信号
   - 业务层: 自定义业务指标


6.2 报警实现
============

6.2.1 报警策略
--------------

**报警级别:**

.. list-table:: 报警级别
   :header-rows: 1
   :widths: 15 20 30 35

   * - 级别
     - 颜色
     - 含义
     - 响应要求
   * - P0
     - 红色
     - 严重故障，服务不可用
     - 立即响应，所有相关人员
   * - P1
     - 橙色
     - 重大问题，部分功能受影响
     - 15 分钟内响应
   * - P2
     - 黄色
     - 一般问题，性能下降
     - 1 小时内响应
   * - P3
     - 蓝色
     - 轻微问题，需关注
     - 下一个工作日处理
   * - Info
     - 绿色
     - 信息通知
     - 了解即可

**报警原则:**

- **及时性**: 故障发生后尽快报警
- **准确性**: 减少误报和漏报
- **可操作性**: 报警信息应包含足够的上下文，便于快速定位和处理
- **不疲劳**: 避免报警风暴，使用抑制和聚合策略

6.2.2 报警规则
--------------

.. code-block:: yaml

   # Prometheus 报警规则
   groups:
     - name: potato-service-alerts
       rules:
         # 高错误率报警
         - alert: HighErrorRate
           expr: |
             sum(rate(http_requests_total{status=~"5.."}[5m]))
             /
             sum(rate(http_requests_total[5m]))
             > 0.05
           for: 5m
           labels:
             severity: critical
           annotations:
             summary: "高错误率: {{ $value | humanizePercentage }}"
             description: "服务 {{ $labels.job }} 的 5xx 错误率超过 5%"

         # 高延迟报警
         - alert: HighLatency
           expr: |
             histogram_quantile(0.99,
               rate(http_request_duration_seconds_bucket[5m])
             ) > 2
           for: 5m
           labels:
             severity: warning
           annotations:
             summary: "P99 延迟过高: {{ $value }}s"

         # 服务不可用
         - alert: ServiceDown
           expr: up == 0
           for: 1m
           labels:
             severity: critical
           annotations:
             summary: "服务不可用: {{ $labels.instance }}"

6.2.3 报警通知渠道
------------------

.. code-block:: yaml

   # Alertmanager 配置
   global:
     resolve_timeout: 5m

   route:
     group_by: ['alertname', 'cluster']
     group_wait: 10s
     group_interval: 10s
     repeat_interval: 1h
     receiver: 'default'
     routes:
       - match:
           severity: critical
         receiver: 'pagerduty'
       - match:
           severity: warning
         receiver: 'slack'

   receivers:
     - name: 'default'
       email_configs:
         - to: 'team@example.com'

     - name: 'slack'
       slack_configs:
         - api_url: 'https://hooks.slack.com/services/...'
           channel: '#alerts'

     - name: 'pagerduty'
       pagerduty_configs:
         - service_key: '<key>'

6.2.4 报警抑制与聚合
--------------------

避免报警风暴的策略:

- **分组 (Grouping)**: 将相似的报警合并为一条通知
- **抑制 (Inhibition)**: 高级别报警抑制低级别报警
- **静默 (Silencing)**: 在维护窗口期间静默报警
- **去重 (Deduplication)**: 避免重复发送相同报警


6.3 多语言报警实现
==================

6.3.1 Go: 自定义健康检查与告警
-------------------------------

.. code-block:: go

   // Go 实现轻量级度量检查器
   package alerter

   import (
       "fmt"
       "net/http"
       "encoding/json"
       "time"
   )

   type MetricsChecker struct {
       prometheusURL string
       alertWebhook  string
   }

   type AlertLevel string
   const (
       AlertInfo     AlertLevel = "info"
       AlertWarning  AlertLevel = "warning"
       AlertCritical AlertLevel = "critical"
   )

   func (c *MetricsChecker) CheckErrorRate(threshold float64) error {
       query := `sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m]))`
       result, err := c.queryPrometheus(query)
       if err != nil {
           return err
       }
       if result > threshold {
           return c.sendAlert(AlertCritical,
               fmt.Sprintf("Error rate %.2f%% exceeds threshold %.2f%%",
                   result*100, threshold*100))
       }
       return nil
   }

   func (c *MetricsChecker) CheckP99Latency(thresholdSeconds float64) error {
       query := `histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))`
       result, err := c.queryPrometheus(query)
       if err != nil {
           return err
       }
       if result > thresholdSeconds {
           return c.sendAlert(AlertWarning,
               fmt.Sprintf("P99 latency %.2fs exceeds threshold %.2fs",
                   result, thresholdSeconds))
       }
       return nil
   }

6.3.2 Python: 基于 Elasticsearch 的报警
-----------------------------------------

.. code-block:: python

   from elasticsearch import Elasticsearch
   from datetime import datetime, timedelta
   import httpx

   class MetricsChecker:
       def __init__(self, es_host='localhost:9200'):
           self.es = Elasticsearch([es_host])

       def check_error_rate(self, index: str, threshold: float = 0.05):
           """检查错误率是否超过阈值"""
           now = datetime.utcnow()
           query = {
               "size": 0,
               "query": {"range": {"@timestamp": {"gte": "now-5m"}}},
               "aggs": {
                   "total": {"value_count": {"field": "status"}},
                   "errors": {"filter": {"range": {"status": {"gte": 500}}}}
               }
           }
           result = self.es.search(index=index, body=query)
           total = result['aggregations']['total']['value']
           errors = result['aggregations']['errors']['doc_count']

           if total > 0 and (errors / total) > threshold:
               self.send_alert(
                   level="critical",
                   message=f"Error rate {errors/total:.2%} > {threshold:.2%}"
               )

       def send_alert(self, level: str, message: str):
           """发送报警到 webhook (Slack/飞书/钉钉)"""
           httpx.post(self.webhook_url, json={
               "level": level,
               "message": message,
               "timestamp": datetime.utcnow().isoformat(),
               "service": "potato-server"
           })

6.3.3 C++: 嵌入式系统的度量报警
---------------------------------

.. code-block:: cpp

   // C++ 中对性能敏感的度量检查
   #include <chrono>
   #include <functional>

   class LatencyGuard {
   public:
       using AlertCallback = std::function<void(double, double)>;

       LatencyGuard(double threshold_ms, AlertCallback on_exceed)
           : threshold_ms_(threshold_ms)
           , on_exceed_(std::move(on_exceed))
           , start_(std::chrono::steady_clock::now()) {}

       ~LatencyGuard() {
           auto elapsed = std::chrono::steady_clock::now() - start_;
           double ms = std::chrono::duration<double, std::milli>(elapsed).count();
           if (ms > threshold_ms_) {
               on_exceed_(ms, threshold_ms_);
           }
       }

   private:
       double threshold_ms_;
       AlertCallback on_exceed_;
       std::chrono::steady_clock::time_point start_;
   };

   // 使用: 超过 100ms 自动报警
   void handle_request() {
       LatencyGuard guard(100.0, [](double actual, double threshold) {
           spdlog::warn("Request took {:.1f}ms (threshold: {:.1f}ms)",
                        actual, threshold);
           alert_counter.Increment();
       });
       // ... 处理请求 ...
   }

6.3.2 APDEX 应用性能指数
--------------------------

APDEX (Application Performance Index) 是一个衡量用户满意度的标准:

.. code-block:: text

   APDEX = (满意数 + 容忍数 × 0.5) / 总数

   其中:
   - 满意: 响应时间 ≤ T (如 T = 500ms)
   - 容忍: T < 响应时间 ≤ 4T
   - 失望: 响应时间 > 4T

   APDEX 值:
   - 0.94-1.00: 优秀
   - 0.85-0.93: 良好
   - 0.70-0.84: 一般
   - 0.50-0.69: 差
   - < 0.50: 不可接受

.. code-block:: python

   def calculate_apdex(response_times, threshold_t=0.5):
       """计算 APDEX 分数"""
       satisfied = sum(1 for t in response_times if t <= threshold_t)
       tolerating = sum(1 for t in response_times
                        if threshold_t < t <= 4 * threshold_t)
       total = len(response_times)

       if total == 0:
           return 1.0
       return (satisfied + tolerating * 0.5) / total


6.4 AI 应用的分析与告警策略
============================

AI 应用（LLM/RAG/Agent）的告警策略与传统微服务有显著差异，
因为 AI 应用的"错误"不仅仅是 HTTP 5xx，还包括质量退化、成本失控等。

6.4.1 AI 应用的 USED 告警矩阵
-------------------------------

.. list-table:: AI 应用 USED 告警
   :header-rows: 1
   :widths: 15 25 30 30

   * - 维度
     - 指标
     - 告警条件
     - 说明
   * - Usage
     - Token 使用量
     - 日消耗 > 预算 80%
     - 成本控制
   * - Usage
     - API 调用次数
     - 接近速率限制 (> 80% rate limit)
     - 防止被限流
   * - Saturation
     - 请求队列深度
     - 队列积压 > 100
     - LLM API 处理不过来
   * - Error
     - LLM API 错误率
     - 429/529 错误率 > 5%
     - 被限流或服务过载
   * - Error
     - 质量评分下降
     - Faithfulness < 0.7
     - 生成质量退化
   * - Delay
     - TTFT (首 Token 时间)
     - P99 > 5s
     - 用户等待过久
   * - Delay
     - E2E 延迟
     - P99 > 30s
     - 端到端体验差

6.4.2 成本告警：AI 应用的特有需求
-----------------------------------

传统微服务很少需要"成本告警"，但 LLM 应用的成本可能在一夜之间失控：

.. code-block:: yaml

   # LLM 成本告警规则
   groups:
     - name: llm-cost-alerts
       rules:
         # 单次请求成本异常
         - alert: LLMHighCostPerRequest
           expr: |
             rate(llm_cost_dollars_total[5m])
             / rate(llm_requests_total[5m]) > 0.10
           for: 5m
           annotations:
             summary: "单次 LLM 请求平均成本 > $0.10"

         # 日成本超预算
         - alert: LLMDailyBudgetExceeded
           expr: |
             increase(llm_cost_dollars_total[24h]) > 100
           annotations:
             summary: "LLM 日成本超过 $100 预算"

         # Token 使用量异常飙升
         - alert: LLMTokenSpike
           expr: |
             rate(llm_token_usage_total[5m])
             > 2 * rate(llm_token_usage_total[1h] offset 1d)
           for: 10m
           annotations:
             summary: "LLM Token 使用量是昨天同期的 2 倍以上"

6.4.3 质量告警：从"能用"到"好用"
-----------------------------------

AI 应用的质量退化往往是渐进的，不像传统服务那样突然崩溃：

.. code-block:: text

   传统服务: 正常 → 错误率飙升 → 告警 → 修复
   AI 应用:  正常 → 质量缓慢下降 → 用户投诉增多 → 才发现问题

因此需要持续的质量监控：

- **定期采样评估**: 每小时随机抽取 N 个请求，用 LLM-as-Judge 评估
- **用户反馈关联**: 将用户的 👍👎 反馈与度量数据关联
- **基线对比**: 与历史质量基线对比，检测退化趋势


6.5 本章小结
============

本章讲解了度量数据的分析方法和报警系统的构建:

- USE 方法用于分析系统资源
- RED 方法和黄金信号用于分析微服务
- 报警需要分级、及时、准确且可操作
- APDEX 是衡量用户满意度的标准指标
- AI 应用需要额外的成本告警和质量告警

.. note::

   **关键要点:**

   - 分析方法: USE (资源层) + RED (服务层) + 黄金信号
   - 报警原则: 及时、准确、可操作、不疲劳
   - 使用分组、抑制、静默避免报警风暴
   - APDEX 将复杂的性能数据转化为简单的满意度评分
   - AI 应用的告警维度: 成本 + 质量 + 传统 USED


.. admonition:: 💡 避坑指南：告警系统的 4 个致命错误
   :class: warning

   **错误 1：告警没有 Runbook**

   告警触发了，然后呢？如果 on-call 工程师不知道该怎么处理，告警就是噪音。
   每条告警规则都应该附带一个 Runbook 链接，说明排查步骤和处理方法。

   **错误 2：阈值拍脑袋**

   "CPU > 80% 就告警"——这个 80% 是怎么来的？应该基于历史数据和 SLO 来设定阈值，
   而不是凭感觉。

   **错误 3：只告警不恢复**

   告警触发了通知，但问题自动恢复后没有"恢复通知"。
   结果 on-call 工程师半夜爬起来排查一个已经自愈的问题。

   **错误 4：所有告警同一优先级**

   如果所有告警都是 P0，那就等于没有 P0。
   严格区分 P0 (立即响应)、P1 (1 小时内)、P2 (下个工作日)。


.. admonition:: 📝 思考题

   1. 你的团队是否经历过"告警疲劳"？你会如何改善？
   2. 用 APDEX 公式计算你最重要的 API 的用户满意度。T 值应该设为多少？
   3. 如果你的 AI 应用质量缓慢下降 (每周降 2%)，你的告警系统能检测到吗？
