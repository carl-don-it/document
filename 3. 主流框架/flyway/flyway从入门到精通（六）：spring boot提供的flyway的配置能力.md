# flyway从入门到精通（六）：spring boot提供的flyway的配置能力

![img](img/77e1f013-eb83-4c16-8fcf-343157622128-1720922627802.webp)

[牧羊人刘俏](https://www.jianshu.com/u/ea6255a1cdf0)关注IP属地: 黑龙江

0.0992020.05.04 15:33:38字数 181阅读 1,735

spring boot对flyway提供了丰富的配置能力，我们只需要在yaml文件添加配置信息，如下

```bash
 spring:
    flyway:
        enabled: true
        schemas: lex
```

默认会下载到如下的配置类

```tsx
@ConfigurationProperties(prefix = "spring.flyway")
public class FlywayProperties {

    /**
     * 是否开启flyway，默认是开启的
     */
    private boolean enabled = true;

    /**
     * 是否检查 migration 脚本是否存在，默认是检查的.
     */
    private boolean checkLocation = true;

    /**
     * Locations of migrations scripts. Can contain the special "{vendor}" placeholder to
     * use vendor-specific locations.
         *如下，默认会在db/migration的classpath下查找migrations 文件
     */
    private List<String> locations = new ArrayList<>(Collections.singletonList("classpath:db/migration"));

    /**
     *  SQL migrations文件默认编码方式为UTF_8
     */
    private Charset encoding = StandardCharsets.UTF_8;

    /**
     * 尝试连接数据库的最大的次数
     */
    private int connectRetries;

    /**
     * 指定flyway执行的schemas，逗号隔离，区分大小写
     */
    private List<String> schemas = new ArrayList<>();

    /**
     * schema history table的名字，默认为flyway_schema_history
     */
    private String table = "flyway_schema_history";

    /**
     * Tablespace in which the schema history table is created. Ignored when using a
     * database that does not support tablespaces. Defaults to the default tablespace of
     * the connection used by Flyway.
     */
    private String tablespace;

    /**
     * 当对一个已存在的schema使用baseline能力时，控制台输出的描述性
     */
    private String baselineDescription = "<< Flyway Baseline >>";

    /**
     * 执行baseline，已存在的schema设置的版本信息，如果待添加的sql的版本信息设置的
         *大于此版本信息，那么就会执行migration，否则忽略
     */
    private String baselineVersion = "1";

    /**
     * Username recorded in the schema history table as having applied the migration.
     */
    private String installedBy;

    /**
     * Placeholders and their replacements to apply to sql migration scripts.
     */
    private Map<String, String> placeholders = new HashMap<>();

    /**
     * Prefix of placeholders in migration scripts.
     */
    private String placeholderPrefix = "${";

    /**
     * Suffix of placeholders in migration scripts.
     */
    private String placeholderSuffix = "}";

    /**
     * 是否进行占位符替换，默认是true
     */
    private boolean placeholderReplacement = true;

    /**
     * File name prefix for SQL migrations.
     */
    private String sqlMigrationPrefix = "V";

    /**
     *  SQL migrations文件后缀名，默认是.sql
     */
    private List<String> sqlMigrationSuffixes = new ArrayList<>(Collections.singleton(".sql"));

    /**
     * SQL migrations文件分隔符
     */
    private String sqlMigrationSeparator = "__";

    /**
     * File name prefix for repeatable SQL migrations.
     */
    private String repeatableSqlMigrationPrefix = "R";

    /**
     * Target version up to which migrations should be considered.
     */
    private String target;

    /**
     * JDBC url of the database to migrate. If not set, the primary configured data source
     * is used.
     */
    private String url;

    /**
     * Login user of the database to migrate.
     */
    private String user;

    /**
     * Login password of the database to migrate.
     */
    private String password;

    /**
     * SQL statements to execute to initialize a connection immediately after obtaining
     * it.
     */
    private List<String> initSqls = new ArrayList<>();

    /**
     * Whether to automatically call baseline when migrating a non-empty schema.
         *对一和非空的schema执行migration时是否执行baseline
     */
    private boolean baselineOnMigrate;

    /**
     * 是否关闭clean操作，个人建议设置为true，防止意外情况出现
     */
    private boolean cleanDisabled;

    /**
     * Whether to automatically call clean when a validation error occurs.
     */
    private boolean cleanOnValidationError;

    /**
    *是否将所有的migration sql放在同一个事务里面执行，要看db是否支持
     */
    private boolean group;

    /**
     * 是否忽略丢失的migretion文件，保持默认，为了文件的安全性，建议设置为false
     */
    private boolean ignoreMissingMigrations;

    /**
     * Whether to ignore ignored migrations when reading the schema history table.
     */
    private boolean ignoreIgnoredMigrations;

    /**
     * Whether to ignore pending migrations when reading the schema history table.
     */
    private boolean ignorePendingMigrations;

    /**
     * Whether to ignore future migrations when reading the schema history table.
     */
    private boolean ignoreFutureMigrations = true;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the
     * same migration.
     */
    private boolean mixed;

    /**
     * 建议设置为 false，按照order来执行
       */
    private boolean outOfOrder;

    /**
     * Whether to skip default callbacks. If true, only custom callbacks are used.
     */
    private boolean skipDefaultCallbacks;

    /**
     * Whether to skip default resolvers. If true, only custom resolvers are used.
     */
    private boolean skipDefaultResolvers;

    /**
     * Whether to automatically call validate when performing a migration.
     */
    private boolean validateOnMigrate = true;

    /**
     * Whether to batch SQL statements when executing them. Requires Flyway Pro or Flyway
     * Enterprise.
     */
    private Boolean batch;

    /**
     * File to which the SQL statements of a migration dry run should be output. Requires
     * Flyway Pro or Flyway Enterprise.
     */
    private File dryRunOutput;

    /**
     * Rules for the built-in error handling to override specific SQL states and error
     * codes. Requires Flyway Pro or Flyway Enterprise.
     */
    private String[] errorOverrides;

    /**
     * Licence key for Flyway Pro or Flyway Enterprise.
     */
    private String licenseKey;

    /**
     * Whether to enable support for Oracle SQL*Plus commands. Requires Flyway Pro or
     * Flyway Enterprise.
     */
    private Boolean oracleSqlplus;

    /**
     * Whether to issue a warning rather than an error when a not-yet-supported Oracle
     * SQL*Plus statement is encountered. Requires Flyway Pro or Flyway Enterprise.
     */
    private Boolean oracleSqlplusWarn;

    /**
     * 是否流化执行，需要flyway专业版本的支持
     */
    private Boolean stream;

    /**
     * File name prefix for undo SQL migrations. Requires Flyway Pro or Flyway Enterprise.
     */
    private String undoSqlMigrationPrefix;
}
```

从上可以看到，针对老的数据库，flyway也可以使用baseline的能力，对数据库做升级管理，用户可以针对各自的项目做到非常定制化的配置，如下

```kotlin
spring:
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/lexdemo?useUnicode=true&characterEncoding=UTF-8
    driver-class-name: com.mysql.jdbc.Driver

  flyway:
    enabled: true
    schemas: lex
    baseline-on-migrate: true
    baseline-version: 0
    clean-disabled: true
```

通过分析配置属性，我们可以大概看到flyway提供了callback和resolver两个扩展点，可以让我们定制化回调函数和解析器对sql进行个性化的解析。

flyway从入门到精通（七）：spring boot中flyway执行源码分析