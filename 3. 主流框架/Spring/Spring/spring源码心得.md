itx的这三个项目尤其重要，硬盘没了

![1665140735956](img/1665140735956.png)

## **AnnotationConfigApplicationContext**

[Spring源码系列——容器的启动过程(一)](https://juejin.cn/post/6863759097737838606#comment)

[浪漫先生](https://juejin.cn/user/2383396941350621)

### Reader

```java
//相信熟悉Spring的人一定都知道或用过@ConditionalOnBean / @ConditionalOnClass 等条件注解.而这些条件注解的解析就是ConditionEvaluator.
public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
    Assert.notNull(environment, "Environment must not be null");
    // 设置registry，已经知道它的就是容器本身:AnnotationConfigApplicationContext
    this.registry = registry;
    // 创建条件处理器
    this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
    // 非常关键！ 提前往容器中注册一些必要的后置处理器
    AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
}
```

![img](img/8400f76f427049d5b6ffce9a3f2476c0_tplv-k3u1fbpfcp-zoom-in-crop-mark_3024_0_0_0.awebp)

```java
//因为在Spring当中, 所有的Bean都是通过该方法注入到容器当中的!源码如下
private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
        @Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
        @Nullable BeanDefinitionCustomizer[] customizers) {

    // 根据类生成一个beanDefinition, 具体类型是AnnotatedGenericBeanDefinition
    // 在当前场景中，beanClass就是传入的Config.class
    AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
    // 根据之前reader当中的条件解析器来判断当前的配置类当中是否有条件相关的注解，如果有，则进一步判断是否需要暂时跳过注册。
    // 还记得上文当中Scanner初始化过程中的条件解析器不? 它就是在这里起作用的!
    // 在当前场景中，由于Config类并没有配置任何conditional，因此这里不需要跳过注册
    if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
        return;
    }

    // 设置Supplier函数
    // 在当前场景中，supplier为null
    abd.setInstanceSupplier(supplier);
    
    // 解析bd的ScopeMetadata。在reader初始化时，scopeMetadataResolver就默认初始化为AnnotationScopeMetadataResolver类型了
    // 这里主要是解析类上是否有@Scope注解，如果有，则解析:scopeName和proxyNode
    // scopeName（作用域范围：单例or原型？）
    // proxyNode（代理模式：JDK or Cglib?）
    // @Scope也是非常重要的一个点!! 但在这里不展开讲解，将单独章节进行讲解
    // 在当前场景中，Config没有@Scope注解，因此这里的config将默认为单例，且不采取代理技术。
    ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
    
    // 设置beanDefinition的作用域
    // 当前场景中为singleton
    abd.setScope(scopeMetadata.getScopeName());
    
    // 生成bean的名字
    // 在reader初始化时，默认的beanName生成器为AnnotationBeanNameGenerator。
    // 如果有需要的话，我们自己也可以继承BeanNameGenerator来自定义beanName生成器。一般情况下，用默认的就可以了。
    // 默认的beanName生成策略就是类名首字母小写。
    // 在当前场景中，Config类的beanName就为：config
    String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

    // 重要！！ 处理公共的注解，比如@Lazy、@Order、@Priority、@DependsOn。
    // 这些注解的作用很简单，这里不展开细说。
    // 在当前场景下，Config类没有这些注解。
    AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

    // 如果有其他限定注解，则进行设置
    // 当前场景中, Config类显然是没有的
    if (qualifiers != null) {
        for (Class<? extends Annotation> qualifier : qualifiers) {
            if (Primary.class == qualifier) {
                abd.setPrimary(true);
            }
            else if (Lazy.class == qualifier) {
                abd.setLazyInit(true);
            }
            else {
                abd.addQualifier(new AutowireCandidateQualifier(qualifier));
            }
        }
    }

    // BeanDefinitionCustomizer的作用就是回调处理beanDefinition
    // 当前场景中,不需要回调处理
    if (customizers != null) {
        for (BeanDefinitionCustomizer customizer : customizers) {
            customizer.customize(abd);
        }
    }

    // 将beanDefinition和beanName封装成bdh
    BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
    
    // 重要！！！ 这里将根据scopeMetadata来判断beanDefinition是否需要进行代理。如果需要，则生成代理类的beanDefinition并赋值给bdh！
    // 本场景中，不需要进行代理，因此bdh没有改变。
    definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
    
    // 注册bdh所代表的的beanDefinition
    // 本场景中,就是注册Config类所代表的的bd. 注册成功后,工厂中就包含了7个bd了(别忘了前面注册的6个后置处理器的bd)
    BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
}
```

### Scanner

主要的目的就是扫描类路径下所有的class文件能否解析为bd。

Scanner在扫描的过程中,会使用过滤策略,并且使用了默认的过滤策略.

```java
protected void registerDefaultFilters() {
    // 扫描@Component注解的类
    this.includeFilters.add(new AnnotationTypeFilter(Component.class));
    ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
    try {
...
    //@ManageBean和@Named的作用和@Component是一样的。只是我们通常习惯使用@Component。
```

## ApplicationContext

### refresh

```java
 // 设置beanFactory当中的表达式语言解析器. 比如@Value中的表达式就是这里设置的解析器来解析的.
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
```

## DefaultListableBeanFactory 



### doGetBeanNamesForType()

[Spring源码系列——FactoryBean源码解析](https://juejin.cn/post/6856337888687489038#heading-11)

在ApplicationContext中getBea方法有很多重载，但其实最终都是解析出beanName，根据beanName来找到具体的bean。原因在于：在ApplicationContext中，beanDefinition、beanType、IOC容器等都是基于beanName来做映射关系的。因此我们在阅读源码时，可以发现很多时候都是首先解析出beanName再做进一步操作。又因为beanName的定义方式多种多样，因此解析过程也比较复杂，我们在解析源码时一定要静下心来。由于getBean方法本身涉及到bean的生命周期、循环依赖等复杂场景，所以将getBean方法单独拿出来进行解析。本篇文章我们只需要知道getBean方法能够获取到实际的bean即可。

```java
AbstractApplicationContext#getBean(java.lang.Class)
DefaultListableBeanFactory#resolveBean
DefaultListableBeanFactory#resolveNamedBean
```



```java
private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<>();

		// 检查所有的beanDefinitionNames.beanDefinition在执行 new AnnotationConfigApplicationContext(Config.class)时就以及解析完毕了。因此这里可以直接遍历循环。
		for (String beanName : this.beanDefinitionNames) {
			// 判断是否有别名。一般情况下，我们很少有使用别名的情况。在本例中也一样无别名，因此进入if分支
			if (!isAlias(beanName)) {
				try {
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
					// Only check bean definition if it is complete.
					if (!mbd.isAbstract() && (allowEagerInit ||
							(mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()) &&
									!requiresEagerInitForType(mbd.getFactoryBeanName()))) {
                        // !!! 重要！这里将根据beanName和beanDefinition来判断当前type是否FactoryBean类型。这个参数是区分普通bean和factoryBean的核心。
                        // 请看下面的第7小节的isFactoryBean()方法解析
						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
                        // !!! 重要！是否类型匹配，默认为false.
						boolean matchFound = false;
                        // 是否允许FactoryBean初始化，默认情况下是允许的
						boolean allowFactoryBeanInit = allowEagerInit || containsSingleton(beanName);
						boolean isNonLazyDecorated = dbd != null && !mbd.isLazyInit();
						// 接下来，主要是设置matchFound参数。
                        // 如果不是factoryBean类型，则直接调用isTypeMatch方法来设置matchFound参数，普通bean都是走的这个逻辑。
						if (!isFactoryBean) {
							if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
                        // 如果是factoryBean，进入下面这个else分支
						else  {
						// 首先根据普通的beanName来尝试设置matchFound，如果为true，说明找的是FactoryBean中产生的beanName。
							if (includeNonSingletons || isNonLazyDecorated ||
									(allowFactoryBeanInit && isSingleton(beanName, mbd, dbd))) {
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
                            // ！！！重要！ 如果matchFound依然为false，则说明很有可能是找FactoryBean本身，那么对beanName添加前缀：&，来尝试寻找FactoryBean本身。
							if (!matchFound) {
								// In case of FactoryBean, try to match FactoryBean instance itself next.
								beanName = FACTORY_BEAN_PREFIX + beanName;
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
                        // 如果matchFound为true，则将beanName添加到result中。注意，如果我们找的是FactoryBean类型的话，则beanName已经被添加&前缀了。
						if (matchFound) {
							result.add(beanName);
						}
					}
				}
				catch (CannotLoadBeanClassException | BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably a placeholder: let's ignore it for type matching purposes.
					LogMessage message = (ex instanceof CannotLoadBeanClassException) ?
							LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) :
							LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName);
					logger.trace(message, ex);
					onSuppressedException(ex);
				}
			}
		}
        // ...省略其他代码...
        // 返回找到的beanName结果
		return StringUtils.toStringArray(result);
	}
```

```
isFactoryBean()
```

## ConfigurationClassPostProcessor



```java
AnnotationConfigApplicationContext 会引入 ConfigurationClassPostProcessor
//在这里面便提现了ConfigurationClassPostProcessor的价值!!! 它是通过一个parser对象解析@Configuration注解的类的.
        // 本例中, 就是解析Config.class类,而在Config.class上有@ComponentScan注解,因此会通过scanner扫描该包下的所有@Component注解的类并解析成BeanDefinition添加到BeanFactory的beanDefinitionMap中.
        // 到这里,便知道,Spring就是在这里完成了@Configuration类的解析和bean的初步扫描!
```

[Spring源码系列——ConfigurationClassPostProcessor源码解析](https://juejin.cn/post/6871928510518722573#heading-6)重要的

[Spring5源码解析7-ConfigurationClassPostProcessor (下)](https://segmentfault.com/a/1190000020633405)

[spring源码（五）ConfigurationClassPostProcessor](https://juejin.cn/post/7017699419541733413)

**对"full"配置类进行增强?**

> 提前进行类增强，cglib可以使用super方法，之后再进行getBean之类的。类和对象属于一体。
>
> 而事务之类的切面编程属于getBean之后的，此时属性已经注入完毕，只能用增强类包装原来的对象。

# aop

```xml
	<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-aop</artifactId>
		</dependency>
```



## ProxyFactory

`ProxyFactory`表面上是一个工厂类，更像是一个状态持有类，内置了方法获取最终proxy（通过抽象工厂方法）获取aopproxy工厂，产生aopproxy，aopproxy也是一个持有状态的工厂类，因此也只能用一次，每次都是新的对象。aopproxy直接产生proxy，献祭了自己作为状态持有者。更简单的实现是 `JdkDynamicAopProxy implements AopProxy, InvocationHandler`

@transaction倾向于使用cglib，因为不一定只使用接口

## schema aop

使用`AspectJAwareAdvisorAutoProxyCreator`

用parser把xml中的pointcut和advisor解析成bean，加入一个exposed...，然后逻辑和 `DefaultAdvisorAutoProxyCreator`一样。

## 注解@aspectJ

使用`AnnotationAwareAspectJAutoProxyCreator`

会先做完schema的工作，但是会从aspect中补充advisor。

aspect是bean，然后`AnnotationAwareAspectJAutoProxyCreator`解析aspect并收纳起来。

```java
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules.
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}
```

# 声明式事务管理

底层都是切面aop，生成一个advisor，自动代理每个可能的bean，然后通过pointcut过滤，生成代理，TransactionInterceptor拦截。下面两种方案中，advisor所用的类不大一样，最大的不同是pointcut。

声明式事务的特点是`TransactionInterceptor`内部有一个source可以判断是否开启事务，算是二次拦截。

## tx

```java
//默认使用的织入器是 `AspectJAwareAdvisorAutoProxyCreator`，有其他情况会升级。比如<!-- 配置spring开启注解AOP的支持 --> <aop:aspectj-autoproxy/>。但要注意xml解析顺序，有时候直接就是更高级的织入器
```

基于schema aop的 自动代理，只是advice换成了tx-advice（对应一个`TransactionInterceptor`的bean）。`TransactionInterceptor`	内部再进行通过`TransactionAttributeSource`进行一层过滤（是否开启事务）使用的是`NameMatchTransactionAttributeSource`

> 用的是下面的advisor，并且基于aop的pointcut，method过滤程度依赖aop配置。
>
> ![1666884817986](img/1666884817986.png)

## 注解

类似于@annotation类型的pointcut。过滤精确，程度高。

pointcut和source合一

> ```xml
> <!-- 开启spring对注解事务的支持-->
> <tx:annotation-driven/>
> ```

```java
//默认使用的织入器是 `InfrastructureAdvisorAutoProxyCreator`，有其他情况会升级。比如<!-- 配置spring开启注解AOP的支持 --> <aop:aspectj-autoproxy/>。但要注意xml解析顺序，有时候直接就是更高级的织入器
//同时加入下面三个bean，通过parser解析xml加入
org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
//也依赖source
org.springframework.transaction.interceptor.TransactionInterceptor
//pointcut method的配对代理给下面的类，会做缓存
org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
```

> 完全注解
>
> 和上面一样
>
> ```java
> @EnableTransactionManagement
> 上面三个bean由该类ProxyTransactionManagementConfiguration注入
> ```

# 获取方法参数名

[在获取方法参数名方面，Spring真的就比Mybatis强？](https://blog.csdn.net/liujianyangbj/article/details/114366364)

SM解析字节码方式的前置条件相对比较宽松，只需要编译的时候添加 -g参数就行，缺点就是依赖于ASM框架

Java 要获取接口或者抽象方法的参数的名称，必须的是JDK8以上，而且编译的时候加上-parameters参数，只有这种情况下编译出来的.class才能获取到参数的名称。无论你是通过java反射还是asm字节码技术，前面两个条件必须同时满足。

所以当达到上述两个条件后，直接通过java的反射就可以直接获取参数名称，根本没必要通过asm技术去获取。
所以！Mybatis不是拿不到参数名称，而是必须要 jdk8 以上而且还得是-parameters编译才可以，当满足这两个条件的时候，你也可以不加@Param(’’)注解。

`Java`在编译的时候对于方法，**默认是**`**不会**`**保留方法参数名**，因此如果我们在运行期想从`.class`字节码里直接拿到方法的参数名是做不到的。

获取到的是无意义的`arg0、arg1`

`Java8`新推出的`-parameters`

# environment

五个属性，都是final。不同的子类只是初始化的时候加入不同的propertiysource 

继承了`PropertyResolver`，真正的resolve代理给内部的`PropertySourcesPropertyResolver`

```java
	protected final Log logger = LogFactory.getLog(getClass());

	private final Set<String> activeProfiles = new LinkedHashSet<>();

	private final Set<String> defaultProfiles = new LinkedHashSet<>(getReservedDefaultProfiles());
//拿来装PropertySource
	private final MutablePropertySources propertySources = new MutablePropertySources();
//真正的解析者
	private final ConfigurablePropertyResolver propertyResolver =
			new PropertySourcesPropertyResolver(this.propertySources);
```

`PropertySourcesPropertyResolver`迭代内部的PropertySource来查找，查找后进行深度解析，最后进行转化。

```java
	@Nullable
	protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				if (logger.isTraceEnabled()) {
					logger.trace("Searching for key '" + key + "' in PropertySource '" +
							propertySource.getName() + "'");
				}
				Object value = propertySource.getProperty(key);
				if (value != null) {
					if (resolveNestedPlaceholders && value instanceof String) {
						value = resolveNestedPlaceholders((String) value);
					}
					logKeyFound(key, propertySource, value);
					return convertValueIfNecessary(value, targetValueType);
				}
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Could not find key '" + key + "' in any property source");
		}
		return null;
	}
```

`resolveNestedPlaceholders()`深度解析，最终调用内部的 `org.springframework.util.PropertyPlaceholderHelper#parseStringValue`，这里会迭代解析placeholder，利用`getPropertyAsRawString`（这里重新回到`PropertySourcesPropertyResolver`中）和set来防止死循环。

`PropertyPlaceholderHelper`是个工具方法，用于递归解释placeholder（占位符），很多地方都用到。配合`PlaceholderResolver`

```java
/**
	 * Replaces all placeholders of format {@code ${name}} with the value returned
	 * from the supplied {@link PlaceholderResolver}.
	 * @param value the value containing the placeholders to be replaced
	 * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
		return parseStringValue(value, placeholderResolver, null);
	}

	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, @Nullable Set<String> visitedPlaceholders) {

		int startIndex = value.indexOf(this.placeholderPrefix);
		if (startIndex == -1) {
			return value;
		}
        ...
```



> **PropertySource**

不同的PropertySource都会有各自的property，和各自的get的方法。大部分比较简单，例如就是一个map.get().

而在springboot中，有一个方法，org.springframework.boot.context.properties.source.ConfigurationPropertySources#attach，加入一个特殊的`ConfigurationPropertySourcesPropertySource`，用于接入spring-boot的`Source`体系

```java
	sources.addFirst(new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME,
					new SpringConfigurationPropertySources(sources)));

	private ConfigurationProperty findConfigurationProperty(ConfigurationPropertyName name) {
		if (name == null) {
			return null;
		}
		for (ConfigurationPropertySource configurationPropertySource : getSource()) {
			ConfigurationProperty configurationProperty = configurationPropertySource.getConfigurationProperty(name);
			if (configurationProperty != null) {
				return configurationProperty;
			}
		}
		return null;
	}
```

> ConfigurationPropertySourcesPropertySource，可以看成内部有很多`ConfigurationPropertySource`（`ConfigurationProperty`（就是单个key-value）的容器，spring-boot新增的source表示体系，接口结构类似一个map）。应该是多了一些alias或者mapper的功能。
>
> 不断调用SpringConfigurationPropertySources（就是一个`Iterable<ConfigurationPropertySource>`工具类）的方法，逐个拿出之前放进的**PropertySource**，然后包装成`ConfigurationPropertySource`返回，这时，`ConfigurationPropertySource`就等于**PropertySource**，只不过可能增加一些功能。getvalue最终还是从传入的**PropertySource**中获取。

如果拿不到，是不是会重复拿不到的两次操作，毕竟`ConfigurationPropertySourcesPropertySource`就包含了所有的sources，并迭代了一遍了？



# listener

spring的体系使用 `ApplicationListener`和 `SimpleApplicationEventMulticaster`和`applicationContext`

> `SimpleApplicationEventMulticaster`有缓存机制。内部有缓存和事件对应的listener机制。
>
> ```java
> final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);
> 
> org.springframework.context.event.AbstractApplicationEventMulticaster#getApplicationListeners(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)
> ```
>
> 第一次获取匹配的listeners和beanname对应的listener，如果是单例那么缓存listener，否则缓存bean name。之后排序返回。如果没有缓存bean name，那么排序的结果可以缓存起来。
>
> 第二次获取，如果没有缓存bean name，那么直接返回，都是单例，否则要生成然后排列返回。preFiltered都是true。

Multicaster需要由外部初始化，spring设置预先存入的listeners，还有bean name（因为要拖延初始化），然后发布预先存入的事件。

```java
protected void registerListeners() {
   // Register statically specified listeners first.
   for (ApplicationListener<?> listener : getApplicationListeners()) {
      getApplicationEventMulticaster().addApplicationListener(listener);
   }

   // Do not initialize FactoryBeans here: We need to leave all regular beans
   // uninitialized to let post-processors apply to them!
   String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
   for (String listenerBeanName : listenerBeanNames) {
      getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
   }

   // Publish early application events now that we finally have a multicaster...
   Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
   this.earlyApplicationEvents = null;
   if (!CollectionUtils.isEmpty(earlyEventsToProcess)) {
      for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
         getApplicationEventMulticaster().multicastEvent(earlyEvent);
      }
   }
}
```

# refresh过程

简单来说，初始化容器的几个单点，处理两种postprocesser，listener体系，然后开始getBean。

```java
@Override
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context.
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```

1. `prepareRefresh();`

   清理scanner缓存，设置容器状态，启动时间，调用 `initPropertySources()`，看看environment 的RequiredProperties是否都存在。

   用`earlyApplicationListeners`保存预先设置的`applicationListeners`，多次refresh用得上，初始化`earlyApplicationEvents`

2. 设置ConfigurableListableBeanFactory的id，工具组件，BeanExpressionResolver，ClassLoader，PropertyEditorRegistrar（getBean的时候才用得上）。设置不需要注入依赖的几个接口类型，添加ApplicationContextAwareProcessor用于设置处理bean的aware回调。设置某些类型的注入是什么，注入某些bean。

3. `postProcessBeanFactory(beanFactory);`

   调用子类的处理方法，比如servletweb容器就是添加aware和scope（request,session）

4. `invokeBeanFactoryPostProcessors(beanFactory);`

   BeanDefinitionRegistryPostProcessor：`A`

   BeanFactoryPostProcessor：`B`

   最重要的方法，启动容器的后处理方法，主要是处理容器的bean定义，增加BeanPostProcessor

   1. 先处理`A`，spring-boot有几个新类，但一般只有`ConfigurationClassPostProcessor`最重要
      1. 容器自身手动设置的beanFactoryPostProcessors实例中，如果有`A`，那么先按照`A`调用方法，其余搁置。
      2. 容器中没初始化的`A`，先初始化并调用`PriorityOrdered`，然后`Ordered`，最后初始化并调用剩下的，如果过程中产生新的，那么循环最后一步，直到所有的`A`都被调用一遍。
      3. 由于`A`都是`B`的子类，因此先调用所有的`A`的`B`方法，然后调用搁置的手动设置的`B`。

   2. 处理`B`，一样按照`PriorityOrdered`，`Ordered`，`nonOrdered`的顺序处理，只是最后没有循环，因为不应该在这个接口中注入新bean定义。注意要剔除`A`，已经被执行过了。
   3. 清除缓存。

5. `registerBeanPostProcessors(beanFactory);`

   注册BeanPostProcessor，一样按照`PriorityOrdered`，`Ordered`，`nonOrdered`的顺序处理，MergedBeanDefinitionPostProcessor类型的需要排列到最后。ApplicationListenerDetector，BeanPostProcessorChecker。（5是因为我加了aop-starter起步依赖）

   ![image-20230507151527976](img/image-20230507151527976.png)

6. 国际化相关初始化，不重要，用不上

7. 事件multicast创建

8. 初始化其他特殊bean，`ServletWebServerApplicationContext`这时会启动tomcat

   ```java
   // Initialize other special beans in specific context subclasses.
   onRefresh();
   ```

9. 事件multicast注册容器预先设置的listener和容器中的listener bean name，发布early事件，一般是没有的

   ```java
   				// Check for listener beans and register them.
   				registerListeners();
   ```

   ![image-20230507152501260](img/image-20230507152501260.png)

10. 补充一些bean，**初始化所有单例bean**。之后，前面的这些bean逐个检查，如果是`SmartInitializingSingleton`，那么触发方法。

11. finishRefresh()

    1. clearResourceCaches

    2. 触发LifecycleProcessor的生命周期方法，只是一个代理类，代理其他Lifecycle，一般是没有的，如果引入了web启动依赖就有2个

       ![image-20230507155312162](img/image-20230507155312162.png)

       1. 获取所有的Lifecycle bean，包括SmartLifecycle
       2. 根据phase分类到LifecycleGroup中，排序，按顺序触发start()，web是初始化tomcat。

    3. 发布ContextRefreshedEvent事件，主要是清除缓存，打印日志。

       ![image-20230507155853428](img/image-20230507155853428.png)

    4. 注册mbean

12. 清理缓存finally，容器启动完毕，失败了另算。


