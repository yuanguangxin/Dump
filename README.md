# Dump - A lightweight web framework

## Info

本框架初衷是为了更好的学习理解 Spring 及其源码，不过由于实现的比较完整，目前开始逐渐完善成一个真正可以使用的轻量级框架。其中 web 部分和 ioc 部分的实现思路可参考：

* Web部分：[自己写个Spring MVC](https://zhuanlan.zhihu.com/p/139751932)
* IOC部分：[根据 Spring 源码写一个带有三级缓存的 IOC](https://zhuanlan.zhihu.com/p/144627581)

## Usage

### Maven

```xml
<dependency>
    <groupId>group.dump</groupId>
    <artifactId>Dump</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**Maven仓库正在上传中，若无法找到依赖，可clone本项目，自行打包到本地仓库中(在文件目录执行`mvn clean install`即可)**

### jar包下载

你可以 clone 本项目到你的路径下, 也可以导入 jar 包, [点此下载](https://pan.baidu.com/s/1jIvxiPS)。

### 配置文件

项目唯一需要你配置的就是 Dump 的配置文件, 配置文件名为`dump.properties`,位于根目录下,配置项如下。

```
# Parameters:
#      user                    The user to connect
#      password                The password to use when connecting
#      url                     A link - jdbc:subProtocol:otherStuff (with host, port, db_name)
#      driver                  JDBC driver path
#      max_conn                Maximum and initial number of connections in pool
#      min_conn                Minimum number of connections in pool

user=root
password=root
url=jdbc:mysql://localhost:3306/test?useSSL=false
driver=com.mysql.cj.jdbc.Driver
max_conn=10
min_conn=5
```

当然`web.xml`文件也要配置为 Dump 默认的 Servlet ,代码如下

```xml
<servlet>
    <servlet-name>dump</servlet-name>
    <servlet-class>com.dump.filter.DefaultFilter</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dump</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

好了,只需两个步骤,你已经完成了 Dump 所需的所有配置工作，下面来看看如何使用吧!

### Controller

和 Spring mvc 用法相同, 使用`@Controller`来表示它是一个控制器类,`@RequestMapping`用于匹配它的 url ,`@RequestParam`用于匹配表单的 name 值。当然你也可以直接传人实体类来自动填充属性值，但是需要用`@RequestModel`来修饰一下实体类。除了直接返回一个指定的视图，支持`@ResponseBody`类型以及重定向类型。

```java
@Controller
@RequestMapping("/test")
public class UserController {

    @Resource
    private UserService userService;

    @ResponseBody
    @RequestMapping("/testAjax")
    public String testAjax(@RequestModel User user) {
        userService.test();
        return JSON.toJSONString(user);
    }

    @RequestMapping("/testReturnUrl")
    public String testReturnUrl(@RequestParam("username") String username, @RequestParam(value = "tel", required = false, defaultValue = "110") String tel) {
        return "/success.html";
    }

    @RequestMapping("/testRedirect")
    public String testReturnUrl() {
        return "redirect:/success.html";
    }

}
```

### IOC

使用`@Component`和`@Resource`实现了管理 Bean，以及 Bean 属性的自动装配，完美解决循环依赖问题。目前仅支持按名装配，用法和 Spring 相同。

```java
@Component
public class UserService {

    @Resource
    private UserDao userDao;

    public void test(){
        userDao.test();
    }
}
```

### AOP

使用`@Aspect`,`@Before`,`@After`实现了 AOP 的切面功能，用法和 Spring 相同。

```
@Aspect
public class AspectTest {

    @Before("group.dump.test.testAop()")
    public void before(String arg) {
        System.out.println(arg);
    }

    @After("group.dump.test.testAop()")
    public void after(String arg) {
        System.out.println(arg);
    }
}
```

### ORM

对于对象关系映射, Dump 也提供了基本的函数操作, 包括增删改查和分页操作。相对复杂的查询工作还是依赖于 sql 语句，但再也不用你来创建对象了。基本函数有:`save()`,`delete()`, `update()`, `load()`, `getCount()`, `getAll()`, `selectBysql()`, `selectByPage()`。

```java
public class UserDao {
    public User login(User user){
        List<User> list = SessionFactory.getSession().selectBySql("username = ? and password = ?",User.class,user.getUsername(),user.getPassword());
        SessionFactory.getSession().save(user);//把user加入数据库中
        SessionFactory.getSession().load(User.class,2);//加载id为2的User(删除delete,更新update同理)
        SessionFactory.getSession().getAll(User.class);//得到数据库里user的所有映射类(getCount同理)
        SessionFactory.getSession().selectByPage("select * from user",User.class,int pageNo, int pageSize);//得到指定页制定大小的实体类列表
        if(list.size()!=0){
            return list.get(0);
        }else {
            return null;
        }
    }
}
```

### 逆向工程

Dump 支持自动填充 Model 层代码完成映射,只需要在任何位置运行以下代码:

```java
public class Test{
    public static void main(String[] args) throws Exception{
        //第一个参数为表名,第二个参数为生成的文件路径
        SqlCreator.createModel("student","com.model");
        SqlCreator.createModel("teacher","com.model");
    }
}
```

**以上为 Dump 的基本功能以及用法介绍，Dump 还有很多特性以及细节未能提及到。功能上 Dump 也会持续完善，欢迎持续关注。**