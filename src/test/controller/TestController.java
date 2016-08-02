package test.controller;

import com.dump.filter.Controller;
import com.dump.filter.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class TestController {
    @RequestMapping("/a")
    public String login(String username, HttpServletRequest request){
        System.out.println(username);
        System.out.println("login");
        return "/index.jsp";
    }
}