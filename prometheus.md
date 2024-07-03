```bash
//这种聚合是把不同的tag的速率相加，因为速率只能是同一个tag
sum(rate(demo_api_request_duration_seconds_count{job="demo"}[5m]))
```

https://prometheus.github.io/client_java/getting-started/metric-types/#histogram | Metric Types | client_java
https://www.cnblogs.com/chanshuyi/category/1862951.html | 14. Prometheus 快速入门教程 - 随笔分类 - 陈树义 - 博客园
https://www.cnblogs.com/dahuige/category/2007667.html | prometheus - 随笔分类 - 大辉哥 - 博客园
https://grafana.com/grafana/download?platform=windows | Download Grafana | Grafana Labs
https://prometheus.io/docs/prometheus/latest/configuration/configuration/#scrape_config | Configuration | Prometheus
https://www.cnblogs.com/skymyyang/p/13730607.html | Prometheus监控系统 - Devops、小铁匠 - 博客园
https://aleiwu.com/post/prometheus-bp/#rate-%E7%B1%BB%E5%87%BD%E6%95%B0-recording-rule-%E7%9A%84%E5%9D%91 | Prometheus 不完全避坑指南
https://dbaplus.cn/news-134-5049-1.html | 详解Prometheus四种指标类型，谁还不是个监控老司机了 - 运维 - dbaplus社群
https://weirenxue.github.io/2022/04/22/prometheus_histogram_quantile/ | [Prometheus] 詳解 histogram_quantile(q, sum(rate()) by (le)) 原理 | 薛惟仁 筆記本
https://tw511.com/a/01/58458.html | prometheus Histogram 統計原理 - tw511教學網
https://cloud.tencent.com/developer/article/1579806 | Prometheus 常用函数 histogram_quantile 的若干“反直觉”问题-腾讯云开发者社区-腾讯云
https://stackoverflow.com/questions/55162093/understanding-histogram-quantile-based-on-rate-in-prometheus | histogram - Understanding histogram_quantile based on rate in Prometheus - Stack Overflow
https://blog.csdn.net/qq_34556414/article/details/123010040 | PromQL 分组与聚合函数 基于标签、时间聚合_prometheus group by-CSDN博客
https://blog.csdn.net/gao_grace/article/details/107389110 | 使用Grafana展示Prometheus数据_grafana-prometheus-alertmanager-datasource-CSDN博客
https://blog.csdn.net/qq_34556414/category_10759534_2.html | Prometheus_富士康质检员张全蛋的博客-CSDN博客

https://blog.csdn.net/luanpeng825485697/article/details/82730704 | prometheus之记录规则(recording rules)与告警规则(alerting rule)_prometheus record-CSDN博客
https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html | Histograms and Percentiles :: Micrometer
https://www.cnblogs.com/cjsblog/p/11556029.html | Micrometer 快速入门 - 废物大师兄 - 博客园
https://www.cnblogs.com/xuweiqiang/p/16451862.html | Springboot开启prometheus监控指标获取HTTP请求的吞吐时延等 - 许伟强 - 博客园
https://blog.csdn.net/aixiaoyang168/article/details/100866159#3Spring_Boot__Micrometer_23 | Spring Boot 使用 Micrometer 集成 Prometheus 监控 Java 应用性能_micrometer-registry-prometheus-CSDN博客
https://www.cnblogs.com/zhongwencool/p/prometheus_grafana.html | Prometheus Grafana快速搭建 - 写着写着就懂了 - 博客园
https://hulining.gitbook.io/prometheus/v/v2.18/prometheus/configuration/alerting_rules | 告警规则 | prometheus 中文文档
https://blog.csdn.net/qq_43684922/article/details/131142711 | 【博客655】prometheus如何应对告警目标消失带来的评估缺失问题_如果都是没有数据,那如何针对没有数据告警? 这个prom有没有标准的方案? 还是需要-CSDN博客
https://blog.csdn.net/doyzfly/article/details/113619042 | Prometheus 编写告警规则应对 metric 丢失的问题_prometheus unless on-CSDN博客
https://zhangquan.me/2022/09/09/prometheus-bao-jing-xi-tong-alertmanager/ | Prometheus报警系统AlertManager | 一代键客
https://www.cnblogs.com/hahaha111122222/p/13724063.html | Prometheus 监控报警系统 AlertManager 之邮件告警 - 哈喽哈喽111111 - 博客园
https://www.cnblogs.com/88223100/p/The-easiest-to-understand-Prometheus-alarm-principle.html | 最易懂的Prometheus告警原理详解 - 古道轻风 - 博客园
https://yunlzheng.gitbook.io/prometheus-book/parti-prometheus-ji-chu/alert/prometheus-alert-rule | 自定义Prometheus告警规则 | prometheus-book
https://blog.csdn.net/qq_42883074/article/details/115510359 | prometheus使用 (十五) alertmanager特性--分组_prometheus group by-CSDN博客
https://www.cuiliangblog.cn/detail/article/35 | Alertmanager——配置详解-崔亮的博客