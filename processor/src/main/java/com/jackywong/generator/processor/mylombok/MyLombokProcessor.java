package com.jackywong.generator.processor.mylombok;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.MyLombok;
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
        //利用roundEnv的getElementsAnnotatedWith方法过滤出被MyLombok注解标记的类，并存入set
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(MyLombok.class);
        //遍历set并生成jCTree这个语法树
        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                //这个方法处理遍历语法树得到的类定义部分jcClassDecl
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    //保存类的成员变量
                    //List 是 package com.sun.tools.javac.util
                    List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();
                    //遍历jcTree的所有成员(包括成员变量和成员函数和构造函数)，过滤出其中的成员变量，并添加进jcVariableDeclList
                    for (JCTree tree : jcClassDecl.defs) {
                        if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) tree;
                            jcVariableDeclList = jcVariableDeclList.append(jcVariableDecl);
                        }
                    }
                    jcVariableDeclList.forEach(jcVariableDecl -> {
                        messager.printMessage(Diagnostic.Kind.NOTE, jcVariableDecl.getName() + " has been processed");
                        //将jcVariableDeclList的所有变量转换成需要添加的方法，并添加进jcClassDecl的成员中
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetMethodDecl(jcVariableDecl));
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeSetMethodDecl(jcVariableDecl));
                    });
                    //调用默认的遍历方法遍历处理后的jcClassDecl
                    //利用上面的TreeTranslator去处理jcTree
                    super.visitClassDef(jcClassDecl);
                }
            });
        });
        return true;
    }

    //Getter方法的生成
    private JCTree.JCMethodDecl makeGetMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        //构造方法里的表达式
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(
                // return this.xxx
                treeMaker.Return(
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString("this")),
                                jcVariableDecl.getName()
                        )
                )
        );

        //构造方法体
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());

        JCTree.JCMethodDecl method = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),          //public
                getNewMethodName(jcVariableDecl.getName()), //getXxx()
                jcVariableDecl.vartype,                     //返回类型，和这个属性的类型一样
                List.nil(),                                 //范型参数
                List.nil(),                                 //参数
                List.nil(),                                 //throw表达式
                body,                                       //方法内表达式
                null);

        return method;
    }

    //构造出getXxx的方法名
    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }

    //Setter方法生成
    private JCTree.JCMethodDecl makeSetMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        // 添加调用语句" this.setXXX(xxx); "
        // this.XXX=xxx
        statements.append(
                treeMaker.Exec(
                        treeMaker.Assign(                                          // = 相当于赋值表达式
                                treeMaker.Select(
                                        treeMaker.Ident(
                                                names.fromString("this")),      //this.xxx
                                                jcVariableDecl.getName()
                                ),
                                treeMaker.Ident(jcVariableDecl.getName())          //xxx 这里假设入参的名字和属性的名字一样，所以直接用
                        )
                )
        );
        //参数列表
        JCTree.JCVariableDecl param = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                jcVariableDecl.getName(),
                jcVariableDecl.vartype,null
        );
        List<JCTree.JCVariableDecl> parameters = List.from(new JCTree.JCVariableDecl[]{param});
        // 添加返回语句 " return this; "
        // 转换成代码块
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),// public方法
                setNewMethodName(jcVariableDecl.getName()),// 方法名称
                null,// 方法返回的类型
                List.nil(),// 泛型参数
                parameters,// 方法参数
                List.nil(),// throw表达式
                body,// 方法体
                null// 默认值
        );
    }

    //构造setter方法
    private Name setNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("set" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }

    private Name createNameByString(String name) {
        return names.fromString(name);
    }
}
