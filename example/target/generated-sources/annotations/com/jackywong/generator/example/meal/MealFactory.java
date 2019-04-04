package com.jackywong.generator.example.meal;

import java.lang.String;

public class MealFactory {
  public Meal create(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is null!");
    }
    if ("Calzone".equals(id)) {
      return new com.jackywong.generator.example.meal.CalzonePizza();
    }
    if ("Margherita".equals(id)) {
      return new com.jackywong.generator.example.meal.MargheritaPizza();
    }
    if ("Tiramisu".equals(id)) {
      return new com.jackywong.generator.example.meal.Tiramisu();
    }
    throw new IllegalArgumentException("Unknown id = " + id);
  }
}
