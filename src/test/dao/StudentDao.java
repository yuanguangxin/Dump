package test.dao;

import com.dump.bean.annotation.Autowired;
import com.dump.orm.util.SqlCreater;
import com.dump.proxy.annotation.After;
import com.dump.proxy.annotation.Aspect;
import com.dump.proxy.annotation.Before;
import com.dump.proxy.annotation.Pointcut;

@Autowired
@Aspect
public class StudentDao {
    @Pointcut
    public void test(String a){
        System.out.println("testDao");
    }

    @Before
    public void before(String a){
        System.out.println("before in dao");
    }

    @After
    public void after(String a){
        System.out.println("after in dao");
    }

    public static void main(String[] args) throws Exception{
        //第一个参数为表名,第二个参数为生成的文件路径
        SqlCreater.createModel("student","com.model");
        SqlCreater.createModel("teacher","com.model");
    }
}
