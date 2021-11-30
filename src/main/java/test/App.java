package test;

import Spring.factory.MyClassPathXmlApplication;
import org.dom4j.DocumentException;
import test.dao.UserDao;
import test.service.UserService;

import java.lang.reflect.InvocationTargetException;

public class App {
    public static void main(String[] args) throws DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        MyClassPathXmlApplication myClassPathXmlApplication=new MyClassPathXmlApplication("myspring.xml");
        UserService userService=(UserService) myClassPathXmlApplication.getBean("userService");
        userService.test();
    }
}
