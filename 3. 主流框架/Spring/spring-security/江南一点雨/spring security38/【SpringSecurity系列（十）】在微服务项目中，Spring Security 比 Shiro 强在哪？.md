# 在微服务项目中，Spring Security 比 Shiro 强在哪？

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2020年04月30日 08:08*

今日干货

![Image](img/640-1729316153518.webp)

刚刚发表

查看:66666回复:666

公众号后台回复 ssm，免费获取松哥纯手敲的 SSM 框架学习干货。



![img](img/0.png)**江南一点雨**推荐搜索

springsecurityspringboot

最近松哥一直在和大家介绍 Spring Security 以及 OAuth2 相关的技能点，连载的文章也写了好多篇了，大家在公众号后台回复 **「2020」** 可以获取原创文章索引。

虽然目前 Spring Security 一片火热，但是 Shiro 的市场依然存在，今天我就来稍微的说一说这两个框架的，方便大家在实际项目中选择适合自己的安全管理框架。

首先我要声明一点，框架无所谓好坏，关键是适合当前项目场景，作为一个年轻的程序员更不应该厚此薄彼，或者拒绝学习某一个框架。

小孩子才做选择题，成年人两个都要学！

所以接下来主要结合我自己的经验来说一说这两个框架的优缺点，没有提到的地方也欢迎大家留言补充。

## 1. Spring Security

### 1.1 因为 SpringBoot 而火

Spring Security 并非一个新生的事物，它最早不叫 Spring Security ，叫 Acegi Security，叫 Acegi Security 并不是说它和 Spring 就没有关系了，它依然是为 Spring 框架提供安全支持的。事实上，Java 领域的框架，很少有框架能够脱离 Spring 框架独立存在。

当 Spring Security 还叫 Acegi Security 的时候，虽然功能也还可以，但是实际上这个东西并没有广泛流行开来。最重要的原因就是它的配置太过于繁琐，当时网上流传一句话：“每当有人要使用 Acegi Security，就会有一个精灵死去。” 足见 Acegi Security 的配置是多么可怕。直到今天，当人们谈起 Spring Security 的时候，依然在吐槽它的配置繁琐。

后来 Acegi Security 投入 Spring 的怀抱，改名叫 Spring Security，事情才慢慢开始发生变化。新的开发团队一直在尽力简化 Spring Security 的配置，Spring Security 的配置相比 Acegi Security 确实简化了很多。但是在最初的几年里，Spring Security 依然无法得到广泛的使用。

直到有一天 Spring Boot 像谜一般出现在江湖边缘，彻底颠覆了 JavaEE 的世界。一人得道鸡犬升天，自从 Spring Boot 火了之后，Spring 家族的产品都被带了一把，Spring Security 就是受益者之一，从此飞上枝头变凤凰。

Spring Boot/Spring Cloud 现在作为 Java 开发领域最最主流的技术栈，这一点大家应该都没有什么异议，而在 Spring Boot/Spring Cloud 中做安全管理，Spring Security 无疑是最方便的。

你想保护 Spring Boot 中的接口，添加一个 Spring Security 的依赖即可，事情就搞定了，所有接口就保护起来了，甚至不需要一行配置。

有小伙伴可能觉得这个太笼统了，我再举一个实际点的例子。

在微服务架构的项目中，我们可能使用 Eureka 做服务注册中心，默认情况下，Eureka 没有做安全管理，如果你想给 Eureka 添加安全管理，只需要添加 Spring Security 依赖，然后在 application.properties 中配置一下用户名密码即可，Eureka 就自动被保护起来了，别人无法轻易访问；然后各个微服务在注册的时候，只需要把注册地址改为 http://username:password@localhost:8080/eureka 即可。类似的例子还有 Spring Cloud Config 中的安全管理。

在微服务这种场景下，如果你想用 Shiro 代替 Spring Security，那 Shiro 代码量绝对非常可观，Spring Security 则可以非常容易的集成到现在流行的 Spring Boot/Spring Cloud 技术栈中，可以和 Spring Boot、Spring Cloud、Spring Social、WebSocket 等非常方便的整合。

所以我说，因为 Spring Boot/Spring Cloud 火爆，让 Spring Security 跟着沾了一把光。

### 1.2 配置臃肿吗？

**「有的人觉得 Spring Security 配置臃肿。」**

如果是 SSM + Spring Security 的话，我觉得这话有一定道理。

但是如果是 Spring Boot 项目的话，其实并不见得臃肿。Spring Boot 中，通过自动化配置 starter 已经极大的简化了 Spring Security 的配置，我们只需要做少量的定制的就可以实现认证和授权了，这一点，大家可以参考我之前连载的 Spring Security 系列文章基本就能感受到这种便利（公众号后台回复 2020 有文章索引）。

**「有人觉得 Spring Security 中概念复杂。」**

这个是这样的，没错。

Spring Security 由于功能比较多，支持 OAuth2 等原因，就显得比较重量级，不像 Shiro 那样轻便。

但是如果换一个角度，你可能会有不一样的感受。

在 Spring Security 中你会学习到许多安全管理相关的概念，以及常见的安全攻击。这些安全攻击，如果你不是 web 安全方面的专家，很多可能存在的 web 攻击和漏洞你可能很难想到，而 Spring Security 则把这些安全问题都给我们罗列出来并且给出了相应的解决方案。

所以我说，我们学习 Spring Security 的过程，也是在学习 web 安全，各种各样的安全攻击、各种各样的登录方式、各种各样你能想到或者想不到的安全问题，Spring Security 都给我们罗列出来了，并且给出了解决方案，从这个角度来看，你会发现 Spring Security 好像也不是那么让人讨厌。

### 1.3 结合微服务的优势

除了前面和大家介绍的 Spring Security 优势，在微服务中，Spring 官方推出了 Spring Cloud Security 和 Spring Cloud OAuth2，结合微服务这种分布式特性，可以让我们更加方便的在微服务中使用 Spring Security 和 OAuth2，松哥前面的 OAuth2 系列实际上都是基于 Spring Cloud Security 来做的。

可以看到，Spring 官方一直在积极进取，让 Spring Security 能够更好的集成进微服务中。

## 2. Shiro

接下来我们再说说 Apache Shiro。

Apache Shiro 是一个开源安全框架，提供身份验证、授权、密码学和会话管理。Shiro 框架具有直观、易用等特性，同时也能提供健壮的安全性，虽然它的功能不如 Spring Security 那么强大，但是在常规的企业级应用中，其实也够用了。

### 2.1 由来

Shiro 的前身是 JSecurity，2004 年，Les Hazlewood 和 Jeremy Haile 创办了 Jsecurity。当时他们找不到适用于应用程序级别的合适 Java 安全框架，同时又对 JAAS 非常失望，于是就搞了这个东西。

2004 年到 2008 年期间，JSecurity 托管在 SourceForge 上，贡献者包括 Peter Ledbrook、Alan Ditzel 和 Tim Veil。

2008 年，JSecurity 项目贡献给了 Apache 软件基金会（ASF），并被接纳成为 Apache Incubator 项目，由导师管理，目标是成为一个顶级 Apache 项目。期间，Jsecurity 曾短暂更名为 Ki，随后因商标问题被社区更名为“Shiro”。随后项目持续在 Apache Incubator 中孵化，并增加了贡献者 Kalle Korhonen。

2010 年 7 月，Shiro 社区发布了 1.0 版，随后社区创建了其项目管理委员会，并选举 Les Hazlewood 为主席。2010 年 9 月 22 日，Shrio 成为 Apache 软件基金会的顶级项目（TLP）。

### 2.2 有哪些功能

Apache Shiro 是一个强大而灵活的开源安全框架，它干净利落地处理身份认证，授权，企业会话管理和加密。Apache Shiro 的首要目标是易于使用和理解。安全有时候是很复杂的，甚至是痛苦的，但它没有必要这样。框架应该尽可能掩盖复杂的地方，露出一个干净而直观的 API，来简化开发人员在应用程序安全上所花费的时间。

以下是你可以用 Apache Shiro 所做的事情：

1. 验证用户来核实他们的身份
2. 对用户执行访问控制，如：判断用户是否被分配了一个确定的安全角色；判断用户是否被允许做某事
3. 在任何环境下使用Session API，即使没有Web容器
4. 在身份验证，访问控制期间或在会话的生命周期，对事件作出反应
5. 聚集一个或多个用户安全数据的数据源，并作为一个单一的复合用户“视图”
6. 单点登录（SSO）功能
7. 为没有关联到登录的用户启用"Remember Me"服务
8. ...

Apache Shiro 是一个拥有许多功能的综合性的程序安全框架。下面的图表展示了 Shiro 的重点：

![Image](img/640-1729316153476.webp)

Shiro 中有四大基石——身份验证，授权，会话管理和加密。

1. Authentication：有时也简称为“登录”，这是一个证明用户是谁的行为。
2. Authorization：访问控制的过程，也就是决定“谁”去访问“什么”。
3. Session Management：管理用户特定的会话，即使在非 Web 或 EJB 应用程序。
4. Cryptography：通过使用加密算法保持数据安全同时易于使用。

除此之外，Shiro 也提供了额外的功能来解决在不同环境下所面临的安全问题，尤其是以下这些：

1. Web Support：Shiro 的 web 支持的 API 能够轻松地帮助保护 Web 应用程序。
2. Caching：缓存是 Apache Shiro 中的第一层公民，来确保安全操作快速而又高效。
3. Concurrency：Apache Shiro 利用它的并发特性来支持多线程应用程序。
4. Testing：测试支持的存在来帮助你编写单元测试和集成测试。
5. "Run As"：一个允许用户假设为另一个用户身份（如果允许）的功能，有时候在管理脚本很有用。
6. "Remember Me"：在会话中记住用户的身份，这样用户只需要在强制登录时候登录。

### 2.3 学习资料

Shiro 的学习资料并不多，没看到有相关的书籍。张开涛的《跟我学Shiro》是一个非常不错的资料，小伙伴可以搜索了解下。也可以在公众号后台回复 2TB，有相关的视频教程。

### 2.4 优势和劣势

就目前而言，Shiro 最大的问题在于和 Spring 家族的产品进行整合的时候非常不便，在 Spring Boot 推出的很长一段时间里，Shiro 都没有提供相应的 starter，后来虽然有一个 `shiro-spring-boot-web-starter` 出来，但是其实配置并没有简化多少。所以在 Spring Boot/Spring Cloud 技术栈的微服务项目中，Shiro 几乎不存在优势。

但是如果你是传统的 SSM 项目，不是微服务项目，那么无疑使用 Shiro 是最方便省事的，因为它足够简单，足够轻量级。

## 3. 如何取舍

在公司里做开发，这两个要如何取舍，还是要考虑蛮多东西的。

首先，如果是基于 Spring Boot/Spring Cloud 的微服务项目，Spring Security 无疑是最方便的。

如果是就是普通的 SSM 项目，那么 Shiro 基本上也够用。

另外，选择技术栈的时候，我们可能也要考虑团队内工程师的技术栈，如果工程师更擅长 Shiro，那么无疑 Shiro 是合适的，毕竟让工程师去学习一门新的技术，一来可能影响项目进度，而来也可能给项目埋下许多未知的雷。

对于我们个人来说，小孩子才做选择题，成年人两个都要学。

最后，我再罗列一下最近的 Spring Security 系列文章，供大家参考（这个系列还在继续更新中）：

**「Spring Security 系列：」**

1. [挖一个大坑，Spring Security 开搞！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488106&idx=1&sn=258fc609661f22de5517e1bc5c0915ed&scene=21#wechat_redirect)
2. [松哥手把手带你入门 Spring Security，别再问密码怎么解密了](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488113&idx=1&sn=01168c492e22fa287043eb746950da73&scene=21#wechat_redirect)
3. [手把手教你定制 Spring Security 中的表单登录](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488138&idx=1&sn=25d18a61a14e4e6316537b6d45e43dd4&scene=21#wechat_redirect)
4. [Spring Security 做前后端分离，咱就别做页面跳转了！统统 JSON 交互](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488157&idx=2&sn=845178d45e73232a94ed46df12ea76cc&scene=21#wechat_redirect)
5. [Spring Security 中的授权操作原来这么简单](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488185&idx=1&sn=6fe677703312905d39615a001e191bd2&scene=21#wechat_redirect)
6. [Spring Security 如何将用户数据存入数据库？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488194&idx=1&sn=7103031896ba8b9d34095524b292265e&scene=21#wechat_redirect)
7. [Spring Security+Spring Data Jpa 强强联手，安全管理只有更简单！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488229&idx=1&sn=2911c04bf19d41b00b4933d4044590f8&scene=21#wechat_redirect)
8. [Spring Boot + Spring Security 实现自动登录功能](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488300&idx=1&sn=5f08f7d5c9e98a0eb89eeb999b83c66b&scene=21#wechat_redirect)
9. [Spring Boot 自动登录，安全风险要怎么控制？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488306&idx=2&sn=97966ecdad08575eb466dc6dfc717097&scene=21#wechat_redirect)

**「OAuth2 系列：」**

1. [做微服务绕不过的 OAuth2，松哥也来和大家扯一扯](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488209&idx=2&sn=19b1e44fbb1f4c1210f0fa92a618d871&scene=21#wechat_redirect)
2. [这个案例写出来，还怕跟面试官扯不明白 OAuth2 登录流程？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488214&idx=1&sn=5601775213285217913c92768d415eca&scene=21#wechat_redirect)
3. [死磕 OAuth2，教练我要学全套的！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488223&idx=1&sn=d1003f33ac5c866c88aa7542fcdf4992&scene=21#wechat_redirect)
4. [OAuth2 令牌还能存入 Redis ？越玩越溜！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488246&idx=2&sn=b1ee410dbe86f2b748845304f7734d62&scene=21#wechat_redirect)
5. [想让 OAuth2 和 JWT 在一起愉快玩耍？请看松哥的表演](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488267&idx=2&sn=0ac88e1685ef0915e71eb3c223bd732f&scene=21#wechat_redirect)
6. [最近在做 Spring Cloud 项目，松哥和大家分享一点微服务架构中的安全管理思路](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488276&idx=1&sn=403ceff888f155bdb2f0d73931a60532&scene=21#wechat_redirect)
7. [Spring Boot+OAuth2，一个注解搞定单点登录！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488278&idx=1&sn=b21345a1daa86dd48ea89cdb9138def8&scene=21#wechat_redirect)
8. [分分钟让自己的网站接入 GitHub 第三方登录功能](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488282&idx=1&sn=fbee310276a4669e677d70e5ba36431d&scene=21#wechat_redirect)
9. [Spring Boot+OAuth2，如何自定义返回的 Token 信息？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488295&idx=2&sn=c21ddf5dbd9a6c9e101aa768dc5dbe48&scene=21#wechat_redirect)

参考资料：

1. 维基百科-Apache Shiro
2. Shiro参考手册

今日干货

![Image](https://mmbiz.qpic.cn/mmbiz_jpg/GvtDGKK4uYlgtbREKPZQnH2H9BeAv8OrkqXU7R13XZGIc7TuOI3j2P5cU2Kgs7YmdAoK5Ve8kfjCd5r6B7WF4g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

刚刚发表

查看:13500回复:135

公众号后台回复 2TB，免费获取 2TB Java 学习资料。

SpringSecurity系列52

SpringSecurity系列 · 目录







上一篇Spring Boot 自动登录，安全风险要怎么控制？松哥教你两招下一篇SpringSecurity 自定义认证逻辑的两种方式(高级玩法)









# 