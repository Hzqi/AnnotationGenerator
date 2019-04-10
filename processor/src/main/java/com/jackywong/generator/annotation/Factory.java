package com.jackywong.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by huangziqi on 2019/4/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Factory {
    /*
    id和type就是用于组成在同一个Factory里的"条件"或者"情况"

    有几个固定条件
    1 Only classes can be annotated with @Factory since interfaces or abstract classes can not be instantiated with the new operator.
    2 Classes annotated with @Factory must provide at least one public empty default constructor (parameterless). Otherwise we could not instantiate a new instance.
    3 Classes annotated with @Factory must inherit directly or indirectly from the specified type (or implement it if it’s an interface).
    4 @Factory annotations with the same type are grouped together and one Factory class will be generated. The name of the generated class has “Factory” as suffix, for example type = Meal.class will generate MealFactory
    5 id are limited to Strings and must be unique in it’s type group.
     */

    Class type();

    String id();
}
