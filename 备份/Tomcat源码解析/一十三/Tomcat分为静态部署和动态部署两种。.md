[![img](img/35695-20241201073014811-1847930772.jpg)](https://www.doubao.com/?channel=cnblogs&source=hw_db_cnblogs)

[![返回主页](https://www.cnblogs.com/skins/custom/images/logo.gif)](https://www.cnblogs.com/yishi-san/)

# [一十三](https://www.cnblogs.com/yishi-san)

## 

- [博客园](https://www.cnblogs.com/)
- [首页](https://www.cnblogs.com/yishi-san/)
- 
- [联系](https://msg.cnblogs.com/send/一十三)
- 
- [管理](https://i.cnblogs.com/)

随笔 - 20 文章 - 1 评论 - 1 阅读 - 13277

# [Tomcat源码分析--部署应用程序](https://www.cnblogs.com/yishi-san/p/16586466.html)

Tomcat分为静态部署和动态部署两种。

### Tomcat静态部署

所谓静态部署，简单一点可以理解为Tomcat容器未运行时部署应用程序。我们已经知道若将一个war文件放置到webapps目录下，然后启动Tomcat容器，war文件会被解压运行，而这一过程就是静态部署。接下来我们的主要目标就是要学习Tomcat源码是如何书写这一过程的。

在Host容器中有一个HostConfig监听器，它也实现了LifecycleListener接口，所以我们可以在HostConfig类中找到lifecycleEvent方法。当在lifecycleEvent方法中触发START_EVENT事件时就开始进入部署程序的逻辑。START_EVENT最终会调用deployApps(); 我们先来着重看一下deployWARs(appBase, filteredAppPaths)方法。

deployWARs()方法就是用来部署war文件的。在第一次阅读这个方法时你会看到一些重要的if语句，但可能不解其意，没有关系回过头来我们还要来重读这段代码。在第一次阅读该方法的关键是要看到results.add(es.submit(new DeployWar(this, cn, war)));语句。在该语句中会打开一个加载War包的线程。在这个线程中最终会调用deployWAR(ContextName cn, File war)方法。这个方法初看起来会让人云山雾绕，为了便于理解我们需要了解一些额外的知识。

#### 额外知识

第一个额外知识我们需要知道Host组件中的一些重要属性，如下：

xmlBase：此虚拟主机的XML Base目录。这是一个目录的路径名，其中可能包含要部署在此虚拟主机上的上下文 XML 描述符。用户可以设定自定义路径，如果未指定， conf/<engine_name>/<host_name>将使用默认值。

copyXML：boolean值 是否将/META-INF/context.xml文件拷贝到xmlBase所定义的文件路径当中 默认值为 false

unpackWARs：boolean值：true 将war包解压部署，fasle不解压直接部署。默认值是true

deployXML：Boolean值：是否解析/META-INF/context.xml文件 true 解析 fasle 不解析，默认值 true

第二个额外知识是我们需要知道在Tomcat容器中有一个“应用程序上下文"的概念。在这里我们可以将应用程序上下文简单的理解为一个context.xml(这样理解并不准确，但是可以让我们更简单的理解deployWAR()方法)；文件。相对于Tomcat容器来来说，从应用覆盖范围上可以将上下文文件划分为三种，第一种是在我们自己程序中的/META-INF/context.xml下。第二种是适用于一个所有主机下的web程序在$CATALINA_BASE/conf/[enginename]/[hostname]/context.xml.default 文件中，但三种就是适用于当前容器中所有程序的上下文配置在$CATALINA_BASE/conf/context.xml文件中。

第三个额外知识是需要我们知道，我们可以提升某一个应用程序上下文的作用范围，比如我们有一个test-001项目，我们可以将这个项目的context.cml文件拷贝到$CATALINA_BASE/conf/[enginename]/[hostname]/目录下以提升上下文等级。

好了现在我们可以来分析代码了，我们首先来看第一段try-catch代码，在这段代码中主要包含四个重要的if语句

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
try {
    // 从解压后的war包中加载上下文文件
    if (deployThisXML && useXml && !copyXML) {
        synchronized (digesterLock) {
            try {
                context = (Context) digester.parse(xml);
            } catch (Exception e) {
                log.error(sm.getString("hostConfig.deployDescriptor.error", war.getAbsolutePath()), e);
            } finally {
                digester.reset();
                if (context == null) {
                    context = new FailedContext();
                }
            }
        }
        context.setConfigFile(xml.toURI().toURL());
    } else if (deployThisXML && xmlInWar) { // 从war包中加载上下文文件
        synchronized (digesterLock) {
            try (JarFile jar = new JarFile(war)) {
                JarEntry entry = jar.getJarEntry(Constants.ApplicationContextXml);
                try (InputStream istream = jar.getInputStream(entry)) {
                    context = (Context) digester.parse(istream);
                }
            } catch (Exception e) {
                log.error(sm.getString("hostConfig.deployDescriptor.error", war.getAbsolutePath()), e);
            } finally {
                digester.reset();
                if (context == null) {
                    context = new FailedContext();
                }
                context.setConfigFile(UriUtil.buildJarUrl(war, Constants.ApplicationContextXml));
            }
        }
        // 项目中有上下文文件，但是不解析
    } else if (!deployThisXML && xmlInWar) {
        // Block deployment as META-INF/context.xml may contain security
        // configuration necessary for a secure deployment.
        log.error(sm.getString("hostConfig.deployDescriptor.blocked",
                               cn.getPath(), Constants.ApplicationContextXml,
                               new File(host.getConfigBaseFile(), cn.getBaseName() + ".xml")));
    } else {
        // 使用默认配置时，使用默认上下文环境。（这是当下的重点）
        context = (Context) Class.forName(contextClass).getConstructor().newInstance();
    }
} catch (Throwable t) {
    ExceptionUtils.handleThrowable(t);
    log.error(sm.getString("hostConfig.deployWar.error", war.getAbsolutePath()), t);
} finally {
    if (context == null) {
        context = new FailedContext();
    }
}
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

接下来我们看第二段代码，这段代码的主要作用就是在实现刚刚提到的第三个额外知识点，知道了第三点以后以下代码阅读起来就简单很多了。

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
boolean copyThisXml = false;
if (deployThisXML) {
    if (host instanceof StandardHost) {
        copyThisXml = ((StandardHost) host).isCopyXML();
    }

    // If Host is using default value Context can override it.
    // 如果主机使用默认值(默认是false)，则上下文可以覆盖它
    if (!copyThisXml && context instanceof StandardContext) {
        copyThisXml = ((StandardContext) context).getCopyXML();
    }

    if (xmlInWar && copyThisXml) {
        // Change location of XML file to config base
        // 将XML文件的位置更改为config_base
        xml = new File(host.getConfigBaseFile(), cn.getBaseName() + ".xml");
        try (JarFile jar = new JarFile(war)) {
            JarEntry entry = jar.getJarEntry(Constants.ApplicationContextXml);
            try (InputStream istream = jar.getInputStream(entry);
                 FileOutputStream fos = new FileOutputStream(xml);
                 BufferedOutputStream ostream = new BufferedOutputStream(fos, 1024)) {
                byte buffer[] = new byte[1024];
                while (true) {
                    int n = istream.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    ostream.write(buffer, 0, n);
                }
                ostream.flush();
            }
        } catch (IOException e) {
            /* Ignore */
        }
    }
}
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

接下来我们来看最后一段代码(我就不在贴代码了)，最后一段代码又是一段try-catch，这段代码目前对我们来说需要知道两个重点，第一个重点是host.addChild(context);这一行代码。在读到这一行代码时我们必须反应过来这行代码一执行那么随之而来的就是加载Context组件生命周期方法，所以接下来我们需要将重点转移到Context组件当中。第二个重点是finally语句中的代码，在finally语句中主要是在保存当前所部属程序的状态，在这里大家也可以简单思考一下为什么要保存这些状态。

### Tomcat动态部署

所谓动态部署，简单理解就是在Tomcat运行时部署应用或更新应用。假设我们有一个名为test.war的程序包正在tomcat中运行，此时我们更新test.war包后程序会被重新加载，在这个过程中一个随之而来的问题就是Tomcat是如何知到我们更新了程序。一种合理的推断是，在Tomcat中可能存在一段类似于永真循坏的代码一直在检查我们的程序是否发生了变更。事实也确实如此。当tomcat触发STARTING_PREP事件时会调用start方法。在ContainerBase类中startInternal()会调用threadStart();该方法大致内容如下。

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
......
threadDone = false;
String threadName = "ContainerBackgroundProcessor[" + toString() + "]";
thread = new Thread(new ContainerBackgroundProcessor(), threadName);
thread.setDaemon(true);
thread.start();
......
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

这个方法的关键之处是开启了一个后台线程，来调用ContainerBase类的background()方法。background()方法会发送一个PERIODIC_EVENT事件，而当HostConfig类中监听到该事件类型时会调用HostConfig类的check()方法以来检查我们部署的程序是否发生过变更。在check()方法中当下应重点关注deployed变量，在这个变量中保存的就是我们所部署过的程序，那这个变量是在哪里赋值的呢？还记得deployWAR()方法中第二段try-catch语句中的finally语句吗？大家可以在去看一下那段源码。checkResources()就是在检查我们的程序是否发生过变更，而这个是否发生过变更的判断依据是文件的修改时间。

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
protected void check() {
     if (host.getAutoDeploy()) {
         // Check for resources modification to trigger redeployment
         DeployedApplication[] apps = deployed.values().toArray(new DeployedApplication[0]);
         for (DeployedApplication app : apps) {
             if (tryAddServiced(app.name)) {
                 try {
                     checkResources(app, false);
                 } finally {
                     removeServiced(app.name);
                 }
             }
         }
         ......
             // Hotdeploy applications
             deployApps();
     }
 }
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

这个check()方法最终还是会调用deployApps()方法。这里应当注意到静态部署调用deployApps()方法，动态部署同样调用deployApps()方法，但是在静态部署中分析deployWARs()方法时有一段代码只是一笔带过，现在我们在来看看这段代码。

这是deployWARs()方法的主要代码，现在我们看这段代码要把重点放在if (deploymentExists(cn.getName())) 语句上，当程序已经部署了并且没有发生改变时会执行continue语句，也就是不会在重复部署。

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
...... 
File war = new File(appBase, file);
if (file.toLowerCase(Locale.ENGLISH).endsWith(".war") && war.isFile() && !invalidWars.contains(file)) {
    ContextName cn = new ContextName(file, true);
    if (tryAddServiced(cn.getName())) {
        try {
            if (deploymentExists(cn.getName())) {
                DeployedApplication app = deployed.get(cn.getName());
                boolean unpackWAR = unpackWARs;
                if (unpackWAR && host.findChild(cn.getName()) instanceof StandardContext) {
                    unpackWAR = ((StandardContext) host.findChild(cn.getName())).getUnpackWAR();
                }
                if (!unpackWAR && app != null) {
                    // Need to check for a directory that should not be
                    // there
                    File dir = new File(appBase, cn.getBaseName());
                    if (dir.exists()) {
                        if (!app.loggedDirWarning) {
                            log.warn(sm.getString("hostConfig.deployWar.hiddenDir",
                                                  dir.getAbsoluteFile(), war.getAbsoluteFile()));
                            app.loggedDirWarning = true;
                        }
                    } else {
                        app.loggedDirWarning = false;
                    }
                }
                removeServiced(cn.getName());
                continue;
            }
            ......
```

[![复制代码](img/copycode.gif)](javascript:void(0);)

 

分类: [Tomcat](https://www.cnblogs.com/yishi-san/category/2203390.html)

[好文要顶](javascript:void(0);) [关注我](javascript:void(0);) [收藏该文](javascript:void(0);) [微信分享](javascript:void(0);)

[![img](img/20170411131426.png)](https://home.cnblogs.com/u/yishi-san/)

[一十三](https://home.cnblogs.com/u/yishi-san/)
[粉丝 - 2](https://home.cnblogs.com/u/yishi-san/followers/) [关注 - 1](https://home.cnblogs.com/u/yishi-san/followees/)

[+加关注](javascript:void(0);)

0

0

[升级成为会员](https://cnblogs.vip/)

[« ](https://www.cnblogs.com/yishi-san/p/14518150.html)上一篇： [记录关于WebService的简单使用](https://www.cnblogs.com/yishi-san/p/14518150.html)
[» ](https://www.cnblogs.com/yishi-san/p/16609553.html)下一篇： [Tomcat源码分析--类加载器](https://www.cnblogs.com/yishi-san/p/16609553.html)

posted @ 2022-08-14 22:30 [一十三](https://www.cnblogs.com/yishi-san) 阅读(202) 评论(0) [编辑](https://i.cnblogs.com/EditPosts.aspx?postid=16586466) [收藏](javascript:void(0)) [举报](javascript:void(0))





[刷新评论](javascript:void(0);)[刷新页面](https://www.cnblogs.com/yishi-san/p/16586466.html#)[返回顶部](https://www.cnblogs.com/yishi-san/p/16586466.html#top)

发表评论 [升级成为园子VIP会员](https://cnblogs.vip/)



编辑预览



 自动补全

 [退出](javascript:void(0);) [订阅评论](javascript:void(0);) [我的博客](https://www.cnblogs.com/Carl-Don/)

[Ctrl+Enter快捷键提交]

[【推荐】还在用 ECharts 开发大屏？试试这款永久免费的开源 BI 工具！](https://dataease.cn/?utm_source=cnblogs)
[【推荐】编程新体验，更懂你的AI，立即体验豆包MarsCode编程助手](https://www.marscode.cn/?utm_source=advertising&utm_medium=cnblogs.com_ug_cpa&utm_term=hw_marscode_cnblogs&utm_content=home)
[【推荐】凌霞软件回馈社区，博客园 & 1Panel & Halo 联合会员上线](https://www.cnblogs.com/cmt/p/18669224)
[【推荐】抖音旗下AI助手豆包，你的智能百科全书，全免费不限次数](https://www.doubao.com/?channel=cnblogs&source=hw_db_cnblogs)
[【推荐】博客园社区专享云产品让利特惠，阿里云新客6.5折上折](https://market.cnblogs.com/)
[【推荐】轻量又高性能的 SSH 工具 IShell：AI 加持，快人一步](http://ishell.cc/)

[![img](img/35695-20250207193659673-708765730.jpg)](https://www.doubao.com/chat/coding?channel=cnblogs&source=hw_db_cnblogs)

**相关博文：**

·[Tomcat源码分析--类加载器](https://www.cnblogs.com/yishi-san/p/16609553.html)

·[Tomcat源码分析使用NIO接收HTTP请求(六)----变更工程目录](https://www.cnblogs.com/yishi-san/p/16971500.html)

·[Tomcat部署及优化](https://www.cnblogs.com/970618z/p/16649783.html)

·[详解Tomcat 配置文件server.xml](https://www.cnblogs.com/javaxubo/p/17498208.html)

·[Linux-Tomcat文件结构和组成](https://www.cnblogs.com/lyj1023/p/16368704.html)

**阅读排行：**
· [趁着过年的时候手搓了一个低代码框架](https://www.cnblogs.com/codelove/p/18719305)
· [本地部署DeepSeek后，没有好看的交互界面怎么行！](https://www.cnblogs.com/xiezhr/p/18718693)
· [为什么说在企业级应用开发中，后端往往是效率杀手？](https://www.cnblogs.com/jackyfei/p/18712595)
· [AI工具推荐：领先的开源 AI 代码助手——Continue](https://www.cnblogs.com/mingupupu/p/18716802)
· [用 C# 插值字符串处理器写一个 sscanf](https://www.cnblogs.com/hez2010/p/18718386/csharp-interpolated-string-sscanf)

### 公告

昵称： [一十三](https://home.cnblogs.com/u/yishi-san/)
园龄： [7年10个月](https://home.cnblogs.com/u/yishi-san/)
粉丝： [2](https://home.cnblogs.com/u/yishi-san/followers/)
关注： [1](https://home.cnblogs.com/u/yishi-san/followees/)

[+加关注](javascript:void(0))

| [<](javascript:void(0);)2025年2月[>](javascript:void(0);) |      |      |      |      |      |      |
| --------------------------------------------------------- | ---- | ---- | ---- | ---- | ---- | ---- |
| 日                                                        | 一   | 二   | 三   | 四   | 五   | 六   |
| 26                                                        | 27   | 28   | 29   | 30   | 31   | 1    |
| 2                                                         | 3    | 4    | 5    | 6    | 7    | 8    |
| 9                                                         | 10   | 11   | 12   | 13   | 14   | 15   |
| 16                                                        | 17   | 18   | 19   | 20   | 21   | 22   |
| 23                                                        | 24   | 25   | 26   | 27   | 28   | 1    |
| 2                                                         | 3    | 4    | 5    | 6    | 7    | 8    |

### 搜索

 

### 常用链接

- [我的随笔](https://www.cnblogs.com/yishi-san/p/)
- [我的评论](https://www.cnblogs.com/yishi-san/MyComments.html)
- [我的参与](https://www.cnblogs.com/yishi-san/OtherPosts.html)
- [最新评论](https://www.cnblogs.com/yishi-san/comments)
- [我的标签](https://www.cnblogs.com/yishi-san/tag/)

### [我的标签](https://www.cnblogs.com/yishi-san/tag/)

- [JavaScript(1)](https://www.cnblogs.com/yishi-san/tag/JavaScript/)

### [随笔分类](https://www.cnblogs.com/yishi-san/post-categories)

- [go(1)](https://www.cnblogs.com/yishi-san/category/1683619.html)
- [java(1)](https://www.cnblogs.com/yishi-san/category/1473165.html)
- [JavaScript(2)](https://www.cnblogs.com/yishi-san/category/982421.html)
- [MySql(1)](https://www.cnblogs.com/yishi-san/category/1099028.html)
- [Spring(1)](https://www.cnblogs.com/yishi-san/category/1561179.html)
- [Tomcat(8)](https://www.cnblogs.com/yishi-san/category/2203390.html)
- [密码学(1)](https://www.cnblogs.com/yishi-san/category/1775967.html)
- [区块链(3)](https://www.cnblogs.com/yishi-san/category/1545424.html)

### 随笔档案

- [2022年12月(3)](https://www.cnblogs.com/yishi-san/p/archive/2022/12)
- [2022年11月(4)](https://www.cnblogs.com/yishi-san/p/archive/2022/11)
- [2022年8月(2)](https://www.cnblogs.com/yishi-san/p/archive/2022/08)
- [2021年3月(1)](https://www.cnblogs.com/yishi-san/p/archive/2021/03)
- [2020年10月(1)](https://www.cnblogs.com/yishi-san/p/archive/2020/10)
- [2020年6月(1)](https://www.cnblogs.com/yishi-san/p/archive/2020/06)
- [2020年4月(1)](https://www.cnblogs.com/yishi-san/p/archive/2020/04)
- [2020年3月(3)](https://www.cnblogs.com/yishi-san/p/archive/2020/03)
- [2019年10月(1)](https://www.cnblogs.com/yishi-san/p/archive/2019/10)
- [2019年5月(1)](https://www.cnblogs.com/yishi-san/p/archive/2019/05)
- [2019年4月(1)](https://www.cnblogs.com/yishi-san/p/archive/2019/04)
- [2017年4月(1)](https://www.cnblogs.com/yishi-san/p/archive/2017/04)

### [文章分类](https://www.cnblogs.com/yishi-san/article-categories)

- [MySql(1)](https://www.cnblogs.com/yishi-san/category/1099036.html)

### [阅读排行榜](https://www.cnblogs.com/yishi-san/most-viewed)

- [1. DES算法原理(4637)](https://www.cnblogs.com/yishi-san/p/12990973.html)
- [2. 关于Copper.js的简单使用方法(1448)](https://www.cnblogs.com/yishi-san/p/13782983.html)
- [3. sql与集合(872)](https://www.cnblogs.com/yishi-san/p/10624122.html)
- [4. Tomcat源码分析使用NIO接收HTTP请求(一)----简单实现Acceptor、Poller、PollerEvent(858)](https://www.cnblogs.com/yishi-san/p/16900079.html)
- [5. 对于go当中的cli简单理解(798)](https://www.cnblogs.com/yishi-san/p/12592048.html)

### [评论排行榜](https://www.cnblogs.com/yishi-san/most-commented)

- [1. Tomcat源码分析使用NIO接收HTTP请求(一)----简单实现Acceptor、Poller、PollerEvent(1)](https://www.cnblogs.com/yishi-san/p/16900079.html)

### [推荐排行榜](https://www.cnblogs.com/yishi-san/most-liked)

- [1. Tomcat源码分析使用NIO接收HTTP请求(三)----解析请求行(1)](https://www.cnblogs.com/yishi-san/p/16932071.html)
- [2. Tomcat源码分析使用NIO接收HTTP请求(一)----简单实现Acceptor、Poller、PollerEvent(1)](https://www.cnblogs.com/yishi-san/p/16900079.html)

### [最新评论](https://www.cnblogs.com/yishi-san/comments)

- [1. Re:Tomcat源码分析使用NIO接收HTTP请求(一)----简单实现Acceptor、Poller、PollerEvent](https://www.cnblogs.com/yishi-san/p/16900079.html)
- 好文
- --邓等灯等灯

Copyright © 2025 一十三
Powered by .NET 9.0 on Kubernetes