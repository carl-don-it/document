# 【SpringSecurity系列（十四）】自动踢掉上一个登录用户

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年05月18日 10:36*

松哥原创的 Spring Boot 视频教程已经杀青，感兴趣的小伙伴戳这里-->[Spring Boot+Vue+微人事视频教程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247491260&idx=1&sn=6e733419aca3b6f1814d832350d4080a&scene=21#wechat_redirect)

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



------

[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488392&idx=2&sn=e350435c511041021c254137fbe2fa3e&scene=21#wechat_redirect)中，我们讲了在 Spring Security 中如何踢掉前一个登录用户，或者禁止用户二次登录，通过一个简单的案例，实现了我们想要的效果。

但是有一个不太完美的地方，就是我们的用户是配置在内存中的用户，我们没有将用户放到数据库中去。正常情况下，松哥在 Spring Security 系列中讲的其他配置，大家只需要参考[Spring Security+Spring Data Jpa 强强联手，安全管理只有更简单！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488229&idx=1&sn=2911c04bf19d41b00b4933d4044590f8&scene=21#wechat_redirect)一文，将数据切换为数据库中的数据即可。

但是，在做 Spring Security 的 session 并发处理时，直接将内存中的用户切换为数据库中的用户会有问题，今天我们就来说说这个问题，顺便把这个功能应用到微人事中（https://github.com/lenve/vhr）。

本文是松哥最近在连载的 Spring Security 系列第 14 篇，阅读本系列前面的文章有助于更好的理解本文：

1. [【SpringSecurity系列（一）】初识 Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492925&idx=2&sn=b3b8943bce05e97d4f84d92002dd6571&scene=21#wechat_redirect)
2. [【SpringSecurity系列（二）】Spring Security入门](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493000&idx=2&sn=3d2862565e0f22968f1685199c6bdb87&scene=21#wechat_redirect)
3. [【SpringSecurity系列（三）】定制表单登录](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493036&idx=2&sn=0a0356f4724830eb136d673c289437b6&scene=21#wechat_redirect)
4. [【SpringSecurity系列（四）】登录成功返回JSON数据](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493044&idx=2&sn=e7a4f0fd826eeffffdb503cc2316bc50&scene=21#wechat_redirect)
5. [【SpringSecurity系列（五）】授权入门](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493062&idx=2&sn=1480de83f67c3049e7efcc1cce21a918&scene=21#wechat_redirect)
6. [【SpringSecurity系列（六）】自定义登录用户](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493130&idx=2&sn=7dff1f444fc652c23267a1ba89ea11d2&scene=21#wechat_redirect)
7. [【SpringSecurity系列（七）】通过 Spring Data Jpa 持久化用户数据](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493175&idx=2&sn=55ab518981e7952137c0c247205eb6a3&scene=21#wechat_redirect)
8. [【SpringSecurity系列（八）】用户还能自动登录？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493198&idx=2&sn=a6362d7264bd50a35b5cc46ddbd334b0&scene=21#wechat_redirect)
9. [【SpringSecurity系列（九）】降低 RememberMe 的安全风险](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493226&idx=2&sn=1ad5066cc96b6f2a7f05714693cb0aa0&scene=21#wechat_redirect)
10. [在微服务项目中，Spring Security 比 Shiro 强在哪？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488312&idx=1&sn=61e67f7ca0f8a55749dcb064b9456a38&scene=21#wechat_redirect)
11. [【SpringSecurity系列（十一）】自定义认证逻辑](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493279&idx=2&sn=7c89d5a454487174a9ab86d6788b0c34&scene=21#wechat_redirect)
12. [【SpringSecurity系列（十二）】查看登录详情](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493346&idx=2&sn=11f66f1851ad8e5101cb788c709519a1&scene=21#wechat_redirect)
13. [【SpringSecurity系列（十三）】只允许一台设备在线](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493361&idx=2&sn=4c1a841c7cfa88e6d092274b6bec5556&scene=21#wechat_redirect)

本文的案例将基于[【SpringSecurity系列（七）】通过 Spring Data Jpa 持久化用户数据](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493175&idx=2&sn=55ab518981e7952137c0c247205eb6a3&scene=21#wechat_redirect)一文来构建，所以重复的代码我就不写了，小伙伴们要是不熟悉可以参考该篇文章。

## 1.环境准备

首先，我们打开[【SpringSecurity系列（七）】通过 Spring Data Jpa 持久化用户数据](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493175&idx=2&sn=55ab518981e7952137c0c247205eb6a3&scene=21#wechat_redirect)一文中的案例，这个案例结合 Spring Data Jpa 将用户数据存储到数据库中去了。

然后我们将上篇文章中涉及到的登录页面拷贝到项目中（文末可以下载完整案例）：

![Image](img/640-1729337667639.webp)

并在 SecurityConfig 中对登录页面稍作配置：

```
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/js/**", "/css/**", "/images/**");
}
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            ...
            .and()
            .formLogin()
            .loginPage("/login.html")
            .loginProcessingUrl("/doLogin")
            ...
            .and()
            .sessionManagement()
            .maximumSessions(1);
}
```

这里都是常规配置，我就不再多说。注意最后面我们将 session 数量设置为 1。

好了，配置完成后，我们启动项目，并行性多端登录测试。

打开多个浏览器，分别进行多端登录测试，我们惊讶的发现，每个浏览器都能登录成功，每次登录成功也不会踢掉已经登录的用户！

这是怎么回事？

## 2.问题分析

要搞清楚这个问题，我们就要先搞明白 Spring Security 是怎么保存用户对象和 session 的。

Spring Security 中通过 SessionRegistryImpl 类来实现对会话信息的统一管理，我们来看下这个类的源码（部分）：

```
public class SessionRegistryImpl implements SessionRegistry,
  ApplicationListener<SessionDestroyedEvent> {
 /** <principal:Object,SessionIdSet> */
 private final ConcurrentMap<Object, Set<String>> principals;
 /** <sessionId:Object,SessionInformation> */
 private final Map<String, SessionInformation> sessionIds;
 public void registerNewSession(String sessionId, Object principal) {
  if (getSessionInformation(sessionId) != null) {
   removeSessionInformation(sessionId);
  }
  sessionIds.put(sessionId,
    new SessionInformation(principal, sessionId, new Date()));

  principals.compute(principal, (key, sessionsUsedByPrincipal) -> {
   if (sessionsUsedByPrincipal == null) {
    sessionsUsedByPrincipal = new CopyOnWriteArraySet<>();
   }
   sessionsUsedByPrincipal.add(sessionId);
   return sessionsUsedByPrincipal;
  });
 }
 public void removeSessionInformation(String sessionId) {
  SessionInformation info = getSessionInformation(sessionId);
  if (info == null) {
   return;
  }
  sessionIds.remove(sessionId);
  principals.computeIfPresent(info.getPrincipal(), (key, sessionsUsedByPrincipal) -> {
   sessionsUsedByPrincipal.remove(sessionId);
   if (sessionsUsedByPrincipal.isEmpty()) {
    sessionsUsedByPrincipal = null;
   }
   return sessionsUsedByPrincipal;
  });
 }

}
```

这个类的源码还是比较长，我这里提取出来一些比较关键的部分：

1. 首先大家看到，一上来声明了一个 principals 对象，这是一个支持并发访问的 map 集合，集合的 key 就是用户的主体（principal），正常来说，用户的 principal 其实就是用户对象，松哥在之前的文章中也和大家讲过 principal 是怎么样存入到 Authentication 中的（参见：[松哥手把手带你捋一遍 Spring Security 登录流程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488026&idx=2&sn=3bd96d91e822abf753a8e91142e036be&scene=21#wechat_redirect)），而集合的 value 则是一个 set 集合，这个 set 集合中保存了这个用户对应的 sessionid。
2. 如有新的 session 需要添加，就在 registerNewSession 方法中进行添加，具体是调用 principals.compute 方法进行添加，key 就是 principal。
3. 如果用户注销登录，sessionid 需要移除，相关操作在 removeSessionInformation 方法中完成，具体也是调用 principals.computeIfPresent 方法，这些关于集合的基本操作我就不再赘述了。

看到这里，大家发现一个问题，ConcurrentMap 集合的 key 是 principal 对象，用对象做 key，一定要重写 equals 方法和 hashCode 方法，否则第一次存完数据，下次就找不到了，这是 JavaSE 方面的知识，我就不用多说了。

如果我们使用了基于内存的用户，我们来看下 Spring Security 中的定义：

```
public class User implements UserDetails, CredentialsContainer {
 private String password;
 private final String username;
 private final Set<GrantedAuthority> authorities;
 private final boolean accountNonExpired;
 private final boolean accountNonLocked;
 private final boolean credentialsNonExpired;
 private final boolean enabled;
 @Override
 public boolean equals(Object rhs) {
  if (rhs instanceof User) {
   return username.equals(((User) rhs).username);
  }
  return false;
 }
 @Override
 public int hashCode() {
  return username.hashCode();
 }
}
```

可以看到，他自己实际上是重写了 equals 和 hashCode 方法了。

所以我们使用基于内存的用户时没有问题，而我们使用自定义的用户就有问题了。

找到了问题所在，那么解决问题就很容易了，重写 User 类的 equals 方法和 hashCode 方法即可：

```
@Entity(name = "t_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.PERSIST)
    private List<Role> roles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
    ...
    ...
}
```

配置完成后，重启项目，再去进行多端登录测试，发现就可以成功踢掉已经登录的用户了。

如果你使用了 MyBatis 而不是 Jpa，也是一样的处理方案，只需要重写登录用户的 equals 方法和 hashCode 方法即可。

## 3.微人事应用

### 3.1 存在的问题

由于微人事目前是采用了 JSON 格式登录，所以如果项目控制 session 并发数，就会有一些额外的问题要处理。

最大的问题在于我们用自定义的过滤器代替了 UsernamePasswordAuthenticationFilter，进而导致前面所讲的关于 session 的配置，统统失效。所有相关的配置我们都要在新的过滤器 LoginFilter 中进行配置 ，包括 SessionAuthenticationStrategy 也需要我们自己手动配置了。

这虽然带来了一些工作量，但是做完之后，相信大家对于 Spring Security 的理解又会更上一层楼。

### 3.2 具体应用

我们来看下具体怎么实现，我这里主要列出来一些关键代码，**完整代码大家可以从 GitHub 上下载**：https://github.com/lenve/vhr。

首先第一步，我们重写 Hr 类的 equals 和 hashCode 方法，如下：

```
public class Hr implements UserDetails {
    ...
    ...
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hr hr = (Hr) o;
        return Objects.equals(username, hr.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
    ...
    ...
}
```

接下来在 SecurityConfig 中进行配置。

这里我们要自己提供 SessionAuthenticationStrategy，而前面处理 session 并发的是 ConcurrentSessionControlAuthenticationStrategy，也就是说，我们需要自己提供一个 ConcurrentSessionControlAuthenticationStrategy 的实例，然后配置给 LoginFilter，但是在创建 ConcurrentSessionControlAuthenticationStrategy 实例的过程中，还需要有一个 SessionRegistryImpl 对象。

前面我们说过，SessionRegistryImpl 对象是用来维护会话信息的，现在这个东西也要我们自己来提供，SessionRegistryImpl 实例很好创建，如下：

```
@Bean
SessionRegistryImpl sessionRegistry() {
    return new SessionRegistryImpl();
}
```

然后在 LoginFilter 中配置 SessionAuthenticationStrategy，如下：

```
@Bean
LoginFilter loginFilter() throws Exception {
    LoginFilter loginFilter = new LoginFilter();
    loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
                //省略
            }
    );
    loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
                //省略
            }
    );
    loginFilter.setAuthenticationManager(authenticationManagerBean());
    loginFilter.setFilterProcessesUrl("/doLogin");
    ConcurrentSessionControlAuthenticationStrategy sessionStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
    sessionStrategy.setMaximumSessions(1);
    loginFilter.setSessionAuthenticationStrategy(sessionStrategy);
    return loginFilter;
}
```

我们在这里自己手动构建 ConcurrentSessionControlAuthenticationStrategy 实例，构建时传递 SessionRegistryImpl 参数，然后设置 session 的并发数为 1，最后再将 sessionStrategy 配置给 LoginFilter。

> ❝
>
> 其实[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488392&idx=2&sn=e350435c511041021c254137fbe2fa3e&scene=21#wechat_redirect)中，我们的配置方案，最终也是像上面这样，只不过现在我们自己把这个写出来了而已。

这就配置完了吗？没有！session 处理还有一个关键的过滤器叫做 ConcurrentSessionFilter，本来这个过滤器是不需要我们管的，但是这个过滤器中也用到了 SessionRegistryImpl，而 SessionRegistryImpl 现在是由我们自己来定义的，所以，该过滤器我们也要重新配置一下，如下：

```
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            ...
    http.addFilterAt(new ConcurrentSessionFilter(sessionRegistry(), event -> {
        HttpServletResponse resp = event.getResponse();
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(401);
        PrintWriter out = resp.getWriter();
        out.write(new ObjectMapper().writeValueAsString(RespBean.error("您已在另一台设备登录，本次登录已下线!")));
        out.flush();
        out.close();
    }), ConcurrentSessionFilter.class);
    http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);
}
```

在这里，我们重新创建一个 ConcurrentSessionFilter 的实例，代替系统默认的即可。在创建新的 ConcurrentSessionFilter 实例时，需要两个参数：

1. sessionRegistry 就是我们前面提供的 SessionRegistryImpl 实例。
2. 第二个参数，是一个处理 session 过期后的回调函数，也就是说，当用户被另外一个登录踢下线之后，你要给什么样的下线提示，就在这里来完成。

最后，我们还需要在处理完登录数据之后，手动向 SessionRegistryImpl 中添加一条记录：

```
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    SessionRegistry sessionRegistry;
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //省略
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    username, password);
            setDetails(request, authRequest);
            Hr principal = new Hr();
            principal.setUsername(username);
            sessionRegistry.registerNewSession(request.getSession(true).getId(), principal);
            return this.getAuthenticationManager().authenticate(authRequest);
        } 
        ...
        ...
    }
}
```

在这里，我们手动调用 sessionRegistry.registerNewSession 方法，向 SessionRegistryImpl 中添加一条 session 记录。

OK，如此之后，我们的项目就配置完成了。

接下来，重启 vhr 项目，进行多端登录测试，如果自己被人踢下线了，就会看到如下提示：

![Image](img/640-1729337667645.webp)

完整的代码，我已经更新到 vhr 上了，大家可以下载学习。

如果小伙伴们对松哥录制的 vhr 项目视频感兴趣，不妨看看这里：[微人事项目视频教程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488059&idx=1&sn=2ef3e7f14d262130ecab94a0b17de0ca&scene=21#wechat_redirect)

## 4.小结

好了，本文主要和小伙伴们介绍了一个在 Spring Security 中处理 session 并发问题时，可能遇到的一个坑，以及在前后端分离情况下，如何处理 session 并发问题。不知道小伙伴们有没有 GET 到呢？

本文第二小节的案例大家可以从 GitHub 上下载：https://github.com/lenve/spring-security-samples

如果觉得有收获，记得点个在看鼓励下松哥哦～





![Image](img/640-1729337667647.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

SpringSecurity38

SpringSecurity · 目录







上一篇【SpringSecurity系列（十二）】查看登录详情下一篇【SpringSecurity系列（十五）】请求防火墙默认已开启









# 