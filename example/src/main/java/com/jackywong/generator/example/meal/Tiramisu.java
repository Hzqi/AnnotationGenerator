package com.jackywong.generator.example.meal;


import com.jackywong.generator.annotation.Factory;

/**
 * Created by huangziqi on 2019/4/2
 */
@Factory(
        id = "Tiramisu",
        type = Meal.class
)
public class Tiramisu implements Meal {
    @Override
    public float getPrice() {
        return 4.5f;
    }
}
