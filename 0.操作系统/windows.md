# 组策略

[【Windows】Win10家庭版启用组策略gpedit.msc](https://blog.csdn.net/u013642500/article/details/80138799)

[怎样自动以管理员身份运行bat文件?](https://www.zhihu.com/question/34541107#:~:text=%E5%8F%AF%E4%BB%A5%E7%BB%99bat%E6%96%87%E4%BB%B6%E5%88%9B%E5%BB%BA,%E7%AE%A1%E7%90%86%E5%91%98%E8%BA%AB%E4%BB%BD%E8%BF%90%E8%A1%8C%E5%AE%83%E3%80%82)

[Windows权限设置详解](https://www.jianshu.com/p/8efaf3f93488)

[从telnet www.baidu.com 80 来玩一下http](https://www.jianshu.com/p/b5708312a5e0)

[Windows 系统关于用户和权限的逻辑是怎样的？](https://www.zhihu.com/question/66229405)

# 端口、进程

Window 通过cmd查看端口占用、相应进程、杀死进程等的命令

一、 查看所有进程占用的端口
在开始-运行-cmd,输入：netstat -ano 可以查看所有进程

二、查看占用指定端口的程序
当你在用tomcat发布程序时，经常会遇到端口被占用的情况，我们想知道是哪个程序或进程占用了端口，可以用该命令 netstat –ano|findstr [指定端口号]
如：查询占用了8080端口的进程：netstat -ano|findstr "8080"

三、通过任务管理器杀死相关的进程
方法一：使用任务管理器杀死进程
打开任务管理器->查看->选择列->然后勾选PID选项，回到任务管理器上可以查看到对应的pid，然后结束进程
当然上面的方法有时候不好用，就是任务管理器中的进程比较多的时候，然后去找到对应的进程是很麻烦的，所以还有一种方法可以杀死进程的

方法二：使用命令杀死进程
1>首先找到进程号对应的进程名称
tasklist|findstr [进程号]；如：tasklist|findstr 3112

2>然后根据进程名称杀死进程
taskkill /f /t /im [进程名称]；如：taskkill /f /t /im /javaw.exe 

# cmd

1. 改变环境变量后需要重新打开cmd刷新一下

# 删除服务

Sc delete 服务名 

需要cmd 管理员才可以

# 微软输入法

Windows 10：设置→时间和语言→区域和语言→中文(中华人民共和国)→选项→微软拼音→选项→词库和自学习→添加新的或编辑现有的用户自定义短语→添加。

（不想吐槽为什么藏得这么深……）

然后在短语里面输入以下代码：

%yyyy%-%MM%-%dd% %HH%:%mm%:%ss% +0800

 

来自 <<https://blog.walterlv.com/ime/2017/09/18/date-time-format-using-microsoft-pinyin.html>> 