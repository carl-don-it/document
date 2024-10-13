# [长风破浪会有时，直挂云帆济沧海。](https://www.cnblogs.com/harvard)

## 

- [博客园](https://www.cnblogs.com/)
- [首页](https://www.cnblogs.com/harvard/)
- [新随笔](https://i.cnblogs.com/EditPosts.aspx?opt=1)
- [联系](https://msg.cnblogs.com/send/消烟客)
- [订阅](javascript:void(0))
- [管理](https://i.cnblogs.com/)

随笔 - 1  文章 - 4  评论 - 0  阅读 - 1204

# [JConsole远程连接Linux下java程序](https://www.cnblogs.com/harvard/articles/2993125.html)



在java程序启动时添加VM参数 就可以达到JConsole远程连接的目的。

\1. 添加VM arguments

[![复制代码](img/copycode.gif)](javascript:void(0);)

```
1 java 
-Djava.rmi.server.hostname=111.111.111.111 　　　　　// 远程ip地址， 本地输入时用, 注意要能够访问
-Dcom.sun.management.jmxremote 　　　　　　　　　　　　// 　　
-Dcom.sun.management.jmxremote.port=9090 　　　　　　// 监听的端口, 本地输入, 注意防火墙
-Dcom.sun.management.jmxremote.ssl=false 　　　　　　
-Dcom.sun.management.jmxremote.authenticate=true 　// 使用密码, 如果写成false， 下面一句就不用了, 后面的配置密码也不用了
-Dcom.sun.management.jmxremote.password.file=/usr/java/jdk1.6.0_43/jre/lib/management/jmxremote.password // 很明显, 配置密码的地儿
-jar -server 
-Xms10240M -Xmx10240M -Xmn3750m 
-XX:PermSize=128M -XX:MaxPermSize=128M -Xss2048K -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC 
-XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+CMSClassUnloadingEnabled 
-XX:LargePageSizeInBytes=128M -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80 
-XX:SoftRefLRUPolicyMSPerMB=0 -XX:+PrintClassHistogram -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gclog/gc.log 
server.jar
```

[![复制代码](https://assets.cnblogs.com/images/copycode.gif)](javascript:void(0);)

\2. 配置hostname

使用 #hostname查看 hostname

编辑 /etc/hosts文件 如下:

```
1 127.0.0.1    myname localhost.localdomain localhost
2 ::1        localhost6.localdomain6 localhost6
3 111.111.111.111    myname
```

\3. 配置访问用户名和口令

转到： /usr/java/jdk1.6.0_43/jre/lib/management/ （安装JDK的目录）

编辑jmxremote.access文件, 在最后加上

```
1 userName      readwrite \
2               create javax.management.monitor.*,javax.management.timer.* \
3               unregister
```

userName随便写， 就是你连接时需要 用到的

编辑jmxremote.password, 复制jmxremote.password.template, 改名去掉template

在最后添加

```
1 userName    userPassword
```

这里就是在设置密码。

好了， 现在就可以在window上使用JConsole或jvisualvm远程连接了