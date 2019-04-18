package com.jackywong.generator.example;

import com.jackywong.generator.example.mylombok.MyPojo;
import com.jackywong.generator.example.tomapper.User;

import java.util.Date;
import java.util.Map;

/**
 * Created by huangziqi on 2019/4/2
 */
public class Main {
    public static void main(String[] args) {
        MyPojo myPojo = new MyPojo();
        myPojo.setName("polllobok");
        myPojo.setAge(0);
        User user = new User("polllobok",0);

        System.out.println(myPojo.toMap());
        System.out.println(user.toMap());
    }
}
