

# [Decrypt HTTPS Traffic](https://docs.telerik.com/fiddler-everywhere/user-guide/settings/https/https-decryption?_ga=2.92520088.427367054.1599057315-1289729138.1599057315#decrypt-https-traffic)

fiddler如果是https请求需要

如果是需要使用fiddler capture https ，需要导入fiddler发布的证书并且信任（无法信任）

```java
Exception in thread "main" javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
    at com.sun.net.ssl.internal.ssl.Alerts.getSSLException(Unknown Source)
    at com.sun.net.ssl.internal.ssl.SSLSocketImpl.fatal(Unknown Source)
    at com.sun.net.ssl.internal.ssl.Handshaker.fatalSE(Unknown Source)
    at com.sun.net.ssl.internal.ssl.Handshaker.fatalSE(Unknown Source)
    at com.sun.net.ssl.internal.ssl.ClientHandshaker.serverCertificate(Unknown Source)
    at com.sun.net.ssl.internal.ssl.ClientHandshaker.processMessage(Unknown Source)
    at com.sun.net.ssl.internal.ssl.Handshaker.processLoop(Unknown Source)
    at com.sun.net.ssl.internal.ssl.Handshaker.process_record(Unknown Source)
    at com.sun.net.ssl.internal.ssl.SSLSocketImpl.readRecord(Unknown Source)
    at com.sun.net.ssl.internal.ssl.SSLSocketImpl.performInitialHandshake(Unknown ...
```

https://stackoverflow.com/questions/8549749/how-to-capture-https-with-fiddler-in-java

# 修改请求响应

[【Fiddler4.6.3】fiddler超简单临时修改响应内容的方法](https://www.zhaokeli.com/article/8088.html)

[【Fiddler4.6.3】使用Fiddler Script 脚本控制断点修改请求和和响应数据(二)](https://www.zhaokeli.com/article/8089.html)

# 基本用法

[Fiddler抓包工具总结](https://www.cnblogs.com/yyhh/p/5140852.html)

- 1. Fiddler 抓包简介

  - 1） 字段说明
  - 2）. Statistics 请求的性能数据分析
  - 3）. Inspectors 查看数据内容
  - 4）. AutoResponder 允许拦截指定规则的请求
  - 4）. Composer 自定义请求发送服务器
  - 5）. Filters 请求过滤规则

- 2. Fiddler 设置解密HTTPS的网络数据

- 3. Fiddler 抓取Iphone / Android数据包

[保存会话](https://testerhome.com/topics/5481)

# 其他

[Fiddler Filters filtration, hidden js, css, pictures](https://www.programmersought.com/article/68163236525/)

# fiddler截获localhost/127.0.0.1请求的方法

https://blog.csdn.net/weixin_46134675/article/details/106784247

有些域名是不行的，mySpace就好