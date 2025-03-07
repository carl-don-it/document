```bash
//这种聚合是把聚合所有的tag，加上by则是先分组再加。
sum(rate(demo_api_request_duration_seconds_count{job="demo"}[5m]))
```

# 基础

https://www.cnblogs.com/chanshuyi/category/1862951.html | 14. Prometheus 快速入门教程 - 随笔分类 - 陈树义 - 博客园
https://www.cnblogs.com/dahuige/category/2007667.html | prometheus - 随笔分类 - 大辉哥 - 博客园

https://www.cnblogs.com/skymyyang/p/13730607.html | Prometheus监控系统 - Devops、小铁匠 - 博客园

https://kebingzao.com/2024/02/22/prometheus-best-practices-4-cron-pushgateway/ | prometheus + grafana 实战篇(4) - 使用计划任务主动抛送到 pushgateway | Zach Ke's Notes

https://www.cnblogs.com/zhongwencool/p/prometheus_grafana.html | Prometheus Grafana快速搭建 - 写着写着就懂了 - 博客园

https://blog.csdn.net/aixiaoyang168/article/details/100866159#3Spring_Boot__Micrometer_23 | Spring Boot 使用 Micrometer 集成 Prometheus 监控 Java 应用性能_micrometer-registry-prometheus-CSDN博客

https://www.cnblogs.com/xuweiqiang/p/16451862.html | Springboot开启prometheus监控指标获取HTTP请求的吞吐时延等 - 许伟强 - 博客园

https://blog.csdn.net/qq_34556414/category_10759534_2.html | Prometheus_富士康质检员张全蛋的博客-CSDN博客

[3W字干货深入分析基于Micrometer和Prometheus实现度量和监控的方案 ](https://www.cnblogs.com/throwable/p/13257557.html)

https://www.cnblogs.com/skymyyang/p/13730607.html | Prometheus监控系统 - Devops、小铁匠 - 博客园

# metric

https://dbaplus.cn/news-134-5049-1.html | 详解Prometheus四种指标类型，谁还不是个监控老司机了 - 运维 - dbaplus社群
https://weirenxue.github.io/2022/04/22/prometheus_histogram_quantile/ | [Prometheus] 詳解 histogram_quantile(q, sum(rate()) by (le)) 原理 | 薛惟仁 筆記本
https://tw511.com/a/01/58458.html | prometheus Histogram 統計原理 - tw511教學網
https://cloud.tencent.com/developer/article/1579806 | Prometheus 常用函数 histogram_quantile 的若干“反直觉”问题-腾讯云开发者社区-腾讯云
https://stackoverflow.com/questions/55162093/understanding-histogram-quantile-based-on-rate-in-prometheus | histogram - Understanding histogram_quantile based on rate in Prometheus - Stack Overflow

https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html | Histograms and Percentiles :: Micrometer

https://stackoverflow.com/questions/50821924/micrometer-prometheus-gauge-displays-nan | java - Micrometer - Prometheus Gauge displays NaN - Stack Overflow

# query

https://blog.csdn.net/qq_34556414/article/details/123010040 | PromQL 分组与聚合函数 基于标签、时间聚合_prometheus group by-CSDN博客_

https://blog.csdn.net/duke_ding2/article/details/127169072 | Prometheus的查询_prometheus最近一条数据-CSDN博客



# 图表

https://blog.csdn.net/qq_42768234/article/details/134143598 | Grafana 图表 Table 根据 Key 修改背景颜色_grafana背景颜色修改-CSDN博客
https://blog.csdn.net/u013235026/article/details/131082232 | 在Grafana中合并加入两个指标值在table中展示_grafana table 多个metric-CSDN博客
https://blog.csdn.net/u010039418/article/details/136568031 | grafana table合并查询-CSDN博客
https://blog.csdn.net/qq_35753140/article/details/112943229 | Grafana table 表格配置方法_grafana设置表格-CSDN博客
https://panzhongxian.cn/cn/2023/09/grafana-pannel-skills/ | Grafana 常用但难配的图表 - 潘忠显
https://blog.csdn.net/qianhuan_/article/details/122223823   | grafana cat数据源常用query_grafana怎么查看平均响应时间-CSDN博客

# alert

https://zhangquan.me/2022/09/09/prometheus-bao-jing-xi-tong-alertmanager/ | Prometheus报警系统AlertManager | 一代键客
https://www.cnblogs.com/hahaha111122222/p/13724063.html | Prometheus 监控报警系统 AlertManager 之邮件告警 - 哈喽哈喽111111 - 博客园
https://www.cnblogs.com/88223100/p/The-easiest-to-understand-Prometheus-alarm-principle.html | 最易懂的Prometheus告警原理详解 - 古道轻风 - 博客园
https://yunlzheng.gitbook.io/prometheus-book/parti-prometheus-ji-chu/alert/prometheus-alert-rule | 自定义Prometheus告警规则 | prometheus-book
https://blog.csdn.net/qq_42883074/article/details/115510359 | prometheus使用 (十五) alertmanager特性--分组_prometheus group by-CSDN博客
https://www.cuiliangblog.cn/detail/article/35 | Alertmanager——配置详解-崔亮的博客

https://blog.csdn.net/doyzfly/article/details/113619042 | Prometheus 编写告警规则应对 metric 丢失的问题_prometheus unless on-CSDN博客

https://blog.csdn.net/qq_43684922/article/details/131142711 | 【博客655】prometheus如何应对告警目标消失带来的评估缺失问题_如果都是没有数据,那如何针对没有数据告警? 这个prom有没有标准的方案? 还是需要-CSDN博客

https://blog.csdn.net/luanpeng825485697/article/details/82730704 | prometheus之记录规则(recording rules)与告警规则(alerting rule)_prometheus record-CSDN博客

https://pracucci.com/prometheus-understanding-the-delays-on-alerting.html | Prometheus: understanding the delays on alerting

# 客户端

https://prometheus.github.io/client_java/getting-started/metric-types/#histogram | Metric Types | client_java

# step步长和rate

https://cloud.tencent.com/developer/article/1382875 | 详解Prometheus range query中的step参数-腾讯云开发者社区-腾讯云
https://juejin.cn/post/7252159509837578298 | 为什么 Grafana 的图表和实际监控数据不一样 —— 步长是如何计算的自建监控、Grafana、Prometheus - 掘金

https://stackoverflow.com/questions/56882734/grafana-dashboard-not-showing-data-when-zoomed-out | prometheus - Grafana Dashboard not showing data when zoomed out - Stack Overflow
https://cloud.tencent.com/developer/article/1506942 | Prometheus监控：rate与irate的区别-腾讯云开发者社区-腾讯云
https://segmentfault.com/a/1190000040783147 | prometheus -- rate()与irate()分析与源码 - 个人文章 - SegmentFault 思否

https://zhangguanzhang.github.io/2020/07/30/prometheus-rate-and-irate/#/%E7%94%B1%E6%9D%A5 | prometheus的rate与irate内部是如何计算的 · zhangguanzhang's Blog

保证interval大于几个scrape-time，然后step选择interval

# 细节

https://aleiwu.com/post/prometheus-bp/#rate-%E7%B1%BB%E5%87%BD%E6%95%B0-recording-rule-%E7%9A%84%E5%9D%91 | Prometheus 不完全避坑指南

https://cloud.tencent.com/developer/article/2415949 | Prometheus 指标值不准：是 feature，还是 bug？-腾讯云开发者社区-腾讯云

https://github.com/wufeiqun/blog/tree/master/prometheus | blog/prometheus at master · wufeiqun/blog

# micrometer

https://www.cnblogs.com/cjsblog/p/11556029.html | Micrometer 快速入门 - 废物大师兄 - 博客园

https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html | Histograms and Percentiles :: Micrometer



# 文档

https://yunlzheng.gitbook.io/prometheus-book

https://hulining.gitbook.io/prometheus/v2.18

# 案例

显示一段时间的increase，然后计算这一段count的平均时间，会有小数，因为有外延

