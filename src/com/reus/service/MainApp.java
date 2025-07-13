package com.reus.service;

import com.reus.spring.MyApplicationContext;

public class MainApp {


    public static void main(String[] args) {
        /**
         * 获取Spring容器：
         * 1、首先需要提供spring容器，所以在获取容器的时候需要传入配置类
         * 2、需要定义配置类AppConfig 配置类需要指定扫描路径（通过注解的形式：因此需要提供@ComponentScan注解），
         * 扫描该配置类下的所有.class以便对象生成bean
         */
        MyApplicationContext myApplicationContext = new MyApplicationContext(AppConfig.class);

        // 在spring容器中有个很常用的方法getBean,通过指定beanName获取bean对象，所以需要MyApplicationContext提供getBean方法
        UserServiceInterface userService = (UserServiceInterface) myApplicationContext.getBean("userService");

        OrderService orderService = (OrderService) myApplicationContext.getBean("orderService");

        GoodsService goodsService = (GoodsService) myApplicationContext.getBean("goodsService");
        goodsService.testBeanName();

        System.out.println(userService);
        userService.testOrderService();
        System.out.println(orderService);
    }

}
