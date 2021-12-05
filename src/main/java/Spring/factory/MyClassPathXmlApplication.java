package Spring.factory;

import Spring.MyBean;
import Spring.annotation.Resource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * 工厂接口的实现类
 * 获取xml文件，用dom4j解析文件
 * 解析后，得到id和class的键值对，用反射机制把bean创建，并放进HashMap中，方便getBean
 */
public class MyClassPathXmlApplication implements MyFactory{
    public HashMap<String,Object> beanMap;
    public MyClassPathXmlApplication(String filename) {
        //把配置文件传入，进行解析并实例化bean对象
        this.parseXml(filename);
        //在装载完成beanMap后，进行自动注入
        this.autoInjection(filename);
    }

    private void autoInjection(String filename) {
        try {
            //先来判读是否需要自动注入
            SAXReader saxReader=new SAXReader();
            URL url=this.getClass().getClassLoader().getResource(filename);
            Document document= saxReader.read(url);
            XPath xPath=document.createXPath("beans/annotation-config");
            List<Element> annotations=xPath.selectNodes(document);
            if(annotations.size()==0){//没有标签，不需要自动注入，直接返回
                return;
            }
            /**
             * 开始自动注入
             */
            /**
             * 思路：
             * 遍历所有配置文件中的类，检查有没有需要依赖注入的类
             * 当检查到它的某个属性需要依赖注入的时候，递归地检查它的那个需要依赖注入的属性的类
             * 递归地检查完一个属性之后，才能将其从beanmap中提取出来并注入进去
             * 依次为所有的类进行依赖注入。
             * 每次递归结束后，更新beanmap，并且当注入完成后，要修改注解，表示这个属性已经完成注入
             * 注解寻找被注入的bean对象的方法：
             * 通过id寻找，该属性的名字是否有对应id
             * 如果没有对应id，则通过类来寻找
             */
            xPath=document.createXPath("beans/bean");
            List<Element> beans=xPath.selectNodes(document);
            for(int i=0;i<beans.size();i++){
                /**
                 * 开始检查所有注册了的类里面是否有需要自动注入的属性
                 */
                Class thisClass=Class.forName(beans.get(i).attributeValue("class"));
                //遍历所有的属性，并查看是否有Resource注解
                inject(thisClass,beans.get(i).attributeValue("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void inject(Class thisClass,String id){
        try{
            Object obj=thisClass.newInstance();
            Field[] fields=thisClass.getDeclaredFields();
            for(Field field:fields){
                Annotation[] annoations=field.getAnnotations();
                for(Annotation annotation:annoations){
                    if(annotation.annotationType()==Class.forName("Spring.annotation.Resource")){
                        //检测到了resource注解，进行递归地注入
                        inject(fields.getClass(),field.getName());//把需要注入的这个属性先收拾好
                        String injectedid=field.getName();//然后再把它从map里面拿出来，进行注入
                        Object injectedobj=beanMap.get(injectedid);//得到需要注入的属性
                        //进行注入，设置该属性的值
                        field.set(obj,injectedobj);
                        //注入完成，此时无需再次查询注解，直接退出
                        break;
                    }
                }
            }
            beanMap.put(id,obj);
        }catch (Exception e){

        }
    }
    private void parseXml(String filename){
        try{
            //解析配置文件
            SAXReader saxReader=new SAXReader();
            URL url=this.getClass().getClassLoader().getResource(filename);
            Document document=saxReader.read(url);
            XPath xPath=document.createXPath("beans/bean");
            List<Element> beans=xPath.selectNodes(document);
            if(beans!=null&&beans.size()!=0){
                beanMap=new HashMap<>();
                for(Element bean:beans){
                    /**
                     * 执行bean实例化
                     */
                    String id=bean.attributeValue("id");
                    String className=bean.attributeValue("class");
                    MyBean myBean=new MyBean(id,className);

                    Method[] methods;
                    Object obj=Class.forName(className).newInstance();;
                    XPath xPathProperty=document.createXPath("beans/bean[@id='"+id+"']/property");
                    List<Element> properties=xPathProperty.selectNodes(document);
                    if(properties.size()!=0){
                        /**
                         * 证明这个Bean使用了set方法注入，必然会有一个无参构造函数
                         */
                        /**
                         * 进行IOC的set方法注入
                         * 首先在本节点下找到property节点，然后实例化，并set进去
                         * 必须有set方法，才能成功注入，否则无效，即使在配置文件写了也没用
                         */
                        obj=Class.forName(className).newInstance();
                        methods = obj.getClass().getMethods();
                        for(int i=0;i<methods.length;i++){
                            for(Element property:properties){
                                if(("set"+property.attributeValue("name")).equals(methods[i].getName())){
                                    Class in=beanMap.get(property.attributeValue("ref")).getClass();
                                    Method m =  obj.getClass().getDeclaredMethod(methods[i].getName(), new Class[]{in});
                                    m.invoke(obj, new Object[]{beanMap.get(property.attributeValue("ref"))});
                                    break;
                                }
                            }
                        }
                    }
                    beanMap.put(id,obj);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object getBean(String id) {
        return beanMap.get(id);
    }
}
