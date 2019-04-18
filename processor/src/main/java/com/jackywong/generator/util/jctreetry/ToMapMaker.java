package com.jackywong.generator.util.jctreetry;

import com.jackywong.generator.annotation.ToMapper;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by huangziqi on 2019/4/18
 */
public class ToMapMaker extends Maker{

    public ToMapMaker(JavacTrees trees, TreeMaker treeMaker, Names names) {
        super(trees, treeMaker, names);
    }

    /**
     * 创建toMap方法
     * @param var2method
     * @return
     */
    public JCTree.JCMethodDecl createToMapMethod(Map<String, JCTree.JCMethodDecl> var2method){
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        List<JCTree.JCExpression> typeArgs = createTypeargs("String","Object"); //<String,Object>
        JCTree.JCTypeApply typeApply = createTypeApply("java.util.Map",typeArgs);           //Map<String,Object>
        JCTree.JCTypeApply objectTypeApply = createTypeApply("java.util.HashMap",List.nil());//HashMap<>
        JCTree.JCNewClass newClass = createNewClass(objectTypeApply,null);           //new HashMap<>()
        JCTree.JCVariableDecl mapVar = createVarAssigInit("map",typeApply,newClass);//Map<String,Object> map = new HashMap<>()
        statements.append(mapVar);

        var2method.forEach((name,jcMethodDecl) -> {
            ListBuffer<JCTree.JCExpression> putArgs = new ListBuffer<>();
            JCTree.JCLiteral key = treeMaker.Literal(TypeTag.CLASS,name);
            //this.getXxx()
            JCTree.JCMethodInvocation value = createMethodInvocation("this",jcMethodDecl.getName().toString(),List.nil());
            putArgs.append(key);
            putArgs.append(value);
            //map.put(xxx,this.getXxx)
            JCTree.JCMethodInvocation putApply = createMethodInvocation("map","put",putArgs.toList());

            statements.append(
                    treeMaker.Exec(
                            putApply
                    )
            );
        });
        statements.append(
                treeMaker.Return(
                        treeMaker.Ident(names.fromString("map"))
                )
        );

        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("toMap"),
                typeApply,
                List.nil(),                                 //范型参数
                List.nil(),                                 //参数
                List.nil(),                                 //throw表达式
                body,                                       //方法内表达式
                null
        );
        return methodDecl;
    }

    /**
     * 将变量和对应的getter方法用map封装
     * @param variableDecls
     * @param methodDecls
     * @return
     */
    public Map<String, JCTree.JCMethodDecl> makeVariable2Method(List<JCTree.JCVariableDecl> variableDecls, List<JCTree.JCMethodDecl> methodDecls) {
        Map<String, JCTree.JCMethodDecl> var2method = new HashMap<>();

        for (JCTree.JCVariableDecl variableDecl : variableDecls) {
            String varName = variableDecl.getName().toString();
            String getterNameSuffix = (varName.charAt(0)+"").toUpperCase() + varName.substring(1);
            String getterName = "get"+getterNameSuffix;

            //查找对应的getter方法并放入map中
            methodDecls.stream()
                    .filter(jcMethodDecl ->
                            jcMethodDecl.getName().toString().equals(getterName))
                    .findFirst()
                    .filter(jcMethodDecl -> {
                        boolean isMethodPublic = jcMethodDecl.getModifiers().getFlags().contains(Modifier.PUBLIC);
                        boolean isMethodSameReturn = jcMethodDecl.getReturnType().toString()
                                .equals(variableDecl.vartype.toString());
                        boolean isMethodNoParam = jcMethodDecl.getParameters().isEmpty();
                        return (isMethodNoParam && isMethodSameReturn && isMethodPublic);
                    }).ifPresent(jcMethodDecl ->
                        var2method.put(variableDecl.getName().toString(),jcMethodDecl)
                    );
        }
        return var2method;
    }
}
