package com.air.aiagent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 大多数自定义注解的时候，都是需要这两个注解
@Target(ElementType.METHOD)  // 注解的生效范围，这里是针对方法打上的注解
@Retention(RetentionPolicy.RUNTIME)  // 指定注解在什么时候生效，运行时
public @interface LoginCheck {

}
