package con.dao;

import com.dump.bean.annotation.Autowired;
import com.dump.orm.Session;
import com.dump.proxy.annotation.After;
import com.dump.proxy.annotation.Aspect;
import com.dump.proxy.annotation.Before;
import com.dump.proxy.annotation.Pointcut;
import con.model.User;

import java.util.List;

@Autowired
@Aspect
public class UserDao {
    public String register(User user, String again){
        if(user.getPassword().equals(again)){
            List<User> list = Session.getSession().selectBysql("select * from user where username = ?",User.class,user.getUsername());
            if(list.size()!=0){
                return "exist";
            }else {
                Session.getSession().save(user);
                return "true";
            }
        }else {
            return "notsame";
        }
    }

    @Pointcut
    public User login(User user){
        System.out.println("login()");
        List<User> list = Session.getSession().selectBysql("select * from user where username = ? and password = ?",User.class,user.getUsername(),user.getPassword());
        if(list.size()!=0){
            return list.get(0);
        }else {
            return null;
        }
    }

    @Before
    public void before(User user){
        System.out.println(user.getUsername()+"正在登录");
    }

    @After
    public void after(User user){
        System.out.println("After login()");
    }
}
