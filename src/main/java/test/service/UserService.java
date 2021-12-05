package test.service;

import Spring.annotation.Resource;
import test.dao.UserDao;

public class UserService {
    @Resource
    public UserDao userDao;

    public void setuserDao(UserDao userDao) {
        this.userDao = userDao;
    }
    public void test(){
        userDao.test();
        System.out.println("UserService...");
    }
}
