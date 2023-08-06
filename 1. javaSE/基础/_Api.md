# Math

`java.lang.Math` 类包含用于执行基本数学运算的方法，如初等指数、对数、平方根和三角函数。类似这样的工具类，其所有方法均为静态方法，并且不会创建对象，调用起来非常简单。  

# Objects

在JDK7添加了一个Objects工具类，它提供了一些方法来操作对象，它由一些静态的实用方法组成，这些方法是nullsave（空指针安全的）或null-tolerant（容忍空指针的），用于计算对象的hashcode、返回对象的字符串表示形式、比较两个对象。  

# DigDecimal

### 使用

```java
    //四则运算
    @Test
    public void test1() {
        BigDecimal a = new BigDecimal("123");
        BigDecimal b = new BigDecimal("456");

        BigDecimal c = a.add(b);// 加
        BigDecimal d = a.subtract(b);// 减
        BigDecimal e = a.multiply(b);// 乘

        // 除
        // 参数2:指定精度,保留6位小数 ; 参数3: 选择舍入模式,此处为 四舍五入
        BigDecimal f = a.divide(b, 6, BigDecimal.ROUND_HALF_UP);

        // jdk 1.9中第三个参数 被RoundingMode取代
        //        BigDecimal f = a.divide(b, RoundingMode.DOWN);//舍弃小数位
        //        BigDecimal f = a.divide(b, 2, RoundingMode.HALF_DOWN);//2位小数;舍入模式为大于0.5进1，否则舍弃。


    }

    //比较大小
    @Test
    public void test2() {
        // 结果 : 1 表示 大于; 0 表示 等于; -1 表示 小于 .
        BigDecimal a1 = new BigDecimal(0.5);
        BigDecimal b1 = new BigDecimal(0.2);
        int c1 = a1.compareTo(b1); // 结果 C = 1
    }

    //正负
    @Test
    public void sign() {
        //返回 1 表示值 为正值 ;  0 表示 为 0 ;  -1 表示 负数 。
        BigDecimal a = new BigDecimal("123");
        int b = a.signum(); // 结果是 1
    }

    //构造器
    @Test
    public void constructTest() {
        //当我们用double类型的数据作为参数时，构造出的BigDecimal 对象value1并不能保证数据的准确性。
        //而用String作为参数时构造对象时，数据的准确性是有保证的。
        BigDecimal value1 = new BigDecimal(10.511);
        System.out.println("value1: " + value1);
        BigDecimal value2 = new BigDecimal("10.511");
        System.out.println("value2: " + value2);

    }


    //不可变
    @Test
    public void immutableTest() {
        BigDecimal count = new BigDecimal("1.3");
        BigDecimal add = count.add(new BigDecimal("9.2"));
        System.out.println("count:" + count);
        System.out.println("add:" + add);

    }
```

# [系统属性和环境变量](http://blog.xiayf.cn/2019/06/25/java-prop-env/)

2019-06-25 TueBy [xiayf](http://blog.xiayf.cn/author/xiayf.html)

原文：[Java System.getProperty vs System.getenv](https://www.baeldung.com/java-system-get-property-vs-system-getenv)

## 1、简介

Java 应用代码中会自动引入 `java.lang` 包。这个包包含很多常用的类，包括 `NullPointerException`、`Object`、`Math`、`String` 等等。

其中 `java.lang.System` 类是一个 final 类，这意味着开发者无法继承它，其所有方法都是静态的（static）。

System 类中有两个方法，分别来**读取系统属性（system properties）和环境变量（environment variables）**，下面我们来看看这两者的区别。

## System.getProperty()

Java 平台使用一个 `Properties` 对象来提供**本地系统相关的信息和配置**，我们称之为 **系统属性**。

系统属性包括当前用户、当前 Java 运行时版本 以及 文件路径分隔符诸如此类的信息。

如下代码中，我们使用 `System.getProperty("log_dir")` 来读取 *log_dir* 属性值。我们也会使用默认值参数，这样如果属性不存在，`getProperty` 则返回 */tmp/log*：

```
String log_dir = System.getProperty("log_dir", "/tmp/log");
```

如果希望在运行时变更系统属性，则可以使用 `System.setProperty` 方法：

```
System.setProperty("log_dir", "/tmp/log");
```

我们可以以如下格式使用命令行参数向应用传递指定属性或配置值：

```
java -jar jarName -DpropertyName=value
```

比如 将 app.jar 的 foo 属性值设置为 bar：

```java
java -jar app -Dfoo="bar"
```

System.getProperty 返回的一定是一个字符串。

## System.getenv()

环境变量是类似 Properties 的一些 键/值 对。许多操作系统都提供环境变量的方式向应用传递配置信息。

设置环境变量的方式，各操作系统之间有所不同。例如，Windows 中，我们使用控制面板中的系统工具（System Utility）应用来设置，而 Unix 系统则使用 shell 脚本。

**创建一个进程时，该进程默认会从其父进程继承一个克隆的上下文环境**。

如下代码片段演示：使用一个 lambda 表达式来输出所有环境变量。

```
System.getenv().forEach((k, v) -> {
    System.out.println(k + ":" + v);
});
```

**getenv() 返回一个只读的 `Map`**。尝试向该映射中添加值，会抛出 `UnsupportedOperationException` 异常。

可以使用变量名称作为参数调用 `getenv` 来获取单个变量值：

```
String log_dir = System.getenv("log_dir");
```

此外，我们可以在应用中创建一个新进程，并向其上下文环境中添加新的环境变量。

Java 中，我们使用 `ProcessBuilder` 类来创建新进程，该类有一个名为 `environment` 的方法，此方法返回一个 `Map`，不过这个映射不是只读的，这样就可以向其添加新元素：

```java
ProcessBuilder pb = new ProcessBuilder(args);
Map<String, String> env = pb.environment();
env.put("log_dir", "/tmp/log");
Process process = pb.start();
```

## 4、区别

这两者本质上都是提供 字符串类型 键值 信息的映射，区别在于：

1. 我们可以在运行时变更 系统属性（Properties），但是 环境变量（Environment Variables）仅是操作系统环境变量的一个不可变拷贝。（**cmd也体现了不可变，修改环境变量后需要重启cmd**）
2. 仅 Java 平台包含这个 系统属性 特性，而 环境变量 则是操作系统层面提供，全局可用的 - 运行在同一个机器上的所有应用都可以访问。
3. 系统属性 在打包应用时就必须存在[1](http://blog.xiayf.cn/2019/06/25/java-prop-env/#fn-1)，而 环境变量 则任意时刻都可以在操作系统中创建。

## 5、总结一下

虽然这两者在概念上比较相似，但是 系统属性 和 环境变量 的应用方式差别很大。

二选一通常考量的是生效范围。使用 环境变量，同一个应用可以部署到多个机器上运行不同的实例，并在操作系统级别或者在 AWS / Azure 云平台控制台中进行配置，以免更新配置时还得重新构建应用（**译注：其实使用 系统属性 也可以实现这个效果，比如在 shell 脚本中获取系统环境变量，然后作为系统属性通过 Java 命令行参数传递给应用**）。

`getProperty` 方法名称是驼峰风格，但 `getenv` 不是，谨记！

原文是这么写的，但我认为这句话有问题。系统属性明明可以在应用运行时通过命令行参数指定，也可以将属性文件打包到应用包中，在运行时加载（通过 System.getProperties().load 方法）。  

## 代码

![image-20200319112141998](img/image-20200319112141998.png)

# Class.isAssignableFrom与 instanceof 区别

1. isAssignableFrom 是用来判断一个类Class1和另一个类Class2是否相同或是另一个类的超类或接口。 

   通常调用格式是：Class1.isAssignableFrom (Class2) 

   	调用者和参数都是  java.lang.Class  类型。 

2. 而  instanceof  是用来判断一个对象实例是否是一个类或接口的或其子类子接口的实例。 

   格式是：  oo  instanceof  TypeName  

   ​	  第一个参数是对象实例名，第二个参数是具体的类名或接口名

```java
public class TestCase {  
    public static void main(String[] args) {  
        TestCase test = new TestCase();  
        test.testIsAssignedFrom1();  
        test.testIsAssignedFrom2();  
        test.testIsAssignedFrom3();  
        test.testInstanceOf1();  
        test.testInstanceOf2();  
    }  
  
    public void testIsAssignedFrom1() {  
        System.out.println(String.class.isAssignableFrom(Object.class));  
    }  
  
    public void testIsAssignedFrom2() {  
        System.out.println(Object.class.isAssignableFrom(Object.class));  
    }  
  
    public void testIsAssignedFrom3() {  
        System.out.println(Object.class.isAssignableFrom(String.class));  
    }  
  
    public void testInstanceOf1() {  
        String ss = "";  
        System.out.println(ss instanceof Object);  
    }  
  
    public void testInstanceOf2() {  
        Object o = new Object();  
        System.out.println(o instanceof Object);  
    }  
  
}  
//false  
//true  
//true  
//true  
//true  
```

# 执行外部程序和命令

### Runtime

[Java获取Windows系统指定软件进程号及启动软件](https://blog.csdn.net/loongshawn/article/details/53009445)

[Runtime.getRuntime().exec()](https://www.cnblogs.com/xinmengwuheng/p/5970255.html)

```java
public Process Runtime.getRuntime().exec(String[] cmdArray,String envp,File  dir) throws IOException; // 多个重载

//cmdarray - 包含所调用命令及其参数的数组。
//envp - 字符串数组，其中每个元素的环境变量的设置格式为 name=value，如果子进程应该继承当前进程的环境，或该参数为 null。
//dir - 子进程的工作目录；如果子进程应该继承当前进程的工作目录，则该参数为 null。
```

**bin/sh**

```java
Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c", "javap -l xxx > output.txt"});
```

**cmd**

```java
//打开记事本
String cmd = "cmd /k start notepad";
Process exec2 = Runtime.getRuntime().exec(cmd);
exec2.destroy();
```

> cmd /c dir 是执行完dir命令后关闭命令窗口。 
>
> cmd /k dir 是执行完dir命令后不关闭命令窗口。 
>
> cmd /c start dir 会打开一个新窗口后执行dir指令，原窗口会关闭。 
>
> cmd /k start dir 会打开一个新窗口后执行dir指令，原窗口不会关闭。 

### **Process**

```java
destroy()    //杀掉子进程
exitValue()    //返回子进程的出口值，值 `0` 表示正常终止
getErrorStream()    //获取子进程的错误流
getInputStream()    //获取子进程的输入流
getOutputStream()    //获取子进程的输出流
waitFor()    //导致当前线程等待，如有必要，一直要等到由该 `Process` 对象表示的进程已经终止。如果已终止该子进程，此方法立即返回。如果没有终止该子进程，调用的线程将被阻塞，直到退出子进程，根据惯例，`0` 表示正常终止
```

# main函数中args参数传递

.运行Java程序的同时，可以通过输入参数给main函数中的接收参数数组args[]，供程序内部使用

```java
package org.test;
public class Test {
	public static void main(String[] args) {
		System.out.println(args[0]);
		System.out.println(args[1]);
		System.out.println(args[2]);
	}
}

//运行： java org.test.Test aaa bbb ccc
```

# 退出程序

```java
//do something before terminate the jvm, if jvm process is kill , then won't help
Runtime.getRuntime().addShutdownHook(new Thread() {
    @Override
    public void run() {
        seleniumPool.close();
    }
});

//terminate the jvm
Runtime.getRuntime().exit(status);
```

# Stream

[Java 8 Stream(1)-流的使用及技巧](https://blog.csdn.net/weixin_45505313/article/details/103749552)

[Java 8 Stream(2)-原理解析](https://blog.csdn.net/weixin_45505313/article/details/106150967)

[13万字详细分析JDK中Stream的实现原理](https://www.throwx.cn/2021/10/06/stream-of-jdk/)

[Java Stream源码分析及知识点总结](https://blog.csdn.net/qq_36263268/article/details/113175067)

[google](https://www.google.com.hk/search?q=java+stream+%E6%9E%B6%E6%9E%84&newwindow=1&ei=NtO-Y970FIWhhwOw_L-gCg&ved=0ahUKEwje59-S6L_8AhWF0GEKHTD-D6QQ4dUDCA8&uact=5&oq=java+stream+%E6%9E%B6%E6%9E%84&gs_lcp=Cgxnd3Mtd2l6LXNlcnAQAzIFCCEQoAEyBQghEKABOgoIABBHENYEELADOgUIABCABEoECEEYAEoECEYYAFCOBViQEmCNFGgBcAF4AIABuAKIAdILkgEHMC40LjIuMZgBAKABAcgBAcABAQ&sclient=gws-wiz-serp)

[恕我直言你可能真的不会java第6篇：Stream性能差？不要人云亦云](https://zhuanlan.zhihu.com/p/150396560)

[Java 8 Stream的性能到底如何？](https://segmentfault.com/a/1190000004171551)

  

# 可变参数

可以采取数组方式传入或者多个参数。如果传入的是Object[] 数组的方式，那么JDK 拿到数组后就会拆分，这个时候new String[]{“1”,”2”}，就会拆分成了两个String类型的参数。因此想要传入数组参数的时候

```java
method.invoke(null,new String[]{"1","2"});

//改成：

method.invoke(null,new Object[]{new String[]{"1","2"}});
```

# Timer

[深入 Java Timer 定时调度器实现原理](https://juejin.cn/post/6844903741565435918)

[java.util.concurrent之ScheduledExecutorService——替代Timer，实现多线程任务调度](https://blog.csdn.net/weixin_41888813/article/details/90767979?depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-6&utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-6)

[Java 中的定时任务：Timer基础 Timer的缺陷 ScheduledExecutorService优势](https://blog.csdn.net/u010003835/article/details/71480565)

# hashcode

[浅谈Java中的hashcode方法 ](https://www.cnblogs.com/dolphin0520/p/3681042.html)



# 代理

https://blog.csdn.net/sbc1232123321/article/details/79334130

# JNDI

https://www.iteye.com/blog/shitou521-696006 | JNDI到底是什么，有什么作用 - 石头 - ITeye博客
https://blog.csdn.net/w372426096/article/details/80449710 | [分布式]：关于命名服务的知识点都在这里了_Franco蜡笔小强的博客-CSDN博客
https://www.zhihu.com/question/505406778 | 各种java中间件为什么要提供jndi的支持？ - 知乎
https://blog.csdn.net/wanxiaoderen/article/details/106638603 | b java 之JNDI介绍--- SPI机制 & Java.Util.serviceLoader_jndi spi_舞动的痞老板的博客-CSDN博客

# SecurityManager

https://nicky-chen.github.io/2018/07/13/java-securitymanager/ | Java安全之SecurityManager
https://www.cnblogs.com/yiwangzhibujian/p/6207212.html | java安全管理器SecurityManager入门 - 已往之不谏 - 博客园