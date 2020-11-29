package online.stringtek.toy.framework.toyspring.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Transactional {
}
