package com.jackywong.generator.processor.lombok;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.MyLombok;
import com.jackywong.generator.util.GetSetMaker;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Created by huangziqi on 2019/4/4
 * 原作者是https://wiootk.github.io/blog/back/2018/02/08/myLombok.html#%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.jackywong.generator.annotation.MyLombok")//该处理器需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//该处理器支持的源码版本
public class MyLombokProcessor extends AbstractProcessor {
    //在编译期打log用
    private Messager messager;
    //提供了待处理的抽象语法树
    private JavacTrees trees;
    //封装了创建AST节点的一些方法
    private TreeMaker treeMaker;
    //提供了创建标识符的方法
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);                             //获取JCTree对象
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext(); //获取Java注解处理器的上下文
        this.treeMaker = TreeMaker.instance(context);                                //获取TreeMaker对象
        this.names = Names.instance(context);                                        //获取Names对象
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(MyLombok.class);

        GetSetMaker maker = new GetSetMaker(trees,treeMaker,names);
        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                //这个方法处理遍历语法树得到的类定义部分jcClassDecl
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {

                    ListBuffer<JCTree.JCVariableDecl> jcVariableDeclList = new ListBuffer<>();
                    for (JCTree tree : jcClassDecl.defs) {
                        if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) tree;
                            jcVariableDeclList.append(jcVariableDecl);
                        }
                    }
                    List<JCTree> getters = maker.createGetter(jcVariableDeclList.toList());
                    List<JCTree> setters = maker.createSetter(jcVariableDeclList.toList());

                    ListBuffer<JCTree> jcTrees = new ListBuffer<>();
                    for (JCTree tree : jcClassDecl.defs) {
                        jcTrees.append(tree);
                    }

                    jcTrees.appendList(getters);
                    jcTrees.appendList(setters);
                    jcClassDecl.defs = jcTrees.toList();

                    super.visitClassDef(jcClassDecl);
                }
            });
        });
        return true;
    }
}
