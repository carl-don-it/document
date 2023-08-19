# 响应式编程之WebFlux

![img](https://upload.jianshu.io/users/upload_avatars/9112801/5e61f24f-9781-41e5-9ff0-ad6ed47c3cb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)

[pq217](https://www.jianshu.com/u/14294ca8b186)关注IP属地: 广东

0.9032023.02.22 09:10:38字数 3,014阅读 3,976

## 前言

前几篇文章介绍了Reactor以及响应式规范Reactive streams，那么如何将这种响应式编程应用在web开发中呐

## 异步

想一想平时web开发的场景

1. 前端提交请求过来
2. spring(tomcat)从线程池中分配一个线程来应对请求
3. 根据路径和配置/注解调用对应的方法
4. 在我们的方法内一般连接数据库获取数据，阻塞取到结果后做一些计算，最后返回
5. spring(tomcat)负责将返回的数据写入响应并推送给客户端，一次请求结束

在这个过程中工作线程阻塞着等待数据库返回，造成资源浪费，这个之前都有详细描述

那么假设我们的数据库支持异步获取，那么我们传统场景的代码改如何写？看下面这个例子

```java
@GetMapping("/{userId}")
public Result getUserName(@PathVariable Long userId) {
    Result result = new Result(); // 返回结果
    AsyncDB.getUserName(userId, new Callback() {// 异步操作数据库获取用户姓名
        @Override
        public void run(String username) {
            result.setData(username); // 数据库回调时把结果设置到返回对象中
        }
    });
    return result;
}
```

试想一下，这样请求可以获得想要的结果吗？结论显然是不可能，因为回调是在数据库返回结果是通过另一个线程调用的，而当前处理请求的线程是不会等待它，而是直接就返回data==null的Result了

那如果让当前线程等待返回结果可行吗？可行是必然可行的，可又这不又成为阻塞了吗，异步的意义何在呐

再想一下，那么有了类似Reactor这样的响应式库可以解决这样的问题吗？结论是依然不能解决问题，Reactor只是让我们写异步回调的响应式代码更加方便、可读性更高，而其本质和回调是没有区别的

假如我们的spring-boot-web项目引入Reactor依赖，下面写个Controller层的方法返回Mono，如下

```java
@GetMapping("/user")
public Mono<String> getUser() {
    return Mono.just("pq");
}
```

从我们原来的原开发角度考虑，这样必然行不通啊：首先返回的是一个Mono对象，那么前端接受的是个啥？之前也提到过Mono是一个发布者，没有订阅的时候不会有数据啊，总不能让前端js去订阅吧？最后最基本的这个Mono对象甚至都没有实现Serializable，怎么能当成结果返回在网络中传输呐？

理论上确实如此，可是实际上spring mvc已经提供了对这种响应式结果的特殊处理，所以尽管以上代码看起来行不通，而实际上当你真去调用这个接口，返回的确实是字符串“pq”

## Spring MVC

那么当我们的代码返回了Mono或Flux对象，Spring MVC是如何处理的呐？

细想一下，Spring MVC甚至没有对Reactor的依赖，那如何对Reactor的特殊对象Mono和Flux做特殊处理？

其实从严格意义上讲，Spring MVC并不是对Reactor有支持，而是对Reactive streams有支持，而Reactor又刚好是Reactive streams的一个实现，这么一看就理解为什么Spring MVC可以对Reactor的对象特殊支持

所以，即是你在Controller中返回的是其它响应式库比如RxJava的Observable对象，Spring MVC一样也可支持，结论就是 **Spring MVC支持响应式返回结果**

我们可以通过查看源码看看Spring MVC是如何支持响应式返回结果的

首先，Spring MVC在调用我们的Controller方法获得返回值后会判断结果的类型，并调用不同的处理器(代码在HandlerMethodReturnValueHandlerComposite)



![img](https://upload-images.jianshu.io/upload_images/9112801-62c1425822d5136d.png?imageMogr2/auto-orient/strip|imageView2/2/w/1154/format/webp)

HandlerMethodReturnValueHandlerComposite

最终在返回结果为响应式结果是`ResponseBodyEmitterReturnValueHandler`中标了，开始进行处理，并最终将结果交由内部的`ReactiveTypeHandler`(反应式类型处理器)处理



![img](https://upload-images.jianshu.io/upload_images/9112801-337bcb8f3e5d997d.png?imageMogr2/auto-orient/strip|imageView2/2/w/1175/format/webp)

ResponseBodyEmitterReturnValueHandler

在ReactiveTypeHandler处理过程中会生成一个订阅器负责订阅实现了Publisher的返回结果(Mono，Flux，Observable)



![img](https://upload-images.jianshu.io/upload_images/9112801-55a6decb64fb0abf.png?imageMogr2/auto-orient/strip|imageView2/2/w/1141/format/webp)

ReactiveTypeHandler

其中connect方法调用的就是订阅方法`subscribe`



![img](https://upload-images.jianshu.io/upload_images/9112801-57a6ca8059ba4d44.png?imageMogr2/auto-orient/strip|imageView2/2/w/856/format/webp)

DeferredResultSubscriber

也就是Reactive streams中规范的订阅方法完成订阅

```java
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}
```

最后通过一个`DeferredResult`对象将异步结果的处理逻辑设置为向request的响应中写数据并返回，至此，SpringMVC就完成了对响应式结果的支持

总结起来如下：

- Spring MVC发现返回结果是响应式的发布者时，会首先订阅它，并将当前请求暂存，当前请求处理线程结束
- 订阅的结果产生时，对应的回调线程会找到暂存的请求，写回响应，完成请求

整个过程你会发现，如果数据库响应时间较长，在这段时间内，不会有任何线程再傻等着结果结束，真正的发挥了响应式的优势

## Spring WebFlux

说了很久Spring MVC对响应式的支持，该回到正题即Spring WebFlux上了

Spring WebFlux的最大特点是基于Reactor开发，支持接口直接返回Mono或Flux

这里有个大问题，既然Spring MVC也同样可以支持响应式结果，为什么还要有Spring WebFlux呐？

这个问题一直困扰我很久，通过读官方文档大概有个基本的认识：

- Spring MVC主要还是做命令式编程的框架，只是额外做了对响应式的支持，相当于一个扩展功能， 而Spring WebFlux是专门为响应式编程而搭建的框架，对响应式的支持必然更全面也更合理
- 还有一个关键的区别，依然是二者定位不一样，SpringMVC默认用户写的是阻塞式代码，所以需要很多线程池来吸收潜在的阻塞请求，而Spring WebFlux默认用户写的是响应式程序，所以会使用很少的线程来处理请求
- Spring WebFlux可以运行在非Servlet容器如Netty上，因为异步非阻塞NIO请求用Netty明显更加合理，而Spring MVC一般运行在Servlet容器如Tomcat上，显然更适合阻塞请求
- 再就是二者使用的依赖工具也不一样，像类似JDBC，JPA这样的开发框架都是同步获取数据的，显然没办法和WebFlux配合使用(理念就完全不合)，对应的也有了响应式数据库的规范接口r2dbc，但并不是所有数据库都支持
- Spring WebFlux基于Reactor开发(所以使用Webflux也不需要单独引用Reactor)

其实个人认为主要差异总结就是一句话：“**理念不同**”

##### 使用

使用WebFlux，当然可以支持Mono和Flux作为响应结果了，如下

```java
@GetMapping("/1")
public String getUser1() { // 普通响应也没问题
    return "pq1";
}

@GetMapping("/2")
public Mono<String> getUser2() { // 支持返回Mono
    return Mono.just("pq2");
}

@GetMapping("/3")
public Mono<String> getUser3() { // 异步完全没问题
    return Mono.create(sink -> {
        new Thread(() -> {
            try {Thread.sleep(1000);} catch (InterruptedException e) {}
            sink.success("pq3");
        }).start();
    });
}
```

还有一种比较适合的场景就是写`event-stream`接口，这种接口相比于websocket更适合服务端向客户端单方向推送数据，而且不需新建一个socket服务

```java
@GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> flux() {
    return Flux.create(sink->{
        new Thread(() -> {
            String[] arr = {"pq1", "pq2", "pq3"};
            for (int i=0;i<3;i++) {
                try {Thread.sleep(1000);} catch (InterruptedException e) {}
                sink.next(arr[i]);
                if (i==2) {
                    sink.complete();
                }
            }
        }).start();
    });
}
```

此时如果用浏览器访问接口，每隔1秒会依次输出"pq1", "pq2", "pq3"(如果使用JS，可以使用EventSource对接接口)

除此了中类似MVC的注解方式(使用@GetMapping等)写接口，WebFlux还支持Functional Endpoints(Functional Endpoints)，写出的代码更有响应式的感觉

```java
@Bean
public RouterFunction<ServerResponse> routes() {
    return RouterFunctions.route().GET("/user/fn", request -> ServerResponse.ok().body(Mono.just("pq"), Mono.class)).build();
}
```

##### 实现

和MVC一样，重点还是想一下WebFlux如何把Mono或Flux写回给请求的，其实思路都一样

- 请求到达，缓存本次连接，执行对应方法
- 订阅Mono或Flux，回调中将结果写回请求

关于源码的具体分析，因为涉及的重点较多，留给下一篇文章单独分析

## 数据库

回到最开始的场景，WEB开发一般场景是要去读取数据库，返回数据处理处理再传递给前端

参照之前的场景描述，用响应式思想修改步骤应该如下

1. 前端提交请求过来
2. 线程池中分配一个处理线程来应对请求
3. 根据路径和配置/注解调用对应的方法
4. 在我们的方法内向数据库发起请求，并订阅回调，方法立即结束，处理线程释放
5. 数据库数据读取成功后，使用新的线程来执行回调，并把结果写回响应，整个请求结束

整个过程中，最根源的发布者其实是数据库，所以要彻底的写出响应式的程序，数据库是需要支持异步请求的：即可以程序发送请求命令并立即返回，执行结束后会主动通知，程序再做出响应

也就是说，**如果数据库服务不支持异步，程序再怎么写都白扯**

比如，非关系型数据库领域，redis和mongo可以支持支持reactive编程，还是以读取redis为例

之前讲过redis客户端lettuce可以用于异步读取，响应式编程配合WebFlux就非常合适了，我们可以引入`spring-boot-starter-data-redis`快速完成lettuce与spring项目的整合

```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <version>2.3.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    <version>2.3.2.RELEASE</version>
</dependency>
```

在配置文件中配置一下redis的地址

```yml
spring:
  redis:
    host: localhost
```

此时就可以轻松使用响应式方式写一个读取redis并返回的web接口

```java
@RestController
@RequestMapping("redis")
public class WebfluxRedisController {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @GetMapping("/names")
    public Mono<String> getNames() {
        ReactiveValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        return opsForValue.get("names");
    }

}
```

## R2DBC

随着时间的发展，越来越多的关系型数据库也开始慢慢支持响应式，传统的数据库操作API即`JDBC`是阻塞读取的规范，显然在响应式领域并不适用

于是出现了一种新的规范即`R2DBC`，全称是"Reactive Relational Database Connectivity"，即响应式关系型数据库连接，它的出现为关系数据库性数据库的响应式读取提供了统一的接口

老牌的`JDBC`出现较早，各种数据库基本都支持，而支持`R2DBC`的数据库当前也在不断增多(说明响应式真的是一种趋势)，比如Postgres, MSSQL, H2，MySQL都开始陆续支持异步读取，并有了对应的实现`R2DBC`的驱动

以Mysql为例，使用WebFlux写一个响应式读取数据库的接口，先引入依赖

```xml
<!--spirng整合r2dbc-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    <version>2.3.2.RELEASE</version>
</dependency>
<!--r2dbc的mysql实现-->
<dependency>
    <groupId>dev.miku</groupId>
    <artifactId>r2dbc-mysql</artifactId>
    <version>0.8.2.RELEASE</version>
</dependency>
```

配置文件，**注意是r2dbcs而不是jdbc**

```yml
spring:
  r2dbc:
    url: r2dbcs:mysql://127.0.0.1:3306/database
    username: root
    password: 123
```

此时建一个user表，并写一个实体映射

```java
@Table("user")
@Data
public class User {
    @Id
    private int id;
    private String name;
    private int age;
}
```

定义一个user响应式仓库

```java
public interface UserRepository extends ReactiveCrudRepository<User, Integer> {
}
```

最终接口如下，非常简单

```java
@RestController
@RequestMapping("mysql")
public class WebfluxMysqlController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public Flux<User> getUsers() {
        return userRepository.findAll();
    }

}
```

如此就轻松写出了一个响应式读取mysql数据库并返回数据的接口

## 最后

从依赖的使用也可以看出

- 我们使用springmvc的命令式编程模式有一系列与之对应的依赖工具，比如JDBC，JPA，Jedis等
- 而响应式开发的WebFlux则有另一套适用的依赖工具如R2DBC，r2dbc-mysql，Lettuce等

当然这不是强制的，依赖随便引入都没问题，但往往只是会发现很蹩脚甚至没有意义，因为理念实在太不相同

所以响应式带来的改变是颠覆性的，就像是vue对js的改变一样，未来的响应式开发可能与现在写的代码完全不一样的风格，更倾向于基于流的函数式开发风格，这也是为什么看使用Gateway写的网关代码根本看不懂

个人认为，将来命令式编程的方式也不会被淘汰，二者并存才更加合理，毕竟如果是速度很快的IO请求使用响应式造成的线程切换反而影响效率，而响应式更适合于延迟高的IO操作且IO密集的微服务使用，比如`Gateway`



最后编辑于 ：2023.02.28 18:34:24