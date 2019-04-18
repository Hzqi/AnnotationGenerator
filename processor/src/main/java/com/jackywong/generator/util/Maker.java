package com.jackywong.generator.util;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

/**
 * Created by huangziqi on 2019/4/18
 * 自己封装了TreeMaker
 */
public class Maker {
    protected JavacTrees trees;
    //封装了创建AST节点的一些方法
    protected TreeMaker treeMaker;
    //提供了创建标识符的方法
    protected Names names;

    public Maker(JavacTrees trees, TreeMaker treeMaker, Names names) {
        this.trees = trees;
        this.treeMaker = treeMaker;
        this.names = names;
    }

    /**
     * 创建范型类型，如<String,Object>
     * @param typeNames 泛型的类型名称
     * @return
     */
    public List<JCTree.JCExpression> createTypeargs(String... typeNames) {
        ListBuffer<JCTree.JCExpression> typeArgs = new ListBuffer<>();
        for (String typeName : typeNames) {
            typeArgs.append(treeMaker.Ident(names.fromString(typeName)));
        }
        return typeArgs.toList();
    }

    /**
     * 创建(变量、方法)类型 如Map<String,Object>
     * @param typeName 类型名称
     * @param genericTypes 泛型类型列表
     * @return
     */
    public JCTree.JCTypeApply createTypeApply(String typeName, List<JCTree.JCExpression> genericTypes) {
        String[] typeNames = typeName.split("\\.");

        JCTree.JCExpression type = null;
        for (String name : typeNames) {
            if(type == null) type = treeMaker.Ident(names.fromString(name));
            else type = treeMaker.Select(type,
                    names.fromString(name)
            );
        }

        JCTree.JCTypeApply typeApply = treeMaker.TypeApply(
                type,
                genericTypes
        );
        return typeApply;
    }

    /**
     * 创建对象 如 new HashMap<String,Object>
     * @param classType 对象类型
     * @param params new对象参数
     * @return
     */
    public JCTree.JCNewClass createNewClass(JCTree.JCTypeApply classType, List<JCTree.JCExpression> params) {
        if(params == null) {
            params = List.nil();
        }
        JCTree.JCNewClass newClass = treeMaker.NewClass(
                null,
                List.nil(),
                classType, //对象类型
                params,    //new时的参数
                null);
        return newClass;
    }

    /**
     * 创建变量初始化赋值的语句，如 Map<String,Object> map = new HashMap<>()
     * @param varName 变量名
     * @param varType 变量类型
     * @param objectType 对象类型
     * @return
     */
    public JCTree.JCVariableDecl createVarAssigInit(String varName, JCTree.JCTypeApply varType, JCTree.JCNewClass objectType) {
        JCTree.JCVariableDecl varExpr = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.BLOCK),
                names.fromString("map"),
                varType,objectType
        );
        return varExpr;
    }

    /**
     * 创建调用表达式，如map.put("xxx","xxx")
     * @param varName
     * @param funcName
     * @param params
     * @return
     */
    public JCTree.JCMethodInvocation createMethodInvocation(String varName, String funcName,List<JCTree.JCExpression> params) {
        JCTree.JCMethodInvocation methodInvocation = treeMaker.Apply(
                List.nil(),                                             //没有类型参数
                treeMaker.Select(                                       //选择器map.put
                        treeMaker.Ident(names.fromString(varName)),
                        names.fromString(funcName)
                ),
                params
        );
        return methodInvocation;
    }
}
