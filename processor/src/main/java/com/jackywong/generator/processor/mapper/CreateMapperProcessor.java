package com.jackywong.generator.processor.mapper;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.CreateMapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * Created by huangziqi on 2019/4/10
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.jackywong.generator.annotation.CreateMapper")//该处理器需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//该处理器支持的源码版本
public class CreateMapperProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(CreateMapper.class)) {
                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    throw new IllegalAccessException(
                            String.format("Only classes can be annotated with @%s",
                            CreateMapper.class.getSimpleName())
                    );
                }

                //直接将Element转换成TypeElement，不是的就已经在上面过滤了
                TypeElement typeElement = (TypeElement) annotatedElement;

                MapperWriter writer = new MapperWriter(typeElement);
                writer.generateCode(elementUtils,filer);
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException e) {
            error(null, e.getMessage());
        }

        return true;
    }

    /* 处理错误 */
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg,args)
        );
    }
}
