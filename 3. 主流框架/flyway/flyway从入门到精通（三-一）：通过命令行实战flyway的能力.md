这小章，通过简单的命令行的执行flyway的migrate命令来进一步的了解flyway的执行原理，
首先在flyway官网下载flyway，我下载的是flyway-6.4.1版本
进入到flyway-6.4.1目录

```css
cd flyway-6.4.1
```

找到文件/conf/flyway.conf，对flyway进行配置，如下

```undefined
flyway.url=jdbc:h2:file:./foobardb
flyway.user=SA
flyway.password=
```

在/sql目录下，创建V1__Create_person_table.sql，内容如下

```csharp
create table PERSON (
    ID int not null,
    NAME varchar(100) not null
);
```

执行flyway的migrate命令如下

```css
flyway-6.4.1> flyway migrate
```

如果一切ok的话，会得到如下的输出结果

```php
Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 1 migration (execution time 00:00.008s)
Creating Schema History table: "PUBLIC"."flyway_schema_history"
Current version of schema "PUBLIC": << Empty Schema >>
Migrating schema "PUBLIC" to version 1 - Create person table
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.033s)
```

在/sql文件夹下继续的添加文件V2__Add_people.sql
内容如下

```csharp
insert into PERSON (ID, NAME) values (1, 'Axel');
insert into PERSON (ID, NAME) values (2, 'Mr. Foo');
insert into PERSON (ID, NAME) values (3, 'Ms. Bar');
```

继续的执行migrate命令如下

```css
flyway-6.4.1> flyway migrate
```

如果一切ok，会得到如下的输出结果

```bash
Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 2 migrations (execution time 00:00.018s)
Current version of schema "PUBLIC": 1
Migrating schema "PUBLIC" to version 2 - Add people
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.016s)
```

通过上面的例子，我们可以看到flyway提供了丰富的自配置信息，可以让我们根据自己的需要进行设置，已满足各种不同的需求，下一小章会对命令行工具的某些功能作进一步的介绍

flyway从入门到精通（三-二）：通过命令行实战flyway的能力