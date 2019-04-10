package com.jackywong.generator.example.tomapper;

import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class CarMapper {
  public static Map<String, Object> toMap(Object obj) {
    Map<String,Object> map = new HashMap<>();
    map.put("name",((com.jackywong.generator.example.tomapper.Car)obj).getName());
    map.put("date",((com.jackywong.generator.example.tomapper.Car)obj).getDate());
    return map;
  }
}
