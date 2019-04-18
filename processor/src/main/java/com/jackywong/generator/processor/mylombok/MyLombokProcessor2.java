package com.jackywong.generator.processor.mylombok;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.TheMapper;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangziqi on 2019/4/16
 * 试一下覆盖已编写的方法
 * 学习用，已放弃
 */
@Deprecated
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.jackywong.generator.annotation.TheMapper"})//该处理器需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//该处理器支持的源码版本
public class MyLombokProcessor2 extends AbstractProcessor {
    //在编译期打log用
    private Messager messager;
    //提供了待处理的抽象语法树
    private JavacTrees trees;
    //封装了创建AST节点的一些方法
    private TreeMaker treeMaker;
    //提供了创建标识符的方法
    private Names names;
    //ToMapper2TheMapper
    public static Map<String, JCTree.JCMethodDecl> mapperMap = new HashMap<>();

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
        Set<? extends Element> theMappers = roundEnv.getElementsAnnotatedWith(TheMapper.class);
        //将Mapper中生成的方法存起来

        theMappers.forEach(element ->{
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {

                    List<JCTree> jcDeclList = List.nil();

                    for (JCTree tree : jcClassDecl.defs) {
                        if (tree.getKind().equals(Tree.Kind.METHOD)) {
                            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) tree;
                            if(jcMethodDecl.getName().toString().equals("toMap")){
//                                JCTree.JCBlock body = jcMethodDecl.getBody();
//                                messager.printMessage(Diagnostic.Kind.NOTE,"body:"+body.toString());
//
//                                List<JCTree.JCStatement> statements = body.getStatements();
//                                statements.forEach(statement -> {
//                                    messager.printMessage(Diagnostic.Kind.NOTE,statement.toString());
//                                });
                                JCTree.JCMethodDecl newJcMethodDecl = newJcMethodDecl();
                                jcDeclList = jcDeclList.append(newJcMethodDecl);
                            }
                        } else {
                            jcDeclList = jcDeclList.append(tree);
                        }
                    }
                    jcClassDecl.defs = jcDeclList;
                    super.visitClassDef(jcClassDecl);
                }
            });
        });

        return true;
    }

    private JCTree.JCMethodDecl newJcMethodDecl() {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        //变量的类型参数   <String,Object>
        ListBuffer<JCTree.JCExpression> typeArgs = new ListBuffer<>();
        typeArgs.append(treeMaker.Ident(names.fromString("String")));
        typeArgs.append(treeMaker.Ident(names.fromString("Object")));

        //变量的类型    Map
        JCTree.JCTypeApply typeApply = treeMaker.TypeApply(
                treeMaker.Ident(names.fromString("Map")),
                typeArgs.toList()
        );

        //对象的类型 HashMap
        JCTree.JCTypeApply typeApply2 = treeMaker.TypeApply(
                treeMaker.Ident(names.fromString("HashMap")),
                List.nil()
        );

        //new对象的表达式 new HashMap<>()
        JCTree.JCNewClass newClass = treeMaker.NewClass(null,List.nil(), //没有Symbol
                typeApply2,                                                          //new 的对象是HashMap
                List.nil(),null);                                         //没有类型参数，没有类定义

        //map的表达式 Map<String.Object> map = new HashMap<>()
        JCTree.JCVariableDecl mapVar = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.BLOCK),
                names.fromString("map"),
                typeApply,newClass
        );

        //构造表达式
        statements.append(
                mapVar
        );

        //构造内容
        ListBuffer<JCTree.JCExpression> putArgs = new ListBuffer<>();
        JCTree.JCLiteral key = treeMaker.Literal(TypeTag.CLASS,"test");
        JCTree.JCLiteral value = treeMaker.Literal(TypeTag.CLASS,"test");
        putArgs.append(key);
        putArgs.append(value);
        JCTree.JCMethodInvocation methodInvocation = treeMaker.Apply(
                List.nil(),                                             //没有类型参数
                treeMaker.Select(                                       //选择器map.put
                        treeMaker.Ident(names.fromString("map")),
                        names.fromString("put")
                ),
                putArgs.toList()
        );
        statements.append(
                treeMaker.Exec(
                        methodInvocation
                )
        );

        //表达式的返回语句 return map
        statements.append(
                treeMaker.Return(
                        treeMaker.Ident(names.fromString("map"))
                )
        );

        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        JCTree.JCMethodDecl method = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("test"),
                typeApply,
                List.nil(),                                 //范型参数
                List.nil(),                                 //参数
                List.nil(),                                 //throw表达式
                body,                                       //方法内表达式
                null
        );
        return method;
    }
}
