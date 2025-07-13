package com.reus.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private Class classValue;

    /**
     * key为bean的名字 value为beanDefinition(即bean定义的相关信息)
     * 主要存放扫描后bean的基本信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 单例池，存放所有被spring容器创建的bean对象
     */
    private ConcurrentHashMap<String, Object> singletonMap = new ConcurrentHashMap<>();

    /**
     * 存放所有实现BeanPostProcessor的bean实例
     */
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 在外层 new MyApplicationContext(),相当于启动一个Spring容器，那么在启动Spring容器的时候，需要完成很多工作：
     *
     * @param className 配置类路径
     */
    public MyApplicationContext(Class className) {
        this.classValue = className;

        // 第一步：扫描.class文件--->定义BeanDefinition对象--->存入beanDefinitionMap
        if (className.isAnnotationPresent(ComponentScan.class)) {
            // 如有配置的有扫描路径的注解，那么我们就拿出该注解的相关信息
            ComponentScan annotation = (ComponentScan) className.getAnnotation(ComponentScan.class);
            // 获取用户配置的value值：即扫描路径 com.reus.service
            String path = annotation.value();

            // 目前我们只知道用户配置的"com.reus.service"包路径，那么如何获取：.class文件路径啦？可以通过类加载器来实现
            path = path.replace(".", File.separator);

            // 配置的路径转化成文件地址路径，获取.class文件
            ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
            // 通过com/reus/service相对文件路径获取到service文件夹的URL路径
            URL resource = classLoader.getResource(path);
            // 然后通过URL获取service文件夹的这个文件
            File file = new File(resource.getFile());
            // 判断file文件是否为文件夹，并获取该文件夹下所有的.class文件
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    // 遍历所有的文件，只需要.class文件，其他文件不需要
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 检查类是否有@Component注解，只有有注解的才是Spring需要装配的bean
                        // 要检查@Component注解，那么只有通过反射，获取类的Class对象，才能获取类上面的注解
                        // 注意类加载器获取Class对象，需要传入的是类的全路基名(包名+类名)：com.reus.service.UserService
                        String fileSeparatorName = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        try {
                            Class<?> clazz = classLoader.loadClass(fileSeparatorName.replace(File.separatorChar, '.'));
                            if (clazz.isAnnotationPresent(Component.class)) {

                                /**
                                 * 这部分代码后面补充
                                 * 判断类是否实现了BeanPostProcessor方法，不能使用 instanceof ，该方式主要是陪段实例是否实现某些口
                                 * 只能是否BeanPostProcessor.class.isAssignableFrom(clazz)的方式
                                 */
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    try {
                                        BeanPostProcessor newInstance = (BeanPostProcessor) clazz.newInstance();
                                        beanPostProcessorList.add(newInstance);
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // 有@Component注解，需要Spring进行装配，即创建bean对象
                                // 在我们spring容器进行管理bean的时候，分为单例bean和多例bean，其中只有单例bean是容器启动的时候创建，多例bean是在真正使用的时候才创建
                                // Spring容器是怎么做的：通过定义BeanDefinition来控制bean的创建
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                String value = "singleton";
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    /**
                                     * 在调试过程中，我配置了@Scope注解，且配置了value为prototype，但是我的service上面根本获取不到@Scope注解，
                                     * 最后检查发现我的@Scope注解定义出错，没有配置注解作用域@Target以及注解的生命周期@Retention
                                     */
                                    Scope scope = clazz.getAnnotation(Scope.class);
                                    value = scope.value();
                                }
                                beanDefinition.setScope(value);

                                /**
                                 * spring在存储key的时候，如果从首字母开始连续多个字段都是大写，则直接存入，
                                 * 如果只有首字母是大写，第二个字母是小写，则将首字母转化成小写存入map
                                 */
                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value();
                                if ("".equals(beanName)) {
                                    // 如果用户没有设置bean的名字，那我们需要自动生成bean的名字
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 第二步：创建单例bean(实例化单例bean)--->存入单例池
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonMap.put(beanName, bean);
            }
        });

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class type = beanDefinition.getType();
        try {
            /**
             * 第一步：实例化
             */
            Object instance = type.getConstructor().newInstance();

            // 获取到bean对象定义的所有属性
            Field[] declaredFields = type.getDeclaredFields();

            /**
             * 第二步：依赖注入
             * 判断这些属性是否需要注入 bean，完成依赖注入
             */
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    // 如果有自动注入注解，则将注解需要的bean设置到该注解上
                    declaredField.setAccessible(true);
                    declaredField.set(instance, getBean(declaredField.getName()));
                }
            }

            /**
             * 第三分：实现 spring 的 Aware 通知机制
             */
            if (instance instanceof BeanNameAware) {
                // 如果当前bean对象实现了BeanNameAware接口，那么这个时候就需要调用接口中的方法，完成Aware回调
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            /**
             * 第四步：初始化前执行
             * BeanPostProcessor 的作用是程序员可以再bean初始化前前后进行一定功能上的处理
             * Spring 提供 BeanPostProcessor 接口，该接口提供两个接口：
             * postProcessBeforeInitialization() 和 postProcessAfterInitialization()
             */
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            /**
             * 第五步：初始化
             * 通过实现 spring 的 InitializingBean 接口，实现接口的方法 afterPropertiesSet()
             */
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            /**
             * 第六步：初始化后执行
             * BeanPostProcessor 的作用是程序员可以再bean初始化后执行postProcessAfterInitialization()
             * 可以在postProcessAfterInitialization()方法中加入AOP逻辑
             */
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过bean的名字获取bean对象
     *
     * @param beanName bean的名字
     * @return bean对象
     */
    public Object getBean(String beanName) {
        /**
         * 思考：
         * 传入名字，如何获取bean对象？
         * 传入的bean名字，需要校验该bean对象是单例还是多例，单例直接获取，多例则需要创建（需要标志单例还是多例，那么需要提供@Scope注解）
         */
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (Objects.isNull(beanDefinition)) {
            throw new NullPointerException();
        }
        String scope = beanDefinition.getScope();
        if ("singleton".equals(scope)) {
            // 单例直接获取
            Object bean = singletonMap.get(beanName);
            if (Objects.isNull(bean)) {
                bean = createBean(beanName, beanDefinition);
                singletonMap.put(beanName, bean);
            }
            return bean;
        } else {
            // 多例则进行创建
            return createBean(beanName, beanDefinition);
        }
    }
}
