# 响应式编程之Reactor

![img](img/5e61f24f-9781-41e5-9ff0-ad6ed47c3cb6.webp)

[pq217](https://wsa.jianshu.io/u/14294ca8b186)关注IP属地: 广东

0.3312023.02.09 20:38:28字数 2,817阅读 623

## 前言

响应式编程在java编程中并不常见，可能比较近的接触也就是spring-gateway中晦涩难懂的响应式代码，一直处于半懵逼，参考了很多文章说响应式是一种未来趋势，所以还是有必要研究一下

因此今天开始研究学习响应式编程系列，计划的学习路线：Reactor使用>Reactor源码研究>WebFlux>Gateway

本文主要记录响应式编程的意义及Reactor框架的使用

## 响应式

首先什么是响应式？说白了就是异步获取结果，这个概念可以用下面例子描述一下同步和异步

> 你去饭店点餐，点完餐坐在座位上等，菜做好开始吃，这就是同步

> 你去饭店点餐，点完餐出去逛街，菜做好了服务员给你打电话，你回来开始吃，这就是异步

关键就在于，**你所需要的结果由服务方准备好后主动通知你**，再此之前你可以做其它事，你得到通知后开始进行响应，这就是响应式

回到程序，通常，我们在读取数据库或者做网络请求时，都是同步阻塞执行的，此时操作线程会一直等待着结果返回(运行状态中)，如果请求时间很长，线程一直占用着CPU的资源，造成资源浪费，这种编程方式就是 “imperative”(迫切的)

但如果写过js，就发现前端的js网络请求就完全就是响应式的编程写法，这里并不是说java很落后，主要还是实际场景问题限制，后续会详说

## 异步编程

在jvm中，我们如何做到异步编程呐，java提供了两种异步编程的方式

- **Callback** 通过使用一个callback方法作为参数，当获得结果后服务方通过调用callback方法
- **Future** 通过java中的Future异步获取执行结果

这两种方式都可以使用，但都有一定的局限性

##### callback

首先是callback的使用，可读性非常差，当多个callback嵌套时，程序就会非常乱，出现传说中的“回调地域”

比如我们举个类似官方文档中的例子：
`Example 1` 一个很常见的场景，获取某用户的喜欢栏目，并截取前两个，涉及到两个服务: UserService(根据用户id获取喜欢栏目ids)，FavoriteService(根据栏目id获取栏目详情)

我们使用callback方式来异步获取两种数据，首先定义callback接口

```java
public interface Callback<T> {
    void onSuccess(T t);
    void onError(String error);
}
```

然后是UserService，根据用户id获取喜欢栏目ids，用sleep来模拟读取时间

```java
public class CallbackUserService {
    public void getFavorites(Long userId, Callback<List<Long>> callback) {
        new Thread(() -> {
            try {
                // 模拟数据库访问时间
                Thread.sleep(1000);
                List<Long> favorites = Arrays.asList(1L,2L,3L);
                callback.onSuccess(favorites);
            } catch (Exception e) {
                callback.onError("读取出现错误");
            }
        }).start();
    }
}
```

然后是FavoriteService，根据栏目id获取栏目详情

```java
public class CallbackFavoriteService {

    private Map<Long, String> names = new HashMap<Long, String>() {{
        put(1L, "football");
        put(2L, "movie");
        put(3L, "film");
    }};

    public void getDetail(Long id, Callback<String> callback) {
        new Thread(() -> {
            try {
                // 模拟数据库访问时间
                Thread.sleep(1000);
                callback.onSuccess(names.get(id));
            } catch (Exception e) {
                callback.onError("读取出现错误");
            }
        }).start();
    }
}
```

这时开始写我们的主代码，如下

```java
userService.getFavorites(23L, new Callback<List<Long>>() {
    @Override
    public void onSuccess(List<Long> favIds) {
        favIds.stream().limit(2).forEach(favId->{
            favoriteService.getDetail(favId, new Callback<String>() {
                @Override
                public void onSuccess(String s) {
                    System.out.println(s);
                }
                @Override
                public void onError(String error) {
                    System.out.println(error);
                }
            });
        });
    }
    @Override
    public void onError(String error) {
        System.out.println(error);
    }
});
while (true) {
    System.out.println("做点其它事。。。");
    Thread.sleep(1000);
}
```

最终代码输出如下：

```undefined
做点其它事。。。
做点其它事。。。
做点其它事。。。
movie
football
做点其它事。。。
```

可以看到在等待的过程中，主线程并没有阻塞，实现了异步编程，但可以看到代码是十分混乱的，如果再加上一个需求：“用户没有喜欢栏目时通过建议服务获取建议栏目”，可以想象对应的代码会有多么混乱不堪

##### Future

[Future](https://www.jianshu.com/p/d276ee6b4cb2)的弱点更明显了，在执行get()方法获取结果时依然会阻塞线程

虽然jdk8中出现了`CompletableFuture`可以真正实现异步编程但是使用起来也是非常麻烦

## Reactive Stream

由于JVM本身的响应式编程支持的缺失，针对JVM响应式编程的扩展库开始陆续出现，首先是RxJava库的出现扩展了JVM的响应式编程，而随着时间的推移一个响应式规范诞生了，即 [Reactive Stream](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.reactive-streams.org%2F)，它为 JVM 上的响应式编程定义了一组接口和交互规则，RxJava从 RxJava2 开始实现 Reactive Stream规范。同时 MongoDB、Reactor、Slick 等也相继实现了 Reactive Stream 规范

Reactive Stream规范所定义的一系列接口也被集成在java 9的Flow包下

## Reactor

本文主要介绍相对较火的Reactor，它在满足响应式编程的同时让代码变的可读性可维护及可维护性非常高

首先思想上，Reactor是一个发布订阅的模式，由服务方发布数据，订阅者获取通知进行相关响应，服务方也可以不停的发布数据，形成动态数据流

而在这个数据流动的中间过程Reactor提供了一系列的中间处理运算符：比如map，take，flatMap等对数据进行中间处理

异步编程的关键在于我们要在数据返回前就知道数据的格式，就比如我们写js对接接口时，一定是提前知道了返回数据的形式才能写出代码

Reactor有两种数据形式，分别用Flux和Mono表示，如果是Flux代表将来发送的是多个数据，如果是Mono代表将来放回的是1个数据(也有可能是0)

Reactor的定义还是非常抽象，我们还是拿`Example 1`，如果我们使用Reactor，这段代码该如何改造？

第一步，引入Reactor框架

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
    <version>3.3.8.RELEASE</version>
</dependency>
```

先改造我们的UserService

```java
public class ReactorUserService {
    // 返回的是多个，即Flux
    public Flux<Long> getFavorites(Long userId) {
        // 创建Flux并返回
        return Flux.create(sink -> {
            new Thread(() -> {
                // 模拟数据库访问时间
                try {
                    Thread.sleep(1000);
                    // 发布数据 1,2,3
                    sink.next(1L);
                    sink.next(2L);
                    sink.next(3L);
                    // 标识发布结束
                    sink.complete();
                } catch (Exception e) {
                    e.printStackTrace();
                    sink.error(new Exception("读取出现错误"));
                }
            }).start();
        });
    }
}
```

然后是FavoriteService

```java
public class ReactorFavoriteService {

    private Map<Long, String> names = new HashMap<Long, String>() {{
        put(1L, "football");
        put(2L, "movie");
        put(3L, "film");
    }};

    // 返回单个数据，所以是Mono
    public Mono<String> getDetail(Long id) {
        return Mono.create(sink -> {
            new Thread(() -> {
                // 模拟数据库访问时间
                try {
                    Thread.sleep(1000);
                    // 发布并完成
                    sink.success(names.get(id));
                } catch (Exception e) {
                    e.printStackTrace();
                    sink.error(new Exception("读取出现错误"));
                }
            }).start();
        });
    }
}
```

开始定义操作流

```java
userService.getFavorites(23L)
        .flatMap(id->favoriteService.getDetail(id)) // 取详情
        .take(2) // 取前两个
        .subscribe(System.out::println, error-> System.out.println("process error:"+error)); // 订阅，处理方式sout
while (true) {
    System.out.println("做点其它事。。。");
    Thread.sleep(1000);
}
```

最终输出：

```undefined
做点其它事。。。
做点其它事。。。
做点其它事。。。
film
football
做点其它事。。。
```

再回头看一下callback的代码，会不会觉得Reactor真香？有没有感觉像在写js...

## Redis异步读取

回头细品一下上面的代码，确实做到了非阻塞获取数据，并在数据获取到时做出相应

但以上的模拟代码肯定是不合理的，虽然主线程没阻塞，但新开了一个线程去阻塞等待结果，很显然是脱了裤子放屁的事

上面我说过，响应式可行的关键是：**你所需要的结果由服务方准备好后主动通知你**，所以从本质上来说，我们以上的例子最根源的发布者是数据库，如果我们给数据库发送请求，数据库准备好数据后主动通知我们，我们再去响应，这才是彻底拥有了响应式的价值~即节省资源

但~很显然我们常用的数据库大多数现阶段不会给我们提供这种服务，但也有特例，比如redis和mongodb就可以异步获取数据，那如果再结合我们的Reactor框架，才能真正做到响应式编程

下面就以redis为例，看看使用Reactor如何做到响应式读取redis数据

##### lettuce

这里介绍一个redis的客户端：lettuce，相比于Jedis这种老牌客户端，lettuce基于netty技术可以实现异步读取redis数据，lettuce更加先进，即便spring-redis的底层也从Jedis变成了lettuce

我们试着使用使用Reactor+Lettuce写一个SuggestionService(异步获取redis中存储的推荐栏目)

```java
public class ReactorRedisSuggestionService {

    private RedisURI redisUri = RedisURI.builder().withHost("127.0.0.1").withPort(6379).build();
    
    public Mono<String> getSuggestions() {
        return Mono.create(sink -> {
            RedisClient redisClient = RedisClient.create(redisUri); // 客户端
            StatefulRedisConnection<String, String> connection = redisClient.connect(); // 连接
            RedisAsyncCommands<String, String> asyncCommands = connection.async(); // 异步指令
            asyncCommands.get("favorites").thenAccept(favorites->{// 异步获取，key为favorites
                sink.success(favorites); // 返回数据后推送给mono
                connection.close();  // 关闭连接
                redisClient.shutdown();
            });
        });

    }
}
```

此时我们流程代码如下

```java
suggestionService.getSuggestions()
                .subscribe(System.out::println, error-> System.out.println("process error:"+error));
```

整个程序执行过程是这样的，主线程向redis发起读取数据请求，redis准备返回数据后交给lettuce的响应线程池中的子线程，子线程根据订阅的处理将结果输出

整个过程没有任何阻塞，也没有一点资源的浪费，是真真正正的响应式编程

实际上lettuce内部也集成了reactor框架，所以SuggestionService可以直接简化成这样

```java
public Mono<String> getSuggestions() {
        RedisReactiveCommands<String, String> reactiveCommands = connection.reactive(); // 响应式指令
        return reactiveCommands.get("favorites"); // key为favorites，返回的就是Mono
}
```

真的很方便的说

## 压测

接下来做一个小的压力测试，分别用同步和异步的方式获取redis数据，使用一个固定大小的线程池模拟处理请求的线程池，看看在同步和异步两种方式下这些线程池多久能从获取任务中释放出来干别的事情

首先是同步代码

```java
@Test
public void sync() {
    Executor pool = Executors.newFixedThreadPool(10); // 请求处理线程 10个
    RedisClient redisClient = RedisClient.create(redisUri);
    StatefulRedisConnection<String, String> connection = redisClient.connect();
    long startTime = System.currentTimeMillis();
    for (int i=0;i<30;i++) { // 模拟30个请求
        pool.execute(()->{
            connection.sync().get("favorites"); // 同步获取
        });
    }
    pool.execute(()->{
        // 执行到这里代表线程池的线程都释放出来了，可以做其它事情了，记录一下时间
        long endTIme = System.currentTimeMillis();
        System.out.println("free:" + (endTIme-startTime));
    });
    for(;;);
}
```

再看看异步代码

```java
@Test
public void async() {
    Executor pool = Executors.newFixedThreadPool(5); // 请求处理线程 5个
    ClientResources res = DefaultClientResources.builder().ioThreadPoolSize(5).build(); // 回调处理线程5个
    RedisClient redisClient = RedisClient.create(res, redisUri);
    StatefulRedisConnection<String, String> connection = redisClient.connect();
    long startTime = System.currentTimeMillis();
    for (int i=0;i<30;i++) { // 模拟30个请求
        pool.execute(()-> {
            RedisReactiveCommands<String, String> reactiveCommands = connection.reactive();
            reactiveCommands.get("favorites").subscribe(); // 异步获取
        });
    }
    // 执行到这里代表线程池的线程都释放出来了，可以做其它事情了，记录一下时间
    pool.execute(()->{
        long endTIme = System.currentTimeMillis();
        System.out.println("free:" + (endTIme-startTime));
    });
    for(;;);
}
```

这里为什么处理模拟请求的线程变成5个了呐，因为lettuce的异步处理回调还占用了5个，这样两种方式实际工作的线程数都是10个，比较公平(实际上异步代码还是吃点亏，因为回调处理的线程不能参与请求)

运行一下结果，差别很大：

- 同步输出：`free 3998` 接近3秒
- 异步输出：`free 11` 才11毫秒

这种差距数据量越大、带宽越低越明显，异步基本没有变化，而同步越来越大

**注意：异步加快数据读取速度，而是在等待数据过程中释放了资源，让CPU可以继续干其他的事，增加系统吞吐量**

## 使用

以上讨论了传统编程方式资源的浪费，以及响应式的种种好处，但为什么这东西还没大火以致彻底颠覆我们的编程方式呐，个人认为主要是以下几点(针对web服务端开发)

- 响应式的关键在于服务方的主动通知，最底层需要依靠[NIO技术](https://www.jianshu.com/p/939b7274efcd)，而我们数据库大多是不支持的
- 大多数情况下我们网络请求读取数据是比较快的，异步锁导致的线程切换反而更加浪费时间，就比如说上面的例子，你去餐厅点餐，如果餐厅做饭很快，你出去溜达等通知反而更浪费时间
- 我们一般写接口返回数据给前端展示，异步操作获取数据后的处理都是其他线程完成的，问题在于其他线程如何获取到当前连接并写回数据到http响应，这处理起来还是很麻烦的(也是WebFlux要解决的问题)
- 传统阻塞代码更易读且易调试
- 转换为非阻塞学习曲线陡峭，写出来的代码甚至不像是java语言...，有点类似node.js

因此我们传统的web服务端场景并不适合使用响应式编程，甚至在spring的WebFlux官网也不建议大家切换响应式编程，只有明确知道这样做可带来的性能提升时才会考虑使用它，比如你有一个微服务专门从redis或mongo这样的数据库读写很大数据缓存，就可以考虑使用它来减少资源浪费，再比如IO密集的网关服务，使用它就可以增加网关的吞吐量