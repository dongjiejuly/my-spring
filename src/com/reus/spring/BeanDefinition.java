package com.reus.spring;

/**
 * 在spring容器启动解析bean对象的时候，不是直接生成bean对象，先是将.class对象解析后
 * 放入BeanDefinition，后续在根据注解来生成bean对象
 * <p>
 * bean的定义需要存放：bean的类型，单例还是多例，是否需要懒加载等等，所以BeanDefinition需要定义这些属性
 */
public class BeanDefinition {

    private Class type;

    private String scope;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
