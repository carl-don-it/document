## [如何开启Run DashBoard](https://blog.csdn.net/m18633778874/article/details/82687389  )

找到.idea下面的workspace.xml文件，加入一段配置代码   源代码位

```xml
<component name="RunDashboard">
 <option name="ruleStates">
     <list>
       <RuleState>
           <option name="name" value="ConfigurationTypeDashboardGroupingRule" />
       </RuleState>
       <RuleState>
          <option name="name" value="StatusDashboardGroupingRule" />
       </RuleState>
     </list>
  </option>
  <option name="contentProportion" value="0.22874807" />
    
         <!-- 配置代码-->
  <option name="configurationTypes">
     <set>
          <option value="SpringBootApplicationConfigurationType" />
     </set>
  </option>
          <!-- 配置代码-->
 </component>
```

## class关联src

在idea点进去class文件，可以有choose sources 的选择![image-20200202163059445](img/image-20200202163059445.png)

## 卡顿

[idea关闭不必要的插件减少内存占用](https://blog.csdn.net/tutian2000/article/details/80074643)

[intellij idea cpu占用率太大太满 运行速度太慢 使了五个解决方法最终成功](https://blog.csdn.net/zdxxinlang/article/details/78391060?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task)

## 单行注释

![image-20200311110511036](img/image-20200311110511036.png)

## Idea 启动项目时，卡在Parsing Java 解决方案

https://blog.csdn.net/Gabriel576282253/article/details/88866623

## 程序并行运行

![image-20210701143151897](img/image-20210701143151897.png)

## idea debug won shutdown hook

https://youtrack.jetbrains.com/issue/IDEA-75946

https://stackoverflow.com/questions/24660408/how-can-i-get-intellij-debugger-to-allow-my-apps-shutdown-hooks-to-run

## springboot yml文件不是绿叶子问题

https://blog.csdn.net/weixin_56408993/article/details/124285097

社区版下载带spring的插件

## 其他

[Skipped breakpoint because it happened inside debugger evaluation - Intellij IDEA](https://stackoverflow.com/questions/47866398/skipped-breakpoint-because-it-happened-inside-debugger-evaluation-intellij-ide)

https://youtrack.jetbrains.com/issue/IDEA-43728

## Local Changes

[IDEA新版本界面看不到Version Control窗口的Local Changes显示](https://blog.csdn.net/ahwsk/article/details/108225435)

## 插件

![1687515676595](img/1687515676595.png)



# 参考文件

[yourbatman](https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzI0MTUwOTgyOQ==&action=getalbum&album_id=1374601107946962947&scene=173&from_msgid=2247490748&from_itemidx=1&count=3&nolastread=1#wechat_redirect)

> **心水功能**
>
> Dependencies
> ✌Analyze Stack Trace or Thread Dump...
> 远程调试
> Stream流调试
> 多线程调试
> Postfix Completion，cast、for、fori、forr、iter
> Maven Helper
> Dependency Analyzer
> idea连接数据库

[鼠标右键菜单添加idea打开文件夹/项目](https://blog.csdn.net/hung_jun/article/details/106883126)

# 安装

[IntelliJ IDEA 2023.1.4激活破解图文教程（亲测有用，永久激活）](https://www.exception.site/essay/idea-reset-eval)

[同一台电脑安装多个版本的idea](https://juejin.cn/post/7216143702124314685)

