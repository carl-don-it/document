# 实践

https://www.cnblogs.com/hellxz/category/1203216.html

> 不重要

# 前言



# spring-cloud-context

> 可能是自身sc容器的一些配置。不知道是否需要第三方支持。

SC的context上下文、配置、生命周期管理、健康检查等等

https://www.cnblogs.com/hzhuxin/p/10496762.html

> 重要
>
> 　**1. SpringCloud Context模块的功能**  ：主要是构建了一个Bootstrap容器，并让其成为原有的springboot程序构建的容器的父容器。
>
> 　　**2. Bootstrap容器的作用：**是为了预先完成一些bean的实例化工作，可以把Bootstrap容器看作是先头部队。
>
> 　　**3. Bootstrap容器的构建：**是利用了Springboot的事件机制，当 springboot 的初始化 Environment  准备好之后会发布一个事件，这个事件的监听器将负责完成Bootstrap容器的创建。构建时是使用 SpringApplicationBuilder 类来完成的。
>
> ​       **4. 如何让Bootstrap容器与应用Context 建立父子关系 ：**由于Bootstrap容器与应用Context都是关联着同一个SpringApplication实例，Bootstrap容器自己完成初始化器的调用之后，会动态添加了一个初始化器 AncestorInitializer，相当于给应用Context 埋了个雷 ，这个初始化器在应用容器进行初始化器调用执行时，完成父子关系的设置。

https://spring-source-code-learning.gitbook.teaho.net/cloud/config/spring-cloud-config.html

[SpringCloud 实现配置加载源码分析](https://juejin.cn/post/7021768647056031774)

[Spring 源码解析十五：SpringCloud 的基础组件](https://segmentfault.com/a/1190000041040670)

[spring-cloud-context源码解读](https://blog.csdn.net/ttyy1112/article/details/103162952)

[Spring Cloud 引导上下文源码分析](https://mdnice.com/writing/f56074ee6913477c98cc4f0de19b0198)

# spring-cloud-commons

大名鼎鼎的SC的commons抽象。含有如：服务注册/发现、熔断器、负载均衡等**公共抽象**，面向该抽象编程可以无需关心底层实现，带来一致的编程感受，这是Spring家族最擅长干的事

> 应该就是定义了一些接口，当作bean来导入，但还需要第三方来实现并注入真正的bean实现类。

[Spring Cloud系列之Commons - 1. 背景与基础知识准备](https://cloud.tencent.com/developer/article/1811700)  

[Spring Cloud系列之Commons - 2. 服务发现 - 如何通过配置文件配置服务实例？](https://cloud.tencent.com/developer/article/1811706)

[spring-cloud-commons 源码分析](http://fangjian0423.github.io/2018/10/02/spring-cloud-commons-analysis/)  

# 参考文献

[Spring Boot基础教程](https://github.com/dyc87112/SpringBoot-Learning)

[SpringCloud微服务实战](https://github.com/hellxz/SpringCloudLearn)

[4 年 46 个版本，一文读懂 Spring Cloud 发展历史](https://blog.csdn.net/csdnnews/article/details/105304531)

[【方向盘】-Spring Cloud](https://blog.csdn.net/f641385712/category_9940544.html)

> 重要

[Spring Cloud 2020.0.0正式发布，再见了Netflix](https://blog.csdn.net/f641385712/article/details/111595426?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522168627237316800192226019%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fblog.%2522%257D&request_id=168627237316800192226019&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~blog~first_rank_ecpm_v1~rank_v31_ecpm-4-111595426-null-null.268^v1^koosearch&utm_term=cloud&spm=1018.2226.3001.4450)

[坑爹项目「spring-cloud-alibaba」，我们也来一个](https://juejin.cn/post/6844903813594218509#heading-7)

> 吐槽了spring cloud是什么，看得懂的人不需要看。

[详细剖析Spring Cloud 和Spring Cloud Alibaba的前世今生](https://www.cnblogs.com/mic112/p/15518522.html)

https://cloud.tencent.com/developer/column/91568

[（一）Spring-Cloud源码分析之核心流程关系及springcloud与springboot包区别（新）](https://blog.csdn.net/Peelarmy/article/details/129182408)

[Spring Cloud](https://blog.csdn.net/ttyy1112/category_9301961.html)

https://www.zhihu.com/question/289129028/answer/2244621878 | 为何说spring cloud适合中小型项目，而不适合大型项目？ - 知乎