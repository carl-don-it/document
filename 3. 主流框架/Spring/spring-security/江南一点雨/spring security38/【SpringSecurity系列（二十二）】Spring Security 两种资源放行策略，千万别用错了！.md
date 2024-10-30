# 【SpringSecurity系列（二十二）】Spring Security 两种资源放行策略，千万别用错了！

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年06月23日 08:00*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



------

松哥原创的四套视频教程已经全部杀青，感兴趣的小伙伴戳这里-->[Spring Boot+Vue+微人事视频教程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488799&idx=1&sn=cdfd5315ff18c979b6f5d390ab4d9059&scene=21#wechat_redirect)

来看今天的正文。

事情的起因是这样，有小伙伴在微信上问了松哥一个问题：

![Image](img/640-1729344948352.webp)

就是他使用 Spring Security 做用户登录，等成功后，结果无法获取到登录用户信息，松哥之前写过相关的文章（[奇怪，Spring Security 登录成功后总是获取不到登录用户信息？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488150&idx=1&sn=7ce078ecccb42871d70f90d19beaa1a0&scene=21#wechat_redirect)），但是他似乎没有看懂。考虑到这是一个非常常见的问题，因此我想今天换个角度再来和大伙聊一聊这个话题。

Spring Security 中，到底该怎么样给资源额外放行？

## 1.两种思路

在 Spring Security 中，有一个资源，如果你希望用户不用登录就能访问，那么一般来说，你有两种配置策略：

第一种就是在 configure(WebSecurity web) 方法中配置放行，像下面这样：

```
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/css/**", "/js/**", "/index.html", "/img/**", "/fonts/**", "/favicon.ico", "/verifyCode");
}
```

第二种方式是在 configure(HttpSecurity http) 方法中进行配置：

```
http.authorizeRequests()
        .antMatchers("/hello").permitAll()
        .anyRequest().authenticated()
```

两种方式最大的区别在于，第一种方式是不走 Spring Security 过滤器链，而第二种方式走 Spring Security 过滤器链，在过滤器链中，给请求放行。

在我们使用 Spring Security 的时候，有的资源可以使用第一种方式额外放行，不需要验证，例如前端页面的静态资源，就可以按照第一种方式配置放行。

有的资源放行，则必须使用第二种方式，例如登录接口。大家知道，登录接口也是必须要暴露出来的，不需要登录就能访问到的，但是我们却不能将登录接口用第一种方式暴露出来，登录请求必须要走 Spring Security 过滤器链，因为在这个过程中，还有其他事情要做。

接下来我以登录接口为例，来和小伙伴们分析一下走 Spring Security 过滤器链有什么不同。

## 2.登录请求分析

首先大家知道，当我们使用 Spring Security，用户登录成功之后，有两种方式获取用户登录信息：

1. `SecurityContextHolder.getContext().getAuthentication()`
2. 在 Controller 的方法中，加入 Authentication 参数

这两种办法，都可以获取到当前登录用户信息。具体的操作办法，大家可以看看松哥之前发布的教程：[Spring Security 如何动态更新已登录用户信息？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488050&idx=1&sn=3cea9d8eb13d7bda1407b111e5c8ee45&scene=21#wechat_redirect)。

这两种方式获取到的数据都是来自 SecurityContextHolder，SecurityContextHolder 中的数据，本质上是保存在 `ThreadLocal` 中，`ThreadLocal` 的特点是存在它里边的数据，哪个线程存的，哪个线程才能访问到。

这样就带来一个问题，当用户登录成功之后，将用户用户数据存在 SecurityContextHolder 中（thread1），当下一个请求来的时候（thread2），想从 SecurityContextHolder 中获取用户登录信息，却发现获取不到！为啥？因为它俩不是同一个 Thread。

但实际上，正常情况下，我们使用 Spring Security 登录成功后，以后每次都能够获取到登录用户信息，这又是怎么回事呢？

这我们就要引入 Spring Security 中的 `SecurityContextPersistenceFilter` 了。

小伙伴们都知道，无论是 Spring Security 还是 Shiro，它的一系列功能其实都是由过滤器来完成的，在 Spring Security 中，松哥前面跟大家聊了 `UsernamePasswordAuthenticationFilter` 过滤器，在这个过滤器之前，还有一个过滤器就是 `SecurityContextPersistenceFilter`，请求在到达 `UsernamePasswordAuthenticationFilter` 之前都会先经过 `SecurityContextPersistenceFilter`。

我们来看下它的源码(部分)：

```
public class SecurityContextPersistenceFilter extends GenericFilterBean {
 public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
   throws IOException, ServletException {
  HttpServletRequest request = (HttpServletRequest) req;
  HttpServletResponse response = (HttpServletResponse) res;
  HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request,
    response);
  SecurityContext contextBeforeChainExecution = repo.loadContext(holder);
  try {
   SecurityContextHolder.setContext(contextBeforeChainExecution);
   chain.doFilter(holder.getRequest(), holder.getResponse());
  }
  finally {
   SecurityContext contextAfterChainExecution = SecurityContextHolder
     .getContext();
   SecurityContextHolder.clearContext();
   repo.saveContext(contextAfterChainExecution, holder.getRequest(),
     holder.getResponse());
  }
 }
}
```

原本的方法很长，我这里列出来了比较关键的几个部分：

1. SecurityContextPersistenceFilter 继承自 GenericFilterBean，而 GenericFilterBean 则是 Filter 的实现，所以 SecurityContextPersistenceFilter 作为一个过滤器，它里边最重要的方法就是 doFilter 了。
2. 在 doFilter 方法中，它首先会从 repo 中读取一个 SecurityContext 出来，这里的 repo 实际上就是 HttpSessionSecurityContextRepository，读取 SecurityContext 的操作会进入到 readSecurityContextFromSession 方法中，在这里我们看到了读取的核心方法 `Object contextFromSession = httpSession.getAttribute(springSecurityContextKey);`，这里的 springSecurityContextKey 对象的值就是 SPRING_SECURITY_CONTEXT，读取出来的对象最终会被转为一个 SecurityContext 对象。
3. SecurityContext 是一个接口，它有一个唯一的实现类 SecurityContextImpl，这个实现类其实就是用户信息在 session 中保存的 value。
4. 在拿到 SecurityContext 之后，通过 SecurityContextHolder.setContext 方法将这个 SecurityContext 设置到 ThreadLocal 中去，这样，在当前请求中，Spring Security 的后续操作，我们都可以直接从 SecurityContextHolder 中获取到用户信息了。
5. 接下来，通过 chain.doFilter 让请求继续向下走（这个时候就会进入到 `UsernamePasswordAuthenticationFilter` 过滤器中了）。
6. 在过滤器链走完之后，数据响应给前端之后，finally 中还有一步收尾操作，这一步很关键。这里从 SecurityContextHolder 中获取到 SecurityContext，获取到之后，会把 SecurityContextHolder 清空，然后调用 repo.saveContext 方法将获取到的 SecurityContext 存入 session 中。

至此，整个流程就很明了了。

每一个请求到达服务端的时候，首先从 session 中找出来 SecurityContext ，然后设置到 SecurityContextHolder 中去，方便后续使用，当这个请求离开的时候，SecurityContextHolder 会被清空，SecurityContext 会被放回 session 中，方便下一个请求来的时候获取。

登录请求来的时候，还没有登录用户数据，但是登录请求走的时候，会将用户登录数据存入 session 中，下个请求到来的时候，就可以直接取出来用了。

看了上面的分析，我们可以至少得出两点结论：

1. 如果我们暴露登录接口的时候，使用了前面提到的第一种方式，没有走 Spring Security，过滤器链，则在登录成功后，就不会将登录用户信息存入 session 中，进而导致后来的请求都无法获取到登录用户信息（后来的请求在系统眼里也都是未认证的请求）
2. 如果你的登录请求正常，走了 Spring Security 过滤器链，但是后来的 A 请求没走过滤器链（采用前面提到的第一种方式放行），那么 A 请求中，也是无法通过 SecurityContextHolder 获取到登录用户信息的，因为它一开始没经过 SecurityContextPersistenceFilter 过滤器链。

## 3.小结

总之，前端静态资源放行时，可以直接不走 Spring Security 过滤器链，像下面这样：

```
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/css/**","/js/**","/index.html","/img/**","/fonts/**","/favicon.ico");
}
```

后端的接口要额外放行，就需要仔细考虑场景了，不过一般来说，不建议使用上面这种方式，建议下面这种方式，原因前面已经说过了：

```
http.authorizeRequests()
        .antMatchers("/hello").permitAll()
        .anyRequest().authenticated()
```

好了，这就是和小伙伴们分享的两种资源放行策略，大家千万别搞错了哦～

有收获的话，记得点个在看鼓励下松哥哦～





![Image](img/640-1729344947429.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

SpringSecurity38

SpringSecurity · 目录







上一篇【SpringSecurity系列（二十）】Spring Security 怎么学？为什么一定需要系统学习？下一篇WebFlux 和 Spring Security 会碰出哪些火花？









# 