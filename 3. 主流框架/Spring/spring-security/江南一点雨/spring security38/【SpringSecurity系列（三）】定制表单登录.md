# 【SpringSecurity系列（三）】定制表单登录

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年04月13日 10:37*

**《深入浅出Spring Security》**一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



**基本配置**





<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_lefttop"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_righttop"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_rightbot"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_leftbot"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>



，时长16:08









**部分源码**





<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_lefttop"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_righttop"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_rightbot"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>

<svg data-v-8b461723="" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2 2" width="4px" height="4px" class="border_filler border_filler_leftbot"><path data-v-8b461723="" d="M1.85.005A2 2 0 000 2V0h2z" fill="#ffffff" fill-rule="evenodd"></path></svg>



，时长04:46









视频看完了，如果小伙伴们觉得松哥的视频风格还能接受，也可以看看松哥自制的 [Spring Boot + Vue 系列视频教程](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488059&idx=1&sn=2ef3e7f14d262130ecab94a0b17de0ca&scene=21#wechat_redirect)

以下是视频笔记。

Spring Security 系列继续。

前面的视频+文章，松哥和大家简单聊了 Spring Security 的基本用法，并且我们一起自定义了一个登录页面，让登录看起来更炫一些！

今天我们来继续深入这个表单配置，挖掘一下这里边常见的其他配置。学习本文，强烈建议大家看一下前置知识（[松哥手把手带你入门 Spring Security，别再问密码怎么解密了](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488113&idx=1&sn=01168c492e22fa287043eb746950da73&scene=21#wechat_redirect)），学习效果更佳。

## 1.登录接口

很多初学者分不清登录接口和登录页面，这个我也很郁闷。我还是在这里稍微说一下。

登录页面就是你看到的浏览器展示出来的页面，像下面这个：

![Image](img/640-1729313881754.webp)

登录接口则是提交登录数据的地方，就是登录页面里边的 form 表单的 action 属性对应的值。

在 Spring Security 中，如果我们不做任何配置，默认的登录页面和登录接口的地址都是 `/login`，也就是说，默认会存在如下两个请求：

- GET http://localhost:8080/login
- POST http://localhost:8080/login

如果是 GET 请求表示你想访问登录页面，如果是 POST 请求，表示你想提交登录数据。

在[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488113&idx=1&sn=01168c492e22fa287043eb746950da73&scene=21#wechat_redirect)中，我们在 SecurityConfig 中自定定义了登录页面地址，如下：

```
.and()
.formLogin()
.loginPage("/login.html")
.permitAll()
.and()
```

当我们配置了 loginPage 为 `/login.html` 之后，这个配置从字面上理解，就是设置登录页面的地址为 `/login.html`。

实际上它还有一个隐藏的操作，就是登录接口地址也设置成 `/login.html` 了。换句话说，新的登录页面和登录接口地址都是 `/login.html`，现在存在如下两个请求：

- GET http://localhost:8080/login.html
- POST http://localhost:8080/login.html

前面的 GET 请求用来获取登录页面，后面的 POST 请求用来提交登录数据。

有的小伙伴会感到奇怪？为什么登录页面和登录接口不能分开配置呢？

其实是可以分开配置的！

在 SecurityConfig 中，我们可以通过 loginProcessingUrl 方法来指定登录接口地址，如下：

```
.and()
.formLogin()
.loginPage("/login.html")
.loginProcessingUrl("/doLogin")
.permitAll()
.and()
```

这样配置之后，登录页面地址和登录接口地址就分开了，各是各的。

此时我们还需要修改登录页面里边的 action 属性，改为 `/doLogin`，如下：

```
<form action="/doLogin" method="post">
<!--省略-->
</form>
```

此时，启动项目重新进行登录，我们发现依然可以登录成功。

那么为什么默认情况下两个配置地址是一样的呢？

我们知道，form 表单的相关配置在 FormLoginConfigurer 中，该类继承自 AbstractAuthenticationFilterConfigurer ，所以当 FormLoginConfigurer 初始化的时候，AbstractAuthenticationFilterConfigurer 也会初始化，在 AbstractAuthenticationFilterConfigurer 的构造方法中，我们可以看到：

```
protected AbstractAuthenticationFilterConfigurer() {
 setLoginPage("/login");
}
```

这就是配置默认的 loginPage 为 `/login`。

另一方面，FormLoginConfigurer 的初始化方法 init 方法中也调用了父类的 init 方法：

```
public void init(H http) throws Exception {
 super.init(http);
 initDefaultLoginFilter(http);
}
```

而在父类的 init 方法中，又调用了 updateAuthenticationDefaults，我们来看下这个方法：

```
protected final void updateAuthenticationDefaults() {
 if (loginProcessingUrl == null) {
  loginProcessingUrl(loginPage);
 }
 //省略
}
```

从这个方法的逻辑中我们就可以看到，如果用户没有给 loginProcessingUrl 设置值的话，默认就使用 loginPage 作为 loginProcessingUrl。

而如果用户配置了 loginPage，在配置完 loginPage 之后，updateAuthenticationDefaults 方法还是会被调用，此时如果没有配置 loginProcessingUrl，则使用新配置的 loginPage 作为 loginProcessingUrl。

好了，看到这里，相信小伙伴就明白了为什么一开始的登录接口和登录页面地址一样了。

## 2.登录参数

说完登录接口，我们再来说登录参数。

在[上篇文章](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488113&idx=1&sn=01168c492e22fa287043eb746950da73&scene=21#wechat_redirect)中，我们的登录表单中的参数是 username 和 password，注意，默认情况下，这个不能变：

```
<form action="/login.html" method="post">
    <input type="text" name="username" id="name">
    <input type="password" name="password" id="pass">
    <button type="submit">
      <span>登录</span>
    </button>
</form>
```

那么为什么是这样呢？

还是回到 FormLoginConfigurer 类中，在它的构造方法中，我们可以看到有两个配置用户名密码的方法：

```
public FormLoginConfigurer() {
 super(new UsernamePasswordAuthenticationFilter(), null);
 usernameParameter("username");
 passwordParameter("password");
}
```

在这里，首先 super 调用了父类的构造方法，传入了 UsernamePasswordAuthenticationFilter 实例，该实例将被赋值给父类的 authFilter 属性。

接下来 usernameParameter 方法如下：

```
public FormLoginConfigurer<H> usernameParameter(String usernameParameter) {
 getAuthenticationFilter().setUsernameParameter(usernameParameter);
 return this;
}
```

getAuthenticationFilter 实际上是父类的方法，在这个方法中返回了 authFilter 属性，也就是一开始设置的 UsernamePasswordAuthenticationFilter 实例，然后调用该实例的 setUsernameParameter 方法去设置登录用户名的参数：

```
public void setUsernameParameter(String usernameParameter) {
 this.usernameParameter = usernameParameter;
}
```

这里的设置有什么用呢？当登录请求从浏览器来到服务端之后，我们要从请求的 HttpServletRequest 中取出来用户的登录用户名和登录密码，怎么取呢？还是在 UsernamePasswordAuthenticationFilter 类中，有如下两个方法：

```
protected String obtainPassword(HttpServletRequest request) {
 return request.getParameter(passwordParameter);
}
protected String obtainUsername(HttpServletRequest request) {
 return request.getParameter(usernameParameter);
}
```

可以看到，这个时候，就用到默认配置的 username 和 password 了。

当然，这两个参数我们也可以自己配置，自己配置方式如下：

```
.and()
.formLogin()
.loginPage("/login.html")
.loginProcessingUrl("/doLogin")
.usernameParameter("name")
.passwordParameter("passwd")
.permitAll()
.and()
```

配置完成后，也要修改一下前端页面：

```
<form action="/doLogin" method="post">
    <div class="input">
        <label for="name">用户名</label>
        <input type="text" name="name" id="name">
        <span class="spin"></span>
    </div>
    <div class="input">
        <label for="pass">密码</label>
        <input type="password" name="passwd" id="pass">
        <span class="spin"></span>
    </div>
    <div class="button login">
        <button type="submit">
            <span>登录</span>
            <i class="fa fa-check"></i>
        </button>
    </div>
</form>
```

注意修改 input 的 name 属性值和服务端的对应。

配置完成后，重启进行登录测试。

## 3.登录回调

在登录成功之后，我们就要分情况处理了，大体上来说，无非就是分为两种情况：

- 前后端分离登录
- 前后端不分登录

两种情况的处理方式不一样。本文我们先来卡第二种前后端不分的登录，前后端分离的登录回调我在下篇文章中再来和大家细说。

### 3.1 登录成功回调

在 Spring Security 中，和登录成功重定向 URL 相关的方法有两个：

- defaultSuccessUrl
- successForwardUrl

这两个咋看没什么区别，实际上内藏乾坤。

首先我们在配置的时候，defaultSuccessUrl 和 successForwardUrl 只需要配置一个即可，具体配置哪个，则要看你的需求，两个的区别如下：

1. defaultSuccessUrl 有一个重载的方法，我们先说一个参数的 defaultSuccessUrl 方法。如果我们在 defaultSuccessUrl 中指定登录成功的跳转页面为 `/index`，此时分两种情况，如果你是直接在浏览器中输入的登录地址，登录成功后，就直接跳转到 `/index`，如果你是在浏览器中输入了其他地址，例如 `http://localhost:8080/hello`，结果因为没有登录，又重定向到登录页面，此时登录成功后，就不会来到 `/index` ，而是来到 `/hello` 页面。
2. defaultSuccessUrl 还有一个重载的方法，第二个参数如果不设置默认为 false，也就是我们上面的的情况，如果手动设置第二个参数为 true，则 defaultSuccessUrl 的效果和 successForwardUrl 一致。
3. successForwardUrl 表示不管你是从哪里来的，登录后一律跳转到 successForwardUrl 指定的地址。例如 successForwardUrl 指定的地址为 `/index` ，你在浏览器地址栏输入 `http://localhost:8080/hello`，结果因为没有登录，重定向到登录页面，当你登录成功之后，就会服务端跳转到 `/index` 页面；或者你直接就在浏览器输入了登录页面地址，登录成功后也是来到 `/index`。

相关配置如下：

```
.and()
.formLogin()
.loginPage("/login.html")
.loginProcessingUrl("/doLogin")
.usernameParameter("name")
.passwordParameter("passwd")
.defaultSuccessUrl("/index")
.successForwardUrl("/index")
.permitAll()
.and()
```

**注意：实际操作中，defaultSuccessUrl 和 successForwardUrl 只需要配置一个即可。**

### 3.2 登录失败回调

与登录成功相似，登录失败也是有两个方法：

- failureForwardUrl
- failureUrl

**这两个方法在设置的时候也是设置一个即可**。failureForwardUrl 是登录失败之后会发生服务端跳转，failureUrl 则在登录失败之后，会发生重定向。

## 4.注销登录

注销登录的默认接口是 `/logout`，我们也可以配置。

```
.and()
.logout()
.logoutUrl("/logout")
.logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST"))
.logoutSuccessUrl("/index")
.deleteCookies()
.clearAuthentication(true)
.invalidateHttpSession(true)
.permitAll()
.and()
```

注销登录的配置我来说一下：

1. 默认注销的 URL 是 `/logout`，是一个 GET 请求，我们可以通过 logoutUrl 方法来修改默认的注销 URL。
2. logoutRequestMatcher 方法不仅可以修改注销 URL，还可以修改请求方式，实际项目中，这个方法和 logoutUrl 任意设置一个即可。
3. logoutSuccessUrl 表示注销成功后要跳转的页面。
4. deleteCookies 用来清除 cookie。
5. clearAuthentication 和 invalidateHttpSession 分别表示清除认证信息和使 HttpSession 失效，默认可以不用配置，默认就会清除。

好了，今天就先说这么多，这块还剩一些前后端分离交互的，松哥在下篇文章再来和大家细说。

[![Image](img/640-1729313881807.webp)](http://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247491260&idx=1&sn=6e733419aca3b6f1814d832350d4080a&chksm=e9c34cdcdeb4c5ca697e3cb15e920bcf7c5144b6c7311de440227380d4caa1ddd6925f5e15e5&scene=21#wechat_redirect)





![Image](img/640-1729313881756.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

**如果感觉有收获，记得点一下右下角在看哦**

SpringSecurity38

SpringSecurity · 目录







上一篇【SpringSecurity系列（二）】Spring Security入门下一篇【SpringSecurity系列（四）】登录成功返回JSON数据









# 