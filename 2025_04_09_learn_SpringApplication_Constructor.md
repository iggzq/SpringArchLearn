# 1.SpringApplication‘s  Constructor ~SpringApplication.class~

## 1.1 SpringApplication application = new SpringApplication(Class<?>... primarySources);~SpringApplication.java~

```java
public SpringApplication(Class<?>... primarySources) {
    this(null, primarySources);
}
```

### ->

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
    // resourceLoader为null
    this.resourceLoader = resourceLoader;
    Assert.notNull(primarySources, "'primarySources' must not be null");
    // primarySources中为SpringBoot项目的启动类
    this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
    // 详情请跳转到*1.2*，WebApplicationType.deduceFromClasspath()通过检测特定类是否存在，来判断SpringBoot项目的类型
    this.properties.setWebApplicationType(WebApplicationType.deduceFromClasspath());
    // 详情跳转到*1.3*，getSpringFactoriesInstances(Class<T> type) SpringBoot的关键类，方法内部会加载项目中所有的spring.factories文件，并会以Map集合存储，key为接口或抽象类，value为具体实现类，并根据传入的class类参数通过‘反射’调用类的constructor方法对类进行实例化
    // 此处getSpringFactoriesInstances(BootstrapRegistryInitializer.class));就是对BootstrapRegistryInitializer的具体实现类进行实例化，并返回以实例化的对象到this.bootstrapRegistryInitializers集合中
    this.bootstrapRegistryInitializers = new ArrayList<>(
            getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
    // 原理同上，但这一步有了缓存，无需再次扫描加载所有spring.factories文件，直接从cache中获取，都是调用了getSpringFactoriesInstances(Class<T> type)方法，获得具体实现类初始化，返回结果
    setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
    // 原理同上
    setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    // 获取当前线程的栈中获取主程序入口
    this.mainApplicationClass = deduceMainApplicationClass();
}
```

## 1.2 WebApplicationType.deduceFromClasspath()~WebApplicationType.java~

```java
private static final String[] SERVLET_INDICATOR_CLASSES = { "jkarta.servlet.Servlet",
        "org.springframework.web.context.ConfigurableWebApplicationContext" };

private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";

private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

static WebApplicationType deduceFromClasspath() {
    /** ClassUtils.isPresent(String className, @Nullable ClassLoader classLoader),ClassUtil内部调用forName方法来判断参数className的类是否存在，
    其forName(String name, @Nullable ClassLoader classLoader),是Class.forName(String name, boolean initialize,ClassLoader loader)
    反射的加强版，其内部支持基本数据类型，如：（int,boolean），引用类型（Character，Integer）和数组类型（int[],String[])）
    常规的class类，依然使用jdk的Class.forName()获取类对象并返回。
    **/
    if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
            && !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
        return WebApplicationType.REACTIVE;
    }
    for (String className : SERVLET_INDICATOR_CLASSES) {
        if (!ClassUtils.isPresent(className, null)) {
            return WebApplicationType.NONE;
        }
    }
    return WebApplicationType.SERVLET;
}
```

## 1.3 getSpringFactoriesInstances(Class<T> type) ~SpringApplication.java~

```java
private <T> List<T> getSpringFactoriesInstances(Class<T> type, ArgumentResolver argumentResolver) {
    return SpringFactoriesLoader.forDefaultResourceLocation(getClassLoader()).load(type, argumentResolver);
}
```

### 1.3.1 SpringFactoriesLoader.forDefaultResourceLocation(@Nullable ClassLoader classLoader) ~SpringFactoriesLoader.java~

#### 1.3.1.1

```java
/**
* The location to look for factories.
* <p>Can be present in multiple JAR files.
*/
public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

public static SpringFactoriesLoader forDefaultResourceLocation(@Nullable ClassLoader classLoader) {
    // 这里有约定大于配置的具体体现，即文件地址和名称固定为META-INF/spring.factories
    return forResourceLocation(FACTORIES_RESOURCE_LOCATION, classLoader);
}
```

#### 1.3.1.2

```java
static final Map<ClassLoader, Map<String, SpringFactoriesLoader>> cache = new ConcurrentReferenceHashMap<>();

public static SpringFactoriesLoader forResourceLocation(String resourceLocation, @Nullable ClassLoader classLoader) {
    Assert.hasText(resourceLocation, "'resourceLocation' must not be empty");
    ClassLoader resourceClassLoader = (classLoader != null ? classLoader :
            SpringFactoriesLoader.class.getClassLoader());
    // cache为Map<ClassLoader, Map<String, SpringFactoriesLoader>>,当cache集合的键没有此处的resourceClassLoader时
    // 新创建一个ConcurrentReferenceHashMap<>,并保存在cache中，key为resourceClassLoader，value为新创建的map对象
    // 这里的loaders即为cache的value,这种情况下为新建的ConcurrentReferenceHashMap对象，内部为empty
    Map<String, SpringFactoriesLoader> loaders = cache.computeIfAbsent(
            resourceClassLoader, key -> new ConcurrentReferenceHashMap<>());
    // 此处目的是将resourceLocation(此处为META-INF/spring.factories)与SpringFactoriesLoader装入到loaders集合中
    // 注意：此处的loaders为cache中的，这一步也将结果报错到了cache中，下一次访问直接读的是cache缓存
    return loaders.computeIfAbsent(resourceLocation, key ->
            new SpringFactoriesLoader(classLoader, loadFactoriesResource(resourceClassLoader, resourceLocation)));
}
```

#### 1.3.1.3

```java
protected static Map<String, List<String>> loadFactoriesResource(ClassLoader classLoader, String resourceLocation) {
	Map<String, List<String>> result = new LinkedHashMap<>();
	try {
        // classLoader.getResources(resourceLocation)为获取项目中所有的spring.factories文件的地址url
		Enumeration<URL> urls = classLoader.getResources(resourceLocation);
        // 遍历每个url地址
		while (urls.hasMoreElements()) {
			UrlResource resource = new UrlResource(urls.nextElement());
            //Properties里有ConcurrentHashMap<Object, Object> map;对象，此处为根据之前获取的url读取spring.factories文件里的内容，并保存在properties对象里的map集合里
            //map集合里key为接口和抽象类，value为具体实现类集合
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            //遍历properties里的map集合，将里面的内容保存到result中
			properties.forEach((name, value) -> {
				String[] factoryImplementationNames = StringUtils.commaDelimitedListToStringArray((String) value);
				List<String> implementations = result.computeIfAbsent(((String) name).trim(),
						key -> new ArrayList<>(factoryImplementationNames.length));
                // 将factoryImplementationNames中的值保存到result的value集合中
				Arrays.stream(factoryImplementationNames).map(String::trim).forEach(implementations::add);
			});
		}
        // 确保每个键对应的实现类列表都是唯一的
		result.replaceAll(SpringFactoriesLoader::toDistinctUnmodifiableList);
	}
	catch (IOException ex) {
		throw new IllegalArgumentException("Unable to load factories from location [" + resourceLocation + "]", ex);
	}
    // 返回集合，并保证集合的不可修改
	return Collections.unmodifiableMap(result);
}
```

### 1.3.2 SpringFactoriesLoader.load(Class<T> factoryType, @Nullable ArgumentResolver argumentResolver)

```java
public <T> List<T> load(Class<T> factoryType, @Nullable ArgumentResolver argumentResolver,
		@Nullable FailureHandler failureHandler) {

	Assert.notNull(factoryType, "'factoryType' must not be null");
    // 根据1.3.1.3中的map集合获取实现类url集合
	List<String> implementationNames = loadFactoryNames(factoryType);
	logger.trace(LogMessage.format("Loaded [%s] names: %s", factoryType.getName(), implementationNames));
	List<T> result = new ArrayList<>(implementationNames.size());
	FailureHandler failureHandlerToUse = (failureHandler != null) ? failureHandler : THROWING_FAILURE_HANDLER;
	// 遍历实现类的url
    for (String implementationName : implementationNames) {
        // 详情见1.3.2.1 实例化实现类
		T factory = instantiateFactory(implementationName, factoryType, argumentResolver, failureHandlerToUse);
		if (factory != null) {
            // 将实例化好的对象保存到result集合中
			result.add(factory);
		}
	}
	AnnotationAwareOrderComparator.sort(result);
    // 返回result集合，此时getSpringFactoriesInstances(BootstrapRegistryInitializer.class)执行完毕
    // 回到SpringApplication.java中的代码中
	return result;
}
```

#### 1.3.2.1

```java
@Nullable
protected <T> T instantiateFactory(String implementationName, Class<T> type,
		@Nullable ArgumentResolver argumentResolver, FailureHandler failureHandler) {

	try {
        // 获取实现类的代理类
		Class<?> factoryImplementationClass = ClassUtils.forName(implementationName, this.classLoader);
		Assert.isTrue(type.isAssignableFrom(factoryImplementationClass), () ->
				"Class [%s] is not assignable to factory type [%s]".formatted(implementationName, type.getName()));
        // FactoryInstantiator.forClass(factoryImplementationClass)获取构造器方法对象，详情见1.3.2.2
		FactoryInstantiator<T> factoryInstantiator = FactoryInstantiator.forClass(factoryImplementationClass);
        // 实例化对象并返回，详情见1.3.2.3
		return factoryInstantiator.instantiate(argumentResolver);
	}
	catch (Throwable ex) {
		failureHandler.handleFailure(type, implementationName, ex);
		return null;
	}
}
```

#### 1.3.2.2

```java
static <T> FactoryInstantiator<T> forClass(Class<?> factoryImplementationClass) {
    // 获取实现类的构造方法
	Constructor<?> constructor = findConstructor(factoryImplementationClass);
	Assert.state(constructor != null, () ->
			"Class [%s] has no suitable constructor".formatted(factoryImplementationClass.getName()));
    // 返回实现类的构造方法
	return new FactoryInstantiator<>((Constructor<T>) constructor);
}
```

#### 1.3.2.3

```java
T instantiate(@Nullable ArgumentResolver argumentResolver) throws Exception {
	Object[] args = resolveArgs(argumentResolver);
	if (isKotlinType(this.constructor.getDeclaringClass())) {
		return KotlinDelegate.instantiate(this.constructor, args);
	}
    // 利用1.3.2.2获取的类的构造方法实例化对象，并返回
	return this.constructor.newInstance(args);
}
```
