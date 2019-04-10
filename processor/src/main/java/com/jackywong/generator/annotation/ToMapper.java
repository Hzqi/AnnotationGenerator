package com.jackywong.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by huangziqi on 2019/4/10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ToMapper {
}
