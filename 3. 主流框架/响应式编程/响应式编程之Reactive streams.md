# 响应式编程之Reactive streams

![img](https://upload.jianshu.io/users/upload_avatars/9112801/5e61f24f-9781-41e5-9ff0-ad6ed47c3cb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)

[pq217](https://www.jianshu.com/u/14294ca8b186)关注IP属地: 广东

0.7962023.02.14 22:03:59字数 1,291阅读 645

## 前言

上文简单介绍了响应式编程和Reactor的使用，今天开始深入了解一下响应式编程的规范，为开始学习Reactor源码做准备

## Reactive streams

上文也简单提到过，响应式的扩展库有很多比如RxJava、Reactor，这就给上层应用带来困扰，比如Spring中如果想支持响应式，那么到底是基于那个扩展库开发呐？如果选择了Reactor，今后想切换Reactor怎么办？

导致这种问题的根本原因在于没有同一的接口规范，就像jdk中有数据库驱动的接口 `Driver`，各种各样的数据库只要去实现它我们的程序就可以自由切换，写代码时也不用关心底层实现

所以一些业界大佬共同定制了一个响应式规范即 [Reactive Stream](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.reactive-streams.org%2F)，maven可以通过以下方式引入

```xml
<dependency>
    <groupId>org.reactivestreams</groupId>
    <artifactId>reactive-streams</artifactId>
    <version>1.0.3</version>
</dependency>
```

*在java9中，响应式规范已被加入JDK中*

接下来就结合Reactive streams接口代码看看到底规范了什么内容

##### 概念

从概念上讲，Reactive streams所描述的响应式编程就是“**发布者发布数据，订阅者根据发布结果作出对应响应**”

所以响应式编程的两个角色：**发布者** and **订阅者**

当然发布者和订阅者之间可以编排一些中间处理流程，这些中间过程对于上游来说是**订阅者**，对于下游来说是**发布者**，所以既是**发布者**又是**订阅者**

最后是**背压**的支持，所谓背压，并没有多么深奥，简单来说就是订阅者能控制发布者的发布速度，此时发布的主动权在订阅者手中，订阅者要多少，发布者就发布多少



![img](https://upload-images.jianshu.io/upload_images/9112801-de4514a448b8150b.png?imageMogr2/auto-orient/strip|imageView2/2/w/906/format/webp)

Backpressure

这张图比较贴切：

- 图1中，订阅者是人，发布者是水瓶，订阅者通过回给发布者压力让其发布(出水)，再做出响应(喝水)，发布的主动权在订阅者，这就是`Backpressure`(背压)
- 图2中，订阅者是人，发布者是喷头，喷头完全忽略订阅者的响应快慢，一个劲的喷水，导致订阅者响应不过来，发布的主动权在发布者

##### 接口

在看Reactive streams源码，发布者抽象: `Publisher`

```java
public interface Publisher<T> {
    // 订阅
    public void subscribe(Subscriber<? super T> s);
}
```

没错，作为一个发布者，最终要的方法是可以接受订阅，至于如何发布等留给实现者自己去实现

再看一下订阅者: `Subscriber`

```java
public interface Subscriber<T> {
    // 订阅成功事件
    public void onSubscribe(Subscription s);
    // 接收到新消息事件
    public void onNext(T t);
    // 异常处理
    public void onError(Throwable t);
    // 订阅完成、结束事件
    public void onComplete();
}
```

定义了订阅者要处理的四种事件，其实也间接的限定了订阅发布的模式

- 订阅成功时，会执行onSubscribe回调
- 发布新消息时，会执行onNext回调
- 发布出错时，会执行onError回调
- 订阅结束时，会执行onComplete回调

再看一下订阅成功回调的返回：`Subscription`，代表“本次订阅”，相当于一次成功订阅的订单，通过它订阅者可以向发布者请求n个数据或主动取消订阅，这就是对“背压”的支持

```java
public interface Subscription {
    // 请求n个数据
    public void request(long n);
    // 取消订阅
    public void cancel();
}
```

再看一下中间处理，上文说到它即是订阅者又是发布者：`Processor`

```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```

其实就是继承了订阅者和发布者

## Reactor

回到Reactor，它是Reactive streams的一个实现，看看它是如何实现响应式规范的

##### Publisher

首先Reactor的Flux和Mono其实就是Reactive streams中的`Publisher`，只不过一个会发布0-N个，一个会发布0-1个



![img](https://upload-images.jianshu.io/upload_images/9112801-2cb15620445c379b.png?imageMogr2/auto-orient/strip|imageView2/2/w/246/format/webp)

Flux

那么我们完全可以实现一个Reactive streams中的订阅者去订阅数据

```java
Flux.just("a", "b", "c").subscribe(new Subscriber<String>() {
    Subscription subscription;
    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        subscription.request(1); // 请求1个
    }

    @Override
    public void onNext(String s) {
        System.out.println(s); // 响应
        subscription.request(1); // 再请求1个
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("completed"); // 完成
    }
});
```

最终输出

```swift
a
b
c
completed
```

##### Consumer

再看一下下面这种一般订阅写法

```java
Flux.just("a", "b", "c", "d").subscribe(System.out::println);
```

此时subscribe订阅的订阅者是Reactor自己封装的订阅者: `Consumer`，在subscribe方法中最终会被转换为`LambdaSubscriber`



![img](https://upload-images.jianshu.io/upload_images/9112801-e24250521cd2cb46.png?imageMogr2/auto-orient/strip|imageView2/2/w/876/format/webp)

Consumer

而LambdaSubscriber就是Reactive streams中的`Subscriber`



![img]()

LambdaSubscriber

之所以使用Consumer不需要去执行`request(n)`是因为LambdaSubscriber在订阅成功时就`request(Long.MAX_VALUE)`(一次性订阅所有）



![img]()

request

所以说Reactor可以支持背压，但大部分常规写法是不考虑背压的，这主要因为一般场景真用不到

##### Processor

最后看一下reactor中的中间操作

```java
Flux.just("a", "b", "c", "d").take(3).subscribe(System.out::println);
```

Reactor的中间操作如take、map、flatMap等并没有实现Processor来做中间操作，而是自己定义了`Operator`中间操作

take:



![img](https://upload-images.jianshu.io/upload_images/9112801-a2eae155c267331f.png?imageMogr2/auto-orient/strip|imageView2/2/w/550/format/webp)

take

map:



![img](https://upload-images.jianshu.io/upload_images/9112801-c3a44f77cd44b8c9.png?imageMogr2/auto-orient/strip|imageView2/2/w/355/format/webp)

map

可能是Processor这个确实实现起来比较麻烦，而且其实主要符合发布订阅规范就基本可以了，Reactor还是支持使用Processor的，但最新版本的Reactor中Processor已被无情弃用

## 最后

下一篇开始研究Reactor是如何实现Reactive streams规范的并提供响应式编程支持的，从源码角度分析，并尝试自己写一个Reactive streams实现来对照Reactor源码