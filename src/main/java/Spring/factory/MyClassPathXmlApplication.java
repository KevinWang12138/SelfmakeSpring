package Spring.factory;

import Spring.MyBean;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                    Object obj=Class.forName(className).newInstance();

                    /**
                     * 进行IOC的set方法注入
                     * 首先在本节点下找到property节点，然后实例化，并set进去
                     */
                    XPath xPathProperty=document.createXPath("beans/bean[@id='"+id+"']/property");
                    Method[] methods = obj.getClass().getMethods();
                    List<Element> properties=xPathProperty.selectNodes(document);
                    for(int i=0;i<methods.length;i++){
                        for(Element property:properties){
                            if(("set"+property.attributeValue("name")).equals(methods[i].getName())){
                                Method m =  obj.getClass().getDeclaredMethod(methods[i].getName(), new Class[]{beanMap.get(property.attributeValue("ref")).getClass()});
                                m.invoke(obj, new Object[]{beanMap.get(property.attributeValue("ref"))});
                                break;
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
