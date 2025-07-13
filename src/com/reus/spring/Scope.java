package com.reus.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    /**
     * singleton：单例模式
     * prototype:原型模式（多例）
     *
     * @return 返回数据
     */
    String value() default "singleton";
}
