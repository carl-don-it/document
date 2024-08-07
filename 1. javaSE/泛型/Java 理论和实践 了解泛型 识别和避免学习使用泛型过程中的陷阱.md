[Java 理论和实践: 了解泛型 识别和避免学习使用泛型过程中的陷阱](https://www.cnblogs.com/rollenholt/articles/2193003.html)

2011-09-27 12:32  [Rollen Holt](https://www.cnblogs.com/rollenholt/)  阅读(869)  评论(0)  [编辑](https://i.cnblogs.com/EditArticles.aspx?postid=2193003)  [收藏](javascript:void(0))  [举报](javascript:void(0))

[Brian Goetz](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html#author1) ([brian@quiotix.com](mailto:brian@quiotix.com?subject=了解泛型)), 首席顾问, Quiotix



**简介：** JDK 5.0 中增加的泛型类型，是 Java 语言中类型安全的一次重要改进。但是，对于初次使用泛型类型的用户来说，泛型的某些方面看起来可能不容易明白，甚至非常奇怪。在本月的“*Java 理论和实践*”中，Brian Goetz 分析了束缚第一次使用泛型的用户的常见陷阱。您可以通过 [讨论论坛](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html)与作者和其他读者分享您对本文的看法。（也可以单击本文顶端或底端的 **讨论**来访问这个论坛。）

表面上看起来，无论语法还是应用的环境（比如容器类），泛型类型（或者泛型）都类似于 C++ 中的模板。但是这种相似性仅限于表面，Java 语言中的泛型基本上完全在编译器中实现，由编译器执行类型检查和类型推断，然后生成普通的非泛型的字节码。这种实现技术称为 *擦除（erasure）*（编译器使用泛型类型信息保证类型安全，然后在生成字节码之前将其清除），这项技术有一些奇怪，并且有时会带来一些令人迷惑的后果。虽然范型是 Java 类走向类型安全的一大步，但是在学习使用泛型的过程中几乎肯定会遇到头痛（有时候让人无法忍受）的问题。

***注意：**本文假设您对 JDK 5.0 中的范型有基本的了解。*

## **泛型不是协变的**

虽然将集合看作是数组的抽象会有所帮助，但是数组还有一些集合不具备的特殊性质。Java 语言中的数组是协变的（covariant），也就是说，如果 `Integer`扩展了 `Number`（事实也是如此），那么不仅 `Integer`是 `Number`，而且 `Integer[]`也是 `Number[]`，在要求`Number[]`的地方完全可以传递或者赋予 `Integer[]`。（更正式地说，如果 `Number`是 `Integer`的超类型，那么 `Number[]`也是`Integer[]`的超类型）。您也许认为这一原理同样适用于泛型类型 —— `List<Number>`是 `List<Integer>`的超类型，那么可以在需要`List<Number>`的地方传递 `List<Integer>`。不幸的是，情况并非如此。

不允许这样做有一个很充分的理由：这样做将破坏要提供的类型安全泛型。如果能够将 `List<Integer>`赋给 `List<Number>`。那么下面的代码就允许将非 `Integer`的内容放入 `List<Integer>`：

```
` List<Integer> li = new ArrayList<Integer>();   List<Number> ln = li; // illegal   ln.add(new Float(3.1415));  `
```



因为 `ln`是 `List<Number>`，所以向其添加 `Float`似乎是完全合法的。但是如果 `ln`是 `li`的别名，那么这就破坏了蕴含在 `li`定义中的类型安全承诺 —— 它是一个整数列表，这就是泛型类型不能协变的原因。

### **其他的协变问题**

数组能够协变而泛型不能协变的另一个后果是，不能实例化泛型类型的数组（`new List<String>[3]`是不合法的），除非类型参数是一个未绑定的通配符（`new List<?>[3]`是合法的）。让我们看看如果允许声明泛型类型数组会造成什么后果：

```
` List<String>[] lsa = new List<String>[10]; // illegal   Object[] oa = lsa;  // OK because List<String> is a subtype of Object   List<Integer> li = new ArrayList<Integer>();   li.add(new Integer(3));   oa[0] = li;   String s = lsa[0].get(0);  `
```



最后一行将抛出 `ClassCastException`，因为这样将把 `List<Integer>`填入本应是 `List<String>`的位置。因为数组协变会破坏泛型的类型安全，所以不允许实例化泛型类型的数组（除非类型参数是未绑定的通配符，比如 `List<?>`）。



[回页首](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html#ibm-pcon)

## **构造延迟**

因为可以擦除功能，所以 `List<Integer>`和 `List<String>`是同一个类，编译器在编译 `List<V>`时只生成一个类（和 C++ 不同）。因此，在编译 `List<V>`类时，编译器不知道 `V`所表示的类型，所以它就不能像知道类所表示的具体类型那样处理 `List<V>`类定义中的类型参数（`List<V>`中的 `V`）。

因为运行时不能区分 `List<String>`和 `List<Integer>`（运行时都是 `List`），用泛型类型参数标识类型的变量的构造就成了问题。运行时缺乏类型信息，这给泛型容器类和希望创建保护性副本的泛型类提出了难题。

比如泛型类 `Foo`：

```
` class Foo<T> {    public void doSomething(T param) { ... }   }  `
```



假设 `doSomething()`方法希望复制输入的 `param`参数，会怎么样呢？没有多少选择。您可能希望按以下方式实现 `doSomething()`：

```
` public void doSomething(T param) {    T copy = new T(param);  // illegal   }  `
```



但是您不能使用类型参数访问构造函数，因为在编译的时候还不知道要构造什么类，因此也就不知道使用什么构造函数。使用泛型不能表达“`T`必须拥有一个拷贝构造函数（copy constructor）”（甚至一个无参数的构造函数）这类约束，因此不能使用泛型类型参数所表示的类的构造函数。

`clone()`怎么样呢？假设在 `Foo`的定义中，`T`扩展了 `Cloneable`：

```
` class Foo<T extends Cloneable> {    public void doSomething(T param) {      T copy = (T) param.clone();  // illegal    }   }  `
```



不幸的是，仍然不能调用 `param.clone()`。为什么呢？因为 `clone()`在 `Object`中是保护访问的，调用 `clone()`必须通过将 `clone()`改写公共访问的类引用来完成。但是重新声明 `clone()`为 public 并不知道 `T`，因此克隆也无济于事。

### **构造通配符引用**

因此，不能复制在编译时根本不知道是什么类的类型引用。那么使用通配符类型怎么样？假设要创建类型为 `Set<?>`的参数的保护性副本。您知道 `Set`有一个拷贝构造函数。而且别人可能曾经告诉过您，如果不知道要设置的内容的类型，最好使用 `Set<?>`代替原始类型的 `Set`，因为这种方法引起的未检查类型转换警告更少。于是，可以试着这样写：

```java
` class Foo {    public void doSomething(Set<?> set) {      Set<?> copy = new HashSet<?>(set);  // illegal    }   }  `
```



不幸的是，您不能用通配符类型的参数调用泛型构造函数，即使知道存在这样的构造函数也不行。不过您可以这样做：

```
` class Foo {    public void doSomething(Set<?> set) {      Set<?> copy = new HashSet<Object>(set);     }   }  `
```



这种构造不那么直观，但它是类型安全的，而且可以像 `new HashSet<?>(set)`那样工作。

可以这么做

```java
public static  <T> void doSomething(T param) throws IllegalAccessException, InstantiationException {
    Class<?> aClass = param.getClass();
    Object o = aClass.newInstance();
    T df = (T) o;
}
```

**构造数组**

如何实现 `ArrayList<V>`？假设类 `ArrayList`管理一个 `V`数组，您可能希望用 `ArrayList<V>`的构造函数创建一个 `V`数组：

```java
 class ArrayList<V> {   
    private V[] backingArray;  
    public ArrayList() {   
        backingArray = new V[DEFAULT_SIZE]; // illegal    
    } 
}  
```

但是这段代码不能工作 —— 不能实例化用类型参数表示的类型数组。编译器不知道 `V`到底表示什么类型，因此不能实例化 `V`数组。

Collections 类通过一种别扭的方法绕过了这个问题，在 Collections 类编译时会产生类型未检查转换的警告。`ArrayList`具体实现的构造函数如下：

```java
 class ArrayList<V> {   
     private V[] backingArray; 
     public ArrayList() {  
         backingArray = (V[]) new Object[DEFAULT_SIZE];  
     } 
     
     public void add(V dsf ) {
         //类型检查
        backingArray[0] = dsf;
        //backingArray[0] = new Object(); illegle Required type: V  Provided: Object
    }
 }  
```



为何这些代码在访问 `backingArray`时没有产生 `ArrayStoreException`呢？无论如何，都不能将 `Object`数组赋给 `String`数组。因为泛型是通过擦除实现的，`backingArray`的类型实际上就是 `Object[]`，因为 `Object`代替了 `V`。这意味着：实际上这个类期望`backingArray`是一个 `Object`数组，但**是编译器要进行额外的类型检查(在调用backingArray的时候)，以确保它包含 `V`类型的对象。**所以这种方法很奏效，但是非常别扭，因此不值得效仿（甚至连泛型 Collections 框架的作者都这么说，请参阅 [参考资料](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html#resources)）。

还有一种方法就是声明 `backingArray`为 `Object`数组，并在使用它的各个地方强制将它转化为 `V[]`。仍然会看到类型未检查转换警告（与上一种方法一样），但是它使一些未明确的假设更清楚了（比如 `backingArray`不应逃避 `ArrayList`的实现）。

```java
mArray = (T[]) Array.newInstance(type, size);
//只能构造无泛型的数组，type可不能带泛型
```

> 转型为泛型没有意义

```java
public class test3<T , EXC extends Throwable> {
    private final static int SIZE = 100;
    
    public void g(Object arg) throws EXC {
        // Cannot perform instanceof check against type parameter T.
        // Use its erasure Object instead since further generic type information will be erased at runtime
        //! if (arg instanceof T) { }

        // Cannot instantiate the type T
        //! T var = new T();

        // Cannot create a generic array of T
        //! T[] array = new T[SIZE];

        //只是转变成object[],如果不把arr当T[]使用则，毫无意义，但是省了后面
        T[] arr = (T[]) new Object[SIZE];
        System.out.println("sdf");
        // Cannot use the type parameter EXC in a catch block
        //! try { } catch (EXC e) {}
    }

    public static void main(String[] args) throws Exception {
        test3<String, Exception> www = new test3<>();
        www.g(null);

    }
}
```



### **其他方法**

最好的办法是向构造函数传递类文字（`Foo.class`），这样，该实现就能在运行时知道 `T`的值。不采用这种方法的原因在于向后兼容性 —— 新的泛型集合类不能与 Collections 框架以前的版本兼容。

下面的代码中 `ArrayList`采用了以下方法：

```
` public class ArrayList<V> implements List<V> {    private V[] backingArray;    private Class<V> elementType;    public ArrayList(Class<V> elementType) {      this.elementType = elementType;      backingArray = (V[]) Array.newInstance(elementType, DEFAULT_LENGTH);    }   }  `
```



但是等一等！仍然有不妥的地方，调用 `Array.newInstance()`时会引起未经检查的类型转换。为什么呢？同样是由于向后兼容性。`Array.newInstance()`的签名是：

```
` public static Object newInstance(Class<?> componentType, int length)  `
```



而不是类型安全的：

```
` public static<T> T[] newInstance(Class<T> componentType, int length)  `
```



为何 `Array`用这种方式进行泛化呢？同样是为了保持向后兼容。要创建基本类型的数组，如 `int[]`，可以使用适当的包装器类中的`TYPE`字段调用 `Array.newInstance()`（对于 `int`，可以传递 `Integer.TYPE`作为类文字）。用 `Class<T>`参数而不是 `Class<?>`泛化`Array.newInstance()`，对于引用类型有更好的类型安全，但是就不能使用 `Array.newInstance()`创建基本类型数组的实例了。也许将来会为引用类型提供新的 `newInstance()`版本，这样就两者兼顾了。

在这里可以看到一种模式 —— 与泛型有关的很多问题或者折衷并非来自泛型本身，而是保持和已有代码兼容的要求带来的副作用。



[回页首](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html#ibm-pcon)

## **泛化已有的类**

在转化现有的库类来使用泛型方面没有多少技巧，但与平常的情况相同，向后兼容性不会凭空而来。我已经讨论了两个例子，其中向后兼容性限制了类库的泛化。

另一种不同的泛化方法可能不存在向后兼容问题，这就是 `Collections.toArray(Object[])`。传入 `toArray()`的数组有两个目的 —— 如果集合足够小，那么可以将其内容直接放在提供的数组中。否则，利用反射（reflection）创建相同类型的新数组来接受结果。如果从头开始重写 Collections 框架，那么很可能传递给 `Collections.toArray()`的参数不是一个数组，而是一个类文字：

```java
 interface Collection<E> {    public T[] toArray(Class<T super E> elementClass);   }  
```



因为 Collections 框架作为良好类设计的例子被广泛效仿，但是它的设计受到向后兼容性约束，所以这些地方值得您注意，不要盲目效仿。

首先，常常被混淆的泛型 Collections API 的一个重要方面是 `containsAll()`、`removeAll()`和 `retainAll()`的签名。您可能认为`remove()`和 `removeAll()`的签名应该是：

```java
 interface Collection<E> {    public boolean remove(E e);  // not really    public void removeAll(Collection<? extends E> c);  // not really   }  
```



但实际上却是：

```
` interface Collection<E> {    public boolean remove(Object o);     public void removeAll(Collection<?> c);   }  `
```



为什么呢？答案同样是因为向后兼容性。`x.remove(o)`的接口表明“如果 `o`包含在 `x`中，则删除它，否则什么也不做。”如果 `x`是一个泛型集合，那么 `o`不一定与 `x`的类型参数兼容。如果 `removeAll()`被泛化为只有类型兼容时才能调用（`Collection<? extends E>`），那么在泛化之前，合法的代码序列就会变得不合法，比如：

```
` // a collection of Integers   Collection c = new HashSet();   // a collection of Objects   Collection r = new HashSet();   c.removeAll(r);  `
```



如果上述片段用直观的方法泛化（将 `c`设为 `Collection<Integer>`，`r`设为 `Collection<Object>`），如果 `removeAll()`的签名要求其参数为 `Collection<? extends E>`而不是 no-op，那么就无法编译上面的代码。**泛型类库的一个主要目标就是不打破或者改变已有代码的语义**，因此，必须用比从头重新设计泛型所使用类型约束更弱的类型约束来定义 `remove()`、`removeAll()`、`retainAll()`和`containsAll()`。

在泛型之前设计的类可能阻碍了“显然的”泛型化方法。这种情况下就要像上例这样进行折衷，但是如果从头设计新的泛型类，理解 Java 类库中的哪些东西是向后兼容的结果很有意义，这样可以避免不适当的模仿。

## **擦除的实现**

因为泛型基本上都是在 Java 编译器中而不是运行库中实现的，所以在生成字节码的时候，差不多所有关于泛型类型的类型信息都被“擦掉”了。换句话说，编译器生成的代码与您手工编写的不用泛型、检查程序的类型安全后进行强制类型转换所得到的代码基本相同。与 C++ 不同，`List<Integer>`和 `List<String>`是同一个类（虽然是不同的类型但都是 `List<?>`的子类型，与以前的版本相比，在 JDK 5.0 中这是一个更重要的区别）。

**擦除意味着一个类不能同时实现 `Comparable<String>`和 `Comparable<Number>`，**因为事实上两者都在同一个接口中，指定同一个`compareTo()`方法。声明 `DecimalString`类以便与 `String`与 `Number`比较似乎是明智的，但对于 Java 编译器来说，这相当于对同一个方法进行了两次声明：

```
`public class DecimalString implements Comparable<Number>, Comparable<String> { ... } // nope  `
```



擦除的另一个后果是，**对泛型类型参数是用强制类型转换或者 `instanceof`毫无意义。**下面的代码完全不会改善代码的类型安全性：

```
` public <T> T naiveCast(T t, Object o) { return (T) o; }  `
```



编译器仅仅发出一个类型未检查转换警告，因为它不知道这种转换是否安全。`naiveCast()`方法实际上根本不作任何转换，`T`直接被替换为 `Object`，与期望的相反，传入的对象被强制转换为 `Object`。

擦除也是造成上述构造问题的原因，即不能创建泛型类型的对象，因为编译器不知道要调用什么构造函数。如果泛型类需要构造用泛型类型参数来指定类型的对象，那么构造函数应该接受类文字（`Foo.class`）并将它们保存起来，以便通过反射创建实例。



[回页首](http://www.ibm.com/developerworks/cn/java/j-jtp01255.html#ibm-pcon)

**结束语**

泛型是 Java 语言走向类型安全的一大步，但是泛型设施的设计和类库的泛化并非未经过妥协。扩展虚拟机指令集来支持泛型被认为是无法接受的，因为这会为 Java 厂商升级其 JVM 造成难以逾越的障碍。因此采用了可以完全在编译器中实现的擦除方法。类似地，在泛型 Java 类库时，保持向后兼容也为类库的泛化方式设置了很多限制，产生了一些混乱的、令人沮丧的结构（如`Array.newInstance()`）。这并非泛型本身的问题，而是与语言的演化与兼容有关。但这些也使得泛型学习和应用起来更让人迷惑，更加困难。