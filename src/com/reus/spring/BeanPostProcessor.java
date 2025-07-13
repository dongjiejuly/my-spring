package com.reus.spring;

/**
 * 模拟 Spring 的 BeanPostProcessor 接口
 */
public interface BeanPostProcessor {

    /**
     * 初始化前指定
     *
     * @param bean     具体的bean对象
     * @param beanName bean的名字
     * @return 返回结果
     */
    Object postProcessBeforeInitialization(Object bean, String beanName);

    /**
     * 初始化后指定
     *
     * @param bean     具体的bean对象
     * @param beanName bean的名字
     * @return 返回结果
     */
    Object postProcessAfterInitialization(Object bean, String beanName);
}
