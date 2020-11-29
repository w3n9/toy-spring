package online.stringtek.toy.framework.toyspring.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repository {
    String value() default "";
}
