package Spring;

/**
 *该类用于记录每一个再配置文件中注册的bean
 * 记录他们的类名和id，方便后续操作
 */
public class MyBean {
    private String id;
    private String className;

    public MyBean(String id, String className) {
        this.id = id;
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
