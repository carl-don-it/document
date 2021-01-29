# JSON

## 介绍

JavaScript Object Notation JavaScript对象表示法

- json现在多用于存储和交换文本信息的语法
- 进行数据的传输
- JSON 比 XML 更小、更快，更易解析。

```java
	1. 基本规则
		* 数据在名称/值对中：json数据是由键值对构成的
			* 键用引号(单双都行)引起来，也可以不使用引号
			* 值得取值类型：
				1. 数字（整数或浮点数）
				2. 字符串（在双引号中）
				3. 逻辑值（true 或 false）
				4. 数组（在方括号中）	{"persons":[{},{}]}
				5. 对象（在花括号中） {"address":{"province"："陕西"....}}
				6. null
		* 数据由逗号分隔：多个键值对由逗号分隔
		* 花括号保存对象：使用{}定义json 格式
		* 方括号保存数组：[]
```

- **例子**

```java
Person p = new Person();
p.setName("张三");
p.setAge(23);
p.setGender(true);

var person = {"name": "张三", age: 23, 'gender': true};

var ps = [{"name": "张三", "age": 23, "gender": true},
          {"name": "李四", "age": 24, "gender": true},
          {"name": "王五", "age": 25, "gender": false}];
```

> javascript中操作json，请看ajax代码

# java bean 和 json 转换

实则是 property 和 json value的转换 （**注意不是field，因为不反射操作field，大部分的框架都是反射getter、setter**）

一般都不是反射直接操作field，而是根据getter方法生成property；反序列化时根据property找到setter设置field。因此可以通过重写getter、setter来改变行为，甚至重写field。甚至没有field都可以。

# json-lib

[Java解析json(一)：json-lib](https://blog.csdn.net/NowUSeeMe/article/details/54847860)

# fastjson

[Java解析json(三)：fastjson](https://blog.csdn.net/NowUSeeMe/article/details/54847891)

# gson

[java解析json(四)：gson](https://blog.csdn.net/NowUSeeMe/article/details/54847902)