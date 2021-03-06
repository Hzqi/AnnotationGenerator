package com.jackywong.generator.example.tomapper;

import com.jackywong.generator.annotation.CreateMapper;
import com.jackywong.generator.annotation.ToMapper;

/**
 * Created by huangziqi on 2019/4/10
 */
@CreateMapper
public class User {
    private String name;
    private Integer age;

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
