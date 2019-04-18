##AnnotationGenerator

这个`AnnotationGenerator`是本人在工作(互联网相关)中，
遇到要写很多VO的情况时想到的。

This `AnnotationGenerator` is my work (internet related).
I came up with the idea of writing a lot of VOs.

当时在想，每一次新增一个数据对象，需要新增一个视图对象VO，
觉得很麻烦，而且前端拿到的也就是JSON，按理说后端只要传Map或者直接数据对象就行(假设是POJO)。
所以当时在想有没有一种方式能直接转换POJO成Map（按照本人目前所知道的，各种JsonAPI由Map转应该比直接由POJO转性能要好，如果不是，还望大家告知我，纠正我一下）。

I was thinking that every time I added a data object, I needed to add a new view object (VO).
I feel very troublesome. And the front end gets JSON, it is reasonable to say that the back end only needs to pass `Map` or direct data object (assuming POJO).
So I was wondering if there was a way to directly convert POJOs into Maps. (As I know now, various JsonAPIs should be better converted from Maps than directly by POJOs. If not, I hope everyone will let me know and correct me. ).

* 第一种想到的方案就是每次都在新增的POJO里添加`toMap`的方法，弊端就是重复的代码工作。
* 第二种想到的是利用反射，每次传进一个对象，反射取里面的成员对象构造成Map。但是弊端就是反射的性能可能不太好。（这里本人没有做详细的性能测试，只是按照本人学识，通过反射取调用`getter`会比直接对象调用`getter`慢，如果不是，还望告知，纠正我一下。）
* 结合上面两个想法，目前想到最优的由POJO直接转成Map的方式就是在编译期自动生成toMap方法，弊端就是编译的时候可能会慢一点点。

* The first option is to add the `toMap` method to the newly added POJO every time. The drawback is the repeated code work.
* The second thing that comes to mind is to use reflection, each time an object is passed in, and the member object of the reflection is constructed as a Map. But the downside is that the performance of the reflection may not be very good. (I didn't do detailed performance testing here, just follow my knowledge. Calling `getter` by reflection will be slower than calling `getter` with the object directly. If not, I would like to inform and correct me.)
* Combining the above two ideas, the current way to convert directly from POJO to Map is to automatically generate the toMap method at compile time. The drawback is that it may be slower when compiling.

主要的实现方式就是利用Java的注解处理器`AnnotationProcessor`，对类添加一个Class级别(或Source级别)的注解，然后在编译的时候那些继承了AbstractProcessor的类就会被执行，
在执行时就通过Java原生API(非常底层的API，连Doc都找不到)，拿到`JCTree`（就是Java的语法树），往这个语法树中添加节点来添加方法。

The main implementation is to use Java's annotation processor `AnnotationProcessor` to add a Class level (or Source level) annotation to the class, and then those classes that inherit AbstractProcessor will be executed at compile time.
At the time of execution, the Java native API (very low-level API, even Doc can not be found), get `JCTree` (that is, the Java syntax tree), add nodes to the syntax tree to add methods.

###用法 Usage
目前一共有三类种注解：`@CreateMapper @MyLombok @ToMapper` 对应分别是：
* 通过生成源代码的方式来添加一个对应的类，里面有一个静态的toMap方法。（这个网上有很多教程的）
* 自动添加Getter Setter方法。（这个肯定没有著名的Lombok功能多，我也是参考学习，希望不要对比）
* 自动添加toMap方法

There are currently three annotations in class level: `@CreateMapper @MyLombok @ToMapper` corresponds to:
* Add a corresponding class by generating source code with a static toMap method. (This online has a lot of tutorials)
* Automatically add the Getter Setter method. (This is definitely not the famous Lombok function, I am also a reference learning, I hope not to compare)
* Automatically add toMap method

如源代码：(Such as source code:)

```java
@MyLombok
@ToMapper
public class MyPojo {
    private String name;
    private Integer age;
}
```
编译后的结果：(Compiled results:)

```java
@ToMapper
public class MyPojo {
    private String name;
    private Integer age;

    public void setAge(Integer age) {this.age = age;}

    public Integer getAge() {return this.age;}

    public void setName(String name) {this.name = name;}

    public String getName() {return this.name;}

    public MyPojo() {
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap();
        map.put("name", this.getName());
        map.put("age", this.getAge());
        return map;
    }
}
```
生成代码的源码：(Source code for generating code:)

```java
@CreateMapper
public class User {
    private String name;
    private Integer age;

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public Integer getAge() {return age;}

    public void setAge(Integer age) {this.age = age;}
}

```

编译时生成的源码:(Source code generated at compile time:)

```java
public class UserMapper {
  public static Map<String, Object> toMap(Object obj) {
    Map<String,Object> map = new HashMap<>();
    map.put("name",((com.jackywong.generator.example.tomapper.User)obj).getName());
    map.put("age",((com.jackywong.generator.example.tomapper.User)obj).getAge());
    return map;
  }
}
```

---
目前就这些东西，后面想到哪些更好的编译期工作再添加。

At the moment, these things are later added to the better compile-time work.