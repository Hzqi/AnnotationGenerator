package com.jackywong.generator.example.mylombok;

import com.jackywong.generator.annotation.ToMapper;

import java.util.Map;

/**
 * Created by huangziqi on 2019/4/4
 */
@ToMapper
public class MyPojo {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,Object> toMap(){return null;}
}
