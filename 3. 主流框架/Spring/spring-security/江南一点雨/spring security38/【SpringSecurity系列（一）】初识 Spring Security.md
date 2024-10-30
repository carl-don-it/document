【SpringSecurity系列（一）】初识 Spring Security

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年04月10日 12:16*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



自从 Spring Boot、Spring Cloud 火起来之后，Spring Security 也跟着沾了一把光！

其实我一直觉得 Spring Security 是一个比 Shiro 优秀很多的权限管理框架，但是重量级、配置繁琐、门槛高这些问题一直困扰着 Spring Security 的开发者，也让很多初学者望而却步。直到 Spring Boot 横空出世，这些问题统统都得到缓解。

在 Spring Boot 或者 Spring Cloud 中，如果想选择一个权限管理框架，几乎毫无疑问的选择 Spring Security，Shiro 在这个环境下已经不具备优势了。

## 1.现有资料

1. 松哥网站上的图文教程

在网站 www.javaboy.org 上，有一个 Spring Boot 模块，里边的安全管理一栏中，松哥大概写了十篇左右的文章来介绍 Spring Security 。当然这个教程也有离线版，大家在公众号【江南一点雨】的后台回复 SpringBoot，可以获取这个教程的 PDF 版。

1. 松哥录制的视频教程

第二个就是松哥之前录制的 Spring Boot 系列视频教程里边的第十章，视频目录如下：

```
├─第 10 章 Spring Boot 安全管理
│      01.安全管理介绍.mp4
│      02.Spring  Security 初体验.mp4
│      03.手工配置用户名密码.mp4
│      04.HttpSecurity 配置.mp4
│      05.登录表单详细配置.mp4
│      06.注销登录配置.mp4
│      07.多个 HttpSecurity.mp4
│      08.密码加密.mp4
│      09.方法安全.mp4
│      10.基于数据库的认证.mp4
│      11.角色继承.mp4
│      12.动态配置权限.mp4
│      13.OAuth2 简介.mp4
│      14.Spring Security 结合 OAuth2.mp4
│      15.整合 Shiro 方式一.mp4
│      16.整合 Shiro 方式二.mp4
│      17.Spring Security 使用 JSON 登录.mp4
```

这个视频是为了微人事项目提供支持的，也就是做微人事项目，这系列视频里边录制的知识点够用了。如果大家对这套视频感兴趣，可以查看这里👉[松哥自制视频教程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488059&idx=1&sn=2ef3e7f14d262130ecab94a0b17de0ca&scene=21#wechat_redirect)

这是松哥之前提供的 Spring Security 相关的教程。

## 2. Spring Security 介绍

Java 领域老牌的权限管理框架当属 Shiro 了。

Shiro 有着众多的优点，例如轻量、简单、易于集成等。

当然 Shiro 也有不足，例如对 OAuth2 支持不够，在 Spring Boot 面前无法充分展示自己的优势等等，特别是随着现在 Spring Boot 和 Spring Cloud 的流行，Spring Security 正在走向舞台舞台中央。

### 2.1 陈年旧事

Spring Security 最早不叫 Spring Security ，叫 Acegi Security，叫 Acegi Security 并不是说它和 Spring 就没有关系了，它依然是为 Spring 框架提供安全支持的。事实上，Java 领域的框架，很少有框架能够脱离 Spring 框架独立存在。

Acegi Security 基于 Spring，可以帮助我们为项目建立丰富的角色与权限管理，但是最广为人诟病的则是它臃肿繁琐的配置，这一问题最终也遗传给了 Spring Security。

在 Acegi Security 时代，网上流传一句话：“每当有人要使用 Acegi Security，就会有一个精灵死去。”足见 Acegi Security 的配置是多么可怕。

当 Acegi Security 投入 Spring 怀抱之后，先把这个名字改了，这就是大家所见到的 Spring Security 了，然后配置也得到了极大的简化。

但是和 Shiro 相比，人们对 Spring Security 的评价依然中重量级、配置繁琐。

直到有一天 Spring Boot 像谜一般出现在江湖边缘，彻底颠覆了 JavaEE 的世界。一人得道鸡犬升天，Spring Security 也因此飞上枝头变凤凰。

到现在，要不要学习 Spring Security 已经不是问题了，无论是 Spring Boot 还是 Spring Cloud，你都有足够多的机会接触到 Spring Security，现在的问题是如何快速掌握 Spring Security？那么看松哥的教程就对了。

### 2.2 核心功能

对于一个权限管理框架而言，无论是 Shiro 还是 Spring Security，最最核心的功能，无非就是两方面：

- 认证
- 授权

通俗点说，认证就是我们常说的登录，授权就是权限鉴别，看看请求是否具备相应的权限。

虽然就是一个简简单单的登录，可是也能玩出很多花样来。

Spring Security 支持多种不同的认证方式，这些认证方式有的是 Spring Security 自己提供的认证功能，有的是第三方标准组织制订的，主要有如下一些：

一些比较常见的认证方式：

- HTTP BASIC authentication headers：基于IETF RFC 标准。
- HTTP Digest authentication headers：基于IETF RFC 标准。
- HTTP X.509 client certificate exchange：基于IETF RFC 标准。
- LDAP：跨平台身份验证。
- Form-based authentication：基于表单的身份验证。
- Run-as authentication：用户用户临时以某一个身份登录。
- OpenID authentication：去中心化认证。

除了这些常见的认证方式之外，一些比较冷门的认证方式，Spring Security 也提供了支持。

- Jasig Central Authentication Service：单点登录。
- Automatic "remember-me" authentication：记住我登录（允许一些非敏感操作）。
- Anonymous authentication：匿名登录。
- ......

作为一个开放的平台，Spring Security 提供的认证机制不仅仅是上面这些。如果上面这些认证机制依然无法满足你的需求，我们也可以自己定制认证逻辑。当我们需要和一些“老破旧”的系统进行集成时，自定义认证逻辑就显得非常重要了。

除了认证，剩下的就是授权了。

Spring Security 支持基于 URL 的请求授权（例如微人事）、支持方法访问授权以及对象访问授权。

## 3.怎么学

安全这一块从来都有说不完的话题，一个简单的注册登录很好做，但是你要是考虑到各种各样的攻击，XSS、CSRF 等等，一个简单的注册登录也能做的很复杂。

幸运的是，即使你对各种攻击不太熟悉，只要你用了 Spring Security，就能自动避免掉很多攻击了，因为 Spring Security 已经自动帮我们完成很多防护了。

从这个角度讲，我们学习 Spring Security，不仅仅是学习 Spring Security 的各种用法，也是去了解去熟悉各种网络攻击。

松哥在未来，将会通过文章+视频的形式来向小伙伴们逐一展示 Spring Security 的用法，从基本的注册登录，到复杂的 OAuth2、Spring Social 登录等，都会和大家介绍。

本教程会默认大家有 Spring Boot 基础，如果小伙伴们对 Spring Boot 尚不熟悉，可以在公众号【江南一点雨】后台回复 SpringBoot，获取松哥纯手敲 Spring Boot 教程。

好了，本文就当是一个引子吧，下篇文章我们就开干。



SpringSecurity38

SpringSecurity · 目录







下一篇【SpringSecurity系列（二）】Spring Security入门









# 



























Scan to Follow