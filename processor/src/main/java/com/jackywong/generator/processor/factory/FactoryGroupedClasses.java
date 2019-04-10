package com.jackywong.generator.processor.factory;

import com.jackywong.generator.annotation.Factory;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by huangziqi on 2019/4/2
 */
/* 用于存放已经注解了Factory的类的数据结构
*  需要注意的是，这里的Factory的注解并不只能生成一个工厂类，可以生成任意工厂类，主要靠注解的id和type去区分*/
public class FactoryGroupedClasses {
    //生成的工厂类后缀
    private static final String SUFFIX = "Factory";

    //完整的类名
    private String qualifiedClassName;

    //存放解析注解的类的数据结构的map
    private Map<String, FactoryAnnotatedClass> itemsMap =
            new LinkedHashMap<String, FactoryAnnotatedClass>();

    public FactoryGroupedClasses(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public void add(FactoryAnnotatedClass toInsert) throws ProcessingException {

        //如果id已存在，就报错
        FactoryAnnotatedClass existing = itemsMap.get(toInsert.getId());
        if (existing != null) {
            throw new ProcessingException(toInsert.getAnnotatedClassElement(),
                    "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                    toInsert.getAnnotatedClassElement().getQualifiedName().toString(), Factory.class.getSimpleName(),
                    toInsert.getId(), existing.getAnnotatedClassElement().getQualifiedName().toString());
        }

        itemsMap.put(toInsert.getId(), toInsert);
    }

    //代码生成
    /*
     $T 是类型替换, 一般用于 ("$T foo", List.class) => List foo. $T 的好处在于 JavaPoet 会自动帮你补全文件开头的 import
     $L 是字面量替换, 比如 ("abc$L123", "FOO") => abcFOO123. 也就是直接替换.
     $S 是字符串替换, 比如: ("$S.length()", "foo") => "foo".length() 注意 $S 是将参数替换为了一个带双引号的字符串. 免去了手写 "\"foo\".length()" 中转义 (\") 的麻烦
     $N 是名称替换, 比如你之前定义了一个函数 MethodSpec methodSpec = MethodSpec.methodBuilder("foo").build(); 现在你可以通过 $N 获取这个函数的名称 ("$N", methodSpec) => foo
     */
    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        TypeElement superClassName = elementUtils.getTypeElement(qualifiedClassName); //从elementUtils中获取父类（即接口）的TypeElement
        String factoryClassName = superClassName.getSimpleName() + SUFFIX; //拼接类名
        String qualifiedFactoryClassName = qualifiedClassName + SUFFIX;
        PackageElement pkg = elementUtils.getPackageOf(superClassName); //获取父类的包Element
        String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString(); //获取包名

        //创建方法
        MethodSpec.Builder method = MethodSpec.methodBuilder("create") //方法名
                .addModifiers(Modifier.PUBLIC)                               //方法签名public
                .addParameter(String.class, "id")                      //方法参数是String类型的，名为id
                .returns(TypeName.get(superClassName.asType()));             //方法返回的是父类（即接口）的类型

        //该方法开始时需要判断id是否为空
        method.beginControlFlow("if (id == null)")                           //id是否为空
                .addStatement("throw new IllegalArgumentException($S)", "id is null!")  //if内的逻辑
                .endControlFlow();

        //循环的一层一层的生成if
        for (FactoryAnnotatedClass item : itemsMap.values()) {
            method.beginControlFlow("if ($S.equals(id))", item.getId())
                    .addStatement("return new $L()", item.getAnnotatedClassElement().getQualifiedName().toString())
                    .endControlFlow();
        }

        //最后的情况抛出异常
        method.addStatement("throw new IllegalArgumentException($S + id)", "Unknown id = ");

        //创建class类
        TypeSpec typeSpec = TypeSpec.classBuilder(factoryClassName).addModifiers(Modifier.PUBLIC)
                .addMethod(method.build()).build();

        // Write file
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }
}
