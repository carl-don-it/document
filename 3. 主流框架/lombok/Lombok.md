本文地址 ： https://www.cnblogs.com/exmyth/p/8710902.html 

###### Lombok简介

> Lombok是一个可以通过简单的注解形式来帮助我们简化消除一些必须有但显得很臃肿的Java代码的工具，通过使用对应的注解，可以在编译源码的时候生成对应的方法。官方地址：[https://projectlombok.org/](https://link.jianshu.com/?t=https%3A%2F%2Fprojectlombok.org%2F)，github地址：[https://github.com/rzwitserloot/lombok](https://link.jianshu.com/?t=https%3A%2F%2Fgithub.com%2Frzwitserloot%2Flombok)。

###### @Getter and @Setter

> 你可以用@Getter / @Setter注释任何字段（当然也可以注释到类上的），让lombok自动生成默认的getter / setter方法。
> 默认生成的方法是public的，如果要修改方法修饰符可以设置**AccessLevel**的值，例如：***@Getter(access = AccessLevel.PROTECTED)\***

###### @ToString

> 生成toString()方法，默认情况下，它会按顺序（以逗号分隔）打印你的类名称以及每个字段。可以这样设置不包含哪些字段***@ToString(exclude = "id")\*** / ***@ToString(exclude = {"id","name"})\***
> 如果继承的有父类的话，可以设置**callSuper** 让其调用父类的toString()方法，例如：***@ToString(callSuper = true)\***

###### @EqualsAndHashCode

> 生成hashCode()和equals()方法，默认情况下，它将使用所有非静态，非transient字段。但可以通过在可选的exclude参数中来排除更多字段。或者，通过在parameter参数中命名它们来准确指定希望使用哪些字段。

###### @NoArgsConstructor 

> @NoArgsConstructor生成一个无参构造方法。当类中有final字段没有被初始化时，编译器会报错，此时可用@NoArgsConstructor(force = true)，然后就会为没有初始化的final字段设置默认值 0 / false / null。对于具有约束的字段（例如@NonNull字段），不会生成检查或分配，因此请注意，正确初始化这些字段之前，这些约束无效。

###### @RequiredArgsConstructor

>  @RequiredArgsConstructor会生成构造方法（可能带参数也可能不带参数），如果带参数，这参数只能是以final修饰的未经初始化的字段，或者是以@NonNull注解的未经初始化的字段
> @RequiredArgsConstructor(staticName = "of")会生成一个of()的静态方法，并把构造方法设置为私有的 

###### @AllArgsConstructor

>  @AllArgsConstructor 生成一个全参数的构造方法 

###### @Data

> @Data 包含了 @ToString、@EqualsAndHashCode、@Getter / @Setter和@RequiredArgsConstructor的功能

###### @Accessors

> @Accessors 主要用于控制生成的getter和setter
> **主要参数介绍**
>
> - fluent boolean值，默认为false。此字段主要为控制生成的getter和setter方法前面是否带get/set
> - chain boolean值，默认false。如果设置为true，setter返回的是此对象，方便链式调用方法
> - prefix 设置前缀 例如：@Accessors(prefix = "abc") private String abcAge 当生成get/set方法时，会把此前缀去掉

###### @Synchronized

> 给方法加上同步锁

```java
import lombok.Synchronized;
public class SynchronizedExample {
   private final Object readLock = new Object();
   
  @Synchronized
  public static void hello() {
    System.out.println("world");
  }
   
  @Synchronized
  public int answerToLife() {
    return 42;
 }
  @Synchronized("readLock")
  public void foo() {
    System.out.println("bar");
   }
}
//等效代码
public class SynchronizedExample {
    private static final Object $LOCK = new Object[0];
    private final Object $lock = new Object[0];
    private final Object readLock = new Object();

    public static void hello() {
      synchronized($LOCK) {
        System.out.println("world");
      }
    }

    public int answerToLife() {
     synchronized($lock) {
        return 42;
      }
    }

    public void foo() {
      synchronized(readLock) {
        System.out.println("bar");
      }
    }
}
```

- ###### @Wither

  > 提供了给final字段赋值的一种方法，相当于重新创建一个类，慎用

```java
//使用lombok注解的
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.Wither;
public class WitherExample {
  @Wither private final int age;
  @Wither(AccessLevel.PROTECTED) @NonNull private final String name;
   
  public WitherExample(String name, int age) {
    if (name == null) throw new NullPointerException();
    this.name = name;
    this.age = age;
  }
}
//等效代码
import lombok.NonNull;
public class WitherExample {
    private final int age;
    private @NonNull final String name;

    public WitherExample(String name, int age) {
     if (name == null) throw new NullPointerException();
     this.name = name;
     this.age = age;
    }

    public WitherExample withAge(int age) {
     return this.age == age ? this : new WitherExample(age, name);
    }

    protected WitherExample withName(@NonNull String name) {
     if (name == null) throw new java.lang.NullPointerException("name");
     return this.name == name ? this : new WitherExample(age, name);
    }
}
```

###### @onX

> 在注解里面添加注解的方式，不知道怎么用

```java
public class SchoolDownloadLimit implements Serializable {
    private static final long serialVersionUID = -196412797757026250L;

    @Getter(onMethod = @_({@Id,@Column(name="id",nullable=false),@GeneratedValue(strategy= GenerationType.AUTO)}))
    @Setter
    private Integer id;

    @Getter(onMethod = @_(@Column(name="school_id")))
    @Setter
    private Integer schoolId;


    @Getter(onMethod = @_(@Column(name = "per_download_times")))
    @Setter
    private Integer perDownloadTimes;

    @Getter(onMethod = @_(@Column(name = "limit_time")))
    @Setter
    private Integer limitTime;

    @Getter(onMethod = @_(@Column(name = "download_to_limit_an_hour")))
    @Setter
    private Integer downloadToLimitInHour;

    @Getter(onMethod = @_(@Column(name = "available")))
    @Setter
    private Integer available = 1;
}
```

###### @Builder

> @Builder注释为你的类生成复杂的构建器API。**Builder模式**
> lets you automatically produce the code required to have your class be instantiable with code such as:

```java
Person.builder().name("Adam Savage").city("San Francisco").job("Mythbusters").job("Unchained Reaction").build();
```

直接看官方示例，对比一下就都明白了

```java
//使用lombok注解的
import lombok.Builder;
import lombok.Singular;
import java.util.Set;
@Builder
public class BuilderExample {
 private String name;
 private int age;
 @Singular private Set<String> occupations;
}
//等效代码
import java.util.Set;
class BuilderExample {
  private String name;
  private int age;
  private Set<String> occupations;

  BuilderExample(String name, int age, Set<String> occupations) {
      this.name = name;
      this.age = age;
      this.occupations = occupations;
  }

  public static BuilderExampleBuilder builder() {
      return new BuilderExampleBuilder();
  }

  public static class BuilderExampleBuilder {
      private String name;
      private int age;
      private java.util.ArrayList<String> occupations;

      BuilderExampleBuilder() {
      }

      public BuilderExampleBuilder name(String name) {
          this.name = name;
          return this;
      }

      public BuilderExampleBuilder age(int age) {
          this.age = age;
          return this;
      }

      public BuilderExampleBuilder occupation(String occupation) {
          if (this.occupations == null) {
              this.occupations = new java.util.ArrayList<String>();
          }

          this.occupations.add(occupation);
          return this;
      }

      public BuilderExampleBuilder occupations(Collection<? extends String> occupations) {
          if (this.occupations == null) {
              this.occupations = new java.util.ArrayList<String>();
          }

          this.occupations.addAll(occupations);
          return this;
      }

      public BuilderExampleBuilder clearOccupations() {
          if (this.occupations != null) {
              this.occupations.clear();
          }

          return this;
      }

      public BuilderExample build() {
          // complicated switch statement to produce a compact properly sized immutable set omitted.
          // go to https://projectlombok.org/features/Singular-snippet.html to see it.
          Set<String> occupations = ...;
          return new BuilderExample(name, age, occupations);
      }

      @java.lang.Override
      public String toString() {
          return "BuilderExample.BuilderExampleBuilder(name = " + this.name + ", age = " + this.age + ", occupations = " + this.occupations + ")";
      }
  }
}
```

###### @Delegate

> 这个注解也是相当的牛逼，看下面的截图，它会该类生成一些列的方法，这些方法都来自与List接口

![]( https://upload-images.jianshu.io/upload_images/4259109-b71f191dc2da6772.png )

 [就介绍这么多了，更多的注解请看官方文档](https://link.jianshu.com/?t=https%3A%2F%2Fprojectlombok.org%2Ffeatures%2Findex.html) 