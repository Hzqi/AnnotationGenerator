package com.jackywong.generator.processor.factory;


import com.jackywong.generator.annotation.Factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

/**
 * Created by huangziqi on 2019/4/2
 */

/* 用于存放将解析注解的类的数据结构 */
public class FactoryAnnotatedClass {
    private TypeElement annotatedClassElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;
    private String id;

    public FactoryAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {
        this.annotatedClassElement = classElement;
        Factory annotation = classElement.getAnnotation(Factory.class);
        id = annotation.id();

        //检查id是否为空
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("id() in @%s for class %s is null or empty! that's not allowed",
                            Factory.class.getSimpleName(), classElement.getQualifiedName().toString()));
        }

        //获取注解了类的名称
        /*
        这里需要注意两种情况，这个class是否已经编译了。
        1、如果这个注解了Factory的class已经编译了，并且作为jar包内的类导入了进来，那么直接就可以使用过了。
        2、如果这个class还没被编译，就要从mirror里取它的type
         */
        try {
            Class<?> clazz = annotation.type();
            qualifiedSuperClassName = clazz.getCanonicalName(); //完整的类型名，如my.aaa.bbb[]
            simpleTypeName = clazz.getSimpleName(); //本class的名
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString(); //会获得继承的类名
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    public TypeElement getAnnotatedClassElement() {
        return annotatedClassElement;
    }

    public String getQualifiedSuperClassName() {
        return qualifiedSuperClassName;
    }

    public String getSimpleTypeName() {
        return simpleTypeName;
    }

    public String getId() {
        return id;
    }
}
