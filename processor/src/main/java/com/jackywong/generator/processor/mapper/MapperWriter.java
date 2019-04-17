package com.jackywong.generator.processor.mapper;

import com.jackywong.generator.annotation.TheMapper;
import com.jackywong.generator.annotation.ToMapper;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangziqi on 2019/4/10
 */
public class MapperWriter {
    private static final String SUFFIX = "Mapper";
    //完整的类名
    private String qualifiedClassName;
    //整个注解的元素
    private TypeElement classElement;

    public MapperWriter(TypeElement classElement) {
        this.classElement = classElement;
        this.qualifiedClassName = classElement.getSimpleName().toString();
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException,ClassNotFoundException {
        String mapperName = qualifiedClassName+SUFFIX;
        PackageElement pkg = elementUtils.getPackageOf(classElement);
        String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();

        MethodSpec.Builder method = createMapper(elementUtils);
        //创建class类
        TypeSpec typeSpec = TypeSpec.classBuilder(mapperName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method.build())
                .addAnnotation(TheMapper.class)
                .build();
        // Write file
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }

    //创建toMap方法
    private MethodSpec.Builder createMapper(Elements elementUtils) throws ClassNotFoundException{
        TypeName stringType = ClassName.get(String.class);
        TypeName objectType = ClassName.get(Object.class);
        TypeName mapStrObjType = ParameterizedTypeName.get(ClassName.get(Map.class),stringType,objectType);

//        Class<?> clazz = Class.forName(elementUtils.getBinaryName(classElement).toString());
        String parameterTypeName = elementUtils.getBinaryName(classElement).toString();

        MethodSpec.Builder method = MethodSpec.methodBuilder("toMap")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addParameter(Object.class,"obj")
                .returns(mapStrObjType);
        method.addStatement("Map<String,Object> map = new $T<>()", HashMap.class);

        List<? extends Element> innerElements = classElement.getEnclosedElements();
        for (Element innerElement : innerElements) {
            //检查是否是private的
            if(innerElement.getModifiers().contains(Modifier.PRIVATE)){
                //检查是否是方法
                if(innerElement.getKind() == ElementKind.FIELD){
                    String fieldName = innerElement.getSimpleName().toString();
                    String getterCode = "get"+fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    String valueCode = "(("+parameterTypeName+")obj)."+getterCode+"()";

                    method.addStatement("map.put($S,$L)", fieldName, valueCode);
                }
            }
        }
        method.addStatement("return map");
        return method;
    }
}
