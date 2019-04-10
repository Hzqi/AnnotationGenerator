package com.jackywong.generator.example.tomapper;

import com.jackywong.generator.annotation.ToMapper;

import java.util.Date;

/**
 * Created by huangziqi on 2019/4/10
 */
@ToMapper
public class Car {
    private String name;
    private Date date;

    public Car(String name, Date date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
