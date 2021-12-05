package Spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)//表示注解应用在属性上
@Retention(RetentionPolicy.RUNTIME)//表示该注解在运行期间内也可以被获取到
public @interface Resource {

}
