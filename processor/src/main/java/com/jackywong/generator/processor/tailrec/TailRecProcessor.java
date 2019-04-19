package com.jackywong.generator.processor.tailrec;

import com.google.auto.service.AutoService;
import com.jackywong.generator.annotation.TailRec;
import com.jackywong.generator.annotation.ToMapper;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
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
 * Created by huangziqi on 2019/4/19
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.jackywong.generator.annotation.TailRec")//该处理器需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//该处理器支持的源码版本
public class TailRecProcessor extends AbstractProcessor {
    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
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
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(TailRec.class);

        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator(){
                @Override
                public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                    JCTree.JCBlock jcBlock = jcMethodDecl.getBody();
                    List<JCTree.JCStatement> statements = jcBlock.getStatements();

                    ListBuffer<JCTree.JCStatement> newStatements = new ListBuffer<>();
                    for (JCTree.JCStatement statement : statements) {
                        JCTree.JCStatement newStmt = findAndChangeReturn2Exec(statement,jcMethodDecl.getName(),jcMethodDecl.getParameters());
                        newStatements.append(newStmt);
                    }

                    JCTree.JCBlock loopBody = treeMaker.Block(0,newStatements.toList());
                    JCTree.JCWhileLoop whileLoop = treeMaker.WhileLoop(
                            treeMaker.Literal(true),
                            loopBody
                    );
                    ListBuffer<JCTree.JCStatement> methodStmt = new ListBuffer<>();
                    methodStmt.append(whileLoop);
                    JCTree.JCBlock methodBody = treeMaker.Block(0,methodStmt.toList());

                    jcMethodDecl.body = methodBody;

                    super.visitMethodDef(jcMethodDecl);
                }
            });
        });

        return true;
    }

    private JCTree.JCStatement findAndChangeReturn2Exec(JCTree.JCStatement statement, Name selfMethodName, List<JCTree.JCVariableDecl> parameters) {
        //有些没有else语句的就是返回null的
        if(statement == null){
            return null;
        }

        //如果是IF判断分支，递归深入直到return为止
        if(statement.getKind().equals(Tree.Kind.IF)){
            JCTree.JCIf jcIf = (JCTree.JCIf) statement.getTree();
            JCTree.JCExpression cond = jcIf.cond;
            JCTree.JCStatement thenPart = findAndChangeReturn2Exec(jcIf.thenpart,selfMethodName,parameters);
            JCTree.JCStatement elsePart = findAndChangeReturn2Exec(jcIf.elsepart,selfMethodName,parameters);
            return treeMaker.If(cond,thenPart,elsePart);
        }
        //如果是return，判断是否是调用自身的方法的递归
        else if(statement.getKind().equals(Tree.Kind.RETURN)){
            JCTree.JCReturn jcReturn = (JCTree.JCReturn) statement.getTree();
            JCTree.JCExpression returnExpr = jcReturn.getExpression();

            //如果是直接返回某个值
            if(returnExpr.getKind().equals(Tree.Kind.IDENTIFIER)){
                return statement;
            }
            //如果是返回方法调用
            if(returnExpr.getKind().equals(Tree.Kind.METHOD_INVOCATION)) {
                JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) returnExpr;
                JCTree.JCIdent methodSelect = (JCTree.JCIdent) methodInvocation.getMethodSelect();
                //返回调用的方法是自己
                if (methodSelect.getName().equals(selfMethodName)){
                    List<JCTree.JCExpression> arguments = methodInvocation.getArguments();
                    ListBuffer<JCTree.JCStatement> execStatemt = new ListBuffer<>();

                    for (int i = 0; i < parameters.size(); i++) {
                        JCTree.JCVariableDecl param = parameters.get(i);
                        JCTree.JCExpression arg = arguments.get(i);
                        JCTree.JCExpressionStatement exprStmt = treeMaker.Exec(
                                treeMaker.Assign(
                                        treeMaker.Ident(param.getName()),
                                        arg
                                )
                        );
                        execStatemt.append(exprStmt);
                    }
                    //加个continue能防止嵌套时的继续
                    execStatemt.append(treeMaker.Continue(null));
                    JCTree.JCBlock block = treeMaker.Block(0,execStatemt.toList());
                    return block;
                }
            }
        }
        //如果是代码块，遍历这个代码块，找到return为止
        else if(statement.getKind().equals(Tree.Kind.BLOCK)){
            JCTree.JCBlock jcBlock = (JCTree.JCBlock) statement.getTree();
            ListBuffer<JCTree.JCStatement> newStmts = new ListBuffer<>();
            for (JCTree.JCStatement blockStatement : jcBlock.getStatements()) {
                JCTree.JCStatement newStmt = findAndChangeReturn2Exec(blockStatement,selfMethodName,parameters);
                newStmts.append(newStmt);
            }
            jcBlock.stats = newStmts.toList();
            return jcBlock;
        }

        //不是以上所有情况时
        return statement;
    }
}
