**为什么使用logback**

记得前几年工作的时候，公司使用的日志框架还是log4j，大约从16年中到现在，不管是我参与的别人已经搭建好的项目还是我自己主导的项目，日志框架基本都换成了logback，总结一下，logback大约有以下的一些优点：

- 内核重写、测试充分、初始化内存加载更小，这一切让logback性能和log4j相比有诸多倍的提升
- logback非常自然地直接实现了slf4j，这个严格来说算不上优点，只是这样，再理解slf4j的前提下会很容易理解logback，也同时很容易用其他日志框架替换logback
- logback有比较齐全的200多页的文档
- logback当配置文件修改了，支持自动重新加载配置文件，扫描过程快且安全，它并不需要另外创建一个扫描线程
- 支持自动去除旧的日志文件，可以控制已经产生日志文件的最大数量

总而言之，如果大家的项目里面需要选择一个日志框架，那么我个人非常建议使用logback。

 

**logback加载**

我们简单分析一下logback加载过程，当我们使用logback-classic.jar时，应用启动，那么logback会按照如下顺序进行扫描：

- 在系统配置文件System Properties中寻找是否有logback.configurationFile对应的value
- 在classpath下寻找是否有logback.groovy（即logback支持groovy与xml两种配置方式）
- 在classpath下寻找是否有logback-test.xml
- 在classpath下寻找是否有logback.xml

以上任何一项找到了，就不进行后续扫描，按照对应的配置进行logback的初始化，具体代码实现可见ch.qos.logback.classic.util.ContextInitializer类的findURLOfDefaultConfigurationFile方法。

当所有以上四项都找不到的情况下，logback会调用ch.qos.logback.classic.BasicConfigurator的configure方法，构造一个ConsoleAppender用于向控制台输出日志，默认日志输出格式为"%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"。

 

**logback的configuration**

logback的重点应当是Appender、Logger、Pattern，在这之前先简单了解一下logback的<configuration>，<configuration>只有三个属性：

- scan：当scan被设置为true时，当配置文件发生改变，将会被重新加载，默认为true
- scanPeriod：检测配置文件是否有修改的时间间隔，如果没有给出时间单位，默认为毫秒，当scan=true时这个值生效，默认时间间隔为1分钟
- debug：当被设置为true时，将打印出logback内部日志信息，实时查看logback运行信息，默认为false

 

**<logger>与<root>**

先从最基本的<logger>与<root>开始。

**<logger>用来设置某一个包或者具体某一个类的日志打印级别、以及指定<appender>**。<logger>可以包含零个或者多个<appender-ref>元素，标识这个appender将会添加到这个logger。<logger>仅有一个name属性、一个可选的level属性和一个可选的additivity属性：

- name：用来指定受此logger约束的某一个包或者具体的某一个类
- level：用来设置打印级别，五个常用打印级别从低至高依次为TRACE、DEBUG、INFO、WARN、ERROR，如果未设置此级别，那么当前logger会继承上级的级别
- additivity：是否向上级logger传递打印信息，默认为true

<root>也是<logger>元素，但是**它是根logger，只有一个level属性，因为它的name就是ROOT**，关于这个地方，有朋友微信上问起，源码在LoggerContext中：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 public LoggerContext() {
 2     super();
 3     this.loggerCache = new ConcurrentHashMap<String, Logger>();
 4 
 5     this.loggerContextRemoteView = new LoggerContextVO(this);
 6     this.root = new Logger(Logger.ROOT_LOGGER_NAME, null, this);
 7     this.root.setLevel(Level.DEBUG);
 8     loggerCache.put(Logger.ROOT_LOGGER_NAME, root);
 9     initEvaluatorMap();
10     size = 1;
11     this.frameworkPackages = new ArrayList<String>();
12 }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

Logger的构造函数为：

```
Logger(String name, Logger parent, LoggerContext loggerContext) {
    this.name = name;
    this.parent = parent;
    this.loggerContext = loggerContext;
}
```

看到第一个参数就是Root的name，而这个Logger.ROOT_LOGGER_NAME的定义为**final public String ROOT_LOGGER_NAME = "ROOT"**，由此可以看出<root>节点的name就是"ROOT"。

接着写一段代码来测试一下：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 public class Slf4jTest {
 2 
 3     @Test
 4     public void testSlf4j() {
 5         Logger logger = LoggerFactory.getLogger(Object.class);
 6         logger.trace("=====trace=====");  
 7         logger.debug("=====debug=====");  
 8         logger.info("=====info=====");  
 9         logger.warn("=====warn=====");  
10         logger.error("=====error=====");  
11     }
12     
13 }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

logback.xml的配置为：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 <?xml version="1.0" encoding="UTF-8" ?>
 2 <configuration scan="false" scanPeriod="60000" debug="false">
 3     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 4         <layout class="ch.qos.logback.classic.PatternLayout">
 5             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
 6         </layout>
 7     </appender>
 8     
 9     <root level="info">
10         <appender-ref ref="STDOUT" />
11     </root>
12     
13 </configuration>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

root将打印级别设置为"info"级别，<appender>暂时不管，控制台的输出为：

```
2018-03-26 22:57:48.779 [main] INFO  java.lang.Object - =====info=====
2018-03-26 22:57:48.782 [main] WARN  java.lang.Object - =====warn=====
2018-03-26 22:57:48.782 [main] ERROR java.lang.Object - =====error=====
```

logback.xml的意思是，当Test方法运行时，root节点将日志级别大于等于info的交给已经配置好的名为"STDOUT"的<appender>进行处理，"STDOUT"将信息打印到控制台上。

接着理解一下<logger>节点的作用，logback.xml修改一下，加入一个只有name属性的<logger>：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 <?xml version="1.0" encoding="UTF-8" ?>
 2 <configuration scan="false" scanPeriod="60000" debug="false">
 3 
 4     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 5         <layout class="ch.qos.logback.classic.PatternLayout">
 6             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
 7         </layout>
 8     </appender>
 9      
10     <logger name="java" />
11       
12      <root level="debug">
13          <appender-ref ref="STDOUT" />
14      </root>
15       
16 </configuration>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

注意这个**name表示的是LoggerFactory.getLogger(XXX.class)，XXX的包路径，包路径越少越是父级**，我们测试代码里面是Object.class，即name="java"是name="java.lang"的父级，root是所有<logger>的父级。看一下输出为：

```
2018-03-27 23:02:02.963 [main] DEBUG java.lang.Object - =====debug=====
2018-03-27 23:02:02.965 [main] INFO  java.lang.Object - =====info=====
2018-03-27 23:02:02.966 [main] WARN  java.lang.Object - =====warn=====
2018-03-27 23:02:02.966 [main] ERROR java.lang.Object - =====error=====
```

出现这样的结果是因为：

- <logger>中没有配置level，即继承父级的level，<logger>的父级为<root>，那么level=debug
- 没有配置additivity，那么additivity=true，表示此<logger>的打印信息向父级<root>传递
- 没有配置<appender-ref>，表示此<logger>不会打印出任何信息

由此可知，<logger>的打印信息向<root>传递，<root>使用"STDOUT"这个<appender>打印出所有大于等于debug级别的日志。举一反三，我们将<logger>的additivity配置为false，那么控制台应该不会打印出任何日志，因为<logger>的打印信息不会向父级<root>传递且<logger>没有配置任何<appender>，大家可以自己试验一下。

接着，我们再配置一个<logger>：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 <?xml version="1.0" encoding="UTF-8" ?>
 2 <configuration scan="false" scanPeriod="60000" debug="false">
 3  
 4     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 5         <layout class="ch.qos.logback.classic.PatternLayout">
 6             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
 7         </layout>
 8     </appender>
 9      
10     <logger name="java" additivity="false" />
11     <logger name="java.lang" level="warn">
12         <appender-ref ref="STDOUT" />
13     </logger>
14     
15     <root level="debug">
16         <appender-ref ref="STDOUT" />
17     </root>
18      
19 </configuration>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

如果读懂了上面的例子，那么这个例子应当很好理解：

- LoggerFactory.getLogger(Object.class)，首先找到name="java.lang"这个<logger>，将日志级别大于等于warn的使用"STDOUT"这个<appender>打印出来
- name="java.lang"这个<logger>没有配置additivity，那么additivity=true，打印信息向上传递，传递给父级name="java"这个<logger>
- name="java"这个<logger>的additivity=false且不关联任何<appender>，那么name="java"这个<appender>不会打印任何信息

由此分析，得出最终的打印结果为：

```
2018-03-27 23:12:16.147 [main] WARN  java.lang.Object - =====warn=====
2018-03-27 23:12:16.150 [main] ERROR java.lang.Object - =====error=====
```

举一反三，上面的name="java"这个<appender>可以把additivity设置为true试试看是什么结果，如果对前面的分析理解的朋友应该很容易想到，有两部分日志输出，一部分是日志级别大于等于warn的、一部分是日志级别大于等于debug的。

 

**<appender>**

接着看一下<appender>，<appender>是<configuration>的子节点，是负责写日志的组件。<appender>有两个必要属性name和class：

- name指定<appender>的名称
- class指定<appender>的全限定名

<appender>有好几种，上面我们演示过的是ConsoleAppender，**ConsoleAppender的作用是将日志输出到控制台**，配置示例为：

```
1 <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
2     <encoder>
3         <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
4     </encoder>
5 </appender>
```

其中，encoder表示对参数进行格式化。我们和上一部分的例子对比一下，发现这里是有所区别的，上面使用了<layout>定义<pattern>，这里使用了<encoder>定义<pattern>，简单说一下：

- <encoder>是0.9.19版本之后引进的，以前的版本使用<layout>，logback极力推荐的是使用<encoder>而不是<layout>
- 最常用的FileAppender和它的子类的期望是使用<encoder>而不再使用<layout>

关于<encoder>中的格式下一部分再说。接着我们看一下FileAppender，**FileAppender的作用是将日志写到文件中**，配置示例为：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
1 <appender name="FILE" class="ch.qos.logback.core.FileAppender">  
2     <file>D:/123.log</file>  
3     <append>true</append>  
4     <encoder>  
5         <pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>  
6     </encoder>  
7 </appender>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

它的几个节点为：

- <file>表示写入的文件名，可以使相对目录也可以是绝对目录，如果上级目录不存在则自动创建
- <appender>如果为true表示日志被追加到文件结尾，如果是false表示清空文件
- <encoder>表示输出格式，后面说
- <prudent>如果为true表示日志会被安全地写入文件，即使其他的FileAppender也在向此文件做写入操作，效率低，默认为false

接着来看一下RollingFileAppender，**RollingFileAppender的作用是滚动记录文件，先将日志记录到指定文件，当符合某个条件时再将日志记录到其他文件**，RollingFileAppender配置比较灵活，因此使用得更多，示例为：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
1 <appender name="ROLLING-FILE-1" class="ch.qos.logback.core.rolling.RollingFileAppender">   
2     <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">   
3         <fileNamePattern>rolling-file-%d{yyyy-MM-dd}.log</fileNamePattern>   
4         <maxHistory>30</maxHistory>    
5     </rollingPolicy>   
6     <encoder>   
7         <pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>   
8     </encoder>   
9 </appender>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

这种是仅仅指定了<rollingPolicy>的写法，<rollingPolicy>的作用是当发生滚动时，定义RollingFileAppender的行为，其中上面的TimeBasedRollingPolicy是最常用的滚动策略，它根据时间指定滚动策略，既负责滚动也负责触发滚动，有以下节点：

- <fileNamePattern>，必要节点，包含文件名及"%d"转换符，"%d"可以包含一个Java.text.SimpleDateFormat指定的时间格式，如%d{yyyy-MM}，如果直接使用%d那么格式为yyyy-MM-dd。RollingFileAppender的file子节点可有可无，通过设置file可以为活动文件和归档文件指定不同的位置
- <maxHistory>，可选节点，控制保留的归档文件的最大数量，如果超出数量就删除旧文件，假设设置每个月滚动且<maxHistory>是6，则只保存最近6个月的文件

向其他还有SizeBasedTriggeringPolicy，用于按照文件大小进行滚动，可以自己查阅一下资料。

 

**异步写日志**

日志通常来说都以文件形式记录到磁盘，例如使用<RollingFileAppender>，这样的话一次写日志就会发生一次磁盘IO，这对于性能是一种损耗，因此更多的，对于每次请求必打的日志（例如请求日志，记录请求API、参数、请求时间），我们会**采取异步写日志的方式而不让此次写日志发生磁盘IO，阻塞线程从而造成不必要的性能损耗**。（不要小看这个点，可以网上查一下服务端性能优化的文章，只是因为将日志改为异步写，整个QPS就有了大幅的提高）。

接着我们看下如何使用logback进行异步写日志配置：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 <?xml version="1.0" encoding="UTF-8" ?>
 2 <configuration scan="false" scanPeriod="60000" debug="false">
 3 
 4     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 5         <encoder>
 6             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
 7         </encoder>
 8     </appender>
 9     
10     <appender name="ROLLING-FILE-1" class="ch.qos.logback.core.rolling.RollingFileAppender">   
11         <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">   
12               <fileNamePattern>D:/rolling-file-%d{yyyy-MM-dd}.log</fileNamePattern>   
13               <maxHistory>30</maxHistory>    
14         </rollingPolicy>   
15         <encoder>
16               <pattern>%-4relative [%thread] %-5level %lo{35} - %msg%n</pattern>   
17         </encoder>   
18     </appender>
19     
20     <!-- 异步输出 -->  
21     <appender name ="ASYNC" class= "ch.qos.logback.classic.AsyncAppender">  
22         <!-- 不丢失日志.默认的,如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志 -->  
23         <discardingThreshold>0</discardingThreshold>  
24         <!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->  
25         <queueSize>256</queueSize>  
26         <!-- 添加附加的appender,最多只能添加一个 -->  
27         <appender-ref ref ="ROLLING-FILE-1"/>  
28     </appender>
29     
30     <logger name="java" additivity="false" />
31     <logger name="java.lang" level="DEBUG">
32         <appender-ref ref="ASYNC" />
33     </logger>
34     
35     <root level="INFO">
36         <appender-ref ref="STDOUT" />
37     </root>
38     
39 </configuration>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

即，我们引入了一个AsyncAppender，先说一下AsyncAppender的原理，再说一下几个参数：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
当我们配置了AsyncAppender，系统启动时会初始化一条名为"AsyncAppender-Worker-ASYNC"的线程

当Logging Event进入AsyncAppender后，AsyncAppender会调用appender方法，appender方法中再将event填入Buffer（使用的Buffer为BlockingQueue，具体实现为ArrayBlockingQueye）前，会先判断当前Buffer的容量以及丢弃日志特性是否开启，当消费能力不如生产能力时，AsyncAppender会将超出Buffer容量的Logging Event的级别进行丢弃，作为消费速度一旦跟不上生产速度导致Buffer溢出处理的一种方式。

上面的线程的作用，就是从Buffer中取出Event，交给对应的appender进行后面的日志推送

从上面的描述我们可以看出，AsyncAppender并不处理日志，只是将日志缓冲到一个BlockingQueue里面去，并在内部创建一个工作线程从队列头部获取日志，之后将获取的日志循环记录到附加的其他appender上去，从而达到不阻塞主线程的效果。因此AsyncAppender仅仅充当的是事件转发器，必须引用另外一个appender来做事。
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

从上述原理，我们就能比较清晰地理解几个参数的作用了：

- discardingThreshold，假如等于20则表示，表示当还剩20%容量时，将丢弃TRACE、DEBUG、INFO级别的Event，只保留WARN与ERROR级别的Event，为了保留所有的events，可以将这个值设置为0，默认值为queueSize/5
- queueSize比较好理解，BlockingQueue的最大容量，默认为256
- includeCallerData表示是否提取调用者数据，这个值被设置为true的代价是相当昂贵的，为了提升性能，默认当event被加入BlockingQueue时，event关联的调用者数据不会被提取，只有线程名这些比较简单的数据
- appender-ref表示AsyncAppender使用哪个具体的<appender>进行日志输出

 

**<encoder>**

<encoder>节点负责两件事情：

- 把日志信息转换为字节数组
- 把字节数组写到输出流

目前PatternLayoutEncoder是唯一有用的且默认的encoder，有一个<pattern>节点，就像上面演示的，用来设置日志的输入格式，使用“%+转换符"的方式，如果要输出"%"则必须使用"\%"对"%"进行转义。

<encoder>的一些可用参数用表格表示一下：

| **转换符**                        | **作  用**                                                   | **是否避免使用** |
| --------------------------------- | ------------------------------------------------------------ | ---------------- |
| c{length}lo{length}logger{length} | 输出日志的logger名称，可有一个整型参数来缩短<logger>名称，有几种情况：1、不输入表示输出完整的<logger>名称2、输入0表示只输出<logger>最右边点号之后的字符串3、输入其他数字表示输出小数点最后边点号之前的字符数量 | 否               |
| C{length}class{length}            | 输出指定记录的请求的调用者的全限定名，length同上             | 是               |
| d{pattern}date{pattern}           | 输出时间格式，模式语法同java.text.SimpleDateFormat兼容       | 否               |
| caller{depth}                     | 输出生成日志的调用者的位置信息，整数选项表示输出信息深度     | 否               |
| L                                 | 输出执行日志的请求行号                                       | 是               |
| mmsgmessage                       | 输出应用程序提供的信息                                       | 否               |
| m                                 | 输入执行日志请求的方法名                                     | 是               |
| n                                 | 输出平台相关的分行符"\n"或者"\r\n"，即换行                   | 否               |
| plelevel                          | 输出日志级别                                                 | 否               |
| rrelative                         | 输出从程序启动到创建日志记录的时间，单位为毫秒               | 否               |
| tthread                           | 输出产生日志的线程名称                                       | 否               |

看到最后一列是"是否避免使用"，这是因为这些信息是无法直接拿到的（比如请求行号、调用方法名），logback必须通过一些特殊手段去获取这些数据（比如在日志打印出产生一个堆栈信息），这种操作会比较影响效率，因此除非必要，否则不建议打印这些数据。

 

**Filter**

最后来看一下<filter>，<filter>是<appender>的一个子节点，表示在当前给到的日志级别下再进行一次过滤，最基本的Filter有ch.qos.logback.classic.filter.LevelFilter和ch.qos.logback.classic.filter.ThresholdFilter，首先看一下LevelFilter：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 <configuration scan="false" scanPeriod="60000" debug="false">
 2 
 3     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 4         <encoder>
 5             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
 6         </encoder>
 7         <filter class="ch.qos.logback.classic.filter.LevelFilter">
 8             <level>WARN</level>
 9             <onMatch>ACCEPT</onMatch>
10             <onMismatch>DENY</onMismatch>
11         </filter>
12     </appender>
13     
14     <logger name="java" additivity="false" />
15     <logger name="java.lang" level="DEBUG">
16         <appender-ref ref="STDOUT" />
17     </logger>
18     
19     <root level="INFO">
20         <appender-ref ref="STDOUT" />
21     </root>
22     
23 </configuration>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

看一下输出：

```
2018-03-31 22:22:58.843 [main] WARN  java.lang.Object - =====warn=====
```

看到尽管<logger>配置的是DEBUG，但是输出的只有warn，因为在<filter>中对匹配到WARN级别时做了ACCEPT（接受），对未匹配到WARN级别时做了DENY（拒绝），当然只能打印出WARN级别的日志。

再看一下ThresholdFilter，配置为：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 <?xml version="1.0" encoding="UTF-8" ?>
 2 <configuration scan="false" scanPeriod="60000" debug="false">
 3 
 4     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 5         <encoder>
 6             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
 7         </encoder>
 8         <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
 9             <level>INFO</level>
10         </filter>
11     </appender>
12     
13     <logger name="java" additivity="false" />
14     <logger name="java.lang" level="DEBUG">
15         <appender-ref ref="STDOUT" />
16     </logger>
17     
18     <root level="INFO">
19         <appender-ref ref="STDOUT" />
20     </root>
21     
22 </configuration>
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

看一下输出为：

```
2018-03-31 22:41:32.353 [main] INFO  java.lang.Object - =====info=====
2018-03-31 22:41:32.358 [main] WARN  java.lang.Object - =====warn=====
2018-03-31 22:41:32.359 [main] ERROR java.lang.Object - =====error=====
```

因为ThresholdFilter的策略是，会将日志级别小于<level>的全部进行过滤，因此虽然指定了DEBUG级别，但是只有INFO及以上级别的才能被打印出来。

**==================================================================================我不能保证写的每个地方都是对的，但是至少能保证不复制、不黏贴，保证每一句话、每一行代码都经过了认真的推敲、仔细的斟酌。每一篇文章的背后，希望都能看到自己对于技术、对于生活的态度。我相信乔布斯说的，只有那些疯狂到认为自己可以改变世界的人才能真正地改变世界。面对压力，我可以挑灯夜战、不眠不休；面对困难，我愿意迎难而上、永不退缩。其实我想说的是，我只是一个程序员，这就是我现在纯粹人生的全部。==================================================================================**