package group.dump.beans.annotation;

import java.lang.annotation.*;

/**
 * @author yuanguangxin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String value() default "";
}
