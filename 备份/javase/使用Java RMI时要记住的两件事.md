# 使用Java RMI时要记住的两件事



dnc8371



于 2020-05-18 23:23:03 发布



阅读量3.1k

![img](img/tobarCollect2.png) 收藏 4

![img](img/newHeart2023Black.png)点赞数

文章标签： [网络](https://so.csdn.net/so/search/s.do?q=网络&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art) [java](https://so.csdn.net/so/search/s.do?q=java&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art) [linux](https://so.csdn.net/so/search/s.do?q=linux&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art) [spring](https://so.csdn.net/so/search/s.do?q=spring&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art) [mysql](https://so.csdn.net/so/search/s.do?q=mysql&t=all&o=vip&s=&l=&f=&viparticle=&from_tracking_code=tag_word&from_code=app_blog_art)





这是一篇简短的博客文章，介绍使用Java [RMI](https://en.wikipedia.org/wiki/Java_remote_method_invocation)时应注意的两个常见陷阱。

### 设置java.rmi.server.hostname

如果您感到陌生，Connection拒绝托管： RMI客户端上的错误消息，并且您确定连接应该正常工作（您仔细检查了所有标准配置，例如网络配置等）。RMI系统属性java.rmi.server.hostname值得研究。

要在远程对象上调用方法，RMI客户端首先必须从RMI注册表中检索远程存根对象。 此存根对象包含服务器地址，该服务器地址稍后将在调用远程方法时用于连接到远程对象（与RMI注册表的连接和与远程对象的连接是两个完全不同的东西）。 默认情况下，服务器将尝试检测自己的地址并将其传递给存根对象。 不幸的是，用于检测服务器地址的算法并不总是产生有用的结果（取决于网络配置）。

通过设置RMI服务器上的系统属性java.rmi.server.hostname，可以覆盖传递给存根对象的服务器地址。

这可以用Java代码完成

```csharp
System.setProperty("java.rmi.server.hostname", "<<rmi server ip>>");
```

或添加Java命令行参数：

```cobol
-Djava.rmi.server.hostname=<<rmi server ip>>
```

### 设置RMI服务端口

如果在通过防火墙进行RMI调用时遇到问题，则应确保为远程对象设置了特定的端口。 默认情况下，RMI注册表使用端口1099，因此请确保在防火墙中打开了此端口。 但是，此端口仅由客户端用于连接到RMI注册表，而不用于存根和远程对象之间的通信。 对于以后的版本，默认情况下使用随机端口。 由于您不想打开防火墙中的所有端口，因此应为RMI远程对象设置特定的端口。

这可以通过重写RMISocketFactory的createServerSocket（）方法来完成：

```java
public class MyRMISocketFactory extends RMISocketFactory {



  private static final int PREFERED_PORT = 1234;



  public ServerSocket createServerSocket(int port) throws IOException {



    if (port == 0) {



      return new ServerSocket(PREFERED_PORT);



    }



    return super.createServerSocket(port);



  }



}
```

默认情况下，如果将0作为参数传递，则createServerSocket（）选择一个空闲的随机端口。 在createServerSocket（）的此修改版本中，将0作为参数传递时，将返回特定端口（1234）。

如果您使用的是Spring的RmiServiceExporter，则可以使用setServicePort（）方法在特定端口上导出服务：

```cobol
<bean class="org.springframework.remoting.rmi.RmiServiceExporter">



  <property name="servicePort" value="1234"/>



  ...



</bean>
```

请注意，多个远程对象/服务可以共享同一端口。 设置特定端口后，只需在防火墙中打开此端口。

**参考：** [mscharhag编程和Stuff](http://www.mscharhag.com/)博客中的[JCG合作伙伴](https://www.javacodegeeks.com/jcg) Michael Scharhag [使用Java RMI时要记住两点](http://www.mscharhag.com/2013/10/two-things-to-remember-when-using-java.html) 。

> 翻译自: <https://www.javacodegeeks.com/2013/11/two-things-to-remember-when-using-java-rmi.html>