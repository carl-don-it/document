# 【SpringSecurity系列（三十）】Spring Security 中的四种权限控制方式

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年07月27日 10:36*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

------

Spring Security 中对于权限控制默认已经提供了很多了，但是，一个优秀的框架必须具备良好的扩展性，恰好，Spring Security 的扩展性就非常棒，我们既可以使用 Spring Security 提供的方式做授权，也可以自定义授权逻辑。一句话，你想怎么玩都可以！

今天松哥来和大家介绍一下 Spring Security 中四种常见的权限控制方式。

- 表达式控制 URL 路径权限
- 表达式控制方法权限
- 使用过滤注解
- 动态权限

四种方式，我们分别来看。

本文是 Spring Security 系列第 30 篇，阅读前面文章有助于更好的理解本文：

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
14. [【SpringSecurity系列（十四）】自动踢掉上一个登录用户](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493382&idx=2&sn=294bfe14613d5f97e817ee3612c6cf8c&scene=21#wechat_redirect)
15. [【SpringSecurity系列（十五）】请求防火墙默认已开启](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493432&idx=2&sn=d13c83bd0d5577b47aa8d78561de8ead&scene=21#wechat_redirect)
16. [【SpringSecurity系列（十六）】会话固定攻击与防御](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493486&idx=2&sn=2935be18e5fd8b3e3043cfad5dce5a35&scene=21#wechat_redirect)
17. [【SpringSecurity系列（十七）】Spring Security 如何处理 Session 共享](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493552&idx=2&sn=76eb35d59aea46f0f7095314f7d988a0&scene=21#wechat_redirect)
18. [【SpringSecurity系列（十八）】SpringBoot 如何防御 CSRF 攻击？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493615&idx=2&sn=dc132cfc80e07b709312a2567ec93678&scene=21#wechat_redirect)
19. [【SpringSecurity系列（十九）】Spring Security 中 CSRF 防御源码解析](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493641&idx=2&sn=5412022deeeef2b55edec9241d5fda0d&scene=21#wechat_redirect)
20. [【SpringSecurity系列（二十）】密码加密的两种姿势](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493759&idx=2&sn=14b4db11a1c0bcc3c2d53e3e7fb5a3c1&scene=21#wechat_redirect)
21. [【SpringSecurity系列（二十一）】Spring Security 怎么学？为什么一定需要系统学习？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493854&idx=2&sn=2fe65a75e6f4b2fdb96d4d117a0a53bd&scene=21#wechat_redirect)
22. [【SpringSecurity系列（二十二）】Spring Security 两种资源放行策略，千万别用错了！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493888&idx=2&sn=d05047d7943387cfc28bd5128bb08744&scene=21#wechat_redirect)
23. [【SpringSecurity系列（二十三）】手把手教你入门 Spring Boot + CAS 单点登录](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493947&idx=2&sn=041e9d4280eb1984d922106f0e876233&scene=21#wechat_redirect)
24. [【SpringSecurity系列（二十四）】SpringBoot+CAS单点登录](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247493986&idx=2&sn=e6b692239cac9e97944dbc2402773cbc&scene=21#wechat_redirect)
25. [【SpringSecurity系列（二十五）】CAS 单点登录对接数据库](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247494022&idx=2&sn=3aa5c41867c1232c32ca7a1d00812046&scene=21#wechat_redirect)
26. [【SpringSecurity系列（二十六）】Spring Boot+CAS 单点登录之自定义登录页面](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247494118&idx=2&sn=abde43b2397d1d508b33f1715b84dff7&scene=21#wechat_redirect)
27. [【SpringSecurity系列（二十七）】Swagger中怎么处理认证问题？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247494186&idx=2&sn=3ce3aff819982fc50db3b0edf65aaa2e&scene=21#wechat_redirect)
28. [【SpringSecurity系列（二十八）】当跨域遇上 Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247494235&idx=2&sn=142c5e0fb6bf964d50acf1d571682b24&scene=21#wechat_redirect)/
29. [【SpringSecurity系列（二十九）】Spring Security 实现 Http Basic 认证](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247494289&idx=2&sn=15fa13270ecc4917335bef81fc3a0ee1&scene=21#wechat_redirect)

## 1.表达式控制 URL 路径权限

首先我们来看第一种，就是通过表达式控制 URL 路径权限，这种方式松哥在之前的文章中实际上和大家讲过，这里我们再来稍微复习一下。

Spring Security 支持在 URL 和方法权限控制时使用 SpEL 表达式，如果表达式返回值为 true 则表示需要对应的权限，否则表示不需要对应的权限。提供表达式的类是 SecurityExpressionRoot：

![Image](img/640-1729403496866.webp)

可以看到，SecurityExpressionRoot 有两个实现类，表示在应对 URL 权限控制和应对方法权限控制时，分别对 SpEL 所做的拓展，例如在基于 URL 路径做权限控制时，增加了 hasIpAddress 选项。

我们来看下 SecurityExpressionRoot 类中定义的最基本的 SpEL 有哪些：

![Image](img/640-1729403497805.webp)

可以看到，这些都是该类对应的表达式，这些表达式我来给大家稍微解释下：

| 表达式               | 备注                                     |
| :------------------- | :--------------------------------------- |
| hasRole              | 用户具备某个角色即可访问资源             |
| hasAnyRole           | 用户具备多个角色中的任意一个即可访问资源 |
| hasAuthority         | 类似于 hasRole                           |
| hasAnyAuthority      | 类似于 hasAnyRole                        |
| permitAll            | 统统允许访问                             |
| denyAll              | 统统拒绝访问                             |
| isAnonymous          | 判断是否匿名用户                         |
| isAuthenticated      | 判断是否认证成功                         |
| isRememberMe         | 判断是否通过记住我登录的                 |
| isFullyAuthenticated | 判断是否用户名/密码登录的                |
| principle            | 当前用户                                 |
| authentication       | 从 SecurityContext 中提取出来的用户对象  |

这是最基本的，在它的继承类中，还有做一些拓展，我这个我就不重复介绍了。

如果是通过 URL 进行权限控制，那么我们只需要按照如下方式配置即可：

```
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .antMatchers("/admin/**").hasRole("admin")
            .antMatchers("/user/**").hasAnyRole("admin", "user")
            .anyRequest().authenticated()
            .and()
            ...
}
```

这里表示访问 `/admin/**` 格式的路径需要 admin 角色，访问 `/user/**` 格式的路径需要 admin 或者 user 角色。

## 2.表达式控制方法权限

当然，我们也可以通过在方法上添加注解来控制权限。

在方法上添加注解控制权限，需要我们首先开启注解的使用，在 Spring Security 配置类上添加如下内容：

```
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    ...
    ...
}
```

这个配置开启了三个注解，分别是：

- @PreAuthorize：方法执行前进行权限检查
- @PostAuthorize：方法执行后进行权限检查
- @Secured：类似于 @PreAuthorize

这三个结合 SpEL 之后，用法非常灵活，这里和大家稍微分享几个 Demo。

```
@Service
public class HelloService {
    @PreAuthorize("principal.username.equals('javaboy')")
    public String hello() {
        return "hello";
    }

    @PreAuthorize("hasRole('admin')")
    public String admin() {
        return "admin";
    }

    @Secured({"ROLE_user"})
    public String user() {
        return "user";
    }

    @PreAuthorize("#age>98")
    public String getAge(Integer age) {
        return String.valueOf(age);
    }
}
```

1. 第一个 hello 方法，注解的约束是，只有当前登录用户名为 javaboy 的用户才可以访问该方法。
2. 第二个 admin 方法，表示访问该方法的用户必须具备 admin 角色。
3. 第三个 user 方法，表示方法该方法的用户必须具备 user 角色，但是注意 user 角色需要加上 `ROLE_` 前缀。
4. 第四个 getAge 方法，表示访问该方法的 age 参数必须大于 98，否则请求不予通过。

可以看到，这里的表达式还是非常丰富，如果想引用方法的参数，前面加上一个 `#` 即可，既可以引用基本类型的参数，也可以引用对象参数。

缺省对象除了 principal ，还有 authentication（参考第一小节）。

## 3.使用过滤注解

Spring Security 中还有两个过滤函数 @PreFilter 和 @PostFilter，可以根据给出的条件，自动移除集合中的元素。

```
@PostFilter("filterObject.lastIndexOf('2')!=-1")
public List<String> getAllUser() {
    List<String> users = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        users.add("javaboy:" + i);
    }
    return users;
}
@PreFilter(filterTarget = "ages",value = "filterObject%2==0")
public void getAllAge(List<Integer> ages,List<String> users) {
    System.out.println("ages = " + ages);
    System.out.println("users = " + users);
}
```

- 在 getAllUser 方法中，对集合进行过滤，只返回后缀为 2 的元素，filterObject 表示要过滤的元素对象。
- 在 getAllAge 方法中，由于有两个集合，因此使用 filterTarget 指定过滤对象。

## 4.动态权限

动态权限主要通过重写拦截器和决策器来实现，这个我在 vhr 的文档中有过详细介绍，大家在公众号【江南一点雨】后台回复 888 可以获取文档，我就不再赘述了。

## 5.小结

好啦，今天就喝小伙伴们稍微聊了一下 Spring Security 中的授权问题，当然这里还有很多细节，后面松哥再和大家一一细聊。

如果小伙伴们觉得有收获，记得点个在看鼓励下松哥哦～





![Image](img/640-1729403496821.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

SpringSecurity38

SpringSecurity · 目录







上一篇【SpringSecurity系列（二十九）】Spring Security 实现 Http Basic 认证下一篇SpringSecurity系列（三十一）】Spring Security 如何实现多种加密方案共存









# 



























Scan to Follow