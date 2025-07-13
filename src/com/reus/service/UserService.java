package com.reus.service;

import com.reus.spring.Autowired;
import com.reus.spring.Component;
import com.reus.spring.Scope;

/**
 * 在spring容器中，对象需要加上对应的注解(如：@Component)才能被扫描并加载成为bean，所有我们需要定义@Component注解
 */
@Component("userService")
@Scope(value = "singleton")
public class UserService implements UserServiceInterface {

    @Autowired
    private OrderService orderService;

    @Override
    public void testOrderService() {
        System.out.println(orderService);
    }
}
