.. _chapter6:

==============================
第 6 章 度量数据的分析与报警
==============================

本章讲解如何分析度量数据，揭示隐藏在图表背后所蕴含的意义，
以及如何构建报警系统。


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


6.4 本章小结
============

本章讲解了度量数据的分析方法和报警系统的构建:

- USE 方法用于分析系统资源
- RED 方法和黄金信号用于分析微服务
- 报警需要分级、及时、准确且可操作
- APDEX 是衡量用户满意度的标准指标

.. note::

   **关键要点:**

   - 分析方法: USE (资源层) + RED (服务层) + 黄金信号
   - 报警原则: 及时、准确、可操作、不疲劳
   - 使用分组、抑制、静默避免报警风暴
   - APDEX 将复杂的性能数据转化为简单的满意度评分
