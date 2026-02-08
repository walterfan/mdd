.. Metrics Driven Development documentation master file

=============================================
度量驱动开发 Metrics Driven Development
=============================================

   *If you can't measure it, you can't manage it.*

   *If you can't measure it, you can't prove it.*

   *If you can't measure it, you can't improve it.*

   -- Walter Fan

.. note::

   **AI 时代的度量驱动开发**

   AI 能让你写代码快 10 倍，但如果没有度量护法加持，你可能在以 10 倍速度制造技术债。
   度量驱动开发 (MDD) 是 AI 辅助编程时代的 **可观测性护法** ——
   确保代码不仅能跑，而且跑得好、跑得稳。


概述
=====

度量驱动开发 (Metrics Driven Development, MDD) 是一种以度量为核心的开发方法论。
它倡导从设计、编码、测试到部署运维的全过程中，将度量内建于系统之中，
通过度量数据来验证目标的达成、优化系统性能、驱动技术决策。

在 AI 辅助编程时代，MDD 与 TDD (测试驱动开发)、活文档 (Living Documentation)
一起构成了保障软件质量的 **三大护法**。

核心理念
--------

- **度量即设计**: 在系统设计阶段就规划好度量方案
- **度量即开发**: 将度量代码视为业务代码的一等公民
- **度量即运维**: 以度量数据驱动日常运维工作
- **度量即决策**: 基于度量数据做出技术和业务决策

度量内容 (USED)
----------------

- **U - Usage (使用量)**: 度量用户使用系统的频率和方式
- **S - Saturation (饱和度)**: 度量系统资源的使用率和剩余容量
- **E - Error (错误)**: 度量系统中故障和异常的发生情况
- **D - Delay (延迟)**: 度量系统响应请求所需的时间

多语言支持
----------

本书代码示例覆盖主流编程语言:

- **Go**: Prometheus client_golang, OpenTelemetry Go SDK
- **Python**: prometheus_client, opentelemetry-python
- **Java**: Micrometer, Spring Boot Actuator
- **C++**: prometheus-cpp, OpenTelemetry C++ SDK


.. toctree::
   :maxdepth: 2
   :caption: 目录

   preface
   chapter1
   chapter2
   chapter3
   chapter4
   chapter5
   chapter6
   chapter7
   chapter8
   appendix
   glossary


索引
=====

* :ref:`genindex`
* :ref:`search`
