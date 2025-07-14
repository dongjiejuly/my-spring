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
（1）模拟创建 Spring 容器 MyApplicationContext，在容器启动时需要传入配置类   
（2）定义配置类 AppConfig，配置类有一个很重要的功能，就是告诉容器需要扫描的包路径   
（3）定义注解 @ComponentScan，并将注解配置到配置类 AppConfig上，这样在将配置类传给 Spring 容器 MyApplicationContext 的时候，就能够解析出包路径   
（4）利用配置上面的包路径，获取到对应的文件夹路径，然后扫描该文件夹下面所有的 .class 文件，在 Spring 中只有配置了类似于 @Component 的注解才生成为 bean 对象放入 Spring 的容器中   
（5）定义注解 @Component，并将 @Component 配置到 需要生成bean对象的类上  
（6）解析所有带有 @Component 的类对象，并将解析结果存入到 ConcurrentHashMap<String, BeanDefinition>（BeanDefinition是一个用于存放bean定义的相关信息的对象，使用beanDefinitionMap是为了在扫描阶段获取到所有对象的定义的基本信息，为后续真正生成bean对象做准备）
- 在解析带有 @Component 的类对象的时候，需要同时解析 @Scope注解，该注解告知了 bean 对象到底是单例还是多例，直接影响 bean 的创建时机（所以需要我们定义 @Scope 注解）
- 在解析带有 @Component 的类对象的时候，另外还需要判断被解析的类是否实现了 BeanPostProcessor 接口，如果实现该接口，需要将对应的实例加入 List<BeanPostProcessor> 的集合中，在对象初始化前后调用该接口中的两个方法  
### 创建bean的过程
（1）遍历 ConcurrentHashMap<String, BeanDefinition> 对象，判断对应的类是否为单例，如果为单例则往后继续执行  
（2）遍历的当前类通过构造器实例化对象（一般是默认无参构造器，实例化完成后，对象的属性值并没有被填充，需要后续进行填充，如果是有参构造器，那么参数部分的属性就会被填充）   
（3）获取当前当前类的所有属性值，判断这些属性是否需要完成依赖注入（有 @Autowired 注解，所以也需要自己定义 @Autowired 注解），如果需要完成依赖注入，则进行依赖注入   
（4）判断对象是否有实现 BeanNameAware 接口，该接口是用于完成 Aware回调功能的，如果有则调用对象的 setBeanName()方法（该方法是接口中的方法）  
（5）判断 List<BeanPostProcessor> 集合中是否有对象，有则调用 postProcessBeforeInitialization(instance, beanName) 方法，完成初始化前操作  
（6）判断对象是否实现了 InitializingBean 接口，如果实现了该接口，则调用 afterPropertiesSet()方法  
（7）判断 List<BeanPostProcessor> 集合中是否有对象，有则调用 postProcessAfterInitialization(instance, beanName) 方法，完成初始化后操作
