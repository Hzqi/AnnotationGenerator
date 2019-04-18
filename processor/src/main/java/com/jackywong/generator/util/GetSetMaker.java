package com.jackywong.generator.util;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

/**
 * Created by huangziqi on 2019/4/18
 */
public class GetSetMaker extends Maker {

    public GetSetMaker(JavacTrees trees, TreeMaker treeMaker, Names names) {
        super(trees, treeMaker, names);
    }

    public List<JCTree> createGetter(List<JCTree.JCVariableDecl> variableDecls) {
        ListBuffer<JCTree> getters = new ListBuffer<>();

        for (JCTree.JCVariableDecl variableDecl : variableDecls) {
            String varName = variableDecl.getName().toString();
            String getterNameSuffix = (varName.charAt(0)+"").toUpperCase() + varName.substring(1);
            String getterName = "get"+getterNameSuffix;

            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.append(
                    treeMaker.Return(
                            treeMaker.Select(
                                    treeMaker.Ident(names.fromString("this")),
                                    variableDecl.getName()
                            )
                    )
            );

            JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
            JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC),
                    names.fromString(getterName),
                    variableDecl.vartype,
                    List.nil(),                                 //范型参数
                    List.nil(),                                 //参数
                    List.nil(),                                 //throw表达式
                    body,                                       //方法内表达式
                    null
            );
            getters.append(methodDecl);
        }
        return getters.toList();
    }

    public List<JCTree> createSetter(List<JCTree.JCVariableDecl> variableDecls) {
        ListBuffer<JCTree> setters = new ListBuffer<>();

        for (JCTree.JCVariableDecl variableDecl : variableDecls) {
            String varName = variableDecl.getName().toString();
            String setterNameSuffix = (varName.charAt(0)+"").toUpperCase() + varName.substring(1);
            String setterName = "set"+setterNameSuffix;

            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.append(
                    treeMaker.Exec(
                            treeMaker.Assign(
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("this")),
                                            variableDecl.getName()
                                    ),
                                    treeMaker.Ident(variableDecl.getName())
                            )
                    )
            );

            JCTree.JCVariableDecl parameter = createMethodParam(variableDecl.getName().toString(),variableDecl.vartype);
            ListBuffer<JCTree.JCVariableDecl> params = new ListBuffer<>();
            params.append(parameter);

            JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
            JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC),
                    names.fromString(setterName),
                    null,
                    List.nil(),                                 //范型参数
                    params.toList(),                                 //参数
                    List.nil(),                                 //throw表达式
                    body,                                       //方法内表达式
                    null
            );
            setters.append(methodDecl);
        }
        return setters.toList();
    }
}
