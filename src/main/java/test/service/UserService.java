package test.service;

import test.dao.UserDao;

public class UserService {
    public UserDao userDao;

    public void setuserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void test(){
        userDao.test();
        System.out.println("UserService...");
    }
}
