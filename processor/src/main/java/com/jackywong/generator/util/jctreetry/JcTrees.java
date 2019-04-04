package com.jackywong.generator.util.jctreetry;

import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Set;

/**
 * Created by huangziqi on 2019/4/4
 *
 * 简单封装JCTree
 * Created by bbhou on 2017/10/12.
 */
public class JcTrees {
    /**
     * 构造器名称
     */
    public static final String CONSTRUCTOR_NAME = "<init>";


    /**
     * 是否为构造器
     * @param jcMethodDecl
     * @return
     */
    public static boolean isConstructor(JCTree.JCMethodDecl jcMethodDecl) {
        String name = jcMethodDecl.name.toString();
        if(CONSTRUCTOR_NAME.equals(name)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为共有方法
     * @param jcMethodDecl
     * @return
     */
    public static boolean isPublicMethod(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers jcModifiers = jcMethodDecl.getModifiers();
        Set<Modifier> modifiers =  jcModifiers.getFlags();
        if(modifiers.contains(Modifier.PUBLIC)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为私有方法
     * @param jcMethodDecl
     * @return
     */
    public static boolean isPrivateMethod(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers jcModifiers = jcMethodDecl.getModifiers();
        Set<Modifier> modifiers =  jcModifiers.getFlags();
        if(modifiers.contains(Modifier.PRIVATE)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为无参方法
     * @param jcMethodDecl
     * @return
     */
    public static boolean isNoArgsMethod(JCTree.JCMethodDecl jcMethodDecl) {
        List<JCTree.JCVariableDecl> jcVariableDeclList = jcMethodDecl.getParameters();
        if(jcVariableDeclList == null
                || jcVariableDeclList.size() == 0) {
            return true;
        }
        return false;
    }
}
