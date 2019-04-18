package com.jackywong.generator.example.tomapper;

import com.jackywong.generator.annotation.MyLombok;
import com.jackywong.generator.annotation.ToMapper;

import java.util.Date;

/**
 * Created by huangziqi on 2019/4/18
 * 演示有警告信息的
 */
@MyLombok
@ToMapper
public class MyPojoPrivate {
    private String name;
    private Integer age;
    private Date date;
}
