## [spring.profiles.active来分区配置](https://www.cnblogs.com/anakin/p/8569827.html)

- **spring.profiles.active**

很多时候，我们项目在开发环境和生成环境的环境配置是不一样的，例如，数据库配置，在开发的时候，我们一般用测试数据库，而在生产环境的时候，我们是用正式的数据，这时候，我们可以利用profile在不同的环境下配置用不同的配置文件或者不同的配置

spring boot允许你通过命名约定按照一定的格式(application-{profile}.properties)来定义多个配置文件，然后通过在application.properyies通过spring.profiles.active来具体激活一个或者多个配置文件，如果没有没有指定任何profile的配置文件的话，spring boot默认会启动application-default.properties。

> profile的配置文件可以按照application.properyies的放置位置一样，放于以下四个位置，
>
> 1. 当前目录的 “/config”的子目录下
> 2. 当前目录下
> 3. classpath根目录的“/config”包下
> 4. classpath的根目录下

在这里我们就定义俩个profile文件，application-cus1.properties和application-cus2.properties，并在俩个文件中都分别写上变量cusvar=cus1和cusvar=cus2


我们在application.properyies也写上，并把profile切换到application-cus1.properties的配置文件

```properties
cusvar=cus3
spring.profiles.active=cus1
```

可以通过这样子来测试

```java
@RestController
@RequestMapping("/task")
public class TaskController {

    @RequestMapping(value = {"/",""})
    public String hellTask(@Value("${cusvar}")String cusvar ){

        return "hello task !! myage is " + cusvar;
    }

}
```

> 在这里可以看到spring.profiles.active激活的profile不同，打印出来的结果也不一样。会覆盖默认的属性

- @**Profile**

除了可以用profile的配置文件来分区配置我们的环境变量，在代码里，我们还可以直接用@Profile注解来进行配置，例如数据库配置，这里我们先定义一个接口
 

```java
public interface DBConnector {
    public void configure();    
}
```


分别定义俩个实现类来实现它

```java
/**
  * 测试数据库
  */
@Component
@Profile("testdb")
public class TestDBConnector implements DBConnector {

    @Override
    public void configure() {

        System.out.println("testdb");

    }
}

/**
 * 生产数据库
 */
@Component
@Profile("devdb")
public class DevDBConnector implements DBConnector {

    @Override
    public void configure() {

        System.out.println("devdb");

    }

}
```

通过在配置文件激活具体使用哪个实现类

```
spring.profiles.active=testdb
```

然后就可以这么用了

```java
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired DBConnector connector ;

    @RequestMapping(value = {"/",""})
    public String hellTask(){

        connector.configure(); //最终打印testdb     
        return "hello task !! myage is " + myage;
    }

}
```

除了spring.profiles.active来激活一个或者多个profile之外，还可以用spring.profiles.include来叠加profile

```properties
spring.profiles: testdb
spring.profiles.include: proddb,prodmq
```

以上就是spring boot用profile的作用

## 通过命令行设置属性值

相信使用过一段时间Spring Boot的用户，一定知道这条命令：`java -jar xxx.jar --server.port=8888`，通过使用--server.port属性来设置xxx.jar应用的端口为8888。

在命令行运行时，连续的两个减号`--`就是对`application.properties`中的属性值进行赋值的标识。所以，`java -jar xxx.jar --server.port=8888`命令，等价于我们在`application.properties`中添加属性`server.port=8888`，该设置在样例工程中可见，读者可通过删除该值或使用命令行来设置该值来验证。

通过命令行来修改属性值固然提供了不错的便利性，但是通过命令行就能更改应用运行的参数，那岂不是很不安全？是的，所以Spring Boot也贴心的提供了屏蔽命令行访问属性的设置，只需要这句设置就能屏蔽：`SpringApplication.setAddCommandLineProperties(false)`。

