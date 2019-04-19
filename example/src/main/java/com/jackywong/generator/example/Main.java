package com.jackywong.generator.example;

import com.jackywong.generator.example.mylombok.MyPojo;
import com.jackywong.generator.example.tailrec.TestTailRec;
import com.jackywong.generator.example.tomapper.User;
import com.jackywong.generator.example.tomapper.UserMapper;

import java.util.Date;
import java.util.Map;

/**
 * Created by huangziqi on 2019/4/2
 */
public class Main {
    public static void main(String[] args) {
        TestTailRec testTailRec = new TestTailRec();

        long res = testTailRec.fact(0,10);
        long res2 = testTailRec.factOld(0,10);
        System.out.println(res == res2);
    }
}
