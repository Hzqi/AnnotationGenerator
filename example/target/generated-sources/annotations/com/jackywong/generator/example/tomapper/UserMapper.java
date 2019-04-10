package com.jackywong.generator.example.tomapper;

import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class UserMapper {
  public static Map<String, Object> toMap(Object obj) {
    Map<String,Object> map = new HashMap<>();
    map.put("name",((com.jackywong.generator.example.tomapper.User)obj).getName());
    map.put("age",((com.jackywong.generator.example.tomapper.User)obj).getAge());
    return map;
  }
}
