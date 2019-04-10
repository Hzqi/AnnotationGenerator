package com.jackywong.generator.example.meal;


import com.jackywong.generator.annotation.Factory;

/**
 * Created by huangziqi on 2019/4/2
 */
@Factory(
        id = "Calzone",
        type = Meal.class
)
public class CalzonePizza implements Meal {
    @Override
    public float getPrice() {
        return 6.0f;
    }
}
