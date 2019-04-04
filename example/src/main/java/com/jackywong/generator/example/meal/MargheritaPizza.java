package com.jackywong.generator.example.meal;


import com.jackywong.generator.annotation.factory.Factory;

/**
 * Created by huangziqi on 2019/4/2
 */
@Factory(
        id = "Margherita",
        type = Meal.class
)
public class MargheritaPizza implements Meal {
    @Override
    public float getPrice() {
        return 8.5f;
    }
}
