# 【SpringSecurity系列（十三）】只允许一台设备在线

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年05月12日 10:36*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 636.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



------

登录成功后，自动踢掉前一个登录用户，松哥第一次见到这个功能，就是在扣扣里边见到的，当时觉得挺好玩的。

自己做开发后，也遇到过一模一样的需求，正好最近的 Spring Security 系列正在连载，就结合 Spring Security 来和大家聊一聊这个功能如何实现。

本文是本系列的第十三篇，阅读前面文章有助于更好的理解本文：

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

## 1.需求分析

在同一个系统中，我们可能只允许一个用户在一个终端上登录，一般来说这可能是出于安全方面的考虑，但是也有一些情况是出于业务上的考虑，松哥之前遇到的需求就是业务原因要求一个用户只能在一个设备上登录。

要实现一个用户不可以同时在两台设备上登录，我们有两种思路：

- 后来的登录自动踢掉前面的登录，就像大家在扣扣中看到的效果。
- 如果用户已经登录，则不允许后来者登录。

这种思路都能实现这个功能，具体使用哪一个，还要看我们具体的需求。

在 Spring Security 中，这两种都很好实现，一个配置就可以搞定。

## 2.具体实现

### 2.1 踢掉已经登录用户

想要用新的登录踢掉旧的登录，我们只需要将最大会话数设置为 1 即可，配置如下：

```
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginPage("/login.html")
            .permitAll()
            .and()
            .csrf().disable()
            .sessionManagement()
            .maximumSessions(1);
}
```

maximumSessions 表示配置最大会话数为 1，这样后面的登录就会自动踢掉前面的登录。这里其他的配置都是我们前面文章讲过的，我就不再重复介绍，文末可以下载案例完整代码。

配置完成后，分别用 Chrome 和 Firefox 两个浏览器进行测试（或者使用 Chrome 中的多用户功能）。

1. Chrome 上登录成功后，访问 /hello 接口。
2. Firefox 上登录成功后，访问 /hello 接口。
3. 在 Chrome 上再次访问 /hello 接口，此时会看到如下提示：

```
This session has been expired (possibly due to multiple concurrent logins being attempted as the same user).
```

可以看到，这里说这个 session 已经过期，原因则是由于使用同一个用户进行并发登录。

### 2.2 禁止新的登录

如果相同的用户已经登录了，你不想踢掉他，而是想禁止新的登录操作，那也好办，配置方式如下：

```
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginPage("/login.html")
            .permitAll()
            .and()
            .csrf().disable()
            .sessionManagement()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true);
}
```

添加 maxSessionsPreventsLogin 配置即可。此时一个浏览器登录成功后，另外一个浏览器就登录不了了。

是不是很简单？

不过还没完，我们还需要再提供一个 Bean：

```
@Bean
HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
```

为什么要加这个 Bean 呢？因为在 Spring Security 中，它是通过监听 session 的销毁事件，来及时的清理 session 的记录。用户从不同的浏览器登录后，都会有对应的 session，当用户注销登录之后，session 就会失效，但是默认的失效是通过调用 StandardSession#invalidate 方法来实现的，这一个失效事件无法被 Spring 容器感知到，进而导致当用户注销登录之后，Spring Security 没有及时清理会话信息表，以为用户还在线，进而导致用户无法重新登录进来（小伙伴们可以自行尝试不添加上面的 Bean，然后让用户注销登录之后再重新登录）。

为了解决这一问题，我们提供一个 HttpSessionEventPublisher ，这个类实现了 HttpSessionListener 接口，在该 Bean 中，可以将 session 创建以及销毁的事件及时感知到，并且调用 Spring 中的事件机制将相关的创建和销毁事件发布出去，进而被 Spring Security 感知到，该类部分源码如下：

```
public void sessionCreated(HttpSessionEvent event) {
 HttpSessionCreatedEvent e = new HttpSessionCreatedEvent(event.getSession());
 getContext(event.getSession().getServletContext()).publishEvent(e);
}
public void sessionDestroyed(HttpSessionEvent event) {
 HttpSessionDestroyedEvent e = new HttpSessionDestroyedEvent(event.getSession());
 getContext(event.getSession().getServletContext()).publishEvent(e);
}
```

OK，虽然多了一个配置，但是依然很简单！

## 3.实现原理

上面这个功能，在 Spring Security 中是怎么实现的呢？我们来稍微分析一下源码。

首先我们知道，在用户登录的过程中，会经过 UsernamePasswordAuthenticationFilter（参考：[松哥手把手带你捋一遍 Spring Security 登录流程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488026&idx=2&sn=3bd96d91e822abf753a8e91142e036be&scene=21#wechat_redirect)），而 UsernamePasswordAuthenticationFilter 中过滤方法的调用是在 AbstractAuthenticationProcessingFilter 中触发的，我们来看下 AbstractAuthenticationProcessingFilter#doFilter 方法的调用：

```
public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
  throws IOException, ServletException {
 HttpServletRequest request = (HttpServletRequest) req;
 HttpServletResponse response = (HttpServletResponse) res;
 if (!requiresAuthentication(request, response)) {
  chain.doFilter(request, response);
  return;
 }
 Authentication authResult;
 try {
  authResult = attemptAuthentication(request, response);
  if (authResult == null) {
   return;
  }
  sessionStrategy.onAuthentication(authResult, request, response);
 }
 catch (InternalAuthenticationServiceException failed) {
  unsuccessfulAuthentication(request, response, failed);
  return;
 }
 catch (AuthenticationException failed) {
  unsuccessfulAuthentication(request, response, failed);
  return;
 }
 // Authentication success
 if (continueChainBeforeSuccessfulAuthentication) {
  chain.doFilter(request, response);
 }
 successfulAuthentication(request, response, chain, authResult);
```

在这段代码中，我们可以看到，调用 attemptAuthentication 方法走完认证流程之后，回来之后，接下来就是调用 sessionStrategy.onAuthentication 方法，这个方法就是用来处理 session 的并发问题的。具体在：

```
public class ConcurrentSessionControlAuthenticationStrategy implements
  MessageSourceAware, SessionAuthenticationStrategy {
 public void onAuthentication(Authentication authentication,
   HttpServletRequest request, HttpServletResponse response) {

  final List<SessionInformation> sessions = sessionRegistry.getAllSessions(
    authentication.getPrincipal(), false);

  int sessionCount = sessions.size();
  int allowedSessions = getMaximumSessionsForThisUser(authentication);

  if (sessionCount < allowedSessions) {
   // They haven't got too many login sessions running at present
   return;
  }

  if (allowedSessions == -1) {
   // We permit unlimited logins
   return;
  }

  if (sessionCount == allowedSessions) {
   HttpSession session = request.getSession(false);

   if (session != null) {
    // Only permit it though if this request is associated with one of the
    // already registered sessions
    for (SessionInformation si : sessions) {
     if (si.getSessionId().equals(session.getId())) {
      return;
     }
    }
   }
   // If the session is null, a new one will be created by the parent class,
   // exceeding the allowed number
  }

  allowableSessionsExceeded(sessions, allowedSessions, sessionRegistry);
 }
 protected void allowableSessionsExceeded(List<SessionInformation> sessions,
   int allowableSessions, SessionRegistry registry)
   throws SessionAuthenticationException {
  if (exceptionIfMaximumExceeded || (sessions == null)) {
   throw new SessionAuthenticationException(messages.getMessage(
     "ConcurrentSessionControlAuthenticationStrategy.exceededAllowed",
     new Object[] {allowableSessions},
     "Maximum sessions of {0} for this principal exceeded"));
  }

  // Determine least recently used sessions, and mark them for invalidation
  sessions.sort(Comparator.comparing(SessionInformation::getLastRequest));
  int maximumSessionsExceededBy = sessions.size() - allowableSessions + 1;
  List<SessionInformation> sessionsToBeExpired = sessions.subList(0, maximumSessionsExceededBy);
  for (SessionInformation session: sessionsToBeExpired) {
   session.expireNow();
  }
 }
}
```

这段核心代码我来给大家稍微解释下：

1. 首先调用 sessionRegistry.getAllSessions 方法获取当前用户的所有 session，该方法在调用时，传递两个参数，一个是当前用户的 authentication，另一个参数 false 表示不包含已经过期的 session（在用户登录成功后，会将用户的 sessionid 存起来，其中 key 是用户的主体（principal），value 则是该主题对应的 sessionid 组成的一个集合）。
2. 接下来计算出当前用户已经有几个有效 session 了，同时获取允许的 session 并发数。
3. 如果当前 session 数（sessionCount）小于 session 并发数（allowedSessions），则不做任何处理；如果 allowedSessions 的值为 -1，表示对 session 数量不做任何限制。
4. 如果当前 session 数（sessionCount）等于 session 并发数（allowedSessions），那就先看看当前 session 是否不为 null，并且已经存在于 sessions 中了，如果已经存在了，那都是自家人，不做任何处理；如果当前 session 为 null，那么意味着将有一个新的 session 被创建出来，届时当前 session 数（sessionCount）就会超过 session 并发数（allowedSessions）。
5. 如果前面的代码中都没能 return 掉，那么将进入策略判断方法 allowableSessionsExceeded 中。
6. allowableSessionsExceeded 方法中，首先会有 exceptionIfMaximumExceeded 属性，这就是我们在 SecurityConfig 中配置的 maxSessionsPreventsLogin 的值，默认为 false，如果为 true，就直接抛出异常，那么这次登录就失败了（对应 2.2 小节的效果），如果为 false，则对 sessions 按照请求时间进行排序，然后再使多余的 session 过期即可（对应 2.1 小节的效果）。

## 4.小结

如此，两行简单的配置就实现了 Spring Security 中 session 的并发管理。是不是很简单？不过这里还有一个小小的坑，松哥将在下篇文章中继续和大家分析。

本文案例大家可以从 GitHub 上下载：https://github.com/lenve/spring-security-samples

好了，不知道小伙伴们有没有 GET 到呢？如果 GET 到了记得点个在看鼓励下松哥哦～





![Image](img/640-1729336841134.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）









# 