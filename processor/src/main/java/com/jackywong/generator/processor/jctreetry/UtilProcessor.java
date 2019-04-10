package com.jackywong.generator.processor.jctreetry;

/**
 * Created by huangziqi on 2019/4/4
 */
import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.Util;
import com.jackywong.generator.util.jctreetry.JcTrees;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 工具注解执行器
 *
 * @see Util 工具类注解
 * 原作者 Created by bbhou on 2017/10/12.
 */
@AutoService(Processor.class)
public class UtilProcessor extends AbstractProcessor {

    private Messager messager;

    private Trees trees;

    private TreeMaker treeMaker;

    private Name.Table names;

    /**
     * 初始化，获取编译环境
     *
     * @param env
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE,"进入init注解处理器里面");
        trees = Trees.instance(env);
        messager.printMessage(Diagnostic.Kind.NOTE,trees.toString());
        Context context = ((JavacProcessingEnvironment) env).getContext();
        messager.printMessage(Diagnostic.Kind.NOTE,context.toString());
        treeMaker = TreeMaker.instance(context);
        messager.printMessage(Diagnostic.Kind.NOTE,treeMaker.toString());
        names = Names.instance(context).table;
        messager.printMessage(Diagnostic.Kind.NOTE,names.toString());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Util.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE,"============================================== UtilProcessor START ==============================================");
        // 处理有 @Util 注解的元素
        for (Element element : roundEnv.getElementsAnnotatedWith(Util.class)) {
            // 只处理作用在类上的注解
            if (element.getKind() == ElementKind.CLASS) {
                addPrivateConstructor(element);
                addFinalModifier(element);
            }
        }
        messager.printMessage(Diagnostic.Kind.NOTE,"============================================== UtilProcessor END ==============================================");
        return true;
    }

    /**
     * 添加私有构造器
     *
     * @param element 拥有注解的元素
     */
    private void addPrivateConstructor(Element element) {
        JCTree tree = (JCTree) trees.getTree(element);
        tree.accept(new TreeTranslator() {

            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                messager.printMessage(Diagnostic.Kind.NOTE,"访问ClassDef");
                jcClassDecl.mods = (JCTree.JCModifiers) this.translate((JCTree) jcClassDecl.mods);
                jcClassDecl.typarams = this.translateTypeParams(jcClassDecl.typarams);
                jcClassDecl.extending = (JCTree.JCExpression) this.translate((JCTree) jcClassDecl.extending);
                jcClassDecl.implementing = this.translate(jcClassDecl.implementing);

                ListBuffer<JCTree> statements = new ListBuffer<>();

                List<JCTree> oldList = this.translate(jcClassDecl.defs);
                boolean hasPrivateConstructor = false;  //是否拥有私有构造器

                //1. 将原来的方法添加进来
                //2. 判断是否已经有默认私有构造器
                for (JCTree jcTree : oldList) {
                    if (isPublicDefaultConstructor(jcTree)) {
                        continue;   //不添加共有默认构造器
                    }
                    if (isPrivateDefaultConstructor(jcTree)) {
                        hasPrivateConstructor = true;
                    }
                    statements.append(jcTree);
                }

                if (!hasPrivateConstructor) {
                    JCTree.JCBlock block = treeMaker.Block(0L, List.<JCTree.JCStatement>nil()); //代码方法内容
                    JCTree.JCMethodDecl constructor = treeMaker.MethodDef(
                            treeMaker.Modifiers(Flags.PRIVATE, List.<JCTree.JCAnnotation>nil()), //private
                            names.fromString(JcTrees.CONSTRUCTOR_NAME),                          //构造器名称
                            null,                                                    //抛异常
                            List.<JCTree.JCTypeParameter>nil(),                                  //范型参数
                            List.<JCTree.JCVariableDecl>nil(),                                   //参数列表
                            List.<JCTree.JCExpression>nil(),                                     //throw表达式
                            block,                                                               //代码方法内容
                            null);

                    statements.append(constructor);
                    jcClassDecl.defs = statements.toList(); //更新
                }

                this.result = jcClassDecl;
            }
        });
    }

    /**
     * 添加 final 修饰符
     * 1. 将工具类的修饰符定义为: public final;
     *
     * @param element 拥有注解的元素
     */
    private void addFinalModifier(Element element) {
        JCTree tree = (JCTree) trees.getTree(element);
        tree.accept(new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                jcClassDecl.mods = treeMaker.Modifiers(Flags.PUBLIC | Flags.FINAL, List.<JCTree.JCAnnotation>nil());
            }
        });
    }


    /**
     * 是否为私有默认构造器
     *
     * @param jcTree
     * @return
     */
    private boolean isPrivateDefaultConstructor(JCTree jcTree) {

        if (jcTree.getKind() == Tree.Kind.METHOD) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
            if (JcTrees.isConstructor(jcMethodDecl)
                    && JcTrees.isNoArgsMethod(jcMethodDecl)
                    && JcTrees.isPrivateMethod(jcMethodDecl)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否为共有默认构造器
     *
     * @param jcTree
     * @return
     */
    private boolean isPublicDefaultConstructor(JCTree jcTree) {

        if (jcTree.getKind() == Tree.Kind.METHOD) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
            if (JcTrees.isConstructor(jcMethodDecl)
                    && JcTrees.isNoArgsMethod(jcMethodDecl)
                    && JcTrees.isPublicMethod(jcMethodDecl)) {
                return true;
            }
        }

        return false;
    }
}
