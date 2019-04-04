package com.jackywong.generator.annotation.jctreetry;

/**
 * Created by huangziqi on 2019/4/4
 */

import java.lang.annotation.*;

/**
 * 当工具类添加此注解。
 * 1. 将其 constructor 默认 private{};
 * 2. 将当前类设置为 final;
 *
 * 不足之处：
 * 1. 对于 java 类，如果是直接声明私有构造器，则 new XXX() 直接提示错误
 * 但是如果使用编译时异常，需要运行时才会报错。
 * Created by bbhou on 2017/9/29.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Inherited
public @interface Util {
}
