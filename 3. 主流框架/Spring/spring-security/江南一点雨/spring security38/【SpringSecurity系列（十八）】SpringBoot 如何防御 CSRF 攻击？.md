# 【SpringSecurity系列（十八）】SpringBoot 如何防御 CSRF 攻击？

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年06月03日 10:37*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



------

CSRF 就是跨域请求伪造，英文全称是 Cross Site Request Forgery。

这是一种非常常见的 Web 攻击方式，其实是很好防御的，但是由于经常被很多开发者忽略，进而导致很多网站实际上都存在 CSRF 攻击的安全隐患。

今天松哥就来和大家聊一聊什么是 CSRF 攻击以及 CSRF 攻击该如何防御。

本文是本系列第 18 篇，阅读本系列前面文章有助于更好的理解本文：

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

## 1.CSRF原理

想要防御 CSRF 攻击，那我们得先搞清楚什么是 CSRF 攻击，松哥通过下面一张图，来和大家梳理 CSRF 攻击流程：

![Image](img/640-1729339124474.webp)

其实这个流程很简单：

1. 假设用户打开了招商银行网上银行网站，并且登录。
2. 登录成功后，网上银行会返回 Cookie 给前端，浏览器将 Cookie 保存下来。
3. 用户在没有登出网上银行的情况下，在浏览器里边打开了一个新的选项卡，然后又去访问了一个危险网站。
4. 这个危险网站上有一个超链接，超链接的地址指向了招商银行网上银行。
5. 用户点击了这个超链接，由于这个超链接会自动携带上浏览器中保存的 Cookie，所以用户不知不觉中就访问了网上银行，进而可能给自己造成了损失。

CSRF 的流程大致就是这样，接下来松哥用一个简单的例子和小伙伴们展示一下 CSRF 到底是怎么回事。

## 2.CSRF实践

接下来，我创建一个名为 csrf-1 的 Spring Boot 项目，这个项目相当于我们上面所说的网上银行网站，创建项目时引入 Web 和 Spring Security 依赖，如下：

![Image](img/640-1729339124370.webp)

创建成功后，方便起见，我们直接将 Spring Security 用户名/密码 配置在 application.properties 文件中：

```
spring.security.user.name=javaboy
spring.security.user.password=123
```

然后我们提供两个测试接口：

```
@RestController
public class HelloController {
    @PostMapping("/transfer")
    public void transferMoney(String name, Integer money) {
        System.out.println("name = " + name);
        System.out.println("money = " + money);
    }
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
```

假设 `/transfer` 是一个转账接口（这里是假设，主要是给大家演示 CSRF 攻击，真实的转账接口比这复杂）。

最后我们还需要配置一下 Spring Security，因为 Spring Security 中默认是可以自动防御 CSRF 攻击的，所以我们要把这个关闭掉：

```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .csrf()
                .disable();
    }
}
```

配置完成后，我们启动 csrf-1 项目。

接下来，我们再创建一个 csrf-2 项目，这个项目相当于是一个危险网站，为了方便，这里创建时我们只需要引入 web 依赖即可。

项目创建成功后，首先修改项目端口：

```
server.port=8081
```

然后我们在 resources/static 目录下创建一个 hello.html ，内容如下：

```
<body>
<form action="http://localhost:8080/transfer" method="post">
    <input type="hidden" value="javaboy" name="name">
    <input type="hidden" value="10000" name="money">
    <input type="submit" value="点击查看美女图片">
</form>
</body>
```

这里有一个超链接，超链接的文本是**点击查看美女图片**，当你点击了超链接之后，会自动请求 `http://localhost:8080/transfer` 接口，同时隐藏域还携带了两个参数。

配置完成后，就可以启动 csrf-2 项目了。

接下来，用户首先访问 csrf-1 项目中的接口，在访问的时候需要登录，用户就执行了登录操作，访问完整后，用户并没有执行登出操作，然后用户访问 csrf-2 中的页面，看到了超链接，好奇这美女到底长啥样，一点击，结果钱就被人转走了。

## 3.CSRF防御

先来说说防御思路。

CSRF 防御，一个核心思路就是在前端请求中，添加一个随机数。

因为在 CSRF 攻击中，黑客网站其实是不知道用户的 Cookie 具体是什么的，他是让用户自己发送请求到网上银行这个网站的，因为这个过程会自动携带上 Cookie 中的信息。

所以我们的防御思路是这样：用户在访问网上银行时，除了携带 Cookie 中的信息之外，还需要携带一个随机数，如果用户没有携带这个随机数，则网上银行网站会拒绝该请求。黑客网站诱导用户点击超链接时，会自动携带上 Cookie 中的信息，但是却不会自动携带随机数，这样就成功的避免掉 CSRF 攻击了。

Spring Security 中对此提供了很好的支持，我们一起来看下。

### 3.1 默认方案

Spring Security 中默认实际上就提供了 csrf 防御，但是需要开发者做的事情比较多。

首先我们来创建一个新的 Spring Boot 工程，创建时引入 Spring Security、Thymeleaf 和 web 依赖。

![Image](img/640-1729339124505.webp)

项目创建成功后，我们还是在 application.properties 中配置用户名/密码：

```
spring.security.user.name=javaboy
spring.security.user.password=123
```

接下来，我们提供一个测试接口：

```
@Controller
public class HelloController {
    @PostMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }
}
```

注意，这个测试接口是一个 POST 请求，因为默认情况下，GET、HEAD、TRACE 以及 OPTIONS 是不需要验证 CSRF 攻击的。

然后，我们在 resources/templates 目录下，新建一个 thymeleaf 模版，如下：

```
<body>
<form action="/hello" method="post">
    <input type="hidden" th:value="${_csrf.token}" th:name="${_csrf.parameterName}">
    <input type="submit" value="hello">
</form>
</body>
```

注意，在发送 POST 请求的时候，还额外携带了一个隐藏域，隐藏域的 key 是 `${_csrf.parameterName}`，value 则是 `${_csrf.token}`。

这两个值服务端会自动带过来，我们只需要在前端渲染出来即可。

接下来给前端 hello.html 页面添加一个控制器，如下：

```
@GetMapping("/hello")
public String hello2() {
    return "hello";
}
```

添加完成后，启动项目，我们访问 hello 页面，在访问时候，需要先登录，登录成功之后，我们可以看到登录请求中也多了一个参数，如下：

![Image](img/640-1729339123759.webp)

可以看到，这里也多了 `_csrf` 参数。

这里我们用了 Spring Security 的默认登录页面，如果大家使用自定义登录页面，可以参考上面 hello.html 的写法，通过一个隐藏域传递 `_csrf` 参数。

访问到 hello 页面之后，再去点击按钮，就可以访问到 hello 接口了。

> ❝
>
> 小伙伴们可以自行尝试在 hello.html 页面中，去掉 `_csrf` 参数，看看访问 hello 接口的效果。

这是 Spring Security 中默认的方案，通过 Model 将相关的数据带到前端来。

如果你的项目是前后端不分项目，这种方案就可以了，如果你的项目是前后端分离项目，这种方案很明显不够用。

### 3.2 前后端分离方案

如果是前后端分离项目，Spring Security 也提供了解决方案。

这次不是将 `_csrf` 放在 Model 中返回前端了，而是放在 Cookie 中返回前端，配置方式如下：

```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}
```

有小伙伴可能会说放在 Cookie 中不是又被黑客网站盗用了吗？其实不会的，大家注意如下两个问题：

1. 黑客网站根本不知道你的 Cookie 里边存的啥，他也不需要知道，因为 CSRF 攻击是浏览器自动携带上 Cookie 中的数据的。
2. 我们将服务端生成的随机数放在 Cookie 中，前端需要从 Cookie 中自己提取出来 `_csrf` 参数，然后拼接成参数传递给后端，单纯的将 Cookie 中的数据传到服务端是没用的。

理解透了上面两点，你就会发现 `_csrf` 放在 Cookie 中是没有问题的，但是大家注意，配置的时候我们通过 withHttpOnlyFalse 方法获取了 CookieCsrfTokenRepository 的实例，该方法会设置 Cookie 中的 HttpOnly 属性为 false，也就是允许前端通过 js 操作 Cookie（否则你就没有办法获取到 `_csrf`）。

配置完成后，重启项目，此时我们就发现返回的 Cookie 中多了一项：

![Image](img/640-1729339123766.webp)

接下来，我们通过自定义登录页面，来看看前端要如何操作。

首先我们在 resources/static 目录下新建一个 html 页面叫做 login.html：

```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <script src="js/jquery.min.js"></script>
    <script src="js/jquery.cookie.js"></script>
</head>
<body>
<div>
    <input type="text" id="username">
    <input type="password" id="password">
    <input type="button" value="登录" id="loginBtn">
</div>
<script>
    $("#loginBtn").click(function () {
        let _csrf = $.cookie('XSRF-TOKEN');
        $.post('/login.html',{username:$("#username").val(),password:$("#password").val(),_csrf:_csrf},function (data) {
            alert(data);
        })
    })
</script>
</body>
</html>
```

这段 html 我给大家解释下：

1. 首先引入 jquery 和 jquery.cookie ，方便我们一会操作 Cookie。
2. 定义三个 input，前两个是用户名和密码，第三个是登录按钮。
3. 点击登录按钮之后，我们先从 Cookie 中提取出 XSRF-TOKEN，这也就是我们要上传的 csrf 参数。
4. 通过一个 POST 请求执行登录操作，注意携带上 `_csrf` 参数。

服务端我们也稍作修改，如下：

```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login.html")
                .successHandler((req,resp,authentication)->{
                    resp.getWriter().write("success");
                })
                .permitAll()
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}
```

一方面这里给 js 文件放行。

另一方面配置一下登录页面，以及登录成功的回调，这里简单期间，登录成功的回调我就给一个字符串就可以了。大家感兴趣的话，可以查看[本系列](https://mp.weixin.qq.com/mp/appmsgalbum?action=getalbum&album_id=1319828555819286528&__biz=MzI1NDY0MTkzNQ==#wechat_redirect)前面文章，有登录成功后回调的详细解释。

OK，所有事情做完之后，我们访问 login.html 页面，输入用户名密码进行登录，结果如下：

![Image](img/640-1729339123762.webp)

可以看到，我们的 `_csrf` 配置已经生效了。

> ❝
>
> 小伙伴们可以自行尝试从登录参数中去掉 `_csrf`，然后再看看效果。

## 4.小结

好了，今天主要和小伙伴们介绍了 csrf 攻击以及如何防御的问题。大家看到，csrf 攻击主要是借助了浏览器默认发送 Cookie 的这一机制，所以如果你的前端是 App、小程序之类的应用，不涉及浏览器应用的话，其实可以忽略这个问题，如果你的前端包含浏览器应用的话，这个问题就要认真考虑了。

好了 ，本文就说到这里，本文相关案例我已经上传到 GitHub ，大家可以自行下载:https://github.com/lenve/spring-security-samples

好啦，不知道小伙伴们有没有 GET 到呢？如果有收获，记得点个在看鼓励下松哥哦～





![Image](img/640-1729339123763.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

SpringSecurity38

SpringSecurity · 目录







上一篇【SpringSecurity系列（十七）】Spring Security 如何处理 Session 共享下一篇【SpringSecurity系列（十九）】Spring Security 中 CSRF 防御源码解析









# 