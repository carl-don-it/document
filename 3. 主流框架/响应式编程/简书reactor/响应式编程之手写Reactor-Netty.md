# 响应式编程之手写Reactor-Netty

![img](https://upload.jianshu.io/users/upload_avatars/9112801/5e61f24f-9781-41e5-9ff0-ad6ed47c3cb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)

[pq217](https://www.jianshu.com/u/14294ca8b186)关注IP属地: 广东

0.6082023.02.25 12:05:57字数 1,536阅读 705

## 前言

从使用到源码，研究了很久WebFlux及Reactor

[响应式编程之Reactor](https://www.jianshu.com/p/d62e3da430ba)

[响应式编程之Reactive streams](https://www.jianshu.com/p/6f7e6cced58a)

[响应式编程之手写Reactor](https://www.jianshu.com/p/c69b5f56c313)

[响应式编程之WebFlux](https://www.jianshu.com/p/d297b049cbb3)

[响应式编程之Reactor-Netty](https://www.jianshu.com/p/079f81cab79e)

今天准备整合一下知识，自己写出一个类似Reactor-Netty的框架，可以练习一下Reactor的使用，同时回顾一下netty的知识

原材料即`Reactor`，`Netty`

最终实现如下的效果即可，既可以像Reactor-Netty一样写一个接口，并支持响应式返回，底层使用Netty进行网络通讯

```java
DisposableServer server = HttpServer.create().port(7892) // 绑定端口
        .route( // 路由
                routes -> routes.get("/hello", (request, response) ->
                        response.sendString(Mono.just("Hello World"))
                ).get("/hello2", (request, response) ->
                        response.sendString(Mono.just("Hello World2"))
                )
        )
        .bindNow();
server.onDispose().block();
```

此时访问端口7892的"/hello"路径就会返回“Hello World”

## 依赖

要实现出这样的效果，首先就是要引入两个依赖`Reactor`，`Netty`

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
    <version>3.3.8.RELEASE</version>
</dependency>
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.51.Final</version>
</dependency>
```

## netty服务

然后思路也并不复杂，不过就是定义一个类：`HttpServer`，然后create方法时启动一个Netty服务端即可，尝试一下如下

```java
public class HttpServerV1 {

    ServerBootstrap bootstrap; // netty服务构造器

    public static HttpServerV1 create() {// 静态创建
        return new HttpServerV1();
    }

    public HttpServerV1() { // 初始化，开始创建netty服务端构造器
        bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) { // 用一个简单的时间处理器，单纯打印
                        ch.pipeline().addLast(new HttpRequestDecoder(), new HttpResponseEncoder(), new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if (msg instanceof DefaultHttpRequest) {
                                    DefaultHttpRequest request = (DefaultHttpRequest) msg; // 请求信息
                                    ByteBuf result = Unpooled.copiedBuffer("Hello World: " + request.uri(), CharsetUtil.UTF_8);
                                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, result);
                                    ctx.writeAndFlush(response); // 返回
                                    ctx.channel().close(); // 关闭连接
                                }
                            }
                        });
                    }
                });
    }

    public HttpServerV1 port(int port) { // 设置端口
        bootstrap.localAddress(new InetSocketAddress(port));
        return this;
    }

    public HttpServerV1 bindNow() { // 开始绑定端口
        bootstrap.bind();
        return this;
    }
}
```

有了netty很简单就写完了，一个简单的web接口：请求后返回“hello world”+ 请求路径，使用如下

```java
public static void main(String[] args) {
    HttpServerV1.create().port(7893).bindNow();
}
```

此时浏览器访问7893端口，输出“Hello world”+ 请求路径



![img](https://upload-images.jianshu.io/upload_images/9112801-5e6a23f388ddd762.png?imageMogr2/auto-orient/strip|imageView2/2/w/369/format/webp)

Hello world

## 守护线程&阻塞

此时再回头看reactor-netty的使用例子，有一句`server.onDispose().block()`，意思是阻塞至通道服务关闭，如果去掉block()方法则运行的服务很快结束了



![img](https://upload-images.jianshu.io/upload_images/9112801-c0c7078cea864128.png?imageMogr2/auto-orient/strip|imageView2/2/w/548/format/webp)

去掉block()



![img](https://upload-images.jianshu.io/upload_images/9112801-1461acd0f26d992d.png?imageMogr2/auto-orient/strip|imageView2/2/w/934/format/webp)

程序直接结束

这里我当时比较奇怪，为什么我写的HttpServer会一直运行不需要写什么阻塞

调查了一下，发现原来reactor-netty创建的NioEventLoop都是守护线程，所以main线程如果结束后netty就停止了，至于为什么是守护线程，可能是因为为了回收资源吧

总之不管因为什么，我也这么干吧，先建一个线程工厂，生产的线程都是守护线程

```java
public class ReactorNettyThreadFactory implements ThreadFactory {
    AtomicInteger threadNo = new AtomicInteger(0);
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "reactor-nio-" + (threadNo.incrementAndGet()));
        thread.setDaemon(true); // 守护线程
        return thread;
    }
}
```

此时Netty服务初始化代码变为

```java
 ThreadFactory threadFactory = new ReactorNettyThreadFactory();
 bootstrap
    .group(new NioEventLoopGroup(1, threadFactory), new NioEventLoopGroup(threadFactory)
```

这是所有的EventLoop的线程都是守护线程，如果main方法执行完毕程序就结束了，这样肯定不行，所以main方法中一定要加上阻塞才能让服务一直运行

阻塞到什么时候呐，我们是web服务程序，应该阻塞到服务通道关闭，而刚好Netty的bind()方法可以获取到channel关闭的Future，此时bindNow方法变为如下

```java
private ChannelFuture closeFuture; // 通道的关闭的Future
public HttpServer bindNow() {
    closeFuture = bootstrap.bind().channel().closeFuture();
    return this;
}
```

main方法如何阻塞到channel关闭呐，一个`closeFuture.sync()`其实就可以，但我们使用Reactor，当然要发挥Reactor的优势，因为我们可能还会在close事件发生时订阅一些操作，所以我们把closeFuture转换为Reactor的Mono发布者，发布得就是通道关闭事件，取名为`onDispose`，即服务关闭的发布者

```java
public Mono<Void> onDispose() { // 这里源码实现更复杂，简化一下
    return Mono.create(sink->{
        closeFuture.addListener((ChannelFutureListener) future -> sink.success());
    });
}
```

此时回到使用，使用代码如下：

```java
public static void main(String[] args) {
    HttpServer httpServer = HttpServer.create()
            .port(7893)
            .bindNow();
    httpServer.onDispose().block();
}
```

感觉上就和reactor-netty的使用很像了，如果不block()，程序立马结束

但此时我们的web服务只有一个，无法根据路径走不同的方法，所以下一步：加路由

## 路由

路由也好理解，就是一个path到方法的映射map，先对照reactor-netty学一下我们的方法应该是如何抽象

首先有两个参数：request(用于获取请求的参数)，response(用于写回响应)

request简单一点直接用netty的`DefaultHttpRequest`

但response可不简单，它有一个send方法用于写回数据，它接受的参数是一个Publisher，所以这个方法的作用是在Publisher发布时能写回数据至客户端channel，所以send方法本质是**订阅一个程序数据准备好后，发布数据至客户端的步骤**，由于writeAndFlush也是异步操作，所以要再返回一个Publisher发布写完事件，以便后续关闭通道的相关处理，由于这个发布者只是事件没有数据所以是`Void`，整个过程使用`flatMap`即可实现，如下

```java
public class HttpServerResponse {

    private ChannelHandlerContext ctx;

    public HttpServerResponse(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Mono<Void> sendString(Mono<String> publisher) {
        return send(publisher.flatMap(content-> Mono.just(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8))));
    }

    public Mono<Void> send(Mono<ByteBuf> publisher) {
        return publisher.flatMap(content-> Mono.create(sink-> {
            ChannelFuture channelFuture = ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content));
            channelFuture.addListener(future -> {
                sink.success();
            });
        }));
    }
}
```

此时我们的自定义方法的结构出来了，两个参数：netty的`HttpRequest`和自己封装的`HttpServerResponse`，一个返回结果：`Publisher<Void>`

可以用JDK的BiFunction代表方法的抽象

```java
BiFunction<? super HttpRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler
```

我们把一个映射和方法的对应用实体描述一下：

```java
@AllArgsConstructor
static final class HttpRouteHandler {
    private String path; // 路径
    private BiFunction<? super HttpRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler; // 方法

    public Publisher<Void> apply(HttpRequest request,
                                 HttpServerResponse response) { // 执行方法
        return handler.apply(request, response);
    }

    public boolean test(HttpRequest request) { // 是否是某个请求
        return request.uri().equals(path);
    }
}
```

再用一个集合存储所有path->方法的映射

```java
public class HttpServerRoutes {

    private List<HttpRouteHandler> handlers = new ArrayList<>(); // 映射集合

    // 添加get请求path和方法映射
    public HttpServerRoutes get(String path,
                                BiFunction<? super HttpRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler) {
        handlers.add(new HttpRouteHandler(path, handler));
        return this;
    }

    // 选择路由对应的处理方法执行
    public Publisher<Void> apply(HttpRequest request, HttpServerResponse response) {
        for (HttpRouteHandler handler : handlers) {
            if (handler.test(request)) { // 路径对应上
                return handler.apply(request, response); // 执行
            }
        }
        return Mono.empty();
    }

}
```

## 最终

最后就是我们的HttpServer构建器，要可以配置路由，并再请求到达时执行路由的方法，完整代码如下

```java
public class HttpServer {

    ServerBootstrap bootstrap; // netty服务构造器

    ChannelFuture closeFuture; // 通道的关闭的Future

    HttpServerRoutes handler; // 路由

    public static HttpServer create() {
        return new HttpServer();
    }

    /**
     * 初始化，开始创建netty服务端构造器
     */
    public HttpServer() {
        bootstrap = new ServerBootstrap();
        ThreadFactory threadFactory = new ReactorNettyThreadFactory();
        bootstrap.group(new NioEventLoopGroup(1, threadFactory), new NioEventLoopGroup(threadFactory))
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {//创建通道初始化对象，设置初始化参数
                    @Override
                    protected void initChannel(SocketChannel ch) { // 用一个简单的时间处理器，单纯打印
                        ch.pipeline().addLast(new HttpRequestDecoder(), new HttpResponseEncoder(), new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if (msg instanceof DefaultHttpRequest) {
                                    DefaultHttpRequest request = (DefaultHttpRequest) msg; // 请求
                                    HttpServerResponse response = new HttpServerResponse(ctx); // 响应
                                    handler.apply(request, response) // 执行方法
                                    .subscribe(new ChannelDisposeSubscriber(ctx)); // 订阅
                                }
                            }
                        });
                    }
                });
    }

    public HttpServer port(int port) {
        bootstrap.localAddress(new InetSocketAddress(port));
        return this;
    }


    /**
     * 设置路由
     * @return
     */
    public HttpServer route(Consumer<? super HttpServerRoutes> routesBuilder) {
        handler = new HttpServerRoutes();
        routesBuilder.accept(handler);
        return this;
    }

    public HttpServer bindNow() {
        closeFuture = bootstrap.bind().channel().closeFuture();
        return this;
    }

    public Mono<Void> onDispose() {
        return Mono.create(sink->{
            closeFuture.addListener((ChannelFutureListener) future -> sink.success());
        });
    }
}
```

其中handler.apply方法完成了订阅操作，订阅的就是响应已写回客户端的事件，所以对应的处理就是关闭客户端通道

```java
@AllArgsConstructor
public class ChannelDisposeSubscriber implements Subscriber<Void> {

    private ChannelHandlerContext ctx;

    @Override
    public void onComplete() {
        ctx.close(); // 写回响应数据后关闭通道
    }
}
```

到此一个基于基于Netty的http服务就写完了，可以接受响应式的返回结果，使用如下

```java
public static void main(String[] args) {
    HttpServer httpServer = HttpServer.create()
            .port(7893)
            .route(routes -> routes
                    .get("/hello",
                            (request, response) -> response.sendString(Mono.just("Hello World"))
                    ).get("/hello2",
                            (request, response) -> response.send(Mono.just(Unpooled.copiedBuffer("Hello World2", CharsetUtil.UTF_8)))
                    ).get("/hello3",
                            (request, response) -> response.sendString(Mono.create(sink->{
                                try {Thread.sleep(1000);} catch (InterruptedException e) {}
                                sink.success("Hello World3");
                            }))
                    )
            )
            .bindNow();
    httpServer.onDispose().block();
}
```

测试结果如下



![img](https://upload-images.jianshu.io/upload_images/9112801-2e3f8c2d9e6605bc.png?imageMogr2/auto-orient/strip|imageView2/2/w/369/format/webp)

测试

## 小结

不得不说，初次使用Reactor写功能，跟原命令行写法的思维差异真的很大，总结如下

- 服务维护一个path至方法的映射
- 请求到达执行对应方法，反回的是一个发布者，发布的事件是请求处理结束
- 执行方法后得到返回的发布者后立即订阅，订阅的处理是关闭连接
- 方法内部通过执行`response.send`方法可以给执行结果发布者(类似Mono和Flux)添加一个把结果发送到客户端的处理过程

个人认为`response.send`也应该封装进框架中，而不是让用户自己写，因为我们写一个接口一定是要有返回值的，就像如果使用的是WebFlux，一般请求是不需要管response的，方法直接返回Mono就可以了



©著作权归作者所有,转载或内容合作请联系作者