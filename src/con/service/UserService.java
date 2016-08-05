package con.service;

import com.dump.bean.annotation.Autowired;
import con.dao.UserDao;
import con.model.User;

@Autowired
public class UserService {
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String register(User user, String again){
        return userDao.register(user,again);
    }

    public User login(User user) {
        return userDao.login(user);
    }

}
