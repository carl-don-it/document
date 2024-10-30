# 奇怪，Spring Security 登录成功后总是获取不到登录用户信息？

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2020年04月01日 08:08*

今日干货

![Image](img/640-1729566997448.webp)

刚刚发表

查看:66666回复:666

公众号后台回复 ssm，免费获取松哥纯手敲的 SSM 框架学习干货。

有好几位小伙伴小伙伴曾向松哥求助过这个问题。

一开始我觉得这可能是一个小概率 BUG，但是当问的人多了，我觉得这个问题对于新手来说还有一定的普遍性，有必要来写篇文章跟大家仔细聊一聊这个问题，防止小伙伴们掉坑。





<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_lefttop"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_righttop"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_rightbot"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_leftbot"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>



，时长21:03









视频看完了，如果小伙伴们觉得松哥的视频风格还能接受，也可以看看松哥自制的 [Spring Boot + Vue 系列视频教程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488059&idx=1&sn=2ef3e7f14d262130ecab94a0b17de0ca&scene=21#wechat_redirect)

以下是视频笔记。

## 1.问题复现

如果使用了 Spring Security，当我们登录成功后，可以通过如下方式获取到当前登录用户信息：

1. SecurityContextHolder.getContext().getAuthentication()
2. 在 Controller 的方法中，加入 Authentication 参数

这两种办法，都可以获取到当前登录用户信息。具体的操作办法，大家可以看看松哥之前发布的教程：[Spring Security 如何动态更新已登录用户信息？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488050&idx=1&sn=3cea9d8eb13d7bda1407b111e5c8ee45&scene=21#wechat_redirect)。

正常情况下，我们通过如上两种方式的任意一种就可以获取到已经登录的用户信息。

异常情况，就是这两种方式中的任意一种，都返回 null。

都返回 null，意味着系统收到当前请求时并不知道你已经登录了（因为你没有在系统中留下任何有效信息），这会带来两个问题：

1. 无法获取到当前登录用户信息。
2. 当你发送任何请求，系统都会给你返回 401。

## 2.顺藤摸瓜

要弄明白这个问题，我们就得明白 Spring Security 中的用户信息到底是在哪里存的？

前面说了两种数据获取方式，但是这两种数据获取方式，获取到的数据又是从哪里来的？

首先松哥之前和大家聊过，SecurityContextHolder 中的数据，本质上是保存在 `ThreadLocal` 中，`ThreadLocal` 的特点是存在它里边的数据，哪个线程存的，哪个线程才能访问到。

这样就带来一个问题，当不同的请求进入到服务端之后，由不同的 thread 去处理，按理说后面的请求就可能无法获取到登录请求的线程存入的数据，例如登录请求在线程 A 中将登录用户信息存入 `ThreadLocal`，后面的请求来了，在线程 B 中处理，那此时就无法获取到用户的登录信息。

但实际上，正常情况下，我们每次都能够获取到登录用户信息，这又是怎么回事呢？

这我们就要引入 Spring Security 中的 `SecurityContextPersistenceFilter` 了。

小伙伴们都知道，无论是 Spring Security 还是 Shiro，它的一系列功能其实都是由过滤器来完成的，在 Spring Security 中，松哥前面跟大家聊了 `UsernamePasswordAuthenticationFilter` 过滤器，在这个过滤器之前，还有一个过滤器就是 `SecurityContextPersistenceFilter`，请求在到达 `UsernamePasswordAuthenticationFilter` 之前都会先经过 `SecurityContextPersistenceFilter`。

我们来看下它的源码(部分)：

```
publicclass SecurityContextPersistenceFilter extends GenericFilterBean {
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

搞明白这一点之后，再去解决 Spring Security 登录后无法获取到当前登录用户这个问题，就非常 easy 了。

## 3.问题解决

经过上面的分析之后，我们再来回顾一下为什么会发生登录之后无法获取到当前用户信息这样的事情？

最简单情况的就是你在一个新的线程中去执行 `SecurityContextHolder.getContext().getAuthentication()`，这肯定获取不到用户信息，无需多说。例如下面这样：

```
@GetMapping("/menu")
public List<Menu> getMenusByHrId() {
    new Thread(new Runnable() {
        @Override
        public void run() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(authentication);
        }
    }).start();
    return menuService.getMenusByHrId();
}
```

这种简单的问题相信大家都能够很容易排查到。

还有一种隐藏比较深的就是在 SecurityContextPersistenceFilter 的 doFilter 方法中没能从 session 中加载到用户信息，进而导致 SecurityContextHolder 里边空空如也。

在 SecurityContextPersistenceFilter 中没能加载到用户信息，原因可能就比较多了，例如：

- **「上一个请求临走的时候，没有将数据存储到 session 中去。」**
- **「当前请求自己没走过滤器链。」**

什么时候会发生这个问题呢？有的小伙伴可能在配置 SecurityConfig#configure(WebSecurity) 方法时，会忽略掉一个重要的点。

当我们想让 Spring Security 中的资源可以匿名访问时，我们有两种办法：

1. 不走 Spring Security 过滤器链。
2. 继续走 Spring Security 过滤器链，但是可以匿名访问。

这两种办法对应了两种不同的配置方式。其中第一种配置可能会影响到我们获取登录用户信息，第二种则不影响，所以这里我们来重点看看第一种。

不想走 Spring Security 过滤器链，我们一般可以通过如下方式配置：

```
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/css/**","/js/**","/index.html","/img/**","/fonts/**","/favicon.ico","/verifyCode");
}
```

正常这样配置是没有问题的。

如果你很不巧，把登录请求地址放进来了，那就 gg 了。虽然登录请求可以被所有人访问，但是不能放在这里（而应该通过允许匿名访问的方式来给请求放行）。**「如果放在这里，登录请求将不走 SecurityContextPersistenceFilter 过滤器，也就意味着不会将登录用户信息存入 session，进而导致后续请求无法获取到登录用户信息。」**

这也就是一开始小伙伴遇到的问题。

好了，小伙伴们如果在使用 Spring Security 时遇到类似问题，不妨按照本文提供的思路来解决一下。**「如果觉得有收获，记得点一下右下角在看哦」**

SpringSecurity系列52

SpringSecurity系列 · 目录







上一篇前后端分离中，使用 JSON 格式登录原来这么简单！下一篇Spring Boot + Spring Security 实现自动登录功能









# 