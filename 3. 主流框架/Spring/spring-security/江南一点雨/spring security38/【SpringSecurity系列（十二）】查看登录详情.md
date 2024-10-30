# 【SpringSecurity系列（十二）】查看登录详情

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年05月08日 10:36*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



------

[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493279&idx=2&sn=7c89d5a454487174a9ab86d6788b0c34&scene=21#wechat_redirect)跟大家聊了如何使用更加优雅的方式自定义 Spring Security 登录逻辑，更加优雅的方式可以有效避免掉自定义过滤器带来的低效，建议大家一定阅读一下，也可以顺便理解 Spring Security 中的认证逻辑。

本文将在上文的基础上，继续和大家探讨如何存储登录用户详细信息的问题。

本文是本系列第 12 篇，阅读本系列前面文章可以更好的理解本文：

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

好了，不废话了，我们来看今天的文章。

## 1.Authentication

Authentication 这个接口前面和大家聊过多次，今天还要再来聊一聊。

Authentication 接口用来保存我们的登录用户信息，实际上，它是对主体（java.security.Principal）做了进一步的封装。

我们来看下 Authentication 的一个定义：

```
public interface Authentication extends Principal, Serializable {
 Collection<? extends GrantedAuthority> getAuthorities();
 Object getCredentials();
 Object getDetails();
 Object getPrincipal();
 boolean isAuthenticated();
 void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
```

接口的解释如下：

1. getAuthorities 方法用来获取用户的权限。
2. getCredentials 方法用来获取用户凭证，一般来说就是密码。
3. getDetails 方法用来获取用户携带的详细信息，可能是当前请求之类的东西。
4. getPrincipal 方法用来获取当前用户，可能是一个用户名，也可能是一个用户对象。
5. isAuthenticated 当前用户是否认证成功。

这里有一个比较好玩的方法，叫做 getDetails。关于这个方法，源码的解释如下：

Stores additional details about the authentication request. These might be an IP address, certificate serial number etc.

从这段解释中，我们可以看出，该方法实际上就是用来存储有关身份认证的其他信息的，例如 IP 地址、证书信息等等。

实际上，在默认情况下，这里存储的就是用户登录的 IP 地址和 sessionId。我们从源码角度来看下。

## 2.源码分析

松哥的 SpringSecurity 系列已经写到第 12 篇了，看了前面的文章，相信大家已经明白用户登录必经的一个过滤器就是 UsernamePasswordAuthenticationFilter，在该类的 attemptAuthentication 方法中，对请求参数做提取，在 attemptAuthentication 方法中，会调用到一个方法，就是 setDetails。

我们一起来看下 setDetails 方法：

```
protected void setDetails(HttpServletRequest request,
  UsernamePasswordAuthenticationToken authRequest) {
 authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
}
```

UsernamePasswordAuthenticationToken 是 Authentication 的具体实现，所以这里实际上就是在设置 details，至于 details 的值，则是通过 authenticationDetailsSource 来构建的，我们来看下：

```
public class WebAuthenticationDetailsSource implements
  AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {
 public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
  return new WebAuthenticationDetails(context);
 }
}
public class WebAuthenticationDetails implements Serializable {
 private final String remoteAddress;
 private final String sessionId;
 public WebAuthenticationDetails(HttpServletRequest request) {
  this.remoteAddress = request.getRemoteAddr();

  HttpSession session = request.getSession(false);
  this.sessionId = (session != null) ? session.getId() : null;
 }
    //省略其他方法
}
```

默认通过 WebAuthenticationDetailsSource 来构建 WebAuthenticationDetails，并将结果设置到 Authentication 的 details 属性中去。而 WebAuthenticationDetails 中定义的属性，大家看一下基本上就明白，这就是保存了用户登录地址和 sessionId。

那么看到这里，大家基本上就明白了，用户登录的 IP 地址实际上我们可以直接从 WebAuthenticationDetails 中获取到。

我举一个简单例子，例如我们登录成功后，可以通过如下方式随时随地拿到用户 IP：

```
@Service
public class HelloService {
    public void hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        System.out.println(details);
    }
}
```

这个获取过程之所以放在 service 来做，就是为了演示**随时随地**这个特性。然后我们在 controller 中调用该方法，当访问接口时，可以看到如下日志：

```
WebAuthenticationDetails@fffc7f0c: RemoteIpAddress: 127.0.0.1; SessionId: 303C7F254DF8B86667A2B20AA0667160
```

可以看到，用户的 IP 地址和 SessionId 都给出来了。这两个属性在 WebAuthenticationDetails 中都有对应的 get 方法，也可以单独获取属性值。

## 3.定制

当然，WebAuthenticationDetails 也可以自己定制，因为默认它只提供了 IP 和 sessionid 两个信息，如果我们想保存关于 Http 请求的更多信息，就可以通过自定义 WebAuthenticationDetails 来实现。

如果我们要定制 WebAuthenticationDetails，还要连同 WebAuthenticationDetailsSource 一起重新定义。

结合[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493279&idx=2&sn=7c89d5a454487174a9ab86d6788b0c34&scene=21#wechat_redirect)的验证码登录，我跟大家演示一个自定义 WebAuthenticationDetails 的例子。

[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493279&idx=2&sn=7c89d5a454487174a9ab86d6788b0c34&scene=21#wechat_redirect)我们是在 MyAuthenticationProvider 类中进行验证码判断的，回顾一下[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493279&idx=2&sn=7c89d5a454487174a9ab86d6788b0c34&scene=21#wechat_redirect)的代码：

```
public class MyAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String code = req.getParameter("code");
        String verify_code = (String) req.getSession().getAttribute("verify_code");
        if (code == null || verify_code == null || !code.equals(verify_code)) {
            throw new AuthenticationServiceException("验证码错误");
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
```

不过这个验证操作，我们也可以放在自定义的 WebAuthenticationDetails 中来做，我们定义如下两个类：

```
public class MyWebAuthenticationDetails extends WebAuthenticationDetails {

    private boolean isPassed;

    public MyWebAuthenticationDetails(HttpServletRequest req) {
        super(req);
        String code = req.getParameter("code");
        String verify_code = (String) req.getSession().getAttribute("verify_code");
        if (code != null && verify_code != null && code.equals(verify_code)) {
            isPassed = true;
        }
    }

    public boolean isPassed() {
        return isPassed;
    }
}
@Component
public class MyWebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest,MyWebAuthenticationDetails> {
    @Override
    public MyWebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new MyWebAuthenticationDetails(context);
    }
}
```

首先我们定义 MyWebAuthenticationDetails，由于它的构造方法中，刚好就提供了 HttpServletRequest 对象，所以我们可以直接利用该对象进行验证码判断，并将判断结果交给 isPassed 变量保存。**如果我们想扩展属性，只需要在 MyWebAuthenticationDetails 中再去定义更多属性，然后从 HttpServletRequest 中提取出来设置给对应的属性即可，这样，在登录成功后就可以随时随地获取这些属性了。**

最后在 MyWebAuthenticationDetailsSource 中构造 MyWebAuthenticationDetails 并返回。

定义完成后，接下来，我们就可以直接在 MyAuthenticationProvider 中进行调用了：

```
public class MyAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (!((MyWebAuthenticationDetails) authentication.getDetails()).isPassed()) {
            throw new AuthenticationServiceException("验证码错误");
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
```

直接从 authentication 中获取到 details 并调用 isPassed 方法，有问题就抛出异常即可。

最后的问题就是如何用自定义的 MyWebAuthenticationDetailsSource 代替系统默认的 WebAuthenticationDetailsSource，很简单，我们只需要在 SecurityConfig 中稍作定义即可：

```
@Autowired
MyWebAuthenticationDetailsSource myWebAuthenticationDetailsSource;
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            ...
            .and()
            .formLogin()
            .authenticationDetailsSource(myWebAuthenticationDetailsSource)
            ...
}
```

将 MyWebAuthenticationDetailsSource 注入到 SecurityConfig 中，并在 formLogin 中配置 authenticationDetailsSource 即可成功使用我们自定义的 WebAuthenticationDetails。

这样自定义完成后，WebAuthenticationDetails 中原有的功能依然保留，也就是我们还可以利用老办法继续获取用户 IP 以及 sessionId 等信息，如下：

```
@Service
public class HelloService {
    public void hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyWebAuthenticationDetails details = (MyWebAuthenticationDetails) authentication.getDetails();
        System.out.println(details);
    }
}
```

这里类型强转的时候，转为 MyWebAuthenticationDetails 即可。

本文案例大家可以从 GitHub 上下载：https://github.com/lenve/spring-security-samples

好了，不知道小伙伴们有没有 GET 到呢？如果 GET 到了记得点个在看鼓励下松哥哦～





![Image](img/640-1729336240606.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

SpringSecurity38

SpringSecurity · 目录



























上一篇【SpringSecurity系列（九）】降低 RememberMe 的安全风险下一篇【SpringSecurity系列（十四）】自动踢掉上一个登录用户









# 



























Scan to Follow