# 通用教程

[JPA & Spring Data JPA学习与使用小记](https://www.cnblogs.com/z-sm/p/9176636.html)

[JPA入门及深入](https://www.cnblogs.com/antLaddie/p/12985708.html)

https://blog.csdn.net/devilac/article/details/110389372 | SpringData Jpa 之 修改、删除数据_jpa delete 返回值-CSDN博客
[Spring Data JPA实现数据的增删改查操作 ,save()操作，如果id不存在就insert新增，如果id存在就是update - sunny123456 - 博客园](https://www.cnblogs.com/sunny3158/p/16353791.html )
https://www.cnblogs.com/hanzeng1993/p/11926597.html | Spring Data Jpa 之增删改查 - 韩增 - 博客园
https://blog.csdn.net/VIP099/article/details/107576710 | Spring系列：JPA 常用接口和方法_sping-jpa-CSDN博客

[JPA 中的 PersistenceUnit 和 PersistenceContext](https://springdoc.cn/java-persistenceunit-persistencecontext-difference/)

https://www.cnblogs.com/lenve/p/10640472.html | 干货|一文读懂 Spring Data Jpa！ - 江南一点雨 - 博客园


# 深入了解

[Spring Data JPA系列教程](https://blog.csdn.net/qq_40161813/category_11746503.html?spm=1001.2014.3001.5482)

https://so.csdn.net/so/search?q=Spring%20Data%20JPA&t=blog&u=qq_40161813

[Spring Data JPA 之 Session 的 open-in-view 对事务的影响，entitymanager的生命周期和缓存的生命周期](https://blog.csdn.net/qq_40161813/article/details/129477346)

[介绍hibernate中主要的组件](https://blog.csdn.net/qq_40797063/article/details/103481084)

[Java 持久层s四片解说](https://blog.csdn.net/footless_bird/category_12222648.html)

[在 Spring 中正确注入 EntityManager](https://chenhe.me/post/inject-entitymanager-in-spring-correctly)

[从零开始学 Spring Data JPA](https://blog.csdn.net/qq_36259143/category_10026286.html)

[SpringDataJPA+Hibernate框架源码剖析](https://zhuanlan.zhihu.com/p/520513892)

# jpa和hibernate

[JPA&Hibernate---SessionFactory和EntityManagerFactory之间的区别](https://muyinchen.github.io/2017/11/16/JPA&Hibernate---SessionFactory%E5%92%8CEntityManagerFactory%E4%B9%8B%E9%97%B4%E7%9A%84%E5%8C%BA%E5%88%AB/)

[Are Session and EntityManager the same?](https://stackoverflow.com/questions/73256101/are-session-and-entitymanager-the-same)

# spring data jpa

[yaml属性配置](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#appendix.application-properties.data)



# 自动建表

https://blog.csdn.net/u011066470/article/details/106583930 | 解决springboot+整合h2数据库，自动建表不支持驼峰命名规则的解决办法_springboot h2 create table-CSDN博客
https://www.google.com/search?q=spring-boot+h2%E8%87%AA%E5%8A%A8%E7%BB%99%E5%88%9B%E5%BB%BA%E8%A1%A8&oq=spring-boot+h2%E8%87%AA%E5%8A%A8%E7%BB%99%E5%88%9B%E5%BB%BA%E8%A1%A8&gs_lcrp=EgZjaHJvbWUyBggAEEUYOTIKCAEQABiABBiiBDIKCAIQABiABBiiBNIBCTEzODI0ajBqN6gCALACAA&sourceid=chrome&ie=UTF-8 | spring-boot h2自动给创建表 - Google Search
https://blog.csdn.net/a82514921/article/details/108041847 | Java单元测试实践-26.使用JPA自动创建数据库表_jpa 生成表创建语句-CSDN博客

# 缓存的坑

一般事务结束才flush。

一级缓存又称为“Session的缓存”。

 二级缓存又称为“SessionFactory的缓存”。

1. 不要使用事务，都是一条sql一个事务，这样就不会使用缓存。要使用事务请用mp。

2. 尽量通过缓存操作，只使用原生的save和find，保证操作一致性，并且不要使用deleteAll。deleteALLinbatch。

3. @Query是另一套系统，不会修改缓存，因此如果混合使用，那么最好@modify两个属性都加上，清空缓存。

   > [解决：jpa中由于缓存问题引起的，查询出的数据不是数据库中最新数据](https://blog.csdn.net/weixin_43770545/article/details/103732082)

[Spring data jpa 缓存机制总结](https://blog.csdn.net/qq_34485381/article/details/107117550)

[Hibernate JPA 缓存配置和例子](https://blog.csdn.net/qq_36259143/article/details/120207387)

[Hibernate中对象的三种状态及相互转化](https://blog.csdn.net/fg2006/article/details/6436517)

[Hibernate一级缓存和三种状态](https://www.cnblogs.com/longlyseul/p/9863474.html)

> 缓存的生命周期和细节

# save

[使 JPA 中的 repository.save() 不执行 select 语句的方法_jparepository的save delete方法 不执行sql-CSDN博客](https://blog.csdn.net/Souther_Feather/article/details/94839092#:~:text=%E8%A7%A3%E5%86%B3%E6%96%B9%E6%B3%95%E5%8F%AF%E4%BB%A5%E5%85%88%E5%B0%86,%E4%B8%8D%E6%8C%81%E4%B9%85%E5%8C%96%E5%8F%82%E7%85%A7%E5%AF%B9%E8%B1%A1%EF%BC%89%E3%80%82 )

[Spring Data JPA批量插入过慢及其优化 —— 自定义Repository_spring.jpa.properties.hibernate.jdbc-CSDN博客](https://blog.csdn.net/tfstone/article/details/113741890)

https://www.cnblogs.com/sunny3158/p/16353823.html | Spring Data JPA删除及批量删除功能 delete(list)和deleteInBatch(list) 是执行多条sql和条sql 的区别 - sunny123456 - 博客园

[Spring Data JPA 执行 INSERT 时跳过 SELECT](https://springdoc.cn/spring-data-jpa-skip-select-insert/)

# 报错

[【spring data jpa】使用spring data jpa 的删除操作，需要加注解@Modifying @Transactional 否则报错如下： No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call - Angel挤一挤 - 博客园](https://www.cnblogs.com/sxdcgaq8080/p/8984140.html)