package com.jackywong.generator.example;

import com.jackywong.generator.example.jctreetry.UtilTest;
import com.jackywong.generator.example.mylombok.MyPojo;

/**
 * Created by huangziqi on 2019/4/2
 */
public class Main {
    public static void main(String[] args) {
        MyPojo myPojo = new MyPojo();
        myPojo.setName("abc");
        System.out.println(myPojo.getName());
    }
}
