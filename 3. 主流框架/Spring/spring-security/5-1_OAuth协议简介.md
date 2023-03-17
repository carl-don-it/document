# 	5-1 OAuth协议简介

根源：如何暴露自己在本服务器的资源给第三方，也就是帮别人登录自己的账号？一般就是使用cookie或者header作身份标识token。那么如何获取呢？以下两种方法

1. 自己登录获取，控制权限，之后发给第三方。（你以为人人都会web开发，还能黑进服务器控制权限？）

2. 直接给密码第三方登录获取。（密码怎么可以让别人知道？）

3. 重定向服务器登录（授权），然后再重定向 的时候同时把token发给第三方（简单易行，服务器自身提供方法控制权限，用户在不泄露自己密码的同时可以安全提供第三方令牌，提供了open性质，这就是open authentication的由来。）

#### 要解决的问题：用户名密码模式

​	<img src="./img/5-1_OAuth用户名密码模式.png" style="zoom:33%;" />

* **最严重的问题**

  用户需要暴露自己的密码账户（微信、支付宝、谷歌..）给第三方应用

#### OAuth 令牌模式

​	<img src="./img/5-1_OAuth_Token模式.png" style="zoom:33%;" />

#### OAuth 基本流程

​	<img src="./img/5-1_OAuth基本流程.png" style="zoom:33%;" />

#### OAuth 协议中的授权模式

* **授权码模式（authorization code）**
  
  ​	<img src="img/5-1_OAuth授权码模式流程-1591174175110.png" style="zoom: 33%;" />
  
  > 同意授权在认证服务器完成的，其他模式不是
  
* **密码模式（resource owner password credentials）**

* **客户端模式（client credentials）**

* **简化模式（implicit）**
  
  > 第三方应用没有服务器，令牌存储在浏览器
  

# 实施关键

如何授权

第三方如何申请token，有些不需要申请

第三方拿到token后请求http时需要携带token作为header