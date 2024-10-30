# 新版Spring Security 中的路径匹配方案！

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2024年04月22日 10:09* *广东*

针对目前最新版的 Spring Security6，松哥录制了一套从零开始的视频教程，手把手教大家搞懂最新版 Spring Security 的玩法，有需要的小伙伴戳这里：[最新版Spring Security6 视频教程来啦～](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247506969&idx=1&sn=2fce50b5ceeae1ddefba7b2de857fafe&scene=21#wechat_redirect)。

------

Spring Security 是一个功能强大且可高度定制的安全框架，它提供了一套完整的解决方案，用于保护基于 Spring 的应用程序。在 Spring Security 中，路径匹配是权限控制的核心部分，它决定了哪些请求可以访问特定的资源。本文将详细介绍 Spring Security 中的路径匹配策略，并提供相应的代码示例。

在旧版的 Spring Security 中，路径匹配方法有很多，但是新版 Spring Security 对这些方法进行了统一的封装，都是调用 requestMatchers 方法进行处理：

```
public C requestMatchers(RequestMatcher... requestMatchers) {
 Assert.state(!this.anyRequestConfigured, "Can't configure requestMatchers after anyRequest");
 return chainRequestMatchers(Arrays.asList(requestMatchers));
}
```

requestMatchers 方法接收一个 RequestMatcher 类型的参数，RequestMatcher 是一个接口，这个接口是一个用来确定 HTTP 请求是否与给定的模式匹配的工具。这个接口提供了一种灵活的方式来定义请求的匹配规则，从而可以对不同的请求执行不同的安全策略。

所以在新版 Spring Security 中，不同的路径匹配分方案实际上就是不同的 RequestMatcher 的实现类。

## **1. AntPathRequestMatcher**

`AntPathRequestMatcher` 是 Spring 中最常用的请求匹配器之一，它使用 Ant 风格的路径模式来匹配请求的 URI。

### **1.1 什么是 Ant 风格的路径模式**

Ant 风格的路径模式（Ant Path Matching）是一种用于资源定位的模式匹配规则，它源自 Apache Ant 这个 Java 构建工具。在 Ant 中，这种模式被用来指定文件系统中的文件和目录。由于其简单性和灵活性，Ant 风格的路径模式也被其他许多框架和应用程序所采用，包括 Spring Security。

Ant 风格的路径模式使用了一些特殊的字符来表示不同级别的路径匹配：

1. `?`：匹配任何单个字符（除了路径分隔符）。
2. `*`：匹配任何字符的序列（除了路径分隔符），但不包括空字符串。
3. `**`：匹配任何字符的序列，包括空字符串。至少匹配一个字符的序列，并且可以跨越路径分隔符。
4. `{}`：表示一个通配符的选择，可以匹配多个逗号分隔的模式。例如，`{,春夏秋冬}` 可以匹配任何以春夏秋冬开头的字符串。

在 Spring Security 中，Ant 风格的路径模式通常用于定义 URL 路径和安全配置之间的映射关系。例如，你可以使用 Ant 风格的路径模式来指定哪些 URL 路径需要特定的权限或角色。

以下是一些 Ant 风格路径模式的例子：

- `/users/*`：匹配以 `/users/` 开始的任何路径，如 `/users/123` 或 `/users/profile`。
- `/users/**`：匹配以 `/users/` 开始的任何路径，包括子路径，如 `/users/123` 或 `/users/profile/picture`.
- `/users/123`：精确匹配 `/users/123`。
- `/users/{id}`：虽然这不是 Ant 风格的模式，但它展示了路径参数匹配，可以匹配 `/users/123`、`/users/456` 等。
- `/files/**.{jpg,png}`：匹配 `/files/` 下所有以 `.jpg` 或 `.png` 结尾的文件路径，如 `/files/image1.jpg` 或 `/files/folder/image.png`。

通过使用 Ant 风格的路径模式，你可以灵活地定义复杂的 URL 匹配规则，以适应不同的安全需求。

### **1.2 基本用法**

```
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// 创建 AntPathRequestMatcher 实例
RequestMatcher antMatcher = new AntPathRequestMatcher("/users/**", "GET");

// 使用 matcher 进行匹配
boolean isMatch = antMatcher.matches(request);
```

### **1.3 通配符**

- `?` 匹配任何单字符。
- `*` 匹配任何字符序列（但不包括目录分隔符）。
- `**` 匹配任何字符序列，包括目录分隔符。

```
// 匹配 /admin 下的任何资源，包括子目录
RequestMatcher adminMatcher = new AntPathRequestMatcher("/admin/**");

// 匹配 /files 目录下的任何 HTML 文件
RequestMatcher fileMatcher = new AntPathRequestMatcher("/files/*.{html,htm}", "GET");
```

## **2. RegexRequestMatcher**

`RegexRequestMatcher` 使用正则表达式来匹配请求的 URI 和 HTTP 方法。

### **2.1 基本用法**

```
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

// 创建 RegexRequestMatcher 实例
RequestMatcher regexMatcher = new RegexRequestMatcher("^/api/.*", "GET");

// 使用 matcher 进行匹配
boolean isMatch = regexMatcher.matches(request);
```

### **2.2 使用正则表达式**

```
// 匹配任何以 /api 开头的 URI
RequestMatcher apiMatcher = new RegexRequestMatcher("^/api/.*");

// 匹配任何 HTTP 方法
RequestMatcher anyMethodMatcher = new RegexRequestMatcher("^/.*", "GET|POST|PUT|DELETE");
```

### **2.3 结合 Spring Security**

下面这段代码，表示拦截所有以 html、css 以及 js 结尾的请求，这些请求可以直接访问：

```
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers(new RegexRequestMatcher("^.*\\.(htm|css|js)$","GET")).permitAll())
                .formLogin(Customizer.withDefaults())
                .csrf(c -> c.disable());
        return http.build();
    }
}
```

## **3. RequestHeaderRequestMatcher**

`RequestHeaderRequestMatcher` 用来匹配请求头中的键和值。

```
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

// 创建 RequestHeaderRequestMatcher 实例
RequestMatcher headerMatcher = new RequestHeaderRequestMatcher("User-Agent", "Mozilla.*");

// 使用 matcher 进行匹配
boolean isMatch = headerMatcher.matches(request);
```

具体到 Spring Security 中，用法如下：

```
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers(new RequestHeaderRequestMatcher("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")).permitAll())
                .formLogin(Customizer.withDefaults())
                .csrf(c -> c.disable());
        return http.build();
    }
}
```

## **4. NegatedRequestMatcher**

`NegatedRequestMatcher` 允许你否定一个已有的 `RequestMatcher` 的匹配结果。

```
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

// 创建一个 matcher，然后否定它的匹配结果
RequestMatcher notAdminMatcher = new NegatedRequestMatcher(adminMatcher);

// 使用 negated matcher 进行匹配
boolean isNotMatch = notAdminMatcher.matches(request);
```

例如下面这段代码表示除了 `/hello` 之外的地址，全都可以直接访问：

```
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/hello"))).permitAll())
                .formLogin(Customizer.withDefaults())
                .csrf(c -> c.disable());
        return http.build();
    }
}
```

## **5. AndRequestMatcher 和 OrRequestMatcher**

`AndRequestMatcher` 和 `OrRequestMatcher` 分别用来组合多个 `RequestMatcher` 实例，进行“与”或“或”的逻辑匹配。

### **5.1 AndRequestMatcher**

```
import org.springframework.security.web.util.matcher.AndRequestMatcher;

// 组合多个 matcher 进行“与”匹配
RequestMatcher andMatcher = new AndRequestMatcher(apiMatcher, headerMatcher);

// 使用 andMatcher 进行匹配
boolean isMatch = andMatcher.matches(request);
```

### **5.2 OrRequestMatcher**

```
import org.springframework.security.web.util.matcher.OrRequestMatcher;

// 组合多个 matcher 进行“或”匹配
RequestMatcher orMatcher = new OrRequestMatcher(adminMatcher, fileMatcher);

// 使用 orMatcher 进行匹配
boolean isMatch = orMatcher.matches(request);
```

## **6. 总结**

Spring 提供了多种 `RequestMatcher` 实现类，以满足不同的请求匹配需求。通过合理地使用这些匹配器，可以灵活地定义和实施安全策略。在实际应用中，你可能需要根据业务需求选择合适的匹配器，并结合 Spring Security 的配置来实现细粒度的访问控制。

最新版 Spring Security6 视频教程目录：

```
├── 01架构概览
│   ├── 01_SpringSecurity整体介绍.mp4
│   ├── 02_整体架构.mp4
│   ├── 03_登录流程.mp4
│   ├── 04_默认过滤器链.mp4
│   └── 05_登录用户配置.mp4
├── 02基本认证
│   ├── 01.自定义登录页面.mp4
│   ├── 02.登录成功页面跳转.mp4
│   ├── 03.登录成功处理器.mp4
│   ├── 04.登录失败处理器.mp4
│   ├── 05.注销登录.mp4
│   ├── 06.获取当前登录用户数据.mp4
│   ├── 07.请求对象提取当前登录用户.mp4
│   ├── 08.两种资源放行方式分析.mp4
│   ├── 09.JdbcUserDetailsManager.mp4
│   ├── 10.对接 MyBatis.mp4
├── 03认证流程分析
│   ├── 01.AuthenticationManager 分析.mp4
│   ├── 02.认证流程分析.mp4
│   ├── 03.多数据源配置.mp4
│   ├── 04.添加登录验证码.mp4
├── 04初始化流程分析
│   ├── 01.初始化组件.mp4
│   ├── 02.多种用户定义方案.mp4
│   ├── 03.定义多个过滤器链.mp4
│   ├── 04.自定义登录接口.mp4
│   ├── 05.自定义登录过滤器（实现 JSON 登录）.mp4
│   ├── 06.另一种验证码添加思路.mp4
│   ├── 07.开通阿里云短信试用.mp4
│   ├── 08.短信验证码登录.mp4
├── 05密码加密
│   ├── 01.密码加密方案.mp4
│   ├── 02.PasswordEncoder 分析.mp4
│   ├── 03.配置 PasswordEncoder.mp4
│   ├── 04.加密方案自动升级.mp4
│   ├── 05.PasswordEncoder 初始化逻辑分析.mp4
├── 06RememberMe
│   ├── 01.开启 RememberMe.mp4
│   ├── 02.RememberMe 原理分析.mp4
│   ├── 03.RememberMe 持久化令牌.mp4
│   ├── 04.持久化令牌原理分析.mp4
├── 07会话管理
│   ├── 01.会话并发管理.mp4
│   ├── 02.原理分析.mp4
│   ├── 03.会话固定攻击与防御.mp4
├── 08HttpFirewall
│   ├── 01.HttpFirewall 简介.mp4
│   ├── 02.HttpFirewall 严格模式原理分析.mp4
│   ├── 03.HttpFirewall 默认模式分析.mp4
├── 09漏洞保护
│   ├── 01.CSRF 攻击演示.mp4
│   ├── 02.CSRF 防御方案分析.mp4
│   ├── 03.CSRF 防御源码分析.mp4
│   ├── 04.缓存控制.mp4
│   ├── 05.X-Content-Type-Options.mp4
│   ├── 06.Strict-Transport-Security.mp4
│   ├── 07.X-Frame-Options.mp4
│   ├── 08.XSS 攻击与防御.mp4
│   ├── 09.内容安全策略.mp4
│   ├── 10.其他.mp4
│   ├── 11.HTTP 通信安全.mp4
├── 10HTTP认证
│   ├── 01.Basic 认证.mp4
│   ├── 02.Basic 认证原理分析.mp4
│   ├── 03.Digest 认证.mp4
│   ├── 04.Digest 认证原理分析.mp4
├── 11跨域问题
│   ├── 01.三种跨域解决方案.mp4
│   ├── 02.跨域问题分析.mp4
│   ├── 03.Spring Security 处理跨域.mp4
│   ├── 04.原理分析.mp4
├── 12异常处理
│   ├── 01.Spring Security 异常体系.mp4
│   ├── 02.ExceptionTranslationFilter.mp4
│   ├── 03.认证异常.mp4
│   ├── 04.授权异常.mp4
└── 13权限管理
│   ├── 01.搭建权限数据库.mp4
│   ├── 02.基于 URL 地址的权限拦截.mp4
│   ├── 03.基于注解的权限拦截.mp4
│   ├── 04.AuthorizationManager.mp4
│   ├── 05.URL 地址拦截权限原理分析.mp4
│   ├── 06.注解拦截权限原理分析.mp4
│   ├── 07.自定义权限注解表达式.mp4
│   ├── 08.自定义权限拦截规则.mp4
│   ├── 09.常见权限模型.mp4
```

这是一套从零开始的视频教程，手把手教大家搞懂目前最新版 Spring Security6 的玩法，有需要的小伙伴戳这里：[最新版Spring Security6 视频教程来啦～](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247506969&idx=1&sn=2fce50b5ceeae1ddefba7b2de857fafe&scene=21#wechat_redirect)。



SpringSecurity38

SpringSecurity · 目录







上一篇Spring Security6 全新写法，大变样！下一篇Nacos 中的配置文件如何实现加密传输









# 



























Scan to Follow