package com.jackywong.generator.processor.factory;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.factory.Factory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangziqi on 2019/4/2
 */

/*
AutoService注解用于生成  META-INF/services/javax.annotation.processing.Processor的
 */
@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<String, FactoryGroupedClasses>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        try {

            //获取所有注解了Factory的Element，一般有类元素、包元素、可执行元素、属性元素
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Factory.class)) {

                //检查注解了Factory的是不是类级别
                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    throw new ProcessingException(annotatedElement, "Only classes can be annotated with @%s",
                            Factory.class.getSimpleName());
                }

                //直接将Element转换成TypeElement，不是的就已经在上面过滤了
                TypeElement typeElement = (TypeElement) annotatedElement;

                FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement);

                isVaildClass(annotatedClass);

                //通过校验
                FactoryGroupedClasses factoryClass =
                        factoryClasses.get(annotatedClass.getQualifiedSuperClassName());
                //factoryClasses是按照每一个type作为key分组的
                if (factoryClass == null) {
                    String qualifiedGroupName = annotatedClass.getQualifiedSuperClassName();
                    factoryClass = new FactoryGroupedClasses(qualifiedGroupName);
                    factoryClasses.put(qualifiedGroupName, factoryClass);
                }

                // 向每一个以type为组的，添加对应的返回类
                factoryClass.add(annotatedClass);
            }

            //每个以同一个type为组的Factory，生成工厂类
            for (FactoryGroupedClasses factoryClass : factoryClasses.values()) {
                factoryClass.generateCode(elementUtils, filer);
            }
            //生成完代码要把它清空掉，比如说是清掉缓存
            factoryClasses.clear();
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }

        return true;
    }

    //检查是否合规
    private boolean isVaildClass(FactoryAnnotatedClass item) throws ProcessingException {
        TypeElement classElement = item.getAnnotatedClassElement();

        //检查注解了Factory的类是否是public的
        if(!classElement.getModifiers().contains(Modifier.PUBLIC)){
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            return false;
        }

        //检查是否是抽象类
        if(classElement.getModifiers().contains(Modifier.ABSTRACT)){
            error(classElement,"The class %s is not allowed to be abstract.",classElement.getQualifiedName().toString());
            return false;
        }

        //检查类继承，注解类Factory的类必须是注解类里type的子类
        TypeElement superClassElement = elementUtils.getTypeElement(item.getQualifiedSuperClassName());
        //父类是接口
        if(superClassElement.getKind() == ElementKind.INTERFACE){
            //这个子类没有继承那个接口
            if(!classElement.getInterfaces().contains(superClassElement.asType())){
                throw new ProcessingException(classElement,
                        "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        item.getQualifiedSuperClassName());
            }
        } else {
            //父类不是接口，而是类，循环向上查找
            TypeElement currentClass = classElement;
            while(true) {
                TypeMirror superClassType = currentClass.getSuperclass();

                //TypeKind.NONE就代表没有父类了，或者父类是Object了
                if (superClassType.getKind() == TypeKind.NONE) {
                    error(classElement, "The class %s annotated with @%s must inherit from %s",
                            classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                            item.getQualifiedSuperClassName());
                    return false;
                }

                //找到了
                if (superClassType.toString().equals(item.getQualifiedSuperClassName())) {
                    break;
                }

                // 继续往上查找
                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
        }

        //检查是否含有一个无参的构造方法
        for (Element enclosed : classElement.getEnclosedElements()) {
            if(enclosed.getKind() == ElementKind.CONSTRUCTOR){
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if(constructorElement.getParameters().isEmpty() &&
                        constructorElement.getModifiers().contains(Modifier.PUBLIC)){
                    return true;
                }
            }
        }

        //上面查找无参的构造方法没通过时
        error(classElement, "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
        return false;
    }

    /* 处理错误 */
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg,args)
        );
    }
}
