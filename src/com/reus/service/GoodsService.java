package com.reus.service;

import com.reus.spring.Autowired;
import com.reus.spring.BeanNameAware;
import com.reus.spring.Component;
import com.reus.spring.InitializingBean;

/**
 * 如何通过spring容器得到bean的名字？
 * 使用spring的Aware回调机制
 * spring存在BeanNameAware接口，如果需要回调则实现BeanNameAware接口中的setBeanName()方法
 * 所有我们需要再系统中模拟创建BeanNameAware接口，并在对应bean上实现BeanNameAware接口
 */
@Component
public class GoodsService implements BeanNameAware, InitializingBean {

    @Autowired
    private OrderService orderService;

    private String beanName;

    public void testBeanName() {
        System.out.println("打印当前bean的名字：" + beanName);
    }

    /**
     * 该方法在spring容器实例化后会自动调用该方法，当实现BeanNameAware的bean完成实例化的时候就会调用该方法，
     * 告知该bean的名字，这时我们就可以获取到对应bean的名字，并做相应的处理
     *
     * @param beanName bean的名字
     */
    @Override
    public void setBeanName(String beanName) {
        System.out.println("spring 通过Aware回调告知当前bean的名字" + beanName);
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("spring 初始化后执行的方法");
    }
}
