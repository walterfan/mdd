.. _chapter7:

========================
第 7 章 度量驱动的运维
========================

.. admonition:: 本章你将学到

   - 部署策略（滚动、蓝绿、金丝雀）与度量的结合
   - Kubernetes 环境下的度量实践
   - 数据库、缓存、消息队列的运维度量
   - GitOps 与度量驱动的自动化运维
   - eBPF 无侵入式度量技术


开篇故事
========

   *金丝雀发布上线 5 分钟，新版本的错误率是旧版本的 3 倍。
   幸好我们的度量系统配置了自动回滚规则——
   当金丝雀实例的错误率超过基线的 2 倍时，自动回滚到旧版本。*

   *整个过程只影响了 5% 的流量，持续了不到 10 分钟。
   如果没有度量驱动的自动回滚，这个 bug 可能会影响所有用户。*

本章讲解如何通过度量来驱动高质量的运维工作，
涵盖部署升级、数据运维、配置调整和开源组件的度量。


7.1 部署升级
============

7.1.1 部署策略
--------------

.. list-table:: 部署策略对比
   :header-rows: 1
   :widths: 18 25 25 32

   * - 策略
     - 优点
     - 缺点
     - 度量要点
   * - 滚动部署
     - 零停机、逐步替换
     - 新旧版本共存
     - 各版本的错误率、延迟
   * - 蓝绿部署
     - 快速回滚
     - 需要双倍资源
     - 两套环境的健康度
   * - 金丝雀发布
     - 风险可控、渐进式
     - 流量分配复杂
     - 金丝雀 vs 基线对比
   * - A/B 测试
     - 数据驱动决策
     - 需要足够流量
     - 转化率、用户行为差异

7.1.2 金丝雀发布的度量
----------------------

.. code-block:: text

   ┌───────────────────────────────────────────┐
   │           金丝雀发布流程                     │
   │                                            │
   │  阶段1: 5% 流量 → 金丝雀版本               │
   │         ├── 监控错误率 (< 基线 × 1.1)      │
   │         ├── 监控延迟 (P99 < 基线 × 1.2)    │
   │         └── 持续 15 分钟                    │
   │                                            │
   │  阶段2: 25% 流量 → 金丝雀版本              │
   │         ├── 监控同上指标                     │
   │         └── 持续 30 分钟                    │
   │                                            │
   │  阶段3: 50% 流量 → 金丝雀版本              │
   │         ├── 监控同上指标                     │
   │         └── 持续 1 小时                     │
   │                                            │
   │  阶段4: 100% 流量 → 新版本                  │
   │                                            │
   │  任何阶段指标异常 → 自动回滚                 │
   └───────────────────────────────────────────┘

7.1.3 CI/CD 流水线度量
-----------------------

.. code-block:: text

   代码提交 → 构建 → 单元测试 → 集成测试 → 部署 → 验证
      │         │        │          │         │       │
      ▼         ▼        ▼          ▼         ▼       ▼
   提交频率  构建时间  通过率     通过率   部署频率  MTTR
             构建成功率                    部署时间

关键度量指标:

- **构建时间**: 从代码提交到构建完成的时间
- **构建成功率**: 构建成功的比例
- **测试通过率**: 自动化测试的通过率
- **部署频率**: 单位时间内的部署次数
- **部署成功率**: 部署成功的比例
- **回滚率**: 需要回滚的部署比例


7.2 数据运维
============

7.2.1 数据库运维度量
--------------------

**MySQL 关键度量:**

.. code-block:: sql

   -- 慢查询数量
   SHOW GLOBAL STATUS LIKE 'Slow_queries';

   -- 连接数
   SHOW GLOBAL STATUS LIKE 'Threads_connected';
   SHOW GLOBAL STATUS LIKE 'Max_used_connections';

   -- QPS
   SHOW GLOBAL STATUS LIKE 'Questions';

   -- InnoDB 缓冲池命中率
   SELECT
     (1 - (
       (SELECT VARIABLE_VALUE FROM performance_schema.global_status
        WHERE VARIABLE_NAME = 'Innodb_buffer_pool_reads')
       /
       (SELECT VARIABLE_VALUE FROM performance_schema.global_status
        WHERE VARIABLE_NAME = 'Innodb_buffer_pool_read_requests')
     )) * 100 AS buffer_pool_hit_rate;

7.2.2 缓存运维度量
------------------

**Redis 关键度量:**

.. code-block:: bash

   # Redis 信息
   redis-cli INFO

   # 关键指标
   # - used_memory: 内存使用量
   # - connected_clients: 连接客户端数
   # - keyspace_hits / keyspace_misses: 命中率
   # - evicted_keys: 驱逐的 key 数
   # - instantaneous_ops_per_sec: 每秒操作数

.. code-block:: python

   import redis

   def check_redis_health(host='localhost', port=6379):
       r = redis.Redis(host=host, port=port)
       info = r.info()

       # 计算缓存命中率
       hits = info['keyspace_hits']
       misses = info['keyspace_misses']
       total = hits + misses
       hit_rate = hits / total * 100 if total > 0 else 0

       return {
           'memory_used_mb': info['used_memory'] / 1024 / 1024,
           'connected_clients': info['connected_clients'],
           'hit_rate': f'{hit_rate:.2f}%',
           'ops_per_sec': info['instantaneous_ops_per_sec'],
           'evicted_keys': info['evicted_keys'],
       }


7.3 配置调整
============

7.3.1 JVM 调优
--------------

JVM 参数调优需要基于度量数据:

.. code-block:: bash

   # JVM 度量相关参数
   java -server \
     -Xms2g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+PrintGCDetails \
     -XX:+PrintGCDateStamps \
     -Xloggc:/var/log/gc.log \
     -XX:+UseGCLogFileRotation \
     -XX:NumberOfGCLogFiles=10 \
     -XX:GCLogFileSize=10M \
     -jar potato-server.jar

JVM 度量重点关注:

- **GC 频率和耗时**: Full GC 频率应尽量低
- **堆内存使用**: 各代空间的使用率
- **线程数**: 活跃线程和峰值线程数
- **类加载**: 已加载类的数量

7.3.2 线程池调优
----------------

线程池参数的配置需要基于度量:

.. code-block:: java

   // 可监控的线程池
   @Bean
   public ThreadPoolExecutor monitoredThreadPool(
           MeterRegistry registry) {
       ThreadPoolExecutor executor = new ThreadPoolExecutor(
           10,   // corePoolSize
           50,   // maximumPoolSize
           60L,  // keepAliveTime
           TimeUnit.SECONDS,
           new LinkedBlockingQueue<>(100)
       );

       // 注册度量
       Gauge.builder("threadpool.active",
           executor, ThreadPoolExecutor::getActiveCount)
           .register(registry);
       Gauge.builder("threadpool.queue.size",
           executor, e -> e.getQueue().size())
           .register(registry);
       Gauge.builder("threadpool.pool.size",
           executor, ThreadPoolExecutor::getPoolSize)
           .register(registry);

       return executor;
   }

度量指标:

- **活跃线程数 / 最大线程数**: 反映线程池利用率
- **队列深度**: 反映等待处理的任务数
- **拒绝任务数**: 反映是否需要扩容
- **任务执行时间**: 反映任务处理效率

7.3.3 连接池调优
----------------

数据库连接池 (如 HikariCP) 的度量:

.. code-block:: yaml

   # HikariCP 配置
   spring:
     datasource:
       hikari:
         minimum-idle: 5
         maximum-pool-size: 20
         idle-timeout: 30000
         max-lifetime: 1800000
         connection-timeout: 30000
         # 启用度量
         register-mbeans: true


7.4 开源组件的度量
==================

7.4.1 Kafka 度量
-----------------

.. list-table:: Kafka 关键度量
   :header-rows: 1
   :widths: 30 35 35

   * - 指标
     - 说明
     - 预警阈值
   * - Consumer Lag
     - 消费者落后的消息数
     - > 10000 告警
   * - Bytes In/Out
     - 吞吐量
     - 接近带宽上限告警
   * - Under Replicated Partitions
     - 副本不足的分区数
     - > 0 告警
   * - Request Latency
     - 请求延迟
     - P99 > 100ms 告警

7.4.2 Nginx 度量
-----------------

.. code-block:: nginx

   # Nginx 状态模块
   server {
       location /nginx_status {
           stub_status on;
           access_log off;
           allow 127.0.0.1;
           deny all;
       }
   }

关键指标:

- **Active Connections**: 当前活跃连接数
- **Requests Per Second**: 每秒请求数
- **Request Duration**: 请求处理时间
- **HTTP Status Codes**: 状态码分布


7.5 Kubernetes 环境下的运维度量 (新增)
======================================

在 Kubernetes 环境中，运维度量有了新的维度:

7.5.1 Pod 级别度量
------------------

.. code-block:: yaml

   # Prometheus 自动发现 K8s Pod
   scrape_configs:
     - job_name: 'kubernetes-pods'
       kubernetes_sd_configs:
         - role: pod
       relabel_configs:
         - source_labels:
             [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
           action: keep
           regex: true

7.5.2 集群级别度量
------------------

- **节点资源使用**: CPU、内存、磁盘使用率
- **Pod 调度**: 调度延迟、Pending Pod 数
- **服务发现**: Endpoint 变更频率
- **网络**: Pod 间通信延迟、DNS 解析延迟

7.5.3 HPA 自动伸缩
-------------------

基于度量的自动伸缩:

.. code-block:: yaml

   apiVersion: autoscaling/v2
   kind: HorizontalPodAutoscaler
   metadata:
     name: potato-server-hpa
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: potato-server
     minReplicas: 2
     maxReplicas: 10
     metrics:
       - type: Resource
         resource:
           name: cpu
           target:
             type: Utilization
             averageUtilization: 70
       - type: Pods
         pods:
           metric:
             name: http_requests_per_second
           target:
             type: AverageValue
             averageValue: "1000"


7.6 eBPF: 无侵入式度量采集 (新增)
====================================

eBPF (Extended Berkeley Packet Filter) 允许在 Linux 内核中运行沙盒程序，
实现 **零代码修改** 的度量采集:

.. code-block:: text

   ┌─────────────────────────────┐
   │        用户空间               │
   │  ┌──────────────────────┐   │
   │  │ 应用程序 (无需修改)    │   │
   │  └──────────────────────┘   │
   ├─────────────────────────────┤
   │        内核空间               │
   │  ┌──────────────────────┐   │
   │  │   eBPF 程序           │   │
   │  │ - 网络流量度量         │   │
   │  │ - 系统调用追踪         │   │
   │  │ - 函数延迟测量         │   │
   │  └──────────────────────┘   │
   └─────────────────────────────┘

常用的 eBPF 可观测性工具:

- **Cilium Hubble**: Kubernetes 网络可观测性
- **Pixie**: 自动化应用性能监控
- **bpftrace**: 高级追踪工具

.. tip::

   eBPF 的优势在于 **无需修改应用代码** 即可采集度量数据，
   这对于 AI 生成的代码特别有价值 —— 即使 AI 忘了加埋点，
   eBPF 仍然能从内核层面捕获关键性能指标。


7.7 GitOps 与度量驱动部署 (新增)
=================================

GitOps 将 Git 作为基础设施和应用配置的唯一真实来源:

.. code-block:: text

   ┌──────────┐    ┌──────────┐    ┌──────────────┐
   │  Git     │───>│ Argo CD  │───>│ Kubernetes   │
   │  Repo    │    │ / Flux   │    │  Cluster     │
   └──────────┘    └────┬─────┘    └──────┬───────┘
                        │                  │
                        │      度量反馈     │
                        │  ◄───────────── │
                        │                  │
                  ┌─────▼─────┐           │
                  │ 自动回滚   │◄──────────┘
                  │ (指标异常) │  Prometheus
                  └───────────┘  告警触发

GitOps + 度量 = 自动化、可审计、可回滚的部署流程。


7.8 AI 应用的运维度量
======================

AI 应用的运维与传统微服务有几个独特的挑战：

7.8.1 模型版本管理与灰度
--------------------------

LLM 应用的"版本"不仅是代码版本，还包括模型版本和 Prompt 版本：

.. code-block:: text

   传统服务灰度:  代码 v1.2.3 → v1.2.4
   AI 应用灰度:   代码 v1.2.3 + 模型 gpt-4o-2024-08-06 + Prompt v7
                  → 代码 v1.2.3 + 模型 gpt-4o-2025-01-06 + Prompt v8

**模型切换的度量对比：**

.. code-block:: yaml

   # 模型 A/B 测试度量
   # 对比两个模型版本的质量和成本
   - 延迟对比:
       model_a: histogram_quantile(0.99, rate(llm_duration{model="gpt-4o-2024"}[1h]))
       model_b: histogram_quantile(0.99, rate(llm_duration{model="gpt-4o-2025"}[1h]))
   - 成本对比:
       model_a: rate(llm_cost_dollars{model="gpt-4o-2024"}[1h])
       model_b: rate(llm_cost_dollars{model="gpt-4o-2025"}[1h])
   - 质量对比:
       model_a: avg(llm_quality_score{model="gpt-4o-2024"})
       model_b: avg(llm_quality_score{model="gpt-4o-2025"})

7.8.2 向量数据库运维
---------------------

RAG 应用依赖向量数据库，其运维度量包括：

.. list-table:: 向量数据库运维度量
   :header-rows: 1
   :widths: 25 35 40

   * - 度量
     - 指标
     - 告警条件
   * - 索引大小
     - ``vectordb_index_size_bytes``
     - 增长率异常（数据泄漏/重复写入）
   * - 查询延迟
     - ``vectordb_query_duration_seconds``
     - P99 > 500ms
   * - 索引构建
     - ``vectordb_index_build_duration``
     - 构建时间超过 SLA
   * - 召回率
     - ``vectordb_recall_at_k``
     - 低于基线（索引质量退化）

7.8.3 Prompt 版本管理
-----------------------

Prompt 是 AI 应用的核心"配置"，需要像代码一样管理：

.. code-block:: text

   Prompt 变更流程（度量驱动）:

   1. 修改 Prompt → 提交到版本控制
   2. 运行评估数据集 → 对比基线分数
   3. 分数提升 → 灰度发布（10% 流量）
   4. 监控灰度度量 → 质量/延迟/成本对比
   5. 确认无退化 → 全量发布
   6. 持续监控 → 检测长期退化


7.9 本章小结
============

本章介绍了度量驱动运维的方方面面:

- 部署策略 (滚动、蓝绿、金丝雀) 及其度量要点
- CI/CD 流水线的度量
- 数据库、缓存的运维度量
- 配置调优 (JVM、线程池、连接池) 的度量依据
- 开源组件 (Kafka、Nginx) 的度量
- Kubernetes 环境下的运维度量和 HPA
- eBPF 无侵入式度量采集
- GitOps 与度量驱动的自动化部署
- AI 应用的模型灰度、向量数据库运维、Prompt 版本管理

.. note::

   **关键要点:**

   - 金丝雀发布是度量驱动部署的最佳实践
   - 配置调优必须基于度量数据，而非经验猜测
   - Kubernetes HPA 实现了基于度量的自动伸缩
   - AI 应用的"版本"包括代码 + 模型 + Prompt 三个维度
   - eBPF 提供了零侵入的度量采集能力
   - GitOps + 度量 = 自动化可回滚的部署


.. admonition:: 💡 避坑指南：金丝雀发布的 3 个关键点
   :class: warning

   **关键点 1：选对比较指标**

   不要只比较错误率。延迟 P99、内存使用趋势、业务指标 (如转化率) 都应该纳入比较。
   一个版本可能错误率不变，但内存泄漏导致 12 小时后 OOM。

   **关键点 2：流量比例要够**

   金丝雀流量太少 (如 1%)，统计上不显著，可能漏掉问题。
   建议至少 5-10% 的流量，持续观察 15-30 分钟。

   **关键点 3：自动回滚要快**

   检测到异常后，回滚应该在 1 分钟内完成。
   手动回滚太慢——等你打开电脑、登录系统、确认问题，用户已经受影响了。


.. admonition:: 📝 思考题

   1. 你的团队使用什么部署策略？是否有基于度量的自动回滚机制？
   2. 如果你的 AI 应用需要更换 LLM 模型 (如从 GPT-4o 迁移到 Claude Sonnet)，你会如何做灰度发布？
   3. eBPF 度量和应用内埋点各有什么优缺点？在什么场景下应该选择哪种？
