

## 介绍

- **原因**

集合中是可以存放任意对象的，只要把对象存储集合后，那么这时他们都**会被提升成Object类型**。当我们在取出每一个对象，并且进行相应的操作，这时必须采用类型转换。

大家观察下面代码：

~~~java
public class GenericDemo {
	public static void main(String[] args) {
		Collection coll = new ArrayList();
		coll.add("abc");
		coll.add("itcast");
		coll.add(5);//由于集合没有做任何限定，任何类型都可以给其中存放
		Iterator it = coll.iterator();
		while(it.hasNext()){
			//需要打印每个字符串的长度,就要把迭代出来的对象转成String类型
			String str = (String) it.next();
			System.out.println(str.length());
		}
	}
}
//程序在运行时发生了问题**java.lang.ClassCastException**。
~~~

 Collection虽然可以存储各种对象，但实际上通常Collection只存储同一类型对象。例如都是存储字符串对象。

因此在JDK5之后，新增了**泛型**(**Generic**)语法，让你在设计API时可以指定类或方法支持泛型，这样我们使用API的时候也变得更为简洁，并得到了**编译时期的语法检查**。

Java有泛型这一个概念，初衷是为了保证在**运行时出现的错误**能提早放到编译时检查。

* **概念 **：**可以在类或方法中预支地使用未知的类型。声明一个未知的类型。**当没有指定泛型时，默认类型为Object类型。

- **特点**

  1. 泛型是数据类型的一部分，我们将类名与泛型合并一起看做数据类型。  
  2. 编译器类型擦写，逻辑上的数据类型，实际上都是一种数据类型
  3. 泛型数组的创建需要利用反射函数

- **注意**
  **不同的泛型在编译时期（不是运行期间）是不同的类型**，因此不可以用==，需要类型提升

  以下均报错

  ```java
  //Error:(43, 26) java: 不可比较的类型: java.lang.String和java.util.ArrayList<java.lang.String>
  if ("sdf"==  new ArrayList<String>()) { }
  
  //Error:(42, 39) java: 不可比较的类型: java.util.ArrayList<java.lang.Object>和java.util.ArrayList<java.lang.String>
  if (new ArrayList<>() ==  new ArrayList<String>())
      
  //Error:(17, 37) java: 不可比较的类型: java.lang.Class<capture#1, 共 ? extends T[]>和java.lang.Class<java.lang.Object[]>
  public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
  		@SuppressWarnings("unchecked")
  		T[] copy = (newType == Object[].class)    
  ```

  改错：全部做类型提升`(Object)`

  ```java
  if ((Object)"sdf"==  (Object)new ArrayList<String>()) { }
  if ((Object)new ArrayList<>() ==  (Object)new ArrayList<String>())
  T[] copy = ((Object)newType == (Object)Object[].class)   
  ```

  

## 好处

上一节只是讲解了泛型的引入，那么泛型带来了哪些好处呢？

* 将运行时期的ClassCastException，转移到了编译时期变成了编译失败。
* 避免了类型强转的麻烦。

通过我们如下代码体验一下：

~~~java
public class GenericDemo2 {
	public static void main(String[] args) {
        Collection<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("itcast");
        // list.add(5);//当集合明确类型后，存放类型不一致就会编译报错
        // 集合已经明确具体存放的元素类型，那么在使用迭代器的时候，迭代器也同样会知道具体遍历元素类型
        Iterator<String> it = list.iterator();
        while(it.hasNext()){
            String str = it.next();
            //当使用Iterator<String>控制元素类型后，就不需要强转了。获取到的元素直接就是String类型
            System.out.println(str.length());
        }
	}
}
~~~

## 定义与使用

我们在集合中会大量使用到泛型，这里来完整地学习泛型知识。

泛型，用来灵活地将数据类型应用到不同的类、方法、接口当中。将数据类型作为参数进行传递。

### 定义和使用含有泛型的类

定义格式：

~~~
修饰符 class 类名<代表泛型的变量> {  }
~~~

例如，API中的ArrayList集合：

~~~java
class ArrayList<E>{ 
    public boolean add(E e){ }

    public E get(int index){ }
   	....
}
~~~

使用泛型： 即什么时候确定泛型。

**在创建对象的时候确定泛型**

 例如，`ArrayList<String> list = new ArrayList<String>();`

此时，变量E的值就是String类型,那么我们的类型就可以理解为：

~~~java 
class ArrayList<String>{ 
     public boolean add(String e){ }

     public String get(int index){  }
     ...
}
~~~

再例如，`ArrayList<Integer> list = new ArrayList<Integer>();`

此时，变量E的值就是Integer类型,那么我们的类型就可以理解为：

~~~java
class ArrayList<Integer> { 
     public boolean add(Integer e) { }

     public Integer get(int index) {  }
     ...
}
~~~

举例自定义泛型类

~~~java
public class MyGenericClass<MVP> {
	//没有MVP类型，在这里代表 未知的一种数据类型 未来传递什么就是什么类型
	private MVP mvp;
     
    public void setMVP(MVP mvp) {
        this.mvp = mvp;
    }
     
    public MVP getMVP() {
        return mvp;
    }
}
~~~

使用:

~~~java
public class GenericClassDemo {
  	public static void main(String[] args) {		 
         // 创建一个泛型为String的类
         MyGenericClass<String> my = new MyGenericClass<String>();    	
         // 调用setMVP
         my.setMVP("大胡子登登");
         // 调用getMVP
         String mvp = my.getMVP();
         System.out.println(mvp);
         //创建一个泛型为Integer的类
         MyGenericClass<Integer> my2 = new MyGenericClass<Integer>(); 
         my2.setMVP(123);   	  
         Integer mvp2 = my2.getMVP();
    }
}
~~~

###  含有泛型的方法

定义格式：

~~~
修饰符 <代表泛型的变量> 返回值类型 方法名(参数){  }
~~~

例如，

~~~java
public class MyGenericMethod {	  
    public <MVP> void show(MVP mvp) {
    	System.out.println(mvp.getClass());
    }
    
    public <MVP> MVP show2(MVP mvp) {	
    	return mvp;
    }
    public <M> List<M> method03(Character m) {
    	return new ArrayList<>();
    }
}
~~~

使用格式：**调用方法时，确定泛型的类型**

1. 传入参数时确定泛型，参数的类型就是泛型

2. 赋值的时候确定泛型

3. 调用方法的时候确定泛型  

~~~java
public class GenericMethodDemo {
    public static void main(String[] args) {
        // 创建对象
        MyGenericMethod mm = new MyGenericMethod();
        // 演示看方法提示
        mm.show("aaa");
        mm.show(123);
        mm.show(12.45);
        //测试
        List<Object> objects = mm.method03('3');
        objects.add(new Object());
        List<String> m = mm.<String>method03('m');
        m.add("");
    }
}
~~~

### 含有泛型的接口、抽象类、父类

定义格式：

~~~
修饰符 interface接口名<代表泛型的变量> {  }
~~~

例如，

~~~java
public interface MyGenericInterface<E>{  
	public abstract void add(E e);
	
	public abstract E getE();  
}
~~~

使用格式：

**1、定义类时确定泛型的类型**

例如

~~~java
public class MyImp1 implements MyGenericInterface<String> {
	@Override
    public void add(String e) {
        // 省略...
    }

	@Override
	public String getE() {
		return null;
	}
}
~~~

此时，泛型E的值就是String类型。

 **2、始终不确定泛型的类型，直到创建对象时，确定泛型的类型**

 例如 

~~~java
public class MyImp2<E> implements MyGenericInterface<E> {
	@Override
	public void add(E e) {
       	 // 省略...
	}

	@Override
	public E getE() {
		return null;
	}
}
~~~

确定泛型：

~~~java
/*
 * 使用
 */
public class GenericInterface {
    public static void main(String[] args) {
        MyImp2<String>  my = new MyImp2<String>();  
        my.add("aa");
    }
}
~~~

**3、如果接口使用了上下限，则实现类也需要使用上下限，因为是给父类、接口传递的类型需要合法**

```java
public interface ValidateCodeRepository<C extends ValidateCode>{}

public class ValidateCodeRedisRepository<C extends ValidateCode>  implements ValidateCodeRepository<C>{}
```



## 泛型通配符

一般用来接受类型

当使用泛型类或者接口时，**传递的数据中**，泛型类型不确定，可以通过通配符<?>表示。但是一旦使用泛型的通配符后，只能使用Object类中的共性方法，集合中元素自身方法无法使用。

#### 通配符基本使用

泛型的通配符:**不知道使用什么类型来接收的时候,此时可以使用?,?表示未知通配符。**

用于形式参数类型或者普通类型变量

> 此时只能接受数据,不能往该集合中存储数据。

举个例子大家理解使用即可：

~~~java
public static void main(String[] args) {
    Collection<Intger> list1 = new ArrayList<Integer>();
    getElement(list1);
    Collection<String> list2 = new ArrayList<String>();
    getElement(list2);
}
public static void getElement(Collection<?> coll){}
//？代表可以接收任意类型
~~~

> tips:泛型不存在继承关系 Collection<Object> list = new ArrayList<String>();这种是错误的。是泛型不存在，有泛型的主体类型存在继承关系，前提是泛型相同

#### 通配符高级使用----受限泛型

之前设置泛型的时候，实际上是可以任意设置的，只要是类就可以设置。但是在JAVA的泛型中可以指定一个泛型的**上限**和**下限**。

**泛型的上限**：

* **格式**： `类型名称 <? extends 类 > 对象名称`
* **意义**： `只能接收该类型及其子类`

**泛型的下限**：

- **格式**： `类型名称 <? super 类 > 对象名称`
- **意义**： `只能接收该类型及其父类型`

比如：现已知Object类，String 类，Number类，Integer类，其中Number是Integer的父类

~~~java
public static void main(String[] args) {
    Collection<Integer> list1 = new ArrayList<Integer>();
    Collection<String> list2 = new ArrayList<String>();
    Collection<Number> list3 = new ArrayList<Number>();
    Collection<Object> list4 = new ArrayList<Object>();
    
    getElement(list1);
    getElement(list2);//报错
    getElement(list3);
    getElement(list4);//报错
  
    getElement2(list1);//报错
    getElement2(list2);//报错
    getElement2(list3);
    getElement2(list4);
  
}
// 泛型的上限：此时的泛型?，必须是Number类型或者Number类型的子类
public static void getElement1(Collection<? extends Number> coll){}
// 泛型的下限：此时的泛型?，必须是Number类型或者Number类型的父类
public static void getElement2(Collection<? super Number> coll){}
~~~

```java
	//测试泛型数组如何创建
	public void test2() {
		//创建普通数组
		Integer[] integers = new Integer[3];
		ArrayList[] arrayLists = new ArrayList[3];

		//创建集合数组，但是不能使用泛型来创建 new ArrayList<M>[3] 错的
		List<String>[] list = new ArrayList[3];

		//使用泛型类型变量来接收，只能 add 进 String 元素
		list[0] = new ArrayList<>();
		List<String> strings = list[0];
		list[0].add("string");
		System.out.println(list);
	}

	//测试泛型通配符？如何使用，一般用来接受类型
	public void test3() {
		//普通的 ArrayList 对象,普通泛型
		ArrayList<LinkedHashMap<String, String>> linkedHashMaps = new ArrayList<>();
		//添加元素
		linkedHashMaps.add(new LinkedHashMap<String, String>());
		linkedHashMaps.add(new LinkedHashMap<String, String>());

		//赋值给有上限的泛型集合，泛型通配符
		ArrayList<? extends HashMap<String, String>> hashMaps = linkedHashMaps;
		//strings.add(new LinkedHashMap()); 错误，不能添加元素，只能遍历
		System.out.println(Arrays.toString(hashMaps.toArray()));
		//或者强转后可以使用
		
		linkedHashMaps = (ArrayList<LinkedHashMap<String, String>>) hashMaps;
		linkedHashMaps.add(new LinkedHashMap<String, String>());
	}
```



## Class和Class<?>

* 两个获取类对象的方法，返回值不一样

  类对象里面需要一个泛型

  没有上下限的通配符和不用没什么区别

  ```java
  1. public static Class<?> forName(String className)  //返回通配符类型的类对象
     		Class<?> string = Class.forName("java.lang.String"); //没法根据Strig参数确定具体类型，因此只能用<?>
    // 用Class<?>创建对象需要强转：
     		People people =(People)Class.forName("com.lyang.demo.fanxing.People").newInstance();
  
  2. Class<T> (T).class //返回的就是具体类型的类对象
    		Class<String> stringClass = String.class;
     用Class<T>创建对象不需要强转：
     		String s = stringClass.newInstance();  
  
  3. public T newInstance() //反射创建对象 ,可以返回具体类型
  	
  ```

  

## 数组泛型

```java
T[] mArray;
// 不能直接使用mArray = new T[DEFAULT_SIZE];
mArray = (T[]) Array.newInstance(type, size);
```

## 擦除

代码里 `List<Integer> xs=new List<>(); 实际类型参数消失了、擦除了 List xs=new List(); `，只有那些函数签名和类结构里的泛型会存作元数据 供反射使用

## 反射

通过反射获得泛型的实际类型参数

- 把泛型变量当成方法的参数，利用Method类的getGenericParameterTypes方法来获取泛型的实际类型参数

- 例子：

  ```java
  public class GenericTest {
      public static void main(String[] args) throws Exception {
          getParamType();
      }
           /*利用反射获取方法参数的实际参数类型*/
      public static void getParamType() throws NoSuchMethodException{
                  TypeVariable<Class<List>>[] typeParameters = List.class.getTypeParameters();
          System.out.println(Arrays.toString(typeParameters));
          
          
          Method method = GenericTest.class.getMethod("applyMap",Map.class);
          //获取方法的泛型参数的类型
          Type[] types = method.getGenericParameterTypes();
          System.out.println(types[0]);
          //参数化的类型
          ParameterizedType pType  = (ParameterizedType)types[0];
          //原始类型
          System.out.println(pType.getRawType());
          //实际类型参数
          System.out.println(pType.getActualTypeArguments()[0]);
          System.out.println(pType.getActualTypeArguments()[1]);
      }
      /*供测试参数类型的方法*/
      public static void applyMap(Map<Integer,String> map){
      }
  }
  ```

- 输出结果：

  ```stylus
  [E]
  java.util.Map<java.lang.Integer, java.lang.String>
  interface java.util.Map
  class java.lang.Integer
  class java.lang.String
  ```



## 参考文献

https://www.zhihu.com/question/20400700

Java 泛型 <? super T> 中 super 怎么 理解？与 extends 有何不同？ - Philip Fry的回答 - 知乎
https://www.zhihu.com/question/20400700/answer/283109538

Java 泛型 <? super T> 中 super 怎么 理解？与 extends 有何不同？ - 呵呵一笑百媚生的回答 - 知乎
https://www.zhihu.com/question/20400700/answer/117670919