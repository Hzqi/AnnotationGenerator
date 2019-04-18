package com.jackywong.generator.processor.mapper;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.MyLombok;
import com.jackywong.generator.annotation.ToMapper;
import com.jackywong.generator.util.jctreetry.ToMapMaker;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangziqi on 2019/4/18
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.jackywong.generator.annotation.ToMapper")//该处理器需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//该处理器支持的源码版本
public class ToMapperProcessor extends AbstractProcessor {
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
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(ToMapper.class);

        ToMapMaker maker = new ToMapMaker(trees,treeMaker,names);
        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    ListBuffer<JCTree.JCVariableDecl> jcVariableDecls = new ListBuffer<>();
                    ListBuffer<JCTree.JCMethodDecl> jcMethodDecls = new ListBuffer<>();
                    for (JCTree tree : jcClassDecl.defs) {
                        //成员变量
                        if(tree.getKind().equals(Tree.Kind.VARIABLE)) {
                            jcVariableDecls.append((JCTree.JCVariableDecl) tree);
                        } else if(tree.getKind().equals(Tree.Kind.METHOD)) {
                            jcMethodDecls.append((JCTree.JCMethodDecl) tree);
                        }
                    }

                    Map<String, JCTree.JCMethodDecl> var2method = maker.makeVariable2Method(
                            jcVariableDecls.toList(),jcMethodDecls.toList());
                    for (JCTree.JCVariableDecl variableDecl : jcVariableDecls) {
                        String varName = variableDecl.getName().toString();
                        if(var2method.get(varName) == null){
                            String msg = "类"+jcClassDecl.getSimpleName().toString();
                            msg += "的成员变量"+varName+"对应的getter方法不合规:";
                            msg += "(不存在，不为public，返回类型和成员变量不符合)";
                            messager.printMessage(Diagnostic.Kind.WARNING,msg);
                        }
                    }

                    JCTree.JCMethodDecl toMapMethodDecl = maker.createToMapMethod(var2method);
                    ListBuffer<JCTree> jcTrees = new ListBuffer<>();
                    boolean hasAdded = false;
                    for (JCTree tree : jcClassDecl.defs) {
                        if(tree.getKind().equals(Tree.Kind.METHOD)){
                            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) tree;
                            if(jcMethodDecl.getName().toString().equals("toMap")){
                                jcTrees.append(toMapMethodDecl);
                                hasAdded = true;
                            } else {
                                jcTrees.append(tree);
                            }
                        } else {
                            jcTrees.append(tree);
                        }
                    }
                    if(!hasAdded){
                        jcTrees.append(toMapMethodDecl);
                    }

                    jcClassDecl.defs = jcTrees.toList();

                    super.visitClassDef(jcClassDecl);
                }
            });
        });

        return true;
    }
}
