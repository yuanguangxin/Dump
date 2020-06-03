package group.dump.web.annotation;

import group.dump.web.constants.ValueConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yuanguangxin
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String value() default "";

    boolean required() default true;

    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
