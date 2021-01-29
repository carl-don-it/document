# 实现定时任务的方案

1. 使用 JDK 的Timer和TimerTask实现
   可以实现简单的间隔执行任务，无法实现按日历去调度执行任务。
2. 使用Quartz实现
   Quartz 是一个异步任务调度框架，功能丰富，可以实现按日历调度。
3. 使用Spring Task实现
   Spring 3.0后提供Spring Task实现任务调度，支持按日历调度，相比Quartz功能稍简单，但是在开发基本够用，支持注解编程方式

# 快速入门

## 单线程串行化执行，同步

```java
@EnableScheduling
启动类或者配置类加上该注解开启
```

```java
@Component
public class SerializedTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializedTask.class);


//    定义任务调试策略
    @Scheduled(initialDelay = 3000, fixedRate = 5000) //第一次延迟3秒，以后每隔5秒执行一次
//    @Scheduled(cron = "0/3 * * * * *")//每隔3秒去执行
//    @Scheduled(fixedRate = 3000) //在任务开始后3秒执行下一次调度
//    @Scheduled(fixedDelay = 3000) //在任务结束后3秒后才开始执行
    public void task1() {
        LOGGER.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务1结束===============");

    }

//    定义任务调试策略
    @Scheduled(cron = "0/3 * * * * *")//每隔3秒去执行
//    @Scheduled(fixedRate = 3000) //在任务开始后3秒执行下一次调度
//    @Scheduled(fixedDelay = 3000) //在任务结束后3秒后才开始执行
    public void task2() {
        LOGGER.info("===============测试定时任务2开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务2结束===============");

    }
}
```

## 异步执行

配置类添加线程池

```java
@Configuration
@EnableScheduling
public class AsyncTaskConfig implements SchedulingConfigurer, AsyncConfigurer {
    //线程池线程数量
    private int corePoolSize = 5;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();//初始化线程池
        scheduler.setPoolSize(corePoolSize);//线程池容量
        return scheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskScheduler();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
    }
}
```

## 代码

![image-20200224140532594](img/image-20200224140532594.png)

# cron表达式

- **cron表达式包括6部分**：

  秒（0~59） 

  分钟（0~59）

  小时（0~23） 

  月中的天（1~31） 

  月（1~12） 

  周中的天（填写MON，TUE，WED，THU，FRI，SAT,SUN，或数字1~7 1表示MON，依次类推）

- **特殊字符介绍**：
  “/”字符表示指定数值的增量
  “*”字符表示所有可能的值
  “-”字符表示区间范围
  "," 字符表示列举
  “？”字符仅被用于月中的天和周中的天两个子表达式，表示不指定值

- **例子**：
  0/3 * * * * * 每隔3秒执行
  0 0/5 * * * * 每隔5分钟执行
  0 0 0 * * * 表示每天0点执行
  0 0 12 ? * WEN 每周三12点执行
  0 15 10 ? * MON-FRI 每月的周一到周五10点 15分执行
  0 15 10 ? * MON,FRI 每月的周一和周五10点 15分执行