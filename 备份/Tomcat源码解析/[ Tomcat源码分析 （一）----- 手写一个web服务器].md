# [ Tomcat源码分析 （一）----- 手写一个web服务器](https://www.cnblogs.com/java-chen-hao/p/11316521.html)



**目录**

- [http协议简介](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_label0)
- [Socket](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_label1)
- [ServerSocket](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_label2)
- [HttpServer](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_label3)
- [总结](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_label4)

 

**正文**

作为后端开发人员，在实际的工作中我们会非常高频地使用到web服务器。而tomcat作为web服务器领域中举足轻重的一个web框架，又是不能不学习和了解的。

tomcat其实是一个web框架，那么其内部是怎么实现的呢？如果不用tomcat我们能自己实现一个web服务器吗？

首先，tomcat内部的实现是非常复杂的，也有非常多的各类组件，我们在后续章节会深入地了解。
其次，本章我们将自己实现一个web服务器的。

下面我们就自己来实现一个看看。(【注】：参考了《How tomcat works》这本书)

[回到顶部](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_labelTop)

## http协议简介

http是一种协议（超文本传输协议），允许web服务器和浏览器通过Internet来发送和接受数据，是一种请求/响应协议。http底层使用TCP来进行通信。目前，http已经迭代到了2.x版本，从最初的0.9、1.0、1.1到现在的2.x，每个迭代都加了很多功能。

在http中，始终都是客户端发起一个请求，服务器接受到请求之后，然后处理逻辑，处理完成之后再发送响应数据，客户端收到响应数据，然后请求结束。在这个过程中，客户端和服务器都可以对建立的连接进行中断操作。比如可以通过浏览器的停止按钮。

### http协议-请求

一个http协议的请求包含三部分：

1. 方法 URI 协议/版本
2. 请求的头部
3. 主体内容

举个例子

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
POST /examples/default.jsp HTTP/1.1
Accept: text/plain; text/html
Accept-Language: en-gb
Connection: Keep-Alive
Host: localhost
User-Agent: Mozilla/4.0 (compatible; MSIE 4.01; Windows 98)
Content-Length: 33
Content-Type: application/x-www-form-urlencoded
Accept-Encoding: gzip, deflate

lastName=Franks&firstName=Michael
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

数据的第一行包括：方法、URI、协议和版本。在这个例子里，方法为`POST`，URI为`/examples/default.jsp`，协议为`HTTP/1.1`，协议版本号为`1.1`。他们之间通过空格来分离。
请求头部从第二行开始，使用英文冒号（:）来分离键和值。
请求头部和主体内容之间通过空行来分离，例子中的请求体为表单数据。

### http协议-响应

类似于http协议的请求，响应也包含三个部分。

1. 协议 状态 状态描述
2. 响应的头部
3. 主体内容

举个例子

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
HTTP/1.1 200 OK
Server: Microsoft-IIS/4.0
Date: Mon, 5 Jan 2004 13:13:33 GMT
Content-Type: text/html
Last-Modified: Mon, 5 Jan 2004 13:13:12 GMT
Content-Length: 112

<html>
<head>
<title>HTTP Response Example</title> </head>
<body>
Welcome to Brainy Software
</body>
</html>
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

第一行，`HTTP/1.1 200 OK`表示协议、状态和状态描述。
之后表示响应头部。
响应头部和主体内容之间使用空行来分离。

[回到顶部](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_labelTop)

## Socket

Socket，又叫套接字，是网络连接的一个端点（end point）。套接字允许应用程序从网络中读取和写入数据。两个不同计算机的不同进程之间可以通过连接来发送和接受数据。A应用要向B应用发送数据，A应用需要知道B应用所在的IP地址和B应用开放的套接字端口。java里面使用`java.net.Socket`来表示一个套接字。

`java.net.Socket`最常用的一个构造方法为：`public Socket(String host, int port);`，host表示主机名或ip地址，port表示套接字端口。我们来看一个例子：

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
Socket socket = new Socket("127.0.0.1", "8080");
OutputStream os = socket.getOutputStream(); 
boolean autoflush = true;
PrintWriter out = new PrintWriter( socket.getOutputStream(), autoflush);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputstream()));

// send an HTTP request to the web server 
out.println("GET /index.jsp HTTP/1.1"); 
out.println("Host: localhost:8080"); 
out.println("Connection: Close");
out.println();

// read the response
boolean loop = true;
StringBuffer sb = new StringBuffer(8096); 
while (loop) {
    if (in.ready()) { 
        int i=0;
        while (i != -1) {
            i = in.read();
            sb.append((char) i); 
        }
        loop = false;
    }
    Thread.currentThread().sleep(50L);
}
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

这儿通过`socket.getOutputStream()`来发送数据，使用`socket.getInputstream()`来读取数据。

[回到顶部](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_labelTop)

## ServerSocket

Socket表示一个客户端套接字，任何时候如果你想发送或接受数据，都需要构造创建一个Socket。现在假如我们需要一个服务器端的应用程序，我们需要额外考虑更多的东西。因为服务器需要随时待命，它不清楚什么时候一个客户端会连接到它。在java里面，我们可以通过`java.net.ServerSocket`来表示一个服务器套接字。
ServerSocket和Socket不同，它需要等待来自客户端的连接。一旦有客户端和其建立了连接，ServerSocket需要创建一个Socket来和客户端进行通信。
ServerSocket有很多的构造方法，我们拿其中的一个来举例子。

```
public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException;
new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
```

1. port表示端口
2. backlog表示队列的长度
3. bindAddr表示地址

[回到顶部](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_labelTop)

## HttpServer

我们这儿还是看一个例子。

HttpServer表示一个服务器端入口，提供了一个main方法，并一直在8080端口等待，直到客户端建立一个连接。这时，服务器通过生成一个Socket来对此连接进行处理。

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
public class HttpServer {

  /** WEB_ROOT is the directory where our HTML and other files reside.
   *  For this package, WEB_ROOT is the "webroot" directory under the working
   *  directory.
   *  The working directory is the location in the file system
   *  from where the java command was invoked.
   */
  public static final String WEB_ROOT =
    System.getProperty("user.dir") + File.separator  + "webroot";

  // shutdown command
  private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

  // the shutdown command received
  private boolean shutdown = false;

  public static void main(String[] args) {
    HttpServer server = new HttpServer();
    server.await();
  }

  public void await() {
    ServerSocket serverSocket = null;
    int port = 8080;
    try {
      serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    // Loop waiting for a request
    while (!shutdown) {
      Socket socket = null;
      InputStream input = null;
      OutputStream output = null;
      try {
        socket = serverSocket.accept();
        input = socket.getInputStream();
        output = socket.getOutputStream();

        // create Request object and parse
        Request request = new Request(input);
        request.parse();

        // create Response object
        Response response = new Response(output);
        response.setRequest(request);
        response.sendStaticResource();

        // Close the socket
        socket.close();

        //check if the previous URI is a shutdown command
        shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
      }
      catch (Exception e) {
        e.printStackTrace();
        continue;
      }
    }
  }
}
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

Request对象主要完成几件事情

- 解析请求数据
- 解析uri（请求数据第一行）

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
public class Request {

  private InputStream input;
  private String uri;

  public Request(InputStream input) {
    this.input = input;
  }

  public void parse() {
    // Read a set of characters from the socket
    StringBuffer request = new StringBuffer(2048);
    int i;
    byte[] buffer = new byte[2048];
    try {
      i = input.read(buffer);
    }
    catch (IOException e) {
      e.printStackTrace();
      i = -1;
    }
    for (int j=0; j<i; j++) {
      request.append((char) buffer[j]);
    }
    System.out.print(request.toString());
    uri = parseUri(request.toString());
  }

  private String parseUri(String requestString) {
    int index1, index2;
    index1 = requestString.indexOf(' ');
    if (index1 != -1) {
      index2 = requestString.indexOf(' ', index1 + 1);
      if (index2 > index1)
        return requestString.substring(index1 + 1, index2);
    }
    return null;
  }

  public String getUri() {
    return uri;
  }

}
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

Response主要是向客户端发送文件内容（如果请求的uri指向的文件存在）。

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
public class Response {

  private static final int BUFFER_SIZE = 1024;
  Request request;
  OutputStream output;

  public Response(OutputStream output) {
    this.output = output;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public void sendStaticResource() throws IOException {
    byte[] bytes = new byte[BUFFER_SIZE];
    FileInputStream fis = null;
    try {
      File file = new File(HttpServer.WEB_ROOT, request.getUri());
      if (file.exists()) {
        fis = new FileInputStream(file);
        int ch = fis.read(bytes, 0, BUFFER_SIZE);
        while (ch!=-1) {
          output.write(bytes, 0, ch);
          ch = fis.read(bytes, 0, BUFFER_SIZE);
        }
      }
      else {
        // file not found
        String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
          "Content-Type: text/html\r\n" +
          "Content-Length: 23\r\n" +
          "\r\n" +
          "<h1>File Not Found</h1>";
        output.write(errorMessage.getBytes());
      }
    }
    catch (Exception e) {
      // thrown if cannot instantiate a File object
      System.out.println(e.toString() );
    }
    finally {
      if (fis!=null)
        fis.close();
    }
  }
}
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

[回到顶部](https://www.cnblogs.com/java-chen-hao/p/11316521.html#_labelTop)

## 总结

在看了上面的例子之后，我们惊奇地发现，在Java里面实现一个web服务器真容易，代码也非常简单和清晰！

既然我们能很简单地实现web服务器，为啥我们还需要tomcat呢？它又给我们带来了哪些组件和特性呢，它又是怎么组装这些组件的呢，后续章节我们将逐层分析。

这是我们后面将要分析的内容，让我们拭目以待！

分类: [Tomcat源码解析](https://www.cnblogs.com/java-chen-hao/category/1516344.html)

[好文要顶](javascript:void(0);) [关注我](javascript:void(0);) [收藏该文](javascript:void(0);) [微信分享](javascript:void(0);)

[« ](https://www.cnblogs.com/java-chen-hao/p/11190659.html)上一篇： [Spring MVC源码(四) ----- 统一异常处理原理解析](https://www.cnblogs.com/java-chen-hao/p/11190659.html)
[» ](https://www.cnblogs.com/java-chen-hao/p/11316795.html)下一篇： [Tomcat源码分析 （二）----- Tomcat整体架构及组件](https://www.cnblogs.com/java-chen-hao/p/11316795.html)

posted @ 2019-08-08 11:07  阅读(3844) 评论(4)   





  [回复](javascript:void(0);) [引用](javascript:void(0);)

[#1楼](https://www.cnblogs.com/java-chen-hao/p/11316521.html#4320865) 2019-08-08 17:20 [难得糊涂1998](https://www.cnblogs.com/gujun1998/)

自己写的web服务器使用率应该不高吧？

[支持(0)](javascript:void(0);) [反对(0)](javascript:void(0);)

  [回复](javascript:void(0);) [引用](javascript:void(0);)

[#2楼](https://www.cnblogs.com/java-chen-hao/p/11316521.html#4320951) [楼主] 2019-08-08 18:06 [chen_hao](https://www.cnblogs.com/java-chen-hao/)

[@](https://www.cnblogs.com/java-chen-hao/p/11316521.html#4320865) sweetheart1998
不会使用自己写的web服务器，这真是一个简单的例子，后面会讲解tomcat源码

[支持(0)](javascript:void(0);) [反对(0)](javascript:void(0);)

  [回复](javascript:void(0);) [引用](javascript:void(0);)

[#3楼](https://www.cnblogs.com/java-chen-hao/p/11316521.html#4321827) 2019-08-09 17:28 [蝴蝶很美终究蝴蝶飞不过沧海](https://www.cnblogs.com/liizzz/)

学习了^_^

[支持(0)](javascript:void(0);) [反对(0)](javascript:void(0);)

  [回复](javascript:void(0);) [引用](javascript:void(0);)

[#4楼](https://www.cnblogs.com/java-chen-hao/p/11316521.html#5181132) 2023-05-30 10:48 [lisense](https://home.cnblogs.com/u/2596757/)

写的太好了！谢谢你

[支持(0)](javascript:void(0);) [反对(0)](javascript:void(0);)



发表评论 [升级成为园子VIP会员](https://cnblogs.vip/)





 自动补全

[退出](javascript:void(0);)[订阅评论](javascript:void(0);)[我的博客](https://www.cnblogs.com/Carl-Don/)

[Ctrl+Enter快捷键提交]

[【推荐】还在用 ECharts 开发大屏？试试这款永久免费的开源 BI 工具！](https://dataease.cn/?utm_source=cnblogs)
[【推荐】编程新体验，更懂你的AI，立即体验豆包MarsCode编程助手](https://www.marscode.cn/?utm_source=advertising&utm_medium=cnblogs.com_ug_cpa&utm_term=hw_marscode_cnblogs&utm_content=home)
[【推荐】凌霞软件回馈社区，博客园 & 1Panel & Halo 联合会员上线](https://www.cnblogs.com/cmt/p/18669224)
[【推荐】抖音旗下AI助手豆包，你的智能百科全书，全免费不限次数](https://www.doubao.com/?channel=cnblogs&source=hw_db_cnblogs)
[【推荐】博客园社区专享云产品让利特惠，阿里云新客6.5折上折](https://market.cnblogs.com/)
[【推荐】轻量又高性能的 SSH 工具 IShell：AI 加持，快人一步](http://ishell.cc/)

[![img](img/35695-20250207193659673-708765730.jpg)](https://www.doubao.com/chat/coding?channel=cnblogs&source=hw_db_cnblogs)

**编辑推荐：**
· [用 C# 插值字符串处理器写一个 sscanf](https://www.cnblogs.com/hez2010/p/18718386/csharp-interpolated-string-sscanf)
· [Java 中堆内存和栈内存上的数据分布和特点](https://www.cnblogs.com/emanjusaka/p/18709398)
· [开发中对象命名的一点思考](https://www.cnblogs.com/CareySon/p/18711135)
· [.NET Core内存结构体系(Windows环境)底层原理浅谈](https://www.cnblogs.com/lmy5215006/p/18707150)
· [C# 深度学习：对抗生成网络(GAN)训练头像生成模型](https://www.cnblogs.com/whuanle/p/18708861)

**阅读排行：**
· [趁着过年的时候手搓了一个低代码框架](https://www.cnblogs.com/codelove/p/18719305)
· [本地部署DeepSeek后，没有好看的交互界面怎么行！](https://www.cnblogs.com/xiezhr/p/18718693)
· [为什么说在企业级应用开发中，后端往往是效率杀手？](https://www.cnblogs.com/jackyfei/p/18712595)
· [AI工具推荐：领先的开源 AI 代码助手——Continue](https://www.cnblogs.com/mingupupu/p/18716802)
· [用 C# 插值字符串处理器写一个 sscanf](https://www.cnblogs.com/hez2010/p/18718386/csharp-interpolated-string-sscanf)

Copyright © 2025 chen_hao
Powered by .NET 9.0 on Kubernetes