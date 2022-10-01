[Java 理论与实践: 使用通配符简化泛型使用，第 2 部分](http://www.uml.org.cn/j2ee/200808014.asp#1.0)



> 在使用 Java™ 语言的泛型时，通配符非常令人困惑，并且最常见的一个错误就是在使用有界通配符的两种形式的其中之一（“? super T” 和 “? extends T”）时出现错误。您出错了吗？别沮丧，即使是专家也会犯这种错误，本月 Brian Goetz 将展示如何避免这个错误。

在 Java 语言中，数组是协变的（因为一个 Integer 同时也是一个 Number，一个 Integer 数组同时也是一个 Number 数组），但是泛型不是这样的（List<Integer> *并不等于* List<Number>）。**人们会争论哪些选择是 “正确的”，哪些选择是 “错误的” — 当然，每种选择都各有优缺点** — 但有一点毫无疑问，存在两种使用差别很小的语义构造派生类型的类似机制，这将导致大量错误和误解。

**有界通配符（一些有趣的 “? extends T” 通用类型说明符）是语言提供的一种工具，用来处理协变性缺乏** — 有界通配符允许类声明方法参数或返回值何时具有协变性（或相反，声明方法参数或返回值何时具有*逆变性（contravariant）*）。虽然了解何时使用有界通配符是泛型较为复杂的方面，但是，使用有界通配符的压力通常都落在库作者的身上，而非库用户。最常见的有界通配符错误就是忘记使用它们，这就限制了类的使用，或是强制用户不得不重用现有的类。

### 有界通配符的作用

让我们从一个简单的泛型类开始（一个称为 Box 的值容器），它持有一个具有已知类型的值：

```
`public interface Box<T> {     public T get();     public void put(T element); } `
```

由于泛型不具备协变性，Box<Integer> 并不等同于 Box<Number>，尽管 Integer 属于 Number。但是对于 Box 这样的简单泛型类来说，这不成问题，并且常常被忽略，因为 Box<T> 的接口完全指定为 T 类型的变量 — 而不是通过 T 泛型化的类型。直接处理类型变量允许实现多态性。清单 1 展示了这种多态性的两个示例：获取 Box<Integer> 的内容，并将它作为一个 Number，然后将一个 Integer 放入 Box<Number> 中：

**单 1. 通过泛型类利用固有的多态性**


　



```
`                 Box<Integer> iBox = new BoxImpl<Integer>(3); Number num = iBox.get();  Box<Number> nBox = new BoxImpl<Number>(3.2); Integer i = 3; nBox.put(i); `
```

通过使用简单的 Box 类，使我们确信可以没有协变性，因为在需要实现多态的位置，数据已经具有某种形式，使编译器能够应用适当的子类型规则。

然而，如果希望 API 不仅能够处理 T 类型的变量，还能处理通过 T 泛型化的类型，事情将变得更加复杂。假设希望将一个新的方法添加到 Box，该方法允许获得另一个 Box 的内容并其放到清单 2 所示的 Box 中：

**清单 2. 扩展的 Box 接口并不灵活**


　



```
`                 public interface Box<T> {     public T get();     public void put(T element);     public void put(Box<T> box); } `
```

这个扩展 Box 的问题是，只能将内容放到类型参数与原 box 完全相同的 Box 中。因此，清单 3 中的代码就不能进行编译：

**清单 3. 泛型不具备协变性**


　



```
`                 Box<Number> nBox = new BoxImpl<Number>(); Box<Integer> iBox = new BoxImpl<Integer>();  nBox.put(iBox);     // ERROR `
```

显示一条错误消息，表示无法在 Box<Number> 中找到方法 put(Box<Integer>)。如果认为泛型是不具有协变性的，这条错误还讲得通；一个 Box<Integer> 不是 Box<Number>，尽管 Integer 是 Number，但是这使得 Box 类的 “泛型性” 比我们期望的要弱。要提高泛型代码的有效性，可以指定一个上限（或下限），而不是指定某个泛型类型参数的精确类型。这可以使用有界通配符来实现，它的形式为 “? extends T” 或 “? super T”。（有界通配符只能用作类型参数，而不能作为类型本身 — 因此，需要一个有界的命名的类型变量）。在清单 4 中，修改了 put() 的签名以使用一个上限通配符 — Box<? extends T>，这表示 Box 的类型参数可以是 T 或 T 的任何子类。

**清单 4. 对清单 3 的 Box 类的改进解释了协变性**


　



```
`                 public interface Box<T> {     public T get();     public void put(T element);     public void put(Box<? extends T> box); } `
```

现在，[清单 3](http://www.uml.org.cn/j2ee/200808014.asp#listing3) 中的代码可以进行编译并执行，因为 put() 的参数现在可以是参数类型为 T 或 T 的子类型的 Box。由于 Integer 是 Number 的子类型，编译器能够解析方法引用 put(Box<Integer>)，因为 Box<Integer> 匹配有界通配符 Box<? extends Number>。

很容易犯清单 3 中的 Box 错误，即使是专家也难以避免 — 在平台类库中，许多地方都使用 Collection<T>，而不是 Collection<? extends T>。例如，在 java.util.concurrent 包的 AbstractExecutorService 中，invokeAll() 的参数最初是一个 Collection<Callable<T>>。但是，这样使用 invokeAll() 非常麻烦，因为这要求必须由 Callable<T> 参数化的集合持有任务集，而不是由实现 Callable<T> 的类参数化的集合。在 Java 6 中，这种签名被修改为 Collection<? extends Callable<T>> — 这只是为了演示非常容易犯这个错误，正确的修复应该是使 invokeAll() 包含一个 Collection<? extends Callable<? extends T>> 参数。这个参数无疑更加难看，但不会给客户机带来麻烦。

### 下限通配符

上面的大多数有界通配符都进行了限定；“? extends T” 符号为类型添加了一个上限。但是，虽然比较少见，仍然可以使用 “? super T” 符号为类型添加一个下限，表示 “类型 T 以及它的任何超类”。当您希望指定一个回调对象（例如一个比较器）或存放某个值的数据结构，可以使用下限通配符。

假设我们希望增强 Box，使它能够与另一个 box 的内容进行比较。可以通过 containsSame() 方法和 Comparator 回调对象的定义扩展 Box，如清单 5 所示：

**清单 5. 尝试向 Box 添加一个比较方法**


　



```
`                 public interface Box<T> {     public T get();     public void put(T element);     public void put(Box<? extends T> box);      boolean containsSame(Box<? extends T> other,                           EqualityComparator<T> comparator);      public interface EqualityComparator<T> {         public boolean compare(T first, T second);     } } `
```

可以使用一个通配符定义 containsSame() 中另一个 box 的类型，这将避免前面遇到的问题。但是仍然会遇到一个类似的问题；比较器参数必须是 EqualityComparator<T>。这意味着我们不能编写如清单 6 所示的代码：

**清单 6. 使用清单 5 中的比较方法会导致失败**


　



```
`                 public static EqualityComparator<Object> sameObject      = new EqualityComparator<Object>() {         public boolean compare(Object o1, Object o2) {             return o1 == o2;         } };  ...  BoxImpl<Integer> iBox = ...; BoxImpl<Number> nBox = ...;  boolean b = nBox.containsSame(iBox, sameObject); `
```

在这里使用一个 EqualityComparator<Object> 似乎非常合理。既然可以使用泛型指定，客户机就不必为每一个可能的 Box 类型创建独立的比较器了！解决方法是使用一个下限通配符 “? super T”。使用 compareTo() 方法扩展的正确 Box 类如清单 7 所示：

**清单 7. 清单 5 中的比较操作在使用有界通配符后更加灵活**


　



```
`                 public interface Box<T> {     public T get();     public void put(T element);     public void put(Box<? extends T> box);      boolean containsSame(Box<? extends T> other,                           EqualityComparator<? super T> comparator);      public interface EqualityComparator<T> {         public boolean compare(T first, T second);     } } `
```

通过使用一个下限通配符，containsSame() 方法表示需要能够比较 T *或它的任何超类型* 的工具，这就允许我们提供一个能够比较对象的比较器，并且不需要使用 EqualityComparator<Number> 封装它。

### get-put 原则

有一个流传已久的笑话：“佩戴一只手表的人常常知道时间，而佩戴两只手表后反而难以确定了”。由于 Java 语言同时支持上限和下限通配符，那么如何判断何时使用哪一种呢？

这里有一条简单的规则，称为 *get-put 原则*，它解释了应该使用哪一种通配符。get-put 原则首次出现在 Naftalin 和 Wadler 所著的有关泛型的 *Java Generics and Collections* 一书中（参见参考资料），它是这样描述的：

**仅从某个结构中获取值时使用 extends 通配符；仅将值放入某个结构时使用 super 通配符；同时执行以上两种操作时不要使用通配符。**

在应用到 Box 等容器类或 Collections 类时，get-put 原则很好理解，因为 get 和 put 概念和这些类的作用有着自然的联系：存储内容。因此，如果希望应用 get-put 原则来创建一个可以在 Box 之间进行复制的方法，最常见的形式如清单 8 所示，其中复制源使用上限通配符，目标使用下限通配符：

**清单 8. 同时使用上限和下限通配符的 Box 复制方法**


　



```
`                 public static<T> void copy(Box<? extends T> from, Box<? super T> to) {     to.put(from.get()); } `
```

如果对前面的 containsSame() 方法（对 box 使用了上限通配符而对比较器使用了下限通配符）应用 get-put 原则？第一步很简单：需要从其他 box 获取一个值，因此使用一个 extends 通配符。但第二步有点复杂 — 因为比较器并不是容器，因此与从一个数据结构获得或存入值有所不同。

当数据类型并不是一个明显的容器类（例如集合）时，应该这样考虑 get-put 原则：尽管 EqualityComparator 不是一个数据结构，仍然可以向它 “存入” 值 — 即将值传递给它的一个方法。在 containsSame() 方法中，使用 Box 作为值的生成器（从 Box 获取值）并使用比较器作为值的使用者（将值传递给比较器）。因此可以对 Box 使用 extends 通配符，而对比较器使用 super 通配符。

我们可以看到 get-put 应用到了 Collections.sort() 的声明中，如清单 9 所示：

**清单 9. 使用下限通配符的另一个示例**


　



```
`                 public static <T extends Comparable<? super T>> void sort(List<T>list) { ... } `
```

在这里，可以对 List 排序，它由实现 Comparable 的任何类型参数化。但是没有将 sort() 的域限制为具有可相互比较的元素的列表，而是更进一步 — 我们还对能够与其超类型相比较的列表元素进行排序。由于将值放入到比较器来决定两个元素的相对顺序，get-put 原则告诉我们在这里需要使用一个超通配符。

表面上看似循环引用 — T 扩展经过 T 参数化的内容 — 实际上并不是真正的循环。它只是表示对 List<T> 排序的限制，T 需要实现接口 Comparable<X>，其中 X 是 T 或是 T 的一个超类型。

接着是 get-put 原则的最后一部分 — 当同时执行 get 和 put 操作时不要使用通配符。如果可以存入 T 或它的任意子类型，那么就可以获得 T 或它的任意超类型，而惟一可以同时获得或存入的是 T 本身。

### 不要对返回值使用通配符

有时希望对方法的返回类型使用有界通配符。但最好不要这样做，因为返回的有界通配符往往会 “弄脏” 客户机代码。如果某方法返回一个 Box<? extends T>，那么接收返回类型的变量的类型必须是 Box<? extends T>，这将处理有界通配符的工作推给了调用者。有界通配符最适合用于 API，而不是客户机代码。

### 结束语

有界通配符对于提高泛型 API 的灵活性极其有用。正确使用有界通配符的最大绊脚石是认为没有必要使用它们。有些情况适合使用下限通配符，而另一些情况则适合使用上限通配符，通过 get-put 原则可以判断应该使用哪种通配符。

### 参考资料

学习



- 您可以参阅本文在 developerWorks 全球网站上的 [英文原文](http://www.ibm.com/developerworks/java/library/j-jtp07018.html)。
- *Java 理论与实践* （Brian Goetz，developerWorks）：参阅该系列的所有文章。
- [JSR 14](http://www.jcp.org/en/jsr/detail?id=14)：向 Java 编程语言添加泛型。早期规范源自于 [GJ](http://homepages.inf.ed.ac.uk/wadler/gj/index.html#may99)。[Wildcards](http://bracha.org/wildcards.pdf) 是后来添加的。
- *Java Generics and Collections* ：提供了一个全面的泛型处理。
- [Generics FAQ](http://www.angelikalanger.com/GenericsFAQ/JavaGenericsFAQ.html)：Angelika Langer 创建了关于泛型的完整 FAQ。
- “[ *Java 理论与实践*：使用通配符简化泛型使用](http://www.ibm.com/developerworks/cn/java/j-jtp04298.html)”（Brian Goetz，developerWorks，2008 年 5 月）：处理另一个泛型难点：泛型捕获。
- “[ *Java 理论与实践*：了解泛型](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html)”（Brian Goetz，developerWorks，2005 年 1 月）：解释关于实现泛型的擦除方法的一些影响。
- “[JDK 5.0 中的泛型介绍](http://www.ibm.com/developerworks/cn/views/java/tutorials.jsp?cv_doc_id=85169)”（Brian Goetz，developerWorks，2004 年 12 月）：介绍了泛型类型，使您可以在安装时使用抽象类型参数定义类。
- [*Java Concurrency in Practice* ](http://www.amazon.com/exec/obidos/ASIN/0321349601/ref=nosim/none0b69)：使用 Java 代码开发并发程序的 how-to 手册，包括构造和组成线程安全的类和程序、避免风险、管理性能和测试并发应用程序。
- 在 [技术书店](http://www.ibm.com/developerworks/apps/SendTo?bookstore=safari) 浏览关于这些主题和其他技术主题的图书。
- [developerWorks Java 技术专区](http://www.ibm.com/developerworks/cn/java/)：数百篇关于 Java 编程各个方面的文章。

讨论



- 查阅 [developerWorks blogs](http://www.ibm.com/developerworks/blogs/) 并加入 [developerWorks 社区](http://www.ibm.com/developerworks/community?S_TACT=105AGX52&S_CMP=content)。