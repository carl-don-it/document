# SpringSecurity系列（三十一）】Spring Security 如何实现多种加密方案共存

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年08月05日 10:36*

《深入浅出Spring Security》一书已由清华大学出版社正式出版发行，感兴趣的小伙伴戳这里[->->>深入浅出Spring Security](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247492459&idx=1&sn=a3ffb25873c0905b8862fcb8334a68e7&scene=21#wechat_redirect)，一本书学会 Spring Security。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



------

关于密码加密的问题，松哥之前已经和大家聊过了，参考：

- [Spring Boot 中密码加密的两种姿势！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488697&idx=1&sn=ce056ca96b2c5f0d6a83e67d1742a7c8&scene=21#wechat_redirect)

这篇文章中，松哥给大家介绍了两种密码加密方案，但是两种都是独立使用的！能不能在同一个项目中同时存在多种密码加密方案呢？答案是肯定的！

今天松哥就来和大家聊一聊，如何在 Spring Security 中，让多种不同的密码加密方案并存。

本文是 Spring Security 系列第 31 篇，阅读前面文章有助于更好的理解本文：

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
30. [【SpringSecurity系列（三十）】Spring Security 中的四种权限控制方式](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247494308&idx=2&sn=52350eef4e787146cb0c34019e3b4240&scene=21#wechat_redirect)

为什么要加密？常见的加密算法等等这些问题我就不再赘述了，大家可以参考之前的：[Spring Boot 中密码加密的两种姿势！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488697&idx=1&sn=ce056ca96b2c5f0d6a83e67d1742a7c8&scene=21#wechat_redirect)，咱们直接来看今天的正文。

## 1.PasswordEncoder

在 Spring Security 中，跟密码加密/校验相关的事情，都是由 PasswordEncoder 来主导的，PasswordEncoder 拥有众多的实现类：

![Image](img/640-1729403734573.webp)

这些实现类，有的已经过期了，有的用处不大。对于我们而言，最常用的莫过于 BCryptPasswordEncoder。

PasswordEncoder 本身是一个接口，里边只有三个方法：

```
public interface PasswordEncoder {
 String encode(CharSequence rawPassword);
 boolean matches(CharSequence rawPassword, String encodedPassword);
 default boolean upgradeEncoding(String encodedPassword) {
  return false;
 }
}
```

- encode 方法用来对密码进行加密。
- matches 方法用来对密码进行比对。
- upgradeEncoding 表示是否需要对密码进行再次加密以使得密码更加安全，默认为 false。

PasswordEncoder 的实现类，则具体实现了这些方法。

## 2.PasswordEncoder 在哪里起作用

对于我们开发者而言，我们通常都是在 SecurityConfig 中配置一个 PasswordEncoder 的实例，类似下面这样：

```
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

剩下的事情，都是由系统调用的。今天我们就来揭开系统调用的神秘面纱！我们一起来看下系统到底是怎么调用的！

首先，松哥在前面的文章中和大家提到过，Spring Security 中，如果使用用户名/密码的方式登录，密码是在 DaoAuthenticationProvider 中进行校验的，大家可以参考：[SpringSecurity 自定义认证逻辑的两种方式(高级玩法)](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488364&idx=1&sn=fd69d3f218e0b4ab4bad96ec623f5a33&scene=21#wechat_redirect)。

我们来看下 DaoAuthenticationProvider 中密码是如何校验的：

```
protected void additionalAuthenticationChecks(UserDetails userDetails,
  UsernamePasswordAuthenticationToken authentication)
  throws AuthenticationException {
 if (authentication.getCredentials() == null) {
  throw new BadCredentialsException(messages.getMessage(
    "AbstractUserDetailsAuthenticationProvider.badCredentials",
    "Bad credentials"));
 }
 String presentedPassword = authentication.getCredentials().toString();
 if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
  throw new BadCredentialsException(messages.getMessage(
    "AbstractUserDetailsAuthenticationProvider.badCredentials",
    "Bad credentials"));
 }
}
```

可以看到，密码校验就是通过 passwordEncoder.matches 方法来完成的。

那么 DaoAuthenticationProvider 中的 passwordEncoder 从何而来呢？是不是就是我们一开始在 SecurityConfig 中配置的那个 Bean 呢？

我们来看下 DaoAuthenticationProvider 中关于 passwordEncoder 的定义，如下：

```
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
 private PasswordEncoder passwordEncoder;
 public DaoAuthenticationProvider() {
  setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
 }
 public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
  this.passwordEncoder = passwordEncoder;
  this.userNotFoundEncodedPassword = null;
 }

 protected PasswordEncoder getPasswordEncoder() {
  return passwordEncoder;
 }
}
```

从这段代码中可以看到，在 DaoAuthenticationProvider 创建之时，就指定了 PasswordEncoder，似乎并没有用到我们一开始配置的 Bean？其实不是的！在 DaoAuthenticationProvider 创建之时，会制定一个默认的 PasswordEncoder，如果我们没有配置任何 PasswordEncoder，将使用这个默认的 PasswordEncoder，如果我们自定义了 PasswordEncoder 实例，那么会使用我们自定义的 PasswordEncoder 实例！

从何而知呢？

我们再来看看 DaoAuthenticationProvider 是怎么初始化的。

DaoAuthenticationProvider 的初始化是在 InitializeUserDetailsManagerConfigurer#configure 方法中完成的，我们一起来看下该方法的定义：

```
public void configure(AuthenticationManagerBuilder auth) throws Exception {
 if (auth.isConfigured()) {
  return;
 }
 UserDetailsService userDetailsService = getBeanOrNull(
   UserDetailsService.class);
 if (userDetailsService == null) {
  return;
 }
 PasswordEncoder passwordEncoder = getBeanOrNull(PasswordEncoder.class);
 UserDetailsPasswordService passwordManager = getBeanOrNull(UserDetailsPasswordService.class);
 DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
 provider.setUserDetailsService(userDetailsService);
 if (passwordEncoder != null) {
  provider.setPasswordEncoder(passwordEncoder);
 }
 if (passwordManager != null) {
  provider.setUserDetailsPasswordService(passwordManager);
 }
 provider.afterPropertiesSet();
 auth.authenticationProvider(provider);
}
```

从这段代码中我们可以看到：

1. 首先去调用 getBeanOrNull 方法获取一个 PasswordEncoder 实例，getBeanOrNull 方法实际上就是去 Spring 容器中查找对象。
2. 接下来直接 new 一个 DaoAuthenticationProvider 对象，大家知道，在 new 的过程中，DaoAuthenticationProvider 中默认的 PasswordEncoder 已经被创建出来了。
3. 如果一开始从 Spring 容器中获取到了 PasswordEncoder 实例，则将之赋值给 DaoAuthenticationProvider 实例，否则就是用 DaoAuthenticationProvider 自己默认创建的 PasswordEncoder。

至此，就真相大白了，我们配置的 PasswordEncoder 实例确实用上了。

## 3.默认的是什么？

同时大家看到，如果我们不进行任何配置，默认的 PasswordEncoder 也会被提供，那么默认的 PasswordEncoder 是什么呢？我们就从这个方法看起：

```
public DaoAuthenticationProvider() {
 setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
}
```

继续：

```
public class PasswordEncoderFactories {
 public static PasswordEncoder createDelegatingPasswordEncoder() {
  String encodingId = "bcrypt";
  Map<String, PasswordEncoder> encoders = new HashMap<>();
  encoders.put(encodingId, new BCryptPasswordEncoder());
  encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
  encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
  encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
  encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
  encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
  encoders.put("scrypt", new SCryptPasswordEncoder());
  encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
  encoders.put("SHA-256", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
  encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
  encoders.put("argon2", new Argon2PasswordEncoder());

  return new DelegatingPasswordEncoder(encodingId, encoders);
 }

 private PasswordEncoderFactories() {}
}
```

可以看到：

1. 在 PasswordEncoderFactories 中，首先构建了一个 encoders，然后给所有的编码方式都取了一个名字，再把名字做 key，编码方式做 value，统统存入 encoders 中。
2. 最后返回了一个 DelegatingPasswordEncoder 实例，同时传入默认的 encodingId 就是 bcrypt，以及 encoders 实例，DelegatingPasswordEncoder 看名字应该是一个代理对象。

我们来看下 DelegatingPasswordEncoder 的定义：

```
public class DelegatingPasswordEncoder implements PasswordEncoder {
 private static final String PREFIX = "{";
 private static final String SUFFIX = "}";
 private final String idForEncode;
 private final PasswordEncoder passwordEncoderForEncode;
 private final Map<String, PasswordEncoder> idToPasswordEncoder;
 private PasswordEncoder defaultPasswordEncoderForMatches = new UnmappedIdPasswordEncoder();
 public DelegatingPasswordEncoder(String idForEncode,
  Map<String, PasswordEncoder> idToPasswordEncoder) {
  if (idForEncode == null) {
   throw new IllegalArgumentException("idForEncode cannot be null");
  }
  if (!idToPasswordEncoder.containsKey(idForEncode)) {
   throw new IllegalArgumentException("idForEncode " + idForEncode + "is not found in idToPasswordEncoder " + idToPasswordEncoder);
  }
  for (String id : idToPasswordEncoder.keySet()) {
   if (id == null) {
    continue;
   }
   if (id.contains(PREFIX)) {
    throw new IllegalArgumentException("id " + id + " cannot contain " + PREFIX);
   }
   if (id.contains(SUFFIX)) {
    throw new IllegalArgumentException("id " + id + " cannot contain " + SUFFIX);
   }
  }
  this.idForEncode = idForEncode;
  this.passwordEncoderForEncode = idToPasswordEncoder.get(idForEncode);
  this.idToPasswordEncoder = new HashMap<>(idToPasswordEncoder);
 }
 public void setDefaultPasswordEncoderForMatches(
  PasswordEncoder defaultPasswordEncoderForMatches) {
  if (defaultPasswordEncoderForMatches == null) {
   throw new IllegalArgumentException("defaultPasswordEncoderForMatches cannot be null");
  }
  this.defaultPasswordEncoderForMatches = defaultPasswordEncoderForMatches;
 }

 @Override
 public String encode(CharSequence rawPassword) {
  return PREFIX + this.idForEncode + SUFFIX + this.passwordEncoderForEncode.encode(rawPassword);
 }

 @Override
 public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
  if (rawPassword == null && prefixEncodedPassword == null) {
   return true;
  }
  String id = extractId(prefixEncodedPassword);
  PasswordEncoder delegate = this.idToPasswordEncoder.get(id);
  if (delegate == null) {
   return this.defaultPasswordEncoderForMatches
    .matches(rawPassword, prefixEncodedPassword);
  }
  String encodedPassword = extractEncodedPassword(prefixEncodedPassword);
  return delegate.matches(rawPassword, encodedPassword);
 }

 private String extractId(String prefixEncodedPassword) {
  if (prefixEncodedPassword == null) {
   return null;
  }
  int start = prefixEncodedPassword.indexOf(PREFIX);
  if (start != 0) {
   return null;
  }
  int end = prefixEncodedPassword.indexOf(SUFFIX, start);
  if (end < 0) {
   return null;
  }
  return prefixEncodedPassword.substring(start + 1, end);
 }

 @Override
 public boolean upgradeEncoding(String prefixEncodedPassword) {
  String id = extractId(prefixEncodedPassword);
  if (!this.idForEncode.equalsIgnoreCase(id)) {
   return true;
  }
  else {
   String encodedPassword = extractEncodedPassword(prefixEncodedPassword);
   return this.idToPasswordEncoder.get(id).upgradeEncoding(encodedPassword);
  }
 }

 private String extractEncodedPassword(String prefixEncodedPassword) {
  int start = prefixEncodedPassword.indexOf(SUFFIX);
  return prefixEncodedPassword.substring(start + 1);
 }
 private class UnmappedIdPasswordEncoder implements PasswordEncoder {

  @Override
  public String encode(CharSequence rawPassword) {
   throw new UnsupportedOperationException("encode is not supported");
  }

  @Override
  public boolean matches(CharSequence rawPassword,
   String prefixEncodedPassword) {
   String id = extractId(prefixEncodedPassword);
   throw new IllegalArgumentException("There is no PasswordEncoder mapped for the id \"" + id + "\"");
  }
 }
}
```

这段代码比较长，我来和大家挨个解释下：

1. DelegatingPasswordEncoder 也是实现了 PasswordEncoder 接口，所以它里边的核心方法也是两个：encode 方法用来对密码进行编码，matches 方法用来校验密码。
2. 在 DelegatingPasswordEncoder 的构造方法中，通过 通过传入的两个参数 encodingId 和 encoders ，获取到默认的编码器赋值给 passwordEncoderForEncode，默认的编码器实际上就是 BCryptPasswordEncoder。
3. 在 encode 方法中对密码进行编码，但是编码的方式加了前缀，前缀是 `{编码器名称}` ，例如如果你使用 BCryptPasswordEncoder 进行编码，那么生成的密码就类似 `{bcrypt}$2a$10$oE39aG10kB/rFu2vQeCJTu/V/v4n6DRR0f8WyXRiAYvBpmadoOBE.`。这样有什么用呢？每种密码加密之后，都会加上一个前缀，这样看到前缀，就知道该密文是使用哪个编码器生成的了。
4. 最后 matches 方法的逻辑就很清晰了，先从密文中提取出来前缀，再根据前缀找到对应的 PasswordEncoder，然后再调用 PasswordEncoder 的 matches 方法进行密码比对。
5. 如果根据提取出来的前缀，找不到对应的 PasswordEncoder，那么就会调用 UnmappedIdPasswordEncoder#matches 方法，进行密码比对，该方法实际上并不会进行密码比对，而是直接抛出异常。

OK，至此，相信大家都明白了 DelegatingPasswordEncoder 的工作原理了。

如果我们想同时使用多个密码加密方案，看来使用 DelegatingPasswordEncoder 就可以了，而 DelegatingPasswordEncoder 默认还不用配置。

## 4.体验

接下来我们稍微体验一下 DelegatingPasswordEncoder 的用法。

首先我们来生成三个密码作为测试密码：

```
@Test
void contextLoads() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
    encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
    DelegatingPasswordEncoder encoder1 = new DelegatingPasswordEncoder("bcrypt", encoders);
    DelegatingPasswordEncoder encoder2 = new DelegatingPasswordEncoder("MD5", encoders);
    DelegatingPasswordEncoder encoder3 = new DelegatingPasswordEncoder("noop", encoders);
    String e1 = encoder1.encode("123");
    String e2 = encoder2.encode("123");
    String e3 = encoder3.encode("123");
    System.out.println("e1 = " + e1);
    System.out.println("e2 = " + e2);
    System.out.println("e3 = " + e3);
}
```

生成结果如下：

```
e1 = {bcrypt}$2a$10$Sb1gAUH4wwazfNiqflKZve4Ubh.spJcxgHG8Cp29DeGya5zsHENqi
e2 = {MD5}{Wucj/L8wMTMzFi3oBKWsETNeXbMFaHZW9vCK9mahMHc=}4d43db282b36d7f0421498fdc693f2a2
e3 = {noop}123
```

接下来，我们把这三个密码拷贝到 SecurityConfig 中去：

```
@Configuration("aaa")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @Bean
    protected UserDetailsService userDetailsService() {

        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("javaboy").password("{bcrypt}$2a$10$Sb1gAUH4wwazfNiqflKZve4Ubh.spJcxgHG8Cp29DeGya5zsHENqi").roles("admin").build());
        manager.createUser(User.withUsername("sang").password("{noop}123").roles("admin").build());
        manager.createUser(User.withUsername("江南一点雨").password("{MD5}{Wucj/L8wMTMzFi3oBKWsETNeXbMFaHZW9vCK9mahMHc=}4d43db282b36d7f0421498fdc693f2a2").roles("user").build());
        return manager;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("admin")
                .antMatchers("/user/**").hasRole("user")
                ...
    }
}
```

这里三个用户使用三种不同的密码加密方式。

配置完成后，重启项目，分别使用 javaboy/123、sang/123 以及 江南一点雨/123 进行登录，发现都能登录成功。

## 5.意义何在？

为什么我们会有这种需求？想在项目种同时存在多种密码加密方案？其实这个主要是针对老旧项目改造用的，密码加密方式一旦确定，基本上没法再改了（你总不能让用户重新注册一次吧），但是我们又想使用最新的框架来做密码加密，那么无疑，DelegatingPasswordEncoder 是最佳选择。

好啦，这就是今天和小伙伴们分享的多种密码加密方案问题，感兴趣的小伙伴记得点个在看鼓励下松哥哦～





![Image](img/640-1729403734548.webp)









加微信进群







一起切磋Web安全

（已添加松哥微信的小伙伴请勿重复添加）

SpringSecurity38

SpringSecurity · 目录













































上一篇【SpringSecurity系列（三十）】Spring Security 中的四种权限控制方式下一篇Spring Security 玩出花！两种方式 DIY 登录









# 



























Scan to Follow