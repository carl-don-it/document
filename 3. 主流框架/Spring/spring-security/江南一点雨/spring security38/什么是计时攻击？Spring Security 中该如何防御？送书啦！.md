# 什么是计时攻击？Spring Security 中该如何防御？送书啦！

Original 江南一点雨 [江南一点雨](javascript:void(0);) *2021年11月23日 15:16*

松哥最近在研究 Spring Security 源码，发现了很多好玩的代码，抽空写几篇文章和小伙伴们分享一下。

很多人吐槽 Spring Security 比 Shiro 重量级，这个重量级不是凭空来的，重量有重量的好处，就是它提供了更为强大的防护功能。

比如松哥最近看到的一段代码：

```
protected final UserDetails retrieveUser(String username,
  UsernamePasswordAuthenticationToken authentication)
  throws AuthenticationException {
 prepareTimingAttackProtection();
 try {
  UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
  if (loadedUser == null) {
   throw new InternalAuthenticationServiceException(
     "UserDetailsService returned null, which is an interface contract violation");
  }
  return loadedUser;
 }
 catch (UsernameNotFoundException ex) {
  mitigateAgainstTimingAttack(authentication);
  throw ex;
 }
 catch (InternalAuthenticationServiceException ex) {
  throw ex;
 }
 catch (Exception ex) {
  throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
 }
}
```

这段代码位于 DaoAuthenticationProvider 类中，为了方便大家理解，我来简单说下这段代码的上下文环境。

当用户提交用户名密码登录之后，Spring Security 需要根据用户提交的用户名去数据库中查询用户，这块如果大家不熟悉，可以参考松哥之前的文章：

1. [Spring Security 如何将用户数据存入数据库？](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488194&idx=1&sn=7103031896ba8b9d34095524b292265e&scene=21#wechat_redirect)
2. [Spring Security+Spring Data Jpa 强强联手，安全管理只有更简单！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488229&idx=1&sn=2911c04bf19d41b00b4933d4044590f8&scene=21#wechat_redirect)

查到用户对象之后，再去比对从数据库中查到的用户密码和用户提交的密码之间的差异。具体的比对工作，可以参考[Spring Boot 中密码加密的两种姿势！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488697&idx=1&sn=ce056ca96b2c5f0d6a83e67d1742a7c8&scene=21#wechat_redirect)一文。

而上面这段代码就是 Spring Security 根据用户登录时传入的用户名去数据库中查询用户，并将查到的用户返回。方法中还有一个 authentication 参数，这个参数里边保存了用户登录时传入的用户名/密码信息。

那么这段代码有什么神奇之处呢？

我们来一行一行分析。

## 源码梳理

### 1

首先方法一进来调用了 prepareTimingAttackProtection 方法，从方法名字上可以看出，这个是为计时攻击的防御做准备，那么什么又是计时攻击呢？别急，松哥一会来解释。我们先来吧流程走完。prepareTimingAttackProtection 方法的执行很简单，如下：

```
private void prepareTimingAttackProtection() {
 if (this.userNotFoundEncodedPassword == null) {
  this.userNotFoundEncodedPassword = this.passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
 }
}
```

该方法就是将常量 USER_NOT_FOUND_PASSWORD 使用 passwordEncoder 编码之后（如果不了解 passwordEncoder，可以参考 [Spring Boot 中密码加密的两种姿势！](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488697&idx=1&sn=ce056ca96b2c5f0d6a83e67d1742a7c8&scene=21#wechat_redirect)一文），将编码结果赋值给 userNotFoundEncodedPassword 变量。

### 2

接下来调用 loadUserByUsername 方法，根据登录用户传入的用户名去数据库中查询用户，如果查到了，就将查到的对象返回。

### 3

如果查询过程中抛出 UsernameNotFoundException 异常，按理说直接抛出异常，接下来的密码比对也不用做了，因为根据用户名都没查到用户，这次登录肯定是失败的，没有必要进行密码比对操作！

但是大家注意，在抛出异常之前调用了 mitigateAgainstTimingAttack 方法。这个方法从名字上来看，有缓解计时攻击的意思。

我们来看下该方法的执行流程：

```
private void mitigateAgainstTimingAttack(UsernamePasswordAuthenticationToken authentication) {
 if (authentication.getCredentials() != null) {
  String presentedPassword = authentication.getCredentials().toString();
  this.passwordEncoder.matches(presentedPassword, this.userNotFoundEncodedPassword);
 }
}
```

可以看到，这里首先获取到登录用户传入的密码即 presentedPassword，然后调用 passwordEncoder.matches 方法进行密码比对操作，本来该方法的第二个参数是数据库查询出来的用户密码，现在数据库中没有查到用户，所以第二个参数用 userNotFoundEncodedPassword 代替了，userNotFoundEncodedPassword 就是我们一开始调用 prepareTimingAttackProtection 方法时赋值的变量。这个密码比对，从一开始就注定了肯定会失败，那为什么还要比对呢？

## 计时攻击

这就引入了我们今天的主题--计时攻击。

计时攻击是旁路攻击的一种，在密码学中，旁道攻击又称侧信道攻击、边信道攻击（Side-channel attack）。

这种攻击方式并非利用加密算法的理论弱点，也不是暴力破解，而是从密码系统的物理实现中获取的信息。例如：时间信息、功率消耗、电磁泄露等额外的信息源，这些信息可被用于对系统的进一步破解。

旁路攻击有多种不同的分类：

- 缓存攻击（Cache Side-Channel Attacks），通过获取对缓存的访问权而获取缓存内的一些敏感信息，例如攻击者获取云端主机物理主机的访问权而获取存储器的访问权。
- **计时攻击（Timing attack），通过设备运算的用时来推断出所使用的运算操作，或者通过对比运算的时间推定数据位于哪个存储设备，或者利用通信的时间差进行数据窃取。**
- 基于功耗监控的旁路攻击，同一设备不同的硬件电路单元的运作功耗也是不一样的，因此一个程序运行时的功耗会随着程序使用哪一种硬件电路单元而变动，据此推断出数据输出位于哪一个硬件单元，进而窃取数据。
- 电磁攻击（Electromagnetic attack），设备运算时会泄漏电磁辐射，经过得当分析的话可解析出这些泄漏的电磁辐射中包含的信息（比如文本、声音、图像等），这种攻击方式除了用于密码学攻击以外也被用于非密码学攻击等窃听行为，如TEMPEST 攻击。
- 声学密码分析（Acoustic cryptanalysis），通过捕捉设备在运算时泄漏的声学信号捉取信息（与功率分析类似）。
- 差别错误分析，隐密数据在程序运行发生错误并输出错误信息时被发现。
- 数据残留（Data remanence），可使理应被删除的敏感数据被读取出来（例如冷启动攻击）。
- 软件初始化错误攻击，现时较为少见，行锤攻击（Row hammer）是该类攻击方式的一个实例，在这种攻击实现中，被禁止访问的存储器位置旁边的存储器空间如果被频繁访问将会有状态保留丢失的风险。
- 光学方式，即隐密数据被一些视觉光学仪器（如高清晰度相机、高清晰度摄影机等设备）捕捉。

所有的攻击类型都利用了加密/解密系统在进行加密/解密操作时算法逻辑没有被发现缺陷，但是通过物理效应提供了有用的额外信息（这也是称为“旁路”的缘由），而这些物理信息往往包含了密钥、密码、密文等隐密数据。

而上面 Spring Security 中的那段代码就是为了防止计时攻击。

具体是怎么做的呢？假设 Spring Security 从数据库中没有查到用户信息就直接抛出异常了，没有去执行 mitigateAgainstTimingAttack 方法，那么黑客经过大量的测试，再经过统计分析，就会发现有一些登录验证耗时明显少于其他登录，进而推断出登录验证时间较短的都是不存在的用户，而登录耗时较长的是数据库中存在的用户。

现在 Spring Security 中，通过执行 mitigateAgainstTimingAttack 方法，无论用户存在或者不存在，登录校验的耗时不会有明显差别，这样就避免了计时攻击。

可能有小伙伴会说，passwordEncoder.matches 方法执行能耗费多少时间呀？这要看你怎么计时了，时间单位越小，差异就越明显：毫秒（ms）、微秒（µs）、奈秒（ns）、皮秒（ps）、飛秒（fs）、阿秒（as）、仄秒（zs）。

另外，Spring Security 为了安全，passwordEncoder 中引入了一个概念叫做自适应单向函数，这种函数故意执行的很慢并且消耗大量系统资源，所以非常有必要进行计时攻击防御。

关于自适应单向函数，这是另外一个故事了，松哥抽空再和小伙伴们聊～

好啦，接下来就是今天的送书环节了，松哥今年三月份出版了《深入浅出 Spring Security》一书，对 Spring Security 做了深入的讲解，不仅有用法，也有原理分析，是市面上为数不多的讲解 Spring Security 的图书，如果小伙伴们对此感兴趣，不妨看看。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



本次送书活动由清华大学出版社赞助支持，5 本《深入浅出 Spring Security》送给五位幸运小伙伴（仅限本号读者参与哦，获奖后再关注视为无效～），**大家在公众号后台回复 6 获取抽奖链接**。

如果大家对 Spring Boot 感兴趣，也可以看看松哥更早的《Spring Boot+Vue全栈开发实战》，这本书可以与《Vue.js快速入门》一起食用。两本书结合起来，前后端兼顾，深入理解“微人事”项目所揭示的前后端分离技术。

<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>



<iframe src="https://file.daihuo.qq.com/mp_cps_goods_card/v112/index.html" frameborder="0" scrolling="no" class="iframe_ad_container" style="width: 656.989px; height: 0px; border: none; box-sizing: border-box; display: block;"></iframe>





SpringSecurity38

SpringSecurity · 目录







上一篇一个奇怪的登录需求下一篇一个奇怪的登录需求









# 



























Scan to Follow