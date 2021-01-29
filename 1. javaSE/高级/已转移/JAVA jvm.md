# 一 ： java内存模型

原文链接：https://blog.csdn.net/Rainnnbow/article/details/50541079

参考：
《深入理解Java虚拟机-JVM高级特性与最佳实践》第二版 周志明著



## java数据类型

1.  基础数据类型:boolean、byte、short、char、int、long、float、double

2.  引用数据类型:类、接口、数组

   

## **前言**

Java程序的运行是通过JVM来实现的。通过类加载器将class字节码文件加载进JVM，然后根据预定的规则执行。Java虚拟机在执行Java程序的过程中会把它所管理的内存划分为若干个不同的数据区域。这些内存区域被统一叫做**运行时数据区**。Java运行时数据区大致可以划分为5个部分。如下图所示。在这里要特别指出，我们现在说的JVM内存划分是**概念模型**。具体到每个JVM的具体实现可能会有所不同。具体JVM的实现我只会提到**HotSpot虚拟机**的实现细节。
![è¿éåå¾çæè¿°](https://img-blog.csdn.net/20160119104827988)

## **1. 程序计数器**

程序计数器是一块较小的内存空间，它可以看成是当前线程所执行的字节码的**行号指示器**。如果是native方法，则计数器值为空。

- **存储**

程序计数器记录线程当前**要执行的下一条字节码指令的地址**。

由于Java是多线程的，所以为了多线程之间的切换与恢复，**每一个线程都需要单独的程序计数器**，各线程之间互不影响。这类内存区域被称为“线程私有”的内存区域。
由于程序计数器只存储一个字节码指令地址，故此内存区域没有规定任何OutOfMemoryError情况。

## 2. 虚拟机栈

Java虚拟机栈也是线程私有的，它的生命周期与线程相同。

- **存储**

虚拟机栈描述的是**Java方法执行的内存模型**：**每个方法在执行时都会创建一个栈帧（Stack Frame）**用于存储**局部变量表（this，形参，局部变量）、操作数栈、动态链接、方法出口**等信息。

*一个栈帧就代表了一个方法执行的内存模型*，虚拟机栈中存储的就是当前执行的所有方法的栈帧（包括正在执行的和等待执行的）。每一个方法从调用直至执行完成的过程，就对应着一个栈帧在虚拟机中入栈到出栈的过程。

我们平时所说的“局部变量存储在栈中”就是指**方法中的局部变量存储在代表该方法的栈帧的局部变量表**中。

而方法的执行正是从局部变量表中获取数据，放至操作数栈上，然后在操作数栈上进行运算，再将运算结果放入局部变量表中，最后将操作数栈顶的数据返回给方法的调用者的过程。（关于栈帧和基于栈的方法执行，我会在之后写两篇文章专门介绍。敬请期待☺）

虚拟机栈可能出现两种异常：由线程请求的**栈深度过大**超出虚拟机所允许的深度而引起的StackOverflowError异常；以及由虚拟机栈无法提供足够的内存而引起的OutOfMemoryError异常。

## 3. 本地方法栈

本地方法栈与虚拟机栈类似，他们的区别在于：本地方法栈用于执行本地方法（Native方法）；虚拟机栈用于执行普通的Java方法。在HotSpot虚拟机中，就将本地方法栈与虚拟机栈做在了一起。
本地方法栈可能抛出的异常同虚拟机栈一样。



## 4. 堆

Java堆是Java虚拟机所管理的内存中最大的一块。Java堆是被所有线程共享的一块内存区域，在虚拟机启动时创建。

### 4.1 存储

此内存区域的唯一目的就是**存放对象、数组实例**：所有的对象实例以及数组都要在堆上分配（The heap is the runtime data area from which memory for all class instances and arrays is allocated）。但**Class对象比较特殊，它虽然是对象，但是存放在方法区里**。在下面的方法区一节会介绍。

Java堆是垃圾收集器（GC）管理的主要区域。现在的收集器基本都采用分代收集算法：新生代和老年代。而对于不同的”代“采用的垃圾回收算法也不一样。一般新生代使用复制算法；老年代使用标记整理算法。对于不同的”代“，一般使用不同的垃圾收集器，新生代垃圾收集器和老年代垃圾收集器配合工作。(关于垃圾收集算法、垃圾收集器以及堆中具体的分代等知识，我之后会专门写几篇博客来介绍。再次敬请期待☺)

Java堆可以是物理上不连续的内存空间，只要**逻辑上连续即可**。Java堆可能抛出OutOfMemoryError异常。

### 4.2 两种定义

1. JVM规范对抽象的“Java heap”的定义是“存储Java对象的地方”，也就是说Java对象在哪里，哪里就是Java heap。
2. 堆指GC heap。

### 4.3 java7

- **Young 年轻区（代）**

Young区被划分为三部分，Eden区和两个大小严格相同的Survivor区，其中，Survivor区间中，某一时刻只有其中一个是被使用的，另外一个留做垃圾收集时复制对象用，始终保证一个survivor是空的。在Eden区间变满的时候， GC就会将存活的对象移到空闲的Survivor区间中，根据JVM的策略，在经过几次垃圾收集后，任然存活于Survivor的对象将被移动到Tenured区间。

- **Tenured 年老区**

Tenured区主要保存生命周期长的对象，一般是一些老的对象，当一些对象在Young复制转移一定的次数以后，对象就会被转移到Tenured区，一般如果系统中用了application级别的缓存，缓存中的对象往往会被转移到这一区间。

- **Perm 永久区**

Perm代主要保存class,method,filed对象，这部份的空间一般不会溢出，除非一次性加载了很多的类，不过在涉及到热部署的应用服务器的时候，有时候会遇到java.lang.OutOfMemoryError : PermGen space 的错误，造成这个错误的很大原因就有可能是每次都重新部署，但是重新部署后，类的class没有被卸载掉，这样就造成了大量的class对象保存在了perm中，这种情况下，一般重新启动应用服务器可以解决问题。

- **Virtual**

最大内存和初始内存的差值，就是Virtual区 

![1568353046092](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568353046092.png)

### 4.4 java8

java8的内存模型是由2部分组成，年轻代 + 年老代。
年轻代：Eden + 2*Survivor
年老代：OldGen
在jdk1.8中变化最大的Perm区，用Metaspace（元数据空间）进行了替换。
需要特别说明的是：Metaspace所占用的内存空间不是在虚拟机内部，而是在本地内存空间中，这也是与1.7的永久代最大的区别所在。 

![1568353242639](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568353242639.png)

![1568353252354](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568353252354.png)

## 5. 方法区

**方法区（method area）**只是**JVM规范**中定义的一个概念，具体放在哪里，不同的实现可以放在不同的地方。而**永久代**是**Hotspot**虚拟机特有的概念，是方法区的一种实现，别的JVM都没有这个东西。

方法区与Java堆一样，是各个线程共享的内存区域。

### 5.1. 存储

它用于存储已**被虚拟机加载的类信息（元数据）、常量信息、静态变量、即时编译器编译后的代码等数据**。所有的字节码被加载之后，字节码中的信息：类信息、类中的方法信息、常量信息、类中的静态变量（引用）等都会存放在方法区。正如其名字一样：方法区中存放的就是类和方法的所有信息。此外，如果一个类被加载了，就会在方法区生成一个代表该类的Class对象（唯一一种不在堆上生成的对象实例）该对象将作为程序访问方法区中该类的信息的外部接口。有了该对象的存在，才有了反射的实现。

------



### 5.2. 本地内存与堆外内存

`Native memory：本地内存，也称为C-Heap，是供JVM自身进程使用的。`

**判断**：永久代是Hotspot虚拟机特有的概念，是方法区的一种实现，属于本地内存（堆外内存）的一部分。别的JVM都没有这个东西。

1. 而永久代是Hotspot虚拟机特有的概念，是方法区的一种实现------------这个正确

2. 属于本地内存（堆外内存）的一部分-------------------------这个需要定义简写为“堆”的概念在上下文中的定义。

> 如果“堆”是说Java heap，那么这个也对也错。因为JVM规范对抽象的“Java heap”的定义是“存储Java对象的地方”，也就是说Java对象在哪里，哪里就是Java heap。HotSpot的PermGen里是会存储部分Java对象的，例如说一些java.lang.String实例。这些String实例占的部分就得算是Java heap。
>
> 如果“堆”是说GC heap，那么这个错误。PermGen是HotSpot的GC heap的一部分。
>
> 一般说“native memory”都是跟GC heap相对的，所以一般取上述后者的定义，于是这个“属于堆外内存”的说法就不对。

3. 别的JVM都没有这个东西。

> 嗯…虽然不一定有PermGen，但确实存在许多其它JVM是把元数据放在GC heap里的。



------



### 5.3. java7之前

> 在Java 6中，方法区中包含的数据，除了JIT编译生成的代码存放在**native memory的CodeCache区域**（不直接由GC收集），其他都存放在永久代。
>
> 永久代和堆相互隔离，体现在永久代的大小在启动JVM时可以设置一个固定值，不可变。
>
> 在Java7之前，HotSpot虚拟机中将GC分代收集扩展到了方法区，使用永久代来实现了方法区（permgen的任务不止于此）。这个区域的内存回收目标主要是针对常量池的回收和对类型的卸载。但是在之后的HotSpot虚拟机实现中，逐渐开始将方法区从永久代移除。
>
> - **Perm Gen**
>
> Perm Gen全称是Permanent Generation space，是指内存的永久保存区域，因而称之为永久代。这个内存区域用于存放Class和Meta的信息，Class在被 Load的时候被放入这个区域。因为Perm里存储的东西**有一部分**永远不会被JVM垃圾回收的，所以如果你的应用程序LOAD很多CLASS的话，就很可能出现PermGen space错误。默认大小为物理内存的1/64。
>
> - **CodeCache**
>
> CodeCache代码缓冲区的大小在client模式下默认最大是32m，在server模式下默认是48m，这个值也是可以设置的，它所对应的JVM参数为ReservedCodeCacheSize 和 InitialCodeCacheSize。
>
> CodeCache缓存区是可能被充满的，当CodeCache满时，后台会收到CodeCache is full的警告信息，如下所示：“CompilerThread0” java.lang.OutOfMemoryError: requested 2854248 bytes for Chunk::new. Out of swap space?
> 
>
> GC heap区又分为：
>
> - Eden Space（伊甸园）
> - Survivor Space(幸存者区)
> - Old Gen（老年代）
> - Perm Gen（永久代）
>
> 非GC heap区又分：
>
> - Code Cache(代码缓存区)
> - Jvm Stack(java虚拟机栈)
> - Local Method Statck(本地方法栈)*

------

### 5.4. java7

> 在Java 7中，存储在永久代的部分数据就已经转移到Java Heap或者Native memory。但永久代仍存在于JDK 1.7中，并没有完全移除。
>
> 1. 符号引用**Symbol的存储**从PermGen移动到了native memory；
> 2. 字符串常量池(interned strings)转移到了Java heap，
> 3. 并且把静态变量从instanceKlass末尾（位于PermGen内）移动到了java.lang.Class对象的末尾（位于普通Java heap内）；
>
> **判断**：Java7中已经将运行时常量池从永久代移除，在Java 堆（Heap）中开辟了一块区域存放运行时常量池。---------不对！
>
> “常量池”如果说的是SymbolTable / StringTable，这俩table自身原本就一直在native memory里，是它们所引用的东西在哪里更有意思。上面说了，java7是把SymbolTable引用的Symbol移动到了native memory，而StringTable引用的java.lang.String实例则从PermGen移动到了普通Java heap。
>
> StringTable的长度可以通过参数-XX:StringTableSize指定。
>
> Symbols在JDK7和JDK8里都在native memory里，但不在Metaspace里。
> Symbols在native memory里通过引用计数管理，同时有个全局的SymbolTable管理着所有Symbol。请参考 [jdk7u/jdk7u/hotspot: 2cd3690f644c src/share/vm/oops/symbol.hpp](http://link.zhihu.com/?target=http%3A//hg.openjdk.java.net/jdk7u/jdk7u/hotspot/file/2cd3690f644c/src/share/vm/oops/symbol.hpp)

**在JDK1.7中, 已经把原本放在永久代的字符串常量池移出, 放在堆中. 为什么这样做呢?**

> 因为使用永久代来实现方法区不是个好主意, 很容易遇到内存溢出的问题. 我们通常使用PermSize和MaxPermSize设置永久代的大小, 这个大小就决定了永久代的上限, 但是我们不是总是知道应该设置为多大的, 如果使用默认值容易遇到OOM错误。
>
> 类的元数据, 字符串池, 类的静态变量将会从永久代移除, 放入Java heap或者native memory。其中建议JVM的实现中将类的元数据放入 native memory, 将字符串池和类的静态变量放入java堆中. 这样可以加载多少类的元数据就不再由MaxPermSize控制, 而由系统的实际可用空间来控制.
>
> 为什么这么做呢? 减少OOM只是表因, 更深层的原因还是要合并HotSpot和JRockit的代码, JRockit从来没有一个叫永久代的东西, 但是运行良好, 也不需要开发运维人员设置这么一个永久代的大小。当然不用担心运行性能问题了, 在覆盖到的测试中, 程序启动和运行速度降低不超过1%, 但是这一点性能损失换来了更大的安全保障。
> 

------

### 5.5. java8

> 而在Java8中，已经彻底没有了永久代，将方法区直接放在一个与堆不相连的本地内存区域，这个区域被叫做元空间Metaspace，基本上机器有多少内存就可以多大。关于元空间的更多信息，请参考：[Java永久代去哪儿了](http://www.infoq.com/cn/articles/Java-PERMGEN-Removed?utm_campaign=infoq_content&)
>
> ‑XX:MaxPermSize 参数失去了意义，取而代之的是-XX:MaxMetaspaceSize。

**为什么移除永久代**
1、字符串存在永久代中，容易出现性能问题和内存溢出。
2、永久代大小不容易确定，PermSize指定太小容易造成永久代OOM
3、永久代会为 GC 带来不必要的复杂度，并且回收效率偏低。
4、Oracle 可能会将HotSpot 与 JRockit 合二为一。

![1568274376523](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568274376523.png)

## 6. 运行时常量池

运行时常量池是方法区的一部分，关于运行时常量池的介绍，请参考我的另一篇博文：[String放入运行时常量池的时机与String.intern()方法解惑](http://blog.csdn.net/rainnnbow/article/details/50461303)。我还是花了些时间在理解运行时常量池上的。

## 7. 直接内存（还没熟悉）

原文链接：https://blog.csdn.net/leaf_0303/article/details/78961936

博文：<https://www.cnblogs.com/xing901022/p/5243657.html>

**概述**

直接内存并不是虚拟机运行时数据区的一部分，也不是Java 虚拟机规范中农定义的内存区域。在JDK1.4 中新加入了NIO(New Input/Output)类，引入了一种基于通道(Channel)与缓冲区（Buffer）的I/O 方式，它可以使用native 函数库直接分配堆外内存，然后通脱一个存储在Java堆中的DirectByteBuffer 对象作为这块内存的引用进行操作。这样能在一些场景中显著提高性能，因为避免了在Java堆和Native堆中来回复制数据。

- 本机直接内存的分配不会受到Java 堆大小的限制，受到本机总内存大小限制

- 配置虚拟机参数时，不要忽略直接内存 防止出现OutOfMemoryError异常


**直接内存（堆外内存）与堆内存比较**

1. 直接内存申请空间耗费更高的性能，当频繁申请到一定量时尤为明显
2. 直接内存IO读写的性能要优于普通的堆内存，在多次读写操作的情况下差异明显

> 如上文介绍的：Java8以及之后的版本中方法区已经从原来的JVM运行时数据区中被开辟到了一个称作元空间的直接内存区域。

## 8. 内存图

### 1. 对象内存图

![1568453154791](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568453154791.png)

对象调用方法时，根据对象中方法标记（地址值），去类中寻找方法信息。这样哪怕是多个对象，方法信息
只保存一份，节约内存空间 

### 2. 静态原理内存图解 

​	**`static` 修饰的内容**：

- 是随着类的加载而加载的，且只加载一次。

- 存储于一块固定的内存区域（静态区），所以，可以直接被类名调用。

- 它优先于对象存在，所以，可以被所有对象共享。 

![1568453279973](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568453279973.png)

### 3. super和this内存图 

- **super和this的含义**

super ：代表父类的存储空间标识(可以理解为父亲的引用)。
this ：代表当前对象的引用(谁调用就代表谁)。 

- **父类空间优先于子类对象产生**

在每次创建子类对象时，先初始化父类空间，再创建其子类对象本身。目的在于子类对象中包含了其对应的父类空
间，便可以包含其父类的成员，如果父类成员非private修饰，则子类可以随意使用父类成员。代码体现在子类的构造方法调用时，一定先调用父类的构造方法。理解图解如下： 

![1568453325019](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568453325019.png)



![1568453386682](C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568453386682.png)

# 二 ：String问题



## 1. String str = new String("abc")内存分配问题

```java
String str = new String("abc");
```

1. 首先将这行代码分成String str、=、"abc"和new String()四部分来看待。String str只是定义了一个名为str的String类型的变量，因此它并没有创建对象；**=是对变量str进行初始化，将某个对象的引用（或者叫句柄）赋值给它**，显然也没有创建对象；现在只剩下new String(“abc”)了。先看一下new String()构造函数：

```java
 public String(String original) {
    // other code
 }
```

2. 我们是使用new调用了String类的上面那个构造器方法创建了一个对象，并将它的引用赋值给了str变量。但是发现该构造函数的参数是一个String类型的，我们要知道String本身就是一个对象。而该对象正是“abc”。

所以得出结论，**这行代码一共创建了两个对象**（运行时常量池不存在“abc”的情况下），一个是str引用所指向在堆内存中的对象，一个是常量池中的“abc”；

该行代码内存分配问题，可以参考这篇文章：https://blog.csdn.net/qq_28082757/article/details/89886132

## 2. String类为什么是不可变的

- final的出现就是为了为了不想改变，而不想改变的理由有两点：**设计(安全)或者效率**。

final 修饰的类是不被能继承的，所以 final 修饰的类是不能被篡改的。内部数组私有。
了解了这一点，我们再看看问题：

1. 从设计安全)上讲， 
   1)、确保它们不会在子类中改变语义。String类是final类，这意味着不允许任何人定义String的子类。
   

2)、String 一旦被创建是不能被修改的，内部通过多种机制确定，不只是final。可以暴力破解不可变。

2. 从效率上讲： 
   1)、设计成final，JVM才不用对相关方法在虚函数表中查询，而直接定位到String类的相关方法上，提高了执行效率。 
   2)、Java设计者认为共享带来的效率更高。

————————————————

版权声明：本文为CSDN博主「李学凯」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_27093465/article/details/52190915dfg 

------

- **hashSet使用可变类作key**

```java
class Test {
    public static void main(String[] args) {
        HashSet<StringBuilder> hs = new HashSet<StringBuilder>();
        StringBuilder sb1 = new StringBuilder("aaa");
        StringBuilder sb2 = new StringBuilder("aaabbb");
        hs.add(sb1);
        hs.add(sb2); //这时候HashSet里是{"aaa","aaabbb"}
        StringBuilder sb3 = sb1;
        sb3.append("bbb"); //这时候HashSet里是{"aaabbb","aaabbb"}
        System.out.println(hs);
    }
}
//Output:
//[aaabbb, aaabbb]
```

StringBuilder型变量sb1和sb2分别指向了堆内的字面量"aaa"和"aaabbb"。把他们都插入一个HashSet。到这一步没问题。但如果后面我把变量sb3也指向sb1的地址，再改变sb3的值，因为StringBuilder没有不可变性的保护，sb3直接在原先"aaa"的地址上改。导致sb1的值也变了。这时候，HashSet上就出现了两个相等的键值"aaabbb"。**破坏了HashSet键值的唯一性**。所以**千万不要用可变类型做HashMap和HashSet键值。**

## 3. String test **=** "a" **+** "b" **+** "c"在字符串常量池中保存几个引用

```java
`String test = "a" + "b" + "c"; `
```

答案是只创建了一个对象，在常量池中也只保存一个引用。我们使用javap反编译看一下即可得知。

```java
`17:02 $ javap -c TestInternedPoolGC Compiled from "TestInternedPoolGC.java" public class TestInternedPoolGC extends java.lang.Object{ public TestInternedPoolGC();   Code:    0:  aload_0    1:  invokespecial    #1; //Method java/lang/Object."<init>":()V    4:  return  public static void main(java.lang.String[])   throws java.lang.Exception;   Code:    0:  ldc  #2; //String abc    2:  astore_1    3:  return `
```

看到了么，实际上在编译期间，已经将这三个字面量合成了一个。这样做实际上是一种优化，避免了创建多余的字符串对象，也没有发生字符串拼接问题。

# 三 ： 基本类型包装类常量池

## 简述

java中基本类型的包装类的大部分都实现了常量池技术,即除了两种浮点数类型外的其余六种：Character,Byte,Short,Integer,Long,Boolean.但是需要注意，除了Boolean之外的五种封装类只有在[-128,127]范围内才在常量池内有对象。

## 1. Integer

```java
public class test {  
    public static void main(String[] args) {      
        objPoolTest();  
    }  
  
    public static void objPoolTest() {  
        int i = 40;  
        int i0 = 40;  
        Integer i1 = 40;  
        Integer i2 = 40;  
        Integer i3 = 0;  
        Integer i4 = new Integer(40);  
        Integer i5 = new Integer(40);  
        Integer i6 = new Integer(0);  
        Double d1=1.0;  
        Double d2=1.0;  
          
        System.out.println("i=i0\t" + (i == i0));  
        System.out.println("i1=i2\t" + (i1 == i2));  
        System.out.println("i1=i2+i3\t" + (i1 == i2 + i3));  
        System.out.println("i4=i5\t" + (i4 == i5));  
        System.out.println("i4=i5+i6\t" + (i4 == i5 + i6));      
        System.out.println("d1=d2\t" + (d1==d2));   
         
        System.out.println();          
    }  
}  
i=i0    true  
i1=i2   true  
i1=i2+i3        true  
i4=i5   false  
i4=i5+i6        true  
d1=d2   false  
————————————————
版权声明：本文为CSDN博主「buder得儿得儿以得儿以得儿得儿」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/cpcpcp123/article/details/51285312
```

**结果分析**：

1. i和i0均是普通类型(int)的变量，所以数据直接存储在栈中，而栈有一个很重要的特性：**栈中的数据可以共享**。当我们定义了int i = 40;，再定义int i0 = 40;这时候会自动检查栈中是否有40这个数据，如果有，i0会直接指向i的40，不会再添加一个新的40。

2. i1和i2均是引用类型，在栈中存储指针，因为Integer是包装类。由于**Integer包装类实现了常量池技术**，因此i1、i2的40均是从常量池中获取的，均指向同一个地址，因此i1=12。

3. 很明显这是一个加法运算，Java的数学运算都是在栈中进行的，Java会自动对i1、i2进行**拆箱操作转化成整型**，因此i1在数值上等于i2+i3。

4. i4和i5均是引用类型，在栈中存储指针，因为Integer是包装类。但是由于他们各自都是new出来的，因此不再从常量池寻找数据，而是从堆中各自new一个对象，然后各自保存指向对象的指针，所以i4和i5不相等，因为**Java中【==】比较的是两个对象是否是同一个引用（即比较内存地址）**，他们所存指针不同，所指向对象不同。

5. 这也是一个加法运算，和3同理。

6. d1和d2均是引用类型，在栈中存储指针，因为Double是包装类。但**Double包装类没有实现常量池技术**，因此Doubled1=1.0;相当于Double d1=new Double(1.0);，是从堆new一个对象，d2同理。因此d1和d2存放的指针不同，指向的对象不同，所以不相等。
   

## 2. 面试问题

> 问：两个new Integer 128相等吗？
>
> 答：不。因为Integer缓存池默认是-127-128；

> 问：可以修改Integer缓存池范围吗？如何修改？
>
> 答：可以。使用`-Djava.lang.Integer.IntegerCache.high=300`设置Integer缓存池大小

> 问：Integer缓存机制使用了哪种设计模式？
>
> 答：亨元模式；

> 问：Integer是如何获取你设置的缓存池大小？
>
> 答：`sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");`

> 问：`sun.misc.VM.getSavedProperty`和`System.getProperty`有啥区别？
>
> 答：唯一的区别是，`System.getProperty`只能获取非内部的配置信息；例如`java.lang.Integer.IntegerCache.high`、`sun.zip.disableMemoryMapping`、`sun.java.launcher.diag`、`sun.cds.enableSharedLookupCache`等不能获取，这些只能使用`sun.misc.VM.getSavedProperty`获取

后续还有	https://article.itxueyuan.com/vkm172

# 四 ：符号引用

符号引用，顾名思义，就是一个符号，符号引用被使用的时候，才会解析这个符号。如果熟悉linux或unix系统的，可以把这个符号引用看作一个文件的软链接，当使用这个**软连接**的时候，**才会真正解析它，展开它找到实际的文件**

对于符号引用，在**类加载**层面上讨论比较多，源码级别只是一个形式上的讨论。

当一个类被加载时，该类所用到的别的类的符号引用都会保存在常量池，实际代码执行的时候，首次遇到某个别的类时，JVM会对常量池的该类的符号引用展开，转为直接引用，这样下次再遇到同样的类型时，JVM就不再解析，而直接使用这个已经被解析过的直接引用。

除了上述的类加载过程的符号引用说法，对于**源码级别**来说，就是依照引用的解析过程来区别代码中某些数据属于符号引用还是直接引用，如，System.out.println("test" +"abc");//这里发生的效果相当于直接引用，而假设某个Strings = "abc"; System.out.println("test" + s);//这里的发生的效果相当于符号引用，即把s展开解析，也就相当于s是"abc"的一个符号链接，也就是说在编译的时候，class文件并没有直接展看s，而把这个s看作一个符号，在实际的代码执行时，才会展开这个。


# 常量池

## 简述

在class文件中存在一个常量池，里面主要放的字面量和符号引用

方法区中有一个常量池叫做运行时常量池

native memory有个常量池叫做字符串常量池（StringTable），是全局共享的。

> 加载class文件时，class文件中的常量池中的大部分东东会进入运行时常量池，但String是“进入”字符串常量池。这个进入为什么要加引号呢，最上面已经说了，**字符串本身是在堆中**（和其他一般对象一个鸟样，刚出来的时候很大可能是在eden中），然后在StringTable中有指向它的引用。

![img](https://pic1.zhimg.com/80/v2-5c28496e75bc6012e61509408115221a_hd.jpg)

> 作者：匿名用户链接：https://www.zhihu.com/question/55328596/answer/144027981来源：知乎著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

## 1、string literal pool

Java中字符串对象创建有两种形式，一种为字面量形式，如`String str = "droid";`，另一种就是使用**new**这种标准的构造对象的方法，如`String str = new String("droid");`，这两种方式我们在代码编写时都经常使用，尤其是字面量的方式。然而这两种实现其实存在着一些性能和内存占用的差别。这一切都是源于JVM为了减少字符串对象的重复创建，其维护了一个特殊的内存，这段内存被成为字符串常量池或者字符串字面量池。

### 工作原理

当代码中出现字面量形式创建字符串对象时，JVM首先会对这个字面量进行检查，如果字符串常量池中存在相同内容的字符串对象的引用，则将这个引用返回，否则新的字符串对象被创建，然后将这个引用放入字符串常量池，并返回该引用。

------

### 举例说明

- **字面量创建形式**

```JAVA
`String str1 = "droid"; `
```

JVM检测这个字面量，这里我们认为没有内容为`droid`的对象存在。JVM通过字符串常量池查找不到内容为`droid`的字符串对象存在，那么会创建这个字符串对象，然后将刚创建的对象的引用放入到字符串常量池中,并且将引用返回给变量str1。

如果接下来有这样一段代码

```JAVA
`String str2 = "droid"; `
```

同样JVM还是要检测这个字面量，JVM通过查找字符串常量池，发现内容为”droid”字符串对象存在，于是将已经存在的字符串对象的引用返回给变量str2。注意这里不会重新创建新的字符串对象。

验证是否为str1和str2是否指向同一对象，我们可以通过这段代码

```JAVA
`System.out.println(str1 == str2);`
```

- **使用new创建**

```java
`String str3 = new String("droid"); `
```

当我们使用了new来构造字符串对象的时候，不管字符串常量池中有没有相同内容的对象的引用，新的字符串对象都会创建。因此我们使用下面代码测试一下，

```java
`String str3 = new String("droid"); System.out.println(str1 == str3); `
```

结果如我们所想，为`false`，表明这两个变量指向的为不同的对象。

### intern

对于上面使用new创建的字符串对象，如果想将这个对象的引用加入到字符串常量池，可以使用intern方法。

调用intern后，首先检查字符串常量池中是否有该对象的引用，如果存在，则将这个引用返回给变量，否则将引用加入并返回给变量。

```java
`String str4 = str3.intern(); System.out.println(str4 == str1); `
```

输出的结果为`true`。

**主要作用是使堆内存中不需要保留太多字符串对象，今早GC。**

------



### 优缺点

字符串常量池的好处就是减少相同内容字符串的创建，节省内存空间。

如果硬要说弊端的话，就是牺牲了CPU计算时间来换空间。CPU计算时间主要用于在字符串常量池中查找是否有内容相同对象的引用。不过其内部实现为HashTable，所以计算成本较低。

## 2、class constant pool（字节码常量池）

<img src="C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568364465295.png" alt="1568364465295" style="zoom: 80%;" />

**Constant pool** 意为常量池。 常量池可以理解成Class文件中的资源仓库。主要存放的是两大类常量：

`字面量(Literal)`和`符号引用(Symbolic References)`。

字面量类似于java中的常量概念，如文本字符串，final常量等，而符号引用则属于编译原理方面的概念，包括以下三种:

- 类和接口的全限定名(Fully Qualified Name)

- 字段的名称和描述符号(Descriptor)

- 方法的名称和描述符

不同于C/C++, JVM是在加载Class文件的时候才进行的**动态链接**，也就是说**这些字段和方法符号引用只有在**
**运行期转换后才能获得真正的内存入口地址**。当虚拟机运行时，需要从常量池获得对应的符号引用，再在类
创建或运行时解析并翻译到具体的内存地址中。 

直接通过反编译文件来查看字节码内容： 

<img src="C:\Users\TJR_S\AppData\Roaming\Typora\typora-user-images\1568364929599.png" alt="1568364929599" style="zoom:80%;" />

第一个常量是一个方法定义，指向了第4和第18个常量。以此类推查看第4和第18个常量。最后可以拼接成第
一个常量右侧的注释内容: 

```java
java/lang/Object."<init>":()V 
```

这段可以理解为该类的实例构造器的声明，由于Main类没有重写构造方法，所以调用的是父类的构造方法。
此处也说明了Main类的直接父类是Object。 该方法默认返回值是V, 也就是void，无返回值。

------

JVM中的**运行时常量池**中的字符串常量，是在Java应用启动时，由于JVM会加载类，并且由于class文件中有常量池。所以会把所有被加载的类中定义的字面量加载进运行时常量池中。（那部分呢？String那部分orutf-8那部分？）关于字面量，详情参考Java SE Specifications





## 3、runtime constant pool

- 运行时常量池以前位于永久代，jdk1.8 移除了永久代，取而代之的是元空间，位于 native memory。
- 在类加载的时候，class文件常量池中的大部分数据都会进入到运行时常量池
- CONSTANT_Utf8 类型对应的是一个 Symbol 类型的 C++ 对象，内容是跟 Class 文件同样格式的UTF-8编码的字符串
- CONSTANT_String 类型对应的是一个实际的 Java 对象的引用，C++ 类型是 oop
- CONSTANT_Utf8 类型对应的 Symbol 对象在类加载时候就已经创建了
- CONSTANT_String 则是 lazy resolve 的，例如说在第一次引用该项的 `ldc` 指令被第一次执行到的时候才会 resolve。
  那么在尚未 resolve 的时候，HotSpot VM 把它的类型叫做 JVM_CONSTANT_UnresolvedString，内容跟Class文件里一样只是一个index；
  等到 resolve 过后这个项的常量类型就会变成最终的 JVM_CONSTANT_String，而内容则变成实际的那个 oop。



# 常量折叠

常量折叠是Java在编译期做的一个优化，简单的来说，在编译期就把一些表达式计算好，不需要在运行时进行计算。 

并不是所有的常量都会进行折叠，必须是编译期常量之间进行运算才会进行常量折叠，编译器常量就是编译时就能确定其值的常量，这个定义很严格，需要满足以下条件:

> 1. 字面量是编译期常量（数字字面量，字符串字面量等）。
>
> 2. 编译期常量进行简单运算的结果也是编译期常量，如1+2，”a”+”b”。
>
> 3. 被编译器常量赋值的 final 的基本类型和字符串变量也是编译期常量 
>
>

比如: int a = 1 + 2 ，经过常量折叠后就变成了 int a = 3 。 （内存中没有1、2，只有3）

我们举个例子: 

```java
public static void main(String[] args) {
            String s1 = "a" + "bc";
            String s2 = "ab" + "c";
            System.out.println(s1 == s2);
}
//执行结果为true。 
//此时常量池中没有"a","bc","ab","c";只有"abc"
//var1和var2的值都是常量池中的”abc”，是同一个引用，所以会相等。
```



# 参考网页

[借助HotSpot SA来一窥PermGen上的对象](https://link.zhihu.com/?target=http%3A//rednaxelafx.iteye.com/blog/730461)

方法区的Class信息,又称为永久代,是否属于Java堆？https://www.zhihu.com/question/49044988eeeww