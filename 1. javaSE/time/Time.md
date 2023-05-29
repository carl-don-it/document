# 总

OffsetDateTime、ZonedDateTime和Instant和system.currentMillis都能在时间线上以纳秒精度存储一个瞬间.

OffsetDateTime和Instant可用于模型的字段类型，因为它们都表示瞬间值并且还不可变。

LocalDateTime是一个不可变的日期-时间对象。该类不存储时区，通常是默认大家都是使用同一个时区。

如果哪些国家使用夏令时，直接使用Zone就好了，如果没有使用夏令时，可以使用UTC+offset。

转换的时候建议使用instant作跳板一把梭。

2020-01-13T16:00:00.000Z中 

T表示分隔符，Z表示的是UTC。
UTC：世界标准时间，在标准时间上加上8小时，即东八区时间，也就是北京时间。

# 旧的

**总结**： Calendar设置时间，Date是显示时间，Dateformat是转换Date和String

#### Date

表示特定的瞬间，精确到毫秒。  

```java
public Date() 
// 时间戳和日期的转换
public Date(long date) 

public long getTime() 
```

#### DateFormat

https://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html?is-external=true

是日期/时间格式化子类的抽象类，我们通过这个类可以帮我们完成日期和文本之间的转换,也就是可以在Date对象与String对象之间进行来回转换  

| 标识字母（区分大小写） | 含义 |
| ---------------------- | ---- |
| y                      | 年   |
| M                      | 月   |
| d                      | 日   |
| H                      | 时   |
| m                      | 分   |
| s                      | 秒   |

```
new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
```

#### Calendar

`java.util.Calendar` 是日历类，在Date后出现，替换掉了许多Date的方法。该类将所有可能用到的时间信息封装为静态成员变量，方便获取。日历类就是方便获取各个时间属性，**设置时间**。  

#### 用法

```
获取现在的年月日时分秒
从 1970 年 1 月 1 日 0 时 0 分 0 秒到现在的毫秒数,时间戳
某月的一天
打印昨天的当前时刻
格式化日期
```



![image-20200329191357702](img/image-20200329191357702.png)

# 参考文献

[weixin](https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzI0MTUwOTgyOQ==&action=getalbum&album_id=1696358010555547649&scene=21#wechat_redirect)

https://blog.csdn.net/f641385712/category_10749009.html

