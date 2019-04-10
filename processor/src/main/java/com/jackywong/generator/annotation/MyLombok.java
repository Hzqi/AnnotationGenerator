package com.jackywong.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by huangziqi on 2019/4/4
 */
@Target({ElementType.TYPE})//对类的注解
@Retention(RetentionPolicy.SOURCE)//只在编译期起作用
public @interface MyLombok {
}
