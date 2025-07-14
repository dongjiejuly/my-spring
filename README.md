# 项目说明
## 描述
本项目是一个仿照Spring容器来仿写Spring功能的项目，项目中模拟的Spring容器创建容器的部分重要过程：   
1、.class文件的扫描；  
2、对象的实例化；  
3、依赖注入；  
4、模拟Spring的Aware通知机制；  
5、通过BeanPostProcessor接口实现初始化前操作；  
6、通过InitializingBean接口实现初始化；  
7、通过BeanPostProcessor接口实现初始化后操作。  
在bean对象被容器创建的过程中，区分了单例还是原型模式，当对象被成功创建后，又模拟了Spring容器提供对应的bean对象获取方法。

## 技术实现过程
### 扫描过程：   
(1)模拟创建 Spring 容器 MyApplicationContext，在容器启动时需要传入配置类   
(2)定义配置类 AppConfig，配置类有一个很重要的功能，就是告诉容器需要扫描的包路径   
(3)定义注解 @ComponentScan，并将注解配置到配置类 AppConfig上，这样在将配置类传给 Spring 容器 MyApplicationContext 的时候，就能够解析出包路径   
(4)利用配置上面的包路径，获取到对应的文件夹路径，然后扫描该文件夹下面所有的 .class 文件，在 Spring 中只有配置了类似于 @Component 的注解才生成为 bean 对象放入 Spring 的容器中   
(5)定义注解 @Component，并将 @Component 配置到 需要生成bean对象的类上  
(6)解析所有带有 @Component 的类对象，并将解析结果存入到 ConcurrentHashMap<String, BeanDefinition>(BeanDefinition是一个用于存放bean定义的相关信息的对象，使用beanDefinitionMap是为了在扫描阶段获取到所有对象的定义的基本信息，为后续真正生成bean对象做准备)
- 在解析带有 @Component 的类对象的时候，需要同时解析 @Scope注解，该注解告知了 bean 对象到底是单例还是多例，直接影响 bean 的创建时机(所以需要我们定义 @Scope 注解)
- 在解析带有 @Component 的类对象的时候，另外还需要判断被解析的类是否实现了 BeanPostProcessor 接口，如果实现该接口，需要将对应的实例加入 List<BeanPostProcessor> 的集合中，在对象初始化前后调用该接口中的两个方法  
### 创建bean的过程
(1)遍历 ConcurrentHashMap<String, BeanDefinition> 对象，判断对应的类是否为单例，如果为单例则往后继续执行  
(2)遍历的当前类通过构造器实例化对象(一般是默认无参构造器，实例化完成后，对象的属性值并没有被填充，需要后续进行填充，如果是有参构造器，那么参数部分的属性就会被填充)   
(3)获取当前当前类的所有属性值，判断这些属性是否需要完成依赖注入(有 @Autowired 注解，所以也需要自己定义 @Autowired 注解)，如果需要完成依赖注入，则进行依赖注入   
(4)判断对象是否有实现 BeanNameAware 接口，该接口是用于完成 Aware回调功能的，如果有则调用对象的 setBeanName()方法(该方法是接口中的方法)  
(5)判断 List<BeanPostProcessor> 集合中是否有对象，有则调用 postProcessBeforeInitialization(instance, beanName) 方法，完成初始化前操作  
(6)判断对象是否实现了 InitializingBean 接口，如果实现了该接口，则调用 afterPropertiesSet()方法  
(7)判断 List<BeanPostProcessor> 集合中是否有对象，有则调用 postProcessAfterInitialization(instance, beanName) 方法，完成初始化后操作

## Spring的底层原理

### bean的生命周期
UserService.class -> 无参构造器方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autowired的注解 2、赋值) -> bean对象

UserService.class -> 无参构造器方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autowired的注解 2、赋值) -> 放入map(单例池)中 -> bean对象 
当我们将创建的对象放入单例池之后，该对象就可以叫bean对象，后续获取bean对象就可以直接中单例池中获取，不需要再次进行创建

UserService.class -> 无参构造器方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autoware的注解 2、赋值) -> Aware回调机制 -> 初始化前 -> 初始化 -> 初始化后 -> 放入map(单例池)中 -> bean对象
有一种需求：我们的UserService类中有一种属性admin，必须需要通过配置文件、或者数据库查询(如管理员信息)，所以不能直接使用@Autowired注解(依赖注入并不知道查询哪个数据库哪个表，这些数据业务数据)，所以可以在依赖注入后，调用对象的指定
业务方法 xxxInitData()，在改方法中进行数据库查询，并将查询结果封装成对象赋值给普通对象的指定属性即可，这样整个对象放入单例池后，该bean对象的这个普通属性也就有值了。那么要让Spring知道要执行xxxInitData()该方法，需要再该方法上添加
注解 @PostConstruct,添加了该注解的方法会在spring初始化前执行

### 推断构造方法
1、如果只有一个有参的构造方法，则使用该构造方法完成实例化（先by type(可能存在多个)再by name(要么找到一个，要么找不到)）； 
说明：首先根据类型type去找，如果找到一个则结束，将该有参构造器的参数进行注入结束，完成实例化。如果根据type找到多个bean，这时在根据构造器参数的名字去找，找到一个则使用，完成实例化，找不到则报错，无法完成实例化。  
2、如果有多个构造方法，有无参构造方法则使用无参构造方法，如果没有无参构造方法，实例化阶段报错，因为它也不知道使用哪一个构造方法，它会去找无参的构造方法，找不到就报错； 
3、如果有多个构造方法，无论是否有无参构造方法，只要你在某一个构造方法上添加了@Autowired方法，则使用该构造方法完成实例化。如有你在多个构造方法上都添加了@Autowired方法，spring又不知道你要用哪一个构造方法完成实例化，那么又会报错。  
这就是spring的推断构造方法的底层逻辑，第一需要推断需要选择哪一个构造方法，第二选择构造方法后需要判断该构造方法是否有入参，如果有入参又要去寻找哪一个bean对象是需要的入参

### 依赖注入
1、先找到所有标注了@Autowired注解的属性
2、给这些标注了@Autowired注解的属性进行赋值（先byType再byName去找对应的bean给属性赋值）

### @PostConstruct

### 初始化前
UserService.class -> 无参构造器方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autoware的注解 2、赋值) -> Aware回调机制 -> 初始化前(@PostConstruct) -> 初始化 -> 初始化后 -> 放入map(单例池)中 -> bean对象

### 初始化
UserService.class -> 无参构造器方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autoware的注解 2、赋值) -> Aware回调机制 -> 初始化前(@PostConstruct) -> 初始化(afterPropertiesSet()) -> 初始化后 -> 放入map(单例池)中 -> bean对象
还是之前的那个需求：我们的UserService类中有一种属性admin，该属性不是固定的，必须需要通过配置文件、或者数据库查询(如管理员信息)，所以不能直接使用@Autowired注解(依赖注入并不知道查询哪个数据库哪个表，这些数据业务数据)
也可以让UserService实现InitializingBean接口，然后实现其中的afterPropertiesSet()方法，将对应的查询数据库逻辑等写到afterPropertiesSet()方法中
在Spring对象初始化的时候回来执行afterPropertiesSet()方法

### 初始化后
UserService.class -> 无参构造器方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autoware的注解 2、赋值) -> Aware回调机制 -> 初始化前(@PostConstruct) -> 初始化(afterPropertiesSet()) -> 初始化后(AOP) -> 代理对象 -> 放入map(单例池)中 -> bean对象

### AOP
UserService.class -> 推断构造方法 -> 对象(当前对象的属性并没有值) -> 依赖注入(1、找到添加了@Autoware的注解 2、赋值) -> Aware回调机制 -> 初始化前(@PostConstruct) -> 初始化(afterPropertiesSet()) -> 初始化后(AOP) -> 代理对象 -> 放入map(单例池)中 -> bean对象
初始化后执行AOP逻辑，会生成代理对象，代理对象会在内部生成一个target对象，target=被代理的普通对象

### Spring事务

### @Configuration

### 循环依赖


## Spring整合Mybatis
