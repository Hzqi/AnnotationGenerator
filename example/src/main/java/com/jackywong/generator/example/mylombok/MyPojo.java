package com.jackywong.generator.example.mylombok;

import com.jackywong.generator.annotation.MyLombok;
import com.jackywong.generator.annotation.ToMapper;

import java.util.Map;

/**
 * Created by huangziqi on 2019/4/4
 */
@MyLombok
@ToMapper
public class MyPojo {
    private String name;
    private Integer age;
}
