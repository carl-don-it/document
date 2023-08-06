#### String、 StringBuffer、 StringBuilder 的区别？

1. 可变不可变
   String：字符串常量，在修改时不会改变自身；若修改，等于重新生成新的字符串对象。
   StringBuffer：在修改时会改变对象自身，每次操作都是对StringBuffer对象本身进行修改，不是生成新的对象；使用场景：对字符串经常改变情况下，主要方法： append（）， insert（）等。
2. 线程是否安全
   String：对象定义后不可变，线程安全。
   StringBuffer：是线程安全的（对调用方法加入同步锁），执行效率较慢，适用于多线程下操作字符串缓冲区大量数据。
   StringBuilder：是线程不安全的，适用于单线程下操作字符串缓冲区大量数据。
3. 共同点
   StringBuilder 与 StringBuffer 有公共父类 AbstractStringBuilder(抽象类)。StringBuilder、 StringBuffer 的方法都会调用 AbstractStringBuilder 中的公共方法，如 super.append(...)。只是 StringBuffer 会在方法上加 synchronized 关键字，进行同步。

最后，如果程序不是多线程的，那么使用StringBuilder 效率高于 StringBuffer。

#### 字符串和基本类型之间的转换

# 重要

https://ibytecode.com/blog/string-literal-pool/ | String literal pool – iByteCode Technologies
https://javaranch.com/journal/200409/ScjpTipLine-StringsLiterally.html | The SCJP Tip Line - Strings, Literally
https://blog.jamesdbloom.com/JVMInternals.html | JVM Internals
https://tech.meituan.com/2014/03/06/in-depth-understanding-string-intern.html | 深入解析String#intern - 美团技术团队
https://www.mindprod.com/jgloss/interned.html#GC | interned Strings : Java Glossary
https://droidyue.com/blog/2014/08/30/java-details-string-concatenation/ | Java细节：字符串的拼接 - 技术小黑屋

https://www.google.com/search?q=java+intern%E6%80%A7%E8%83%BD&newwindow=1&sxsrf=APwXEddMny3sXDVFM6TawJ1mXqQhF3J8yQ%3A1681996370621&ei=UjpBZIO0JbLKkPIP3tWtoAs&ved=0ahUKEwiDpbzhxLj-AhUyJUQIHd5qC7QQ4dUDCBA&uact=5&oq=java+intern%E6%80%A7%E8%83%BD&gs_lcp=Cgxnd3Mtd2l6LXNlcnAQA0oECEEYAVCYAViYAWDvA2gBcAB4AIABgAKIAYACkgEDMi0xmAEAoAEBwAEB&sclient=gws-wiz-serp#ip=1 | java intern性能 - Google Search
https://heapdump.cn/article/4627319 | 【译】Java String intern()对程序性能有哪些影响？ | HeapDump性能社区
https://blog.51cto.com/flydean/5690151 | JVM系列之:String.intern的性能_51CTO博客_java string intern