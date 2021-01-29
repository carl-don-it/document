# 4-2 SpringSecurity默认配置

> 只要一加入jar包，spring boot就会自动添加最基础的配置
>

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
```

<img src="img/1570595277496.png" style="zoom:%;" />

```java
//看文档注释
//默认Basic方式登陆,默认用户名user,密码会在控制台输出，使用以下的filter进行认证
org.springframework.security.web.authentication.www.BasicAuthenticationFilter
    //处理 Authorization header，认证、鉴权成功，继续doFilter直至鉴权和restAPI
    //认证失败则默认不再进入
    
    //安全性低，文档建议使用digest认证
    org.springframework.security.web.authentication.www.DigestAuthenticationFilter
```

![image-20200603163158494](img/image-20200603163158494.png)