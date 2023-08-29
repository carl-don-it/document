# 响应式编程之手写Reactor

![img](https://upload.jianshu.io/users/upload_avatars/9112801/5e61f24f-9781-41e5-9ff0-ad6ed47c3cb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)

[pq217](https://www.jianshu.com/u/14294ca8b186)关注IP属地: 广东

0.762023.02.19 08:12:25字数 2,239阅读 410

## 前言

前文提到了响应式编程，响应式规范Reactive streams，以及响应式扩展Reactor的简单使用

在使用Reactor时我一直很好奇，它是怎么做到的？

好奇心驱使我想要自己去写一个Reactive streams的实现，并参照Reactor源码来看看大神是如何实现的

话不多说，开始写代码(本文比较啰嗦，因为掺杂了自己的实现思路)

## 简单序列的发布者

看一下Reactor的一个简单例子

```java
Flux.just("a", "b", "c", "d").subscribe(new Subscriber<String>() {
    Subscription subscription;
    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        subscription.request(2);
    }
    @Override
    public void onNext(String s) {
        System.out.println(s);
        subscription.request(1);
    }
    @Override
    public void onError(Throwable t) {
    }
    @Override
    public void onComplete() {
        System.out.println("completed");
    }
});
```

在这个例子中，Reactor的Flux扮演了一个发布者，有固定的的发布序列：abcd，可以支持符合Reactive streams规范的`subscriber`订阅，并且支持背压

接下来就尝试自己写一个Reactive streams的`publisher`以及`subscription`，和Reactor实现一样的效果

##### v1

首先，实现一个publisher在订阅方法`subscribe`中调用subscriber的`onSubscribe`方法，并传递一个`subscription`作为参数

```java
public class PublisherV1<T> implements Publisher<T> {

    final T[] array;

    public PublisherV1(T... array) {
        this.array = array;
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        SubscriptionV1 subscription = new SubscriptionV1(array, s);
        s.onSubscribe(subscription);
    }
}
```

一个简单的发布者就实现了，接下来实现subscription，subscription两个方法，一个request(n)代表请求，一个cancel代表取消订阅

request(n)的实现也很简单，根据n的值循环调用subscriber的`onNext`方法，发布完成后调用`onComplete`，出现错误调用`onError`，为保证发布的顺序，用一个下标index标识当前已发布的位置

```java
class SubscriptionV1 implements Subscription {
    private Subscriber<? super T> subscriber; // 一次订阅对应一个订阅者
    final T[] array; // 序列
    private long index; // 当前位置
    private boolean cancelled; // 是否取消
    public SubscriptionV1(T[] array, Subscriber<? super T> subscriber) {
        this.array = array;
        this.subscriber = subscriber;
        this.index = 0;
    }
    @Override
    public void request(long n) { // 请求
        if (cancelled || index >= array.length) {
            return;
        }
        long fromIndex = index; // 开始位置
        long toIndex = fromIndex + n; // 结束位置
        boolean isComplete = false;
        if (toIndex >= array.length) {
            toIndex = array.length;
            isComplete = true;
        }
        index = toIndex; // 重新标识位置
        for (long i = fromIndex; i < toIndex; i++) { // 根据n循环发布
            subscriber.onNext(array[(int) i]);
        }
        if (isComplete) { // 完成
            subscriber.onComplete();
        }
    }
    @Override
    public void cancel() { // 取消
        this.cancelled = true;
    }
}
```

这个时候我感觉自己实现了，竟如此简单，试一下：

```java
public void test() {
    PublisherV1<String> publisher = new PublisherV1<>("a", "b", "c", "d");
    publisher.subscribe(new Subscriber<String>() {
        private Subscription subscription;
        @Override
        public void onSubscribe(Subscription s) {
            subscription = s;
            subscription.request(2); // 订阅之后申请2个
        }
        @Override
        public void onNext(String s) {
            System.out.println(s);
            subscription.request(1); // 处理完再申请
            
        }
        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
        }
        @Override
        public void onComplete() {
            System.out.println("complete");
        }
    });
}
```

结果如下

```java
a
c
d
complete
b
```

大概对了。。。，但这个顺序怎么回事，b为什么最后才输出，细想一下，原来循环的第一次(输出a)就递归调用了request(1)，输出c，d，complete，最后才到循环的第二个(输出b)

所以我的写法是简单，但没法保证发布顺序，肯定是不行

##### v2

为了解决这个问题，也是冥思苦想，想到一个思路：每次request把要推送的数据放入一个FIFO的队列，加入之后再依次取出队列中的所有数据调用onNext，这样即便递归了，递归内部的request依然要按照队列顺序依次发布，这样就保证了发布的顺序

```java
class SubscriptionV2 implements Subscription {
    //...其它一致省略
    Queue<T> queue = new ArrayDeque<>(); // 新增一个队列
    @Override
    public void request(long n) {
        //...与原逻辑一致省略
        for (long i = fromIndex; i < toIndex; i++) {
            queue.add(array[(int) i]); // 加入队列
        }
        T t; // 数据
        while ((t = queue.poll())!=null) { // 依次取出并发布
            subscriber.onNext(t);
        }
        if (isComplete) {
            subscriber.onComplete();
        }
    }
}
```

再测试一下，满足了输出的顺序a>b>c>d>complete

虽然当前基本满足了刚刚的需求，但我知道一定确实一个重要问题：线程安全

比如当订阅者使用其他线程响应时：

```java
new Subscriber<String>() {
    @Override
    public void onNext(String s) {
        executor.execute(()->{
            System.out.println(s);
            subscription.request(10);
        });
    }
}
```

这时，回头看我实现的request方法，线程安全问题有很多，多线程情况下可能会有很多线程同时调用request(n)方法，此时index的值，包括队列queue都是线程不安全的

想了很久，唯一能想出的办法就是粗暴的给request(n)方法加入synchronized锁，但这样做在多线程下就会造成阻塞：本身为了解决阻塞的响应式库结果自己的代码就存在阻塞，这实在让人无法接受

没办法，只能去看Reactor是如何实现的了，不看不知道，一看吓一跳，Reactor的解法实在是太牛了，大神就是大神

##### v3

Reactor中Flux.just生成的对象是`FluxArray`，它针对保证发布顺序且线程安全的解法是这样的：

- 同样Subscription也有一个`index`代表当前的读取位置
- Subscription中定义一个变量`requested`代表当前的请求个数，初始化0
- 当出现request(n)事件时，requested+=n，如果requested是从0变成n则开始走发布程序，在这个过程中，如果其他线程执行request(n)或者递归的noNext调用了request(n)，只是单纯的增加requested值
- 发布程序逻辑为，回调n次onNext方法，回调结束后看是否requested有新增，如果有新增(其他线程执行request或者noNext递归调用了request)，再次回调新增次数的onNext方法，结束后再次看是否requested有新增...，直到没有新增，修改index值避免下次重复读

这样做的好处是同一时间只有一个线程会执行发布逻辑，而且不会形成递归执行，一石二鸟，保证发布顺序的同时无锁无阻塞

我的解决思路是，既然竞争激励就加锁控制竞争，而原作的思路是，既然竞争激励就干脆不要竞争，只让发布方法在同一时间被执行一次，实在高明的太多

照着写一下

```java
static final class SubscriptionV3<T> implements Subscription {
    private Subscriber<? super T> subscriber;
    final T[] array;
    volatile long requested;
    volatile int index;
    volatile boolean cancelled;
    final AtomicLongFieldUpdater<SubscriptionV3> REQUESTED_UPDATER =
            AtomicLongFieldUpdater.newUpdater(SubscriptionV3.class, "requested");
    public SubscriptionV3(T[] array, Subscriber<? super T> subscriber) {
        this.array = array;
        this.subscriber = subscriber;
    }
    @Override
    public void request(long n) {
        if (addRequested(n) == 0) { // 只有从0->n才会发布
            slowPath(n);
        }
    }
    /**
     * 发布(同一时间只可能有一个线程运行这个方法，并且不会递归)
     * @param n
     */
    private void slowPath(long n) {
        int i = index; // 游标
        int e = 0; // 已发布数量
        int len = array.length; // 数组长度
        for (; ; ) {
            if (cancelled) { // 如果已取消
                return;
            }
            // 发布
            while (i!= len && e!=n) {
                subscriber.onNext(array[i]);
                if (cancelled) { // 如果已取消
                    return;
                }
                i++;
                e++;
            }

            // 已完成
            if (i== len) {
                subscriber.onComplete();
                return;
            }

            n = requested; // 重新或取requested值，因为noNext中可能会改变requested的值，如果有变化，再回到循环发布
            if (n == e) { // 已全发布完成
                index = i;
                // 减掉已发布的值，并重新获取结果(因为其他线程可能在上一步修改了requested)
                n = REQUESTED_UPDATER.addAndGet(this, -e);
                if (n==0) { // 确认没有修改，结束
                    return;
                }
                // 如果还有修改，剩下的值就是依然要发布的数量，重置已发布数量
                e = 0;
            }
        }
    }
    /**
     * 通过cas自旋增加requested个数
     * @param n 增加的个数
     * @return 原值
     */
    private long addRequested(long n) {
        long r, u;
        for (; ; ) {
            r = REQUESTED_UPDATER.get(this);
            if (r==Long.MAX_VALUE) { // 如果已经是最大值，就不要加了，避免出现负值
                return Long.MAX_VALUE;
            }
            u = r + n;
            if (REQUESTED_UPDATER.compareAndSet(this, r, u)) {
                return r;
            }
        }
    }
    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
```

这样就实现了保证发布顺序同时线程的数组发布者，Reactor源代码在`FluxArray.request(long n)`实现中，可自行查看(**源码区分了fastPath和slowPath，fastPath主要解决在一次性订阅所有时，不需要再考虑线程安全和递归了，直接依次执行onNext即可，比slowPath逻辑简单且要快点，效果一样的**)

##### 时序图

最后画一下整个过程的时序图



![img](https://upload-images.jianshu.io/upload_images/9112801-62d305bac99d22a0.png?imageMogr2/auto-orient/strip|imageView2/2/w/512/format/webp)

时序图

## 无背压的订阅者

看下Reactor的一般使用方法

```java
Flux.just("a", "b", "c", "d").subscribe(System.out::println);
```

这种订阅方式简单，不需要主动申请(无背压)，这其实是大部分使用场景

上文也提到过，Reactor其实是自己内部封装了一个订阅者，这个订阅者一但订阅成功就订阅所有数据：`subscription.request(Long.MAX_VALUE)`

因此我也试着封装一个这样的一次性订阅者，并把响应的处理暴露出去

第一步，封装一个一次性订阅最大数据的订阅者

```java
public class DisposableSubscriber<T> implements Subscriber<T> {
    private Consumer<T> consumer;
    private Runnable completeConsumer;
    public DisposableSubscriber(Consumer<T> consumer, Runnable completeConsumer) {
        this.consumer = consumer;
        this.completeConsumer = completeConsumer;
    }
    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE); // 一次性请求所有
    }
    @Override
    public void onNext(T t) {
        consumer.accept(t); // 执行consumer的accept
    }
    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }
    @Override
    public void onComplete() {
        completeConsumer.run(); //执行completeConsumer的run方法
    }
}
```

Publisher中新增订阅Consumer方法，如下

```java
public void subscribe(Consumer<? super T> consumer, Runnable completeConsumer) {
    DisposableSubscriber<? super T> subscriber = new DisposableSubscriber<>(consumer, completeConsumer);
    SubscriptionV3<T> subscription = new SubscriptionV3<>(array, subscriber);
    subscriber.onSubscribe(subscription);
}
```

此时再次使用方法如下：

```java
new PublisherV3<>("a", "b", "c", "d").subscribe(System.out::println, ()->{
    System.out.println("completed");
});
```

有点Reactor的味道了，比较简单不细说了，DisposableSubscriber对应源码的`LambdaSubscriber`

## 编程方式创建序列

上面我们的发布者是一个简单的固定序列，Reactor另一个主要场景是使用编程方式创建序列，如下

```java
Flux.create(sink -> {
    new Thread(()->{
        // 模拟去远程读取数据
        List<String> data = Arrays.asList("a", "b", "c");
        data.forEach(sink::next); // 依次发布
        sink.complete(); // 结束
    }).start();
}).subscribe(System.out::println, null, ()->{
    System.out.println("completed");
});
```

说白了，发布者不是无脑的根据request去发布数据，而是自己也会根据情况通过下沉器：`sink`来发布数据，`sink.next(t)`相当于向序列推送一个数据，request是订阅者向序列请求数据，`sink.complete()`方法通知订阅者已完成，next和complete的触发时机是通过编程方式用户自己定义的

这又是如何实现的呐？同样，我要先试着自己实现一下：

首先，但相比于固定序列，这种create方式的序列是变化的，`sink.next`是向序列里添加值，`request`是从序列里取值，两个方法完全可能不是同一个线程，对序列的操作一定会涉及线程安全问题

其次，`request`方法在没有可用值时，下一次执行`sink.next`应该主动调用`onNext`方法，这就导致需要记录`request`未满足的量，`sink.next`还要去读取这个量，多线程时依然存在线程的安全问题

首先实现了一个`sink`类，两个方法next和complete都是调用构造传入的回调

```java
public class Sink<T> {

    private Consumer<T> nextConsumer; // next回调

    private Runnable completeConsumer; // 完成回调

    public Sink(Consumer<T> nextConsumer, Runnable completeConsumer) {
        this.nextConsumer = nextConsumer;
        this.completeConsumer = completeConsumer;
    }

    public void next(T t) {
        nextConsumer.accept(t);
    }

    public void complete() {
        completeConsumer.run();
    }
}
```

接下来就是实现这个create对象， 有了上次的经验，这次我也花费了好长时间，也写出个无锁版本的create(没有考虑complete和cancel，也没有考虑多订阅者)

```java
public class PublisherCreate<T> implements Publisher<T> {

    private final Consumer<Sink<? super T>> sinkConsumer;

    private List<T> sequence = new CopyOnWriteArrayList<>(); // 序列;

    private Set<SubscriptionCreate> subscriptions = new HashSet<>(); // 所有订阅

    public PublisherCreate(Consumer<Sink<? super T>> sinkConsumer) {
        this.sinkConsumer = sinkConsumer;
    }

    private void onCompleted() {
    }

    private void onNext(T t) {
        sequence.add(t);
        subscriptions.forEach(v -> v.next(t));
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        SubscriptionCreate<T> subscription = new SubscriptionCreate<>(sequence, s);
        subscriptions.add(subscription);
        // 开始生产
        sinkConsumer.accept(new Sink<>(this::onNext, this::onCompleted));
        // 调用订阅回调
        s.onSubscribe(subscription);
    }

    public void subscribe(Consumer<? super T> consumer, Runnable completeConsumer) {
        DisposableSubscriber<? super T> subscriber = new DisposableSubscriber<>(consumer, completeConsumer);
        subscribe(subscriber);
    }

    static final class SubscriptionCreate<T> implements Subscription {

        Subscriber<? super T> subscriber;

        final List<T> sequence; // 序列;

        volatile int index;

        volatile long requested;

        volatile long stock;

        AtomicInteger nextRetry = new AtomicInteger(0);

        AtomicInteger requestRetry = new AtomicInteger(0);

        final AtomicLongFieldUpdater<SubscriptionCreate> REQUESTED_UPDATER =
                AtomicLongFieldUpdater.newUpdater(SubscriptionCreate.class, "requested");

        final AtomicLongFieldUpdater<SubscriptionCreate> STOCK_UPDATER =
                AtomicLongFieldUpdater.newUpdater(SubscriptionCreate.class, "stock");


        public SubscriptionCreate(List<T> sequence, Subscriber<? super T> subscriber) {
            this.sequence = sequence;
            this.subscriber = subscriber;
        }

        public void next(T t) {
            int tryI = 0;
            for (; ; ) {
                if (tryI++>0) {
                    log.warn("next retry: {}", nextRetry.incrementAndGet());
                }
                long stock = this.stock;
                if (!STOCK_UPDATER.compareAndSet(this, stock, stock + 1)) {
                    continue;
                }
                if (stock < 0) {
//                    log.info("next send");
                    subscriber.onNext(t);
                }
                break;
            }

        }

        @Override
        public void request(long n) {
            if (addRequested(n) == 0) {
                int i = index;
                int e = 0;
                for (; ; ) {
                    int tryI = 0;
                    while (e != n) {
                        if (tryI++>0) {
                            log.warn("request retry: {}", requestRetry.incrementAndGet());
                        }
                        long stock = this.stock;
                        if (!STOCK_UPDATER.compareAndSet(this, stock, stock - 1)) {
                            continue;
                        }
                        if (stock > 0) {
//                            log.info("request send");
                            subscriber.onNext(sequence.get(i));
                        }
                        tryI = 0;
                        i++;
                        e++;
                    }
                    n = requested;
                    if (n == e) {
                        n = REQUESTED_UPDATER.addAndGet(this, -e);
                        if (n == 0) {
                            index = i;
                            return;
                        }
                        e = 0;
                    }
                }
            }
        }

        private long addRequested(long n) {
            long r, u;
            for (; ; ) {
                r = REQUESTED_UPDATER.get(this);
                if (r == Long.MAX_VALUE) { // 如果已经是最大值，就不要加了
                    return Long.MAX_VALUE;
                }
                u = r + n;
                if (REQUESTED_UPDATER.compareAndSet(this, r, u)) {
                    return r;
                }
            }
        }

        @Override
        public void cancel() {
        }
    }
}
```

虽然差了很多功能，但也很复杂了已经，总的来说就是通过一个变量存储当前库存值：`stock`， 为正代表有库存，此时request直接调用`onNext`，为负代表request未满足的值，此时next方法直接调用`onNext`，并使用CAS自旋的方式确保线程安全

确实很复杂，就不展开了，反正莫得人看，使用效果如下

```java
new PublisherCreate<>(sink -> {
    new Thread(()->{
        // 假装去远程读取数据
        List<String> data = Arrays.asList("a", "b", "c");
        data.forEach(sink::next); // 发布
        sink.complete(); // 结束
    }).start();
}).subscribe(t-> System.out.println(t), null);
```

后续写了很多测试代码，各种情况都下没有出现线程安全问题，这里就不贴了

## 后续

写到这里真的没精力了，本来计划还要研究如下问题：

- create(编程方式创建序列)只是自己实现了一个残缺功能版，没有对照源码(大概看了一眼更复杂)
- Reactor的中间操作，比如take和map等

实在是研究不动了，只能说前期太天真了，能力有限，觉得自己的理解差不多了，就不深入了，有时间再补补