package online.stringtek.toy.framework.toyspring.factory;


import online.stringtek.toy.framework.toyspring.annotation.Autowired;


public interface ApplicationContext extends BeanFactory {
    //TODO

    <T> T getBean(Class<T> clazz);
}
