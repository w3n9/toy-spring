package online.stringtek.toy.framework.toyspring.factory;

public interface BeanFactory {
    Object getBean(String id);
    <T> T getBean(String id,Class<T> clazz);
}
