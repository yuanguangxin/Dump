package test.controller;

import com.dump.filter.annotation.Controller;
import com.dump.filter.annotation.Param;
import com.dump.filter.annotation.RequestMapping;
import test.model.Student;

import javax.servlet.http.HttpServletRequest;

@Controller
public class TestController {
    @RequestMapping("/a")
    public String login(Student student, HttpServletRequest request){
        System.out.println(student);
        System.out.println(student.getId());
        System.out.println(student.getUsername());
        return "/index.jsp";
    }
}