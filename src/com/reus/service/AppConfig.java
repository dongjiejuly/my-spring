package com.reus.service;


import com.reus.spring.ComponentScan;

/**
 * 该类的作用是配置类
 * 配置当前工程需要扫描的包路径，将对应扫描的.class文件生成bean对象
 */
@ComponentScan(value="com.reus.service")
public class AppConfig {
}
