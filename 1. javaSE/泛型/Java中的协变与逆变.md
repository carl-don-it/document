[
ExtremeGTR's Blog](https://extremegtr.github.io/)

Better to run than curse the road.

[ 首页](https://extremegtr.github.io/)[ 标签**42**](https://extremegtr.github.io/tags/)[ 分类**22**](https://extremegtr.github.io/categories/)[ 归档**46**](https://extremegtr.github.io/archives/)

# [Java中的协变与逆变](https://extremegtr.github.io/2016/07/11/Covariance-And-Contravariance-In-Java/)

 发表于 2016-07-11   更新于 2018-04-28   分类于 [JavaSE](https://extremegtr.github.io/categories/JavaSE/)   阅读次数： 1482
 本文字数： 7k   阅读时长 ≈ 6 分钟

　　“协变”一词我并不是第一次见到了，在以前我学习C++的时候就碰到过，而到后来学习Java的时候也遇到了，而在学习Java泛型时更是碰到“逆变”一词。不过我见得最多的是“协变返回类型”，想必大多数人也跟我差不多。“协变”、“逆变”这些概念性的名词看起来就十分高大上的样子，但是很多基础书籍并没有明确地说明这些概念到底是什么意思，更不用说什么长篇大论描述了。所以，很多时候看到这些概念我都是似懂非懂，而今天自己就查阅了各种资料并通过这篇博文总结一下。

## 概念描述

　　这些概念都是十分抽象的，我觉得没有一定编程基础而来看这些内容的都会是一脸懵逼，完全找不到切入角度。所以，最好先把基础稍微过一遍。
　　关于这些概念的解释，我查阅了很多文章，各个描述不一，但根据我自己的编程经验来看，它们所要表达的意思似乎都是一样的，在这里我就总结一下。如果你想要查看这些文章，打开本文最底下所提供的[参考资料](https://extremegtr.github.io/2016/07/11/Covariance-And-Contravariance-In-Java/#anchor_references)链接即可。下面就开始详述我们最为关心的内容了。

### 文字定义

　　即将要说的那些名词，可以说基本都是源于数学或物理学。当然，在这里我们无需了解它们在数学或物理学上的定义，毕竟这里要探讨的是编程上的知识，不过，如果你非常清楚的话那是再好不过了，因为主体思想是一样的，只是套用到不同的领域上而已，所以说，其实学好基本学科还是挺重要的，更甚，如果对语言文字比较精通，那么就能直接从这些概念名词的字面意思开始入手。而对于基本学科学得比较差的人（比如我自己）来说，只能是根据具体现象来反向记忆这些概念的定义。
　　这些概念定义是广义的，所以它们不仅是适用于Java，更是适用于大多数编程语言。

- **协变（covariance）与逆变（contravariance）统称为变体或变型（variance）。**

- **变型（variance）描述子类型关系在类型变换（type transformation）的作用下是如何变化的。**

  **关于类型变换一词的解释：**
  **这里我选用类型变换一词，只是想整套描述更符合数学/物理学的美学，其实也可以用另一种说法：类型映射或者“类型构造”，或许你更喜欢这些词。**

  **此处所说的类型变换非平日我们经常所见到的诸如隐式类型转换（implicit type conversion）、显示类型转换（explicit type conversion）、强制类型转换（type coertion），这些都统称为类型转换（type conversion），描述的是数值类型以及父子类型的转换，比如int转换为long、String转换为Object。**

  **类型变换指的是在一种类型的基础上构造 / 映射 / 变换出另一种新类型，是质的转变，即原类型与新类型不在一个抽象层面上。**
  **比如：int映射出int[]，String映射出List<String>。**

- **协变 / 共变（covariance）指的是子类型关系在类型变换的作用下保持原样。**

- **逆变 / 反变（contravariance）指的是子类型关系在类型变换的作用下发生逆转。**

- **双变（bivariance）表示子类型关系在类型变换的作用下同时拥有协变与逆变2种效果。**

- **不可变（invariance） 表示子类型关系在类型变换的作用下，既没有协变的效果，也没有逆变的效果。**

看完上面这些文字，如果你还是摸不着头脑，没关系，之后从具体实例来着手，那就能更深刻地明白这些定义到底在说什么。

### 具体公式

有`X`、`Y`2种类型，而符号`≤`表示子类型关系（比如：`X ≤ Y`即类型`X`是类型`Y`的子类型），`f`表示类型变换，
假设`X ≤ Y`，并且`X`和`Y`经过同一类型变换`f`后构造出对应更复杂的类型`f(X)`和`f(Y)`，那么就可以得出如下这些结论：

- **如果f(X) ≤ f(Y)，即保持X和Y的关系，那么类型变换f是协变的（covariant），或具有协变性；**
- **如果f(Y) ≤ f(X)，即逆转X和Y的关系，那么类型变换f是逆变的（contravariant），或具有逆变性；**
- **如果即是f(X) ≤ f(Y)也是f(Y) ≤ f(X)，那么类型变换f是双变的（bivariant），或具有双变性；**
- **如果既不是f(X) ≤ f(Y)也不是f(Y) ≤ f(X)，那么类型变换f是不可变的（invariant），或具有不可变性；**

公式还是抽象的，但在这里我就给出一个最简单的例子你就能马上理解这些公式了：
　　如果有这么2个类型`Animal`和`Cat`，那么它们之间的关系是`Cat ≤ Animal`即`Cat`是`Animal`的子类型；通过它们构造出对应的数组类型`Animal[]`和`Cat[]`（此处就是本文所谓的“类型变换”，即由简单类型构造或映射出更复杂的类型），最终这对数组类型的关系是`Cat[] ≤ Animal[]`，即数组类型也保持着原类型之间的关系，这说明数组具有协变性。

所以你可以这样写代码：

```
Animal[] animals = new Cat[10];
```

复制



## Java中的协变与逆变

Java中支持协变、逆变这种特性的类型不算特别多，在《Thinking In Java》中其实已经有例子了，所以，这里也沿用里面的例子。

首先，这里准备一些基本的自定义类型：`Fruit`、`Apple`、`Orange`、`RedFujiApple`。

```
class Fruit { }

class Apple extends Fruit { }

class Orange extends Fruit { }

class RedFujiApple extends Apple { }
```

复制

### 数组

在Java中数组是具有协变性的，这等于说**子类型的数组可以赋予父类型的数组进行使用，即1个Apple数组是1个Fruit数组**。

```
public class CovariantArrays {
    public static void main(String[] args) {
        Fruit[] fruits = new Apple[10];
        fruits[0] = new Apple();
        fruits[1] = new RedFujiApple();	    

        try {
            // java.lang.ArrayStoreException
            fruits[0] = new Fruit();
        } catch (Exception e) {
            e.printStackTrace();
        }	    

        try {
            // java.lang.ArrayStoreException
            fruits[0] = new Orange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

复制

**利用数组的协变性，运行时代码就有可能出现问题，这说明协变数据并不是类型安全的**。
编译时肯定是不会出错的，因为这些代码符合类型规则，但到了运行时，我们有可能得到一个运行时异常`java.lang.ArrayStoreException`。

所以现在就用上面这份代码作为例子进行问题分析，在推导的时候带着前提条件去分析，并且针对**读取**和**写入**2种情况进行分析：

#### 前提条件

```
Fruit[] fruits = new Apple[10];
```

复制

协变数组`fruits`被类型`Fruit[]`所引用，而实际上的类型是`Apple[]`。

#### 问题详细分析

从之前所看到的前提条件我们可以确定2件事：

- 从`fruits` 协变数组中读取出来的元素是`Fruit`类型。
- 在编译期，编译器是绝对允许我们将`Fruit`以及其子类型的元素写入到`fruits`协变数组中。

从协变数组`fruits`中读取元素是**完全安全**的，无论是编译期还是运行时，都不会发生任何问题，
正如上面所说的，`fruits`所引用的数组其实是`Apple[]`类型，从`Apple[]`类型数组中读取出的元素绝对是`Fruit`类型啊。

但是将`Fruit`类型以及其子类型的元素写入到协变数组`fruits`中是**有可能在运行时出现问题**的；
这是因为编译器是完全没有理由阻止我们将`Fruit`类型以及其子类型的元素写入到协变数组`fruits`中，因为这个操作完全可以通过编译期的类型检查。

**可以试想一下，钻编译器空子而引发的问题：这个协变数组是从别人所写API的方法中返回获取的，对于方法用户的我们而言，可能直接忽视这协变数组的真实类型（正确来讲这不是好的API，因为它本身就不够清晰）；而有时我们写代码写得头昏脑胀，完全忘记自己定义的数组是一个协变数组，我们很自然地把它当作一个普通数组来用；在这2种情况下我们都很自然把某些符合父子关系类型的元素写入到该数组中，编译期没错，然而到了运行时，我们很可能就会收到一个异常java.lang.ArrayStoreException。**

上面的代码示例就很好地反映出这个问题：
　　我尝试把`Fruit`元素和`Orange`元素往协变数组`fruits`中放；`fruits`表面上是`Fruit`数组，而实际上是一个`Apple`数组，那运行时报错是铁定了。`Apple`数组肯定只存放`Apple`元素啊，你放`Orange`进去肯定给你报错啦。

#### 协变数组的结论

**这是Java协变数组的“缺陷”，但我们其实十分清楚引发这个问题深层原因，所以我们在利用数组的协变特性时可以完全规避这个问题：**
**应该尽量把协变数组当作只读数组使用，编译器没有这个强制要求，这必须是人为的强制要求（其实是挺难做到的）。**

既然Java支持协变数组，你可能会问Java支持逆变数组吗？答案是：**Java不支持逆变数组。**

但我们可以试想一下以下的代码，并且假设这些代码是能够在Java中正常工作的（实际上是不能）。

```
Fruit[] fruits = new Fruit[10] {new Apple(), new Orange(), new RedFujiApple()};
Orange[] oranges = fruits; // contravariant array
oranges[0] = new Orange();
```

复制

上面所演示到的逆变数组起着协变数组相反的效果：**父类型的数组可以赋予子类型的数组进行使用，即1个Fruit数组是1个Orange数组。**

假设Java的数组具有逆变性，就像上面所演示的一样，我们可以直接将`Fruit`类型数组`fruits`赋予`Orange`数组`oranges`。

但真的有逆变数组提供给我们使用，同样也是会出现问题的。逆变数组是协变数组的对立面，所以出现问题的地方也是对立的：
**协变数组是在写入元素的时候有可能出现问题，那么逆变数组是在读取元素的时候有可能出现问题了。**

所以，此时请回顾一下思考协变数组时的思路，再将这个思路逆向，我们就可以得出答案了：

正如上面代码所演示的那样，逆变数组`oranges`表面上是`Orange`数组，那运行时报错是铁定了。而实际是`Fruit`数组；即逆变数组`oranges`所引用的数组其实存储的是`Fruit`以及其子类型的元素，所以编译器或许允许我们通过`oranges`读取`Orange`元素，正如之前将其它类型的元素写入协变数组中一样，编译器认为这种行为是正确无误的。但到了运行时就有可能产生运行是错误，如果我们通过逆变数组`oranges`所取的元素刚好不是`Orange`类型，而是`Apple`类型，这样就引发错误产生运行时异常。而对于写入操作，逆变数组进行这种操作是完全安全的，因为`oranges`只能写入`Orange`类型的元素，将`Orange`元素放到`Fruit`数组中那肯定是毫无问题的。

**如果Java真的支持逆变数组，那么我们在使用它的时候估计还是要像使用协变数组一样规避问题：**
**应该尽量把逆变数组当作只写数组使用（当然，有没有这个要求那要看编译器的实现）。**

### 方法

在Java 1.5以及之后就支持**协变返回类型（covariant return type）**这一个特性：
**这特性指的是子类型覆盖父类型方法时，子类型方法的返回类型是父类型方法返回类型的子类型，我们就把这个返回类型称作协变返回类型。**

或许你很懵逼，这方法返回类型也具有协变性？
正如前面概念所说的那样，在这里对应的过程就是简单类型经过“类型转换”后所构造出的复杂类型正是方法返回类型。

```
class Base { }

class Derived extends Base { }

class Super {
    Base get() { 
        return new Base();
    }
}

class Sub extends Super {
    // return type of overridden method is allowed to vary
    @Override
    Derived get() {
        return new Derived();
    }
}

public class Client {
    public static void main(String[] args) {
        Super sup = new Sub();
        Base base = sup.get();
        // Derived
        System.out.println(base.getClass().getSimpleName());
    }
}
```

复制

有了具体代码示例就更好理解了：
　　`Base`和`Derived`之间具有父子类型关系，然后经过“类型转换”后，`Base`成为了`Super`类`get`方法的返回类型，而`Sub`继承`Super`覆盖它的`get`方法时，`Derived`成为了它的返回值；**即Base和Derived成为了抽象层面更高的方法返回类型，但它们仍保持着一种子类型关系，所以说方法返回类型具有协变性**。
　　但这种特性在Java 1.5之前的版本就不适用了，以前子类型覆盖父类型的方法，必须是方法签名和方法返回类型都保持一致的。在Java中关于方法的协变逆变内容我就只知道这么多内容。

### 泛型

Java泛型本身具有不可变性。但尽管如此，使用Java泛型自带的特性——**通配符**，
那可以使得泛型具有协变性或逆变性，在此处就不作详细讨论，在[另一篇探讨Java泛型的博文](https://extremegtr.github.io/2016/05/30/JavaSE-study-advanced-generics/#u901A_u914D_u7B26_uFF08Wildcard_uFF09)里再作详细分析。

## 重点概念相关词语

```
variance           变体、变型、变异性、可变性
covariance         协变、协变性
contravariance     逆变、逆变性
bivariance         双变、双变性
invariance         不变、不变性

variant            变异的、可变的
covariant          协变的
contravariannt     逆变的
bivariant          双变的
invariant          不变的
```



其中，对于`variance`、`variant`、`bivariance`、`bivariant`这4个单词的中文翻译，我还是感觉有点膈应的，
我看书少，见识也少，基础学科也学得不怎么行，而查了好多资料也找不到一个让我感觉比较舒服一点的中文翻译，强迫症简直是不能忍。

现在我确实明白到：
为啥大多数做学术的在科普或者讨论时都是中英混杂，因为基本上都是根据该外语词所对应的现象来记忆它的具体意思，
之所以这样又因为不是每个人都非常精通母语，所以脑袋拼凑不出与之对应的母语词，最终，只能是选择不翻译直接用外语词，
翻译这种事，对母语的精通程度必须要远超出对外语的精通程度，同时又要非常熟悉对应的专业领域，所以就编程专业来说，翻译很好的书非常少。







## 参考资料

- [Comparing covariance/contravariance rules in C#, Java and Scala](http://www.codeproject.com/Articles/899319/Comparing-covariance-contravariance-rules)
- [Covariance and Contravariance in Generics](https://msdn.microsoft.com/en-us/library/dd799517(v=vs.110).aspx)
- [Covariance and contravariance (computer science) From Wikipedia, the free encyclopedia](https://en.wikipedia.org/wiki/Covariance_and_contravariance_(computer_science))
- [Covariance, Invariance and Contravariance explained in plain English?](http://stackoverflow.com/questions/8481301/covariance-invariance-and-contravariance-explained-in-plain-english)

[# JavaSE](https://extremegtr.github.io/tags/JavaSE/)

[ JavaSE学习笔记 - 泛型进阶](https://extremegtr.github.io/2016/05/30/JavaSE-study-advanced-generics/)



[JavaSE学习笔记 - 注解基础 ](https://extremegtr.github.io/2017/02/15/JavaSE-study-annotation/)

[2](https://github.com/ExtremeGTR/extremegtr.github.io/issues/72) comments

Anonymous



[Markdown is supported](https://guides.github.com/features/mastering-markdown/)PreviewLogin with GitHub

![@yangqisheng](img/16619627.png)



yangqisheng

commented

about 2 years ago





写的挺好的。赞！

![@CodeXiaoMai](img/17211193.jfif)



CodeXiaoMai

commented

5 months ago





支持一下

- 文章目录
-  

- 站点概览

1. [1. 概念描述](https://extremegtr.github.io/2016/07/11/Covariance-And-Contravariance-In-Java/#概念描述)
2. [2. Java中的协变与逆变](https://extremegtr.github.io/2016/07/11/Covariance-And-Contravariance-In-Java/#Java中的协变与逆变)
3. [3. 重点概念相关词语](https://extremegtr.github.io/2016/07/11/Covariance-And-Contravariance-In-Java/#重点概念相关词语)
4. [4. 参考资料](https://extremegtr.github.io/2016/07/11/Covariance-And-Contravariance-In-Java/#参考资料)



© 2014 – 2019  ExtremeGTR |  406k |  6:09

由 [Hexo](https://hexo.io/) 强力驱动 v3.9.0

 

|

 

主题 – [NexT.Gemini](https://theme-next.org/) v7.2.0