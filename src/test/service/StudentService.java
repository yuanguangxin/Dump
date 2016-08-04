package test.service;

import com.dump.bean.annotation.Autowired;
import test.dao.StudentDao;

@Autowired
public class StudentService {
    private StudentDao studentDao;

    public void setStudentDao(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    public void test(){
        studentDao.test("");
    }
}
