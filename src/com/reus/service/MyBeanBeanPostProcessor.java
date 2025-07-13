package com.reus.service;

import com.reus.spring.BeanPostProcessor;
import com.reus.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 只有要定义处理类继承BeanPostProcessor方法，那么在所有类创建过程中就会分别执行
 * postProcessBeforeInitialization() 和 postProcessAfterInitialization() 方法，在两个方法中在根据逻辑对对应的bean实现处理，
 * 这样程序员就可以以更加灵活的方式来处理bean对象，比如：1、对原始对象进行改变 2、对对象进行代理
 */
@Component
public class MyBeanBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 对特定的bean进行初始化前处理
        if (beanName.equals("orderService")) {
            System.out.println("bean 初始化前 orderService 的bean进行特殊处理");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // 对特定的bean进行初始化后处理
        if (beanName.equals("userService")) {
            System.out.println("bean 初始化后 userService 的bean进行特殊处理");

            /**
             * 第一个参数：代理的对象
             * 第二个参数：代理的接口
             * 第三个参数：InvocationHandler
             */
            Object proxyInstance = Proxy.newProxyInstance(MyBeanBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("bean 初始化后 对userService进行代理");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
