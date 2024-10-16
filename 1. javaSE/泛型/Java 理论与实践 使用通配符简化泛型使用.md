[Java 理论与实践: 使用通配符简化泛型使用](http://tech.sina.com.cn/s/2008-06-16/08032259552.shtml)



通配符是 Java™ 语言中最复杂的泛型之一，特别是围绕捕获通配符 的处理和令人困惑的错误消息。在这一期的 Java 理论与实践 中，资深 Java 开发人员 Brian Goetz 解释了一些由 javac 生成的怪异错误消息并提供了一些简化泛型使用的技巧和解决方法。

自从泛型被添加到 JDK 5 语言以来，它一直都是一个颇具争议的话题。一部分人认为泛型简化了编程，扩展了类型系统从而使编译器能够检验类型安全;另外一些人认为泛型添加了很多不必要的复杂性。对于泛型我们都经历过一些痛苦的回忆，但毫无疑问通配符是最棘手的部分。

## **通配符基本介绍**

泛型是一种表示类或方法行为对于未知类型的类型约束的方法，比如 “不管这个方法的参数 x 和 y 是哪种类型，它们必须是相同的类型”，“必须为这些方法提供同一类型的参数” 或者 “foo() 的返回值和 bar() 的参数是同一类型的”。

通配符 （使用一个奇怪的问号表示类型参数 ） 是一种表示未知类型的类型约束的方法。通配符并不包含在最初的泛型设计中(起源于 Generic Java(GJ)项目)，从形成 JSR 14 到发布其最终版本之间的五年多时间内完成设计过程并被添加到了泛型中。

通配符在类型系统中具有重要的意义，它们为一个泛型类所指定的类型集合提供了一个有用的类型范围。对泛型类 ArrayList 而言，对于任意(引用)类型 T，ArrayList<?> 类型是 ArrayList<T> 的超类型(类似原始类型 ArrayList 和根类型 Object，但是这些**超类型在执行类型推断方面不是很有用**)。

**通配符类型 List<?> 与原始类型 List 和具体类型 List<Object> 都不相同。**如果说变量 x 具有 List<?> 类型，这表示存在一些 T 类型，其中 x 是 List<T>类型，x 具有相同的结构，尽管我们不知道其元素的具体类型。这并不表示它可以具有任意内容，而是指我们并不了解内容的类型限制是什么 — **但我们知道存在 某种限制**。另一方面，**原始类型 List 是异构的，我们不能对其元素有任何类型限制**，具体类型 List<Object> 表示我们明确地知道它能包含任何对象(当然，泛型的类型系统没有 “列表内容” 的概念，但可以从 List 之类的集合类型轻松地理解泛型)。

通配符在类型系统中的作用部分来自其**不会发生协变(c**ovariant)这一特性。数组是协变的，因为 Integer 是 Number 的子类型，数组类型 Integer[] 是 Number[] 的子类型，**因此在任何需要 Number[] 值的地方都可以提供一个 Integer[] 值。**另一方面，泛型不是协变的， List<Integer> 不是 List<Number> 的子类型，**试图在要求 List<Number> 的位置提供 List<Integer> 是一个类型错误。**这不算很严重的问题 — 也不是所有人都认为的错误 — 但泛型和数组的不同行为的确引起了许多混乱。

## 　**我已使用了一个通配符 — 接下来呢?**

清单 1 展示了一个简单的容器(container)类型 Box，它支持 put 和 get 操作。 Box 由类型参数 T 参数化，该参数表示 Box 内容的类型， Box<String> 只能包含 String 类型的元素。

清单 1. 简单的泛型 Box 类型

```

    public interface Box<T> {

        public T get();

        public void put(T element);
    }

```

通配符的一个好处是允许编写可以操作泛型类型变量的代码，并且不需要了解其具体类型。例如，假设

有一个 Box<?> 类型的变量，比如清单 2 unbox() 方法中的 box 参数。unbox() 如何处理已传递的 box?

清单 2. 带有通配符参数的 Unbox 方法

```

    public void unbox(Box<?> box) {
        System.out.println(box.get());
    }

```

事实证明 Unbox 方法能做许多工作：它能调用 get() 方法，并且能调用任何从 Object 继承而来的方法(比如 hashCode())。**它惟一不能做的事是调用 put() 方法，这是因为在不知道该 Box 实例的类型参数 T 的情况下它不能检验这个操作的安全性。**由于 box 是一个 Box<?> 而不是一个原始的 Box，编译器知道存在一些 T 充当 box 的类型参数，但由于不知道 T 具体是什么，**您不能调用 put() 因为不能检验这么做不会违反 Box 的类型安全限制**(实际上，您可以在一个特殊的情况下调用 put()：当您传递 null 字母时。我们可能不知道 T 类型代表什么，但我们知道 null 字母对任何引用类型而言是一个空值)。

关于 box.get() 的返回类型，unbox() 了解哪些内容呢?它知道 box.get() 是某些未知 T 的 T，因此它可以推断出 get() 的返回类型是 T 的擦除(erasure)，对于一个无上限的通配符就是 Object。因此清单 2 中的表达式 box.get() 具有 Object 类型。

## 通配符捕获

清单 3 展示了一些似乎应该 可以工作的代码，但实际上不能。它包含一个泛型 Box、提取它的值并试图将值放回同一个 Box。

清单 3. 一旦将值从 box 中取出，则不能将其放回

```
public void rebox(Box<?> box) 
{ 
box.put(box.get()); 
} 
Rebox.java:8: put(capture#337 of ?) in Box<capture#337 of ?> cannot be applied to (java.lang.Object) box.put(box.get()); ^ 1 error
```

这个代码看起来应该可以工作，因为取出值的类型符合放回值的类型，然而，编译器生成(令人困惑的)关于 “capture#337 of ?” 与 Object 不兼容的错误消息。

“capture#337 of ?” 表示什么?当编译器遇到一个在其类型中带有通配符的变量，比如 rebox() 的 box 参数，它认识到必然有一些 T ，对这些 T 而言 box 是 Box<T>。它不知道 T 代表什么类型，但它可以为该类型创建一个占位符来指代 T 的类型。占位符被称为这个特殊通配符的捕获(capture)。这种情况下，编译器将名称 “capture#337 of ?” 以 box 类型分配给通配符。每个变量声明中每出现一个通配符都将获得一个不同的捕获，因此在泛型声明 foo(Pair<?,?> x, Pair<?,?> y) 中，编译器将给每四个通配符的捕获分配一个不同的名称，因为任意未知的类型参数之间没有关系。

错**误消息告诉我们不能调用 put()，因为它不能检验 put() 的实参类型与其形参类型是否兼容 — 因为形参的类型是未知的。**在这种情况下，由于 ? 实际表示 “?extends Object” ，编译器已经推断出 box.get() 的类型是 Object，而不是 “capture#337 of ?”。它不能静态地检验对由占位符 “capture#337 of ?” 所识别的类型而言 Object 是否是一个可接受的值。

虽然编译器似乎丢弃了一些有用的信息，我们可以使用一个技巧来使编译器重构这些信息，即对未知的通配符类型命名。清单 4 展示了 rebox() 的实现和一个实现这种技巧的泛型助手方法(helper)：



清单 4. “捕获助手” 方法

```

    public void rebox(Box<?> box) {
        reboxHelper(box);
    }

    private <V> void reboxHelper(Box<V> box) {
        box.put(box.get());
    }
```

助手方法 reboxHelper() 是一个泛型方法，泛型方法引入了额外的类型参数(位于返回类型之前的尖括号中)，这些参数用于表示参数和/或方法的返回值之间的类型约束。然而就 reboxHelper() 来说，泛型方法并不使用类型参数指定类型约束，它允许编译器(通过类型接口)对 box 类型的类型参数命名。

**捕获助手技巧允许我们在处理通配符时绕开编译器的限制。**当 rebox() 调用 reboxHelper() 时，它知道这么做是安全的，因为它自身的 box 参数对一些未知的 T 而言一定是 Box<T>。因为类型参数 V 被引入到方法签名中并且没有绑定到其他任何类型参数，它也可以表示任何未知类型，因此，某些未知 T 的 Box<T> 也可能是某些未知 V 的 Box<V>(这和 lambda 积分中的 α 减法原则相似，允许重命名边界变量)。**现在 reboxHelper() 中的表达式 box.get() 不再具有 Object 类型，它具有 V 类型 — 并允许将 V 传递给 Box<V>.put()。**

我们本来可以将 rebox() 声明为一个泛型方法，类似 reboxHelper()，但这被认为是一种糟糕的 API 设计样式。此处的主要设计原则是 “**如果以后绝不会按名称引用，则不要进行命名**”。**就泛型方法来说，如果一个类型参数在方法签名中只出现一次，它很有可能是一个通配符而不是一个命名的类型参数。**一般来说，带有通配符的 API 比带有泛型方法的 API 更简单，在更复杂的方法声明中类型名称的增多会降低声明的可读性。因为在需要时始终可以通过专有的捕获助手恢复名称，这个方法让您能够保持 API 整洁，同时不会删除有用的信息。

## 类型推断

捕获助手技巧涉及多个因素：类型推断和捕获转换。Java 编译器在很多情况下都不能执行类型推断，但是可以为泛型方法推断类型参数(其他语言更加依赖类型推断，将来我们可以看到 Java 语言中会添加更多的类型推断特性)。如果愿意，您可以指定类型参数的值，但只有当您能够命名该类型时才可以这样做 — 并且不能够表示捕获类型。因此要使用这种技巧，要求编译器能够为您推断类型。捕获转换允许编译器为已捕获的通配符产生一个占位符类型名，以便对它进行类型推断。

当解析一个泛型方法的调用时，编译器将设法推断类型参数它能达到的最具体类型。 例如，对于下面这个泛型方法：

```java
public static<T> T identity(T arg) {
return arg 
}; 


```

和它的调用：

```java
Integer i = 3; 
System.out.println(identity(i));


```

编译器能够推断 T 是 Integer、Number、 Serializable 或 Object，但它选择 Integer 作为满足约束的最具体类型。

当构造泛型实例时，可以使用类型推断减少冗余。例如，使用 Box 类创建 Box<String> 要求您指定两次类型参数 String：

```java
Box<String> box = new BoxImpl<String>();
```

即使可以使用 IDE 执行一些工作，也不要违背 DRY(Don't Repeat Yourself)原则。然而，如果实现类 BoxImpl 提供一个类似清单 5 的泛型工厂方法(这始终是个好主意)，则可以减少客户机代码的冗余：

清单 5. 一个泛型工厂方法，可以避免不必要地指定类型参数

```java
public class BoxImpl<T> implements Box<T> {
    public static<V> Box<V> make() { 
    	return new BoxImpl<V>(); 
    } 
...
}
```

如果使用 BoxImpl.make() 工厂实例化一个 Box，您只需要指定一次类型参数：

```java
Box<String> myBox = BoxImpl.make();
```

泛型 make() 方法为一些类型 V 返回一个 Box<V>，返回值被用于需要 Box<String> 的上下文中。编译器确定 String 是 V 能接受的满足类型约束的最具体类型，因此此处将 V 推断为 String。您还可以**手动地指定 V 的值：**

```java
Box<String> myBox = BoxImpl.<String>make();


```

除了减少一些键盘操作以外，此处演示的工厂方法技巧还提供了优于构造函数的其他优势：您能够为它们提高更具描述性的名称，它们能够返回命名返回类型的子类型，它们不需要为每次调用创建新的实例，从而能够共享不可变的实例(参见 参考资料 中的 Effective Java, Item #1，了解有关静态工厂的更多优点)。

## **结束语**

　　通配符无疑非常复杂：由 Java 编译器产生的一些令人困惑的错误消息都与通配符有关，Java 语言规范中最复杂的部分也与通配符有关。然而如果使用适当，通配符可以提供强大的功能。此处列举的两个技巧 — 捕获助手技巧和泛型工厂技巧 — 都利用了泛型方法和类型推断，如果使用恰当，它们能显著降低复杂性。

　　参考资料

- 您可以参阅本文在 developerWorks 全球站点上的 [英文原文](http://www.ibm.com/developerworks/java/library/j-jtp04298.html?S_TACT=105AGX52&S_CMP=cn-a-j) 。

  

- [*Java 理论与实践* ](http://www.ibm.com/developerworks/cn/java/j-jtp/)（Brian Goetz，developerWorks）：参阅该系列的所有文章。

  

- “[了解泛型](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html)”（Brian Goetz，developerWorks，2005 年 1 月）：了解如何在学习使用泛型时识别和避免一些陷阱。

  

- [JDK 5.0 中的泛型介绍](http://www.ibm.com/developerworks/cn/views/java/tutorials.jsp?cv_doc_id=85169)（Brian Goetz，developerWorks，2004 年 12 月）：developerWorks 投稿人和 Java 编程专家 Brian Goetz 解释了将泛型添加到 Java 语言的动机、语法细节和泛型类型的语义，并介绍了如何在自己的类中使用泛型。

  

- [JSR 14](http://www.jcp.org/en/jsr/detail?id=14)：将泛型添加到 Java 编程语言中。早期的规范来源于 [GJ](http://homepages.inf.ed.ac.uk/wadler/gj/index.html#may99)。[通配符](http://bracha.org/wildcards.pdf) 是后来添加的。

  

- [*Java Generics and Collections* ](http://www.amazon.com/gp/product/0596527756/105-1483343-6367656?ie=UTF8&tag=none0b69&linkCode=xm2&camp=1789&creativeASIN=0596527756)：提供了一个全面的泛型处理。

  

- [*Effective Java* ](http://www.amazon.com/gp/product/0201310058/105-1483343-6367656?ie=UTF8&tag=none0b69&linkCode=xm2&camp=1789&creativeASIN=0596527756): Item 1 进一步探讨了静态工厂方法的优点。

  

- [Generics FAQ](http://www.angelikalanger.com/GenericsFAQ/JavaGenericsFAQ.html): Angelika Langer 创建了关于泛型的完整 FAQ。

  

- [*Java Concurrency in Practice* ](http://www.amazon.com/exec/obidos/ASIN/0321349601/ref=nosim/none0b69)：使用 Java 代码开发并发程序的 how-to 手册，包括构造和组成线程安全的类和程序、避免风险、管理性能和测试并发应用程序。

  

- [技术书店](http://www.ibm.com/developerworks/apps/SendTo?bookstore=safari)：浏览有关各种技术主题的书籍。

  

- [Java 技术专区](http://www.ibm.com/developerworks/cn/java/)：数百篇关于 Java 编程各个方面的文章。

