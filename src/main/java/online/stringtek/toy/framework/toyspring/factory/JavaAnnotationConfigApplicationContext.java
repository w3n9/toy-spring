package online.stringtek.toy.framework.toyspring.factory;


import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import online.stringtek.toy.framework.toyspring.annotation.Autowired;
import online.stringtek.toy.framework.toyspring.annotation.Repository;
import online.stringtek.toy.framework.toyspring.annotation.Service;
import online.stringtek.toy.framework.toyspring.annotation.Transactional;
import online.stringtek.toy.framework.toyspring.tx.TransactionManager;
import online.stringtek.toy.framework.toyspring.util.PackageUtil;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class JavaAnnotationConfigApplicationContext implements ApplicationContext{

    //根据id存储的BeanMap
    private final Map<String,Object> idBeanMap=new HashMap<>();
    //根据
    private final Map<Class<?>,Object> typeBeanMap=new HashMap<>();
    //使用二级Map处理循环依赖
    private final Map<String,Object> inCompletedIdBeanMap  =new HashMap<>();
    private final Map<Class<?>,Object> inCompletedTypeBeanMap  =new HashMap<>();
    private final Map<String,Class<?>> classNameMap=new HashMap<>();

    private List<Class<?>> classes;
    public JavaAnnotationConfigApplicationContext(String basePackage) {
        try {
            scan(basePackage);
            mapClass();
            init();
            proxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proxy() {
        for (Map.Entry<String, Object> entry : idBeanMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Transactional annotation = value.getClass().getAnnotation(Transactional.class);
            if(annotation!=null){
                TransactionManager tx = (TransactionManager)typeBeanMap.get(TransactionManager.class);
                if(tx!=null){
                    Class<?>[] interfaces = value.getClass().getInterfaces();
                    if(interfaces.length==0){//使用CGLib
                        Enhancer enhancer=new Enhancer();
                        enhancer.setSuperclass(value.getClass());
                        enhancer.setCallback((MethodInterceptor) (obj, method, objects, methodProxy) -> {
                            Object result=null;
                            try{
                                tx.beginTransaction();
                                result= method.invoke(value, objects);
                                tx.commit();
                            }catch (InvocationTargetException e){
                                tx.rollback();
                                throw e.getTargetException();
                            }
                            return result;
                        });
                        Object proxy = enhancer.create();
                        typeBeanMap.put(value.getClass(),proxy);
                        idBeanMap.put(key,proxy);
                    }else{//使用JDK动态代理
                        Object proxy= Proxy.newProxyInstance(value.getClass().getClassLoader(), interfaces, (proxyObj, method, args) -> {
                            Object result=null;
                            try{
                                tx.beginTransaction();
                                result=method.invoke(value,args);
                                tx.commit();
                            }catch (InvocationTargetException e){
                                tx.rollback();
                                throw e.getTargetException();
                            }
                            return result;
                        });
                        typeBeanMap.put(interfaces[0],proxy);
                        idBeanMap.put(key,proxy);
                    }
                }
            }
        }
    }

    private void scan(String basePackage) throws ClassNotFoundException {
        String packagePath = basePackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        String rootPath = getClass().getResource("/").getPath();
        String packageLocation=rootPath+packagePath;
        //扫描指定路径下所有的Class文件
        classes = PackageUtil.scan(new File(packageLocation), basePackage);
    }
    private void mapClass() {
        for (Class<?> clazz : classes) {
            classNameMap.put(clazz.getName(),clazz);
        }
    }

    private void init() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Class<?> clazz : classes) {
            load(clazz);
        }
    }

    private void load(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        System.out.println(clazz.getName());
        //TODO 处理没有clazz的情况
        Repository repositoryAnnotation = clazz.getAnnotation(Repository.class);
        Service serviceAnnotation = clazz.getAnnotation(Service.class);
        String beanId=null;
        if(repositoryAnnotation!=null){
            beanId=repositoryAnnotation.value();
        }
        if(serviceAnnotation!=null){
            beanId=serviceAnnotation.value();
        }
        //如果是Spring组件的话
        if(beanId!=null){
            //尚未存在
            //要求必须要有无参构造方法
            if("".equals(beanId)){
                beanId=clazz.getName();
            }
            if(idBeanMap.containsKey(beanId)||inCompletedIdBeanMap.containsKey(beanId)){
                //已经存在
                return;
            }else{
                Object obj = clazz.getDeclaredConstructor().newInstance();
                inCompletedIdBeanMap.put(beanId,obj);
                Class<?>[] interfaces = clazz.getInterfaces();
                if(interfaces.length==0){
                    inCompletedTypeBeanMap.put(clazz,obj);
                }else{
                    inCompletedTypeBeanMap.put(interfaces[0],obj);
                }
                //处理Autowired注入的字段,这里只允许Autowired加在了字段上
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    declaredField.setAccessible(true);
                    Autowired autowiredAnnotation= declaredField.getAnnotation(Autowired.class);
                    if(autowiredAnnotation!=null){
                        //在这里注入对象
                        Class<?> type = declaredField.getType();
                        Object autowiredObj = typeBeanMap.get(type);
                        if(autowiredObj==null){
                            autowiredObj = inCompletedTypeBeanMap.get(type);
                        }
                        //当前还未加载要Autowired的对象
                        if(autowiredObj==null){
                            load(type);
                        }
                        autowiredObj=typeBeanMap.get(type)==null?inCompletedTypeBeanMap.get(type):typeBeanMap.get(type);
                        declaredField.set(obj,autowiredObj);
                    }
                }
                //bean成品放入idBeanMap和typeBeanMap
                idBeanMap.put(beanId,obj);
                inCompletedIdBeanMap.remove(beanId);
                if(interfaces.length==0){
                    typeBeanMap.put(clazz,obj);
                    inCompletedTypeBeanMap.remove(clazz);
                }else{
                    typeBeanMap.put(interfaces[0],obj);
                    inCompletedTypeBeanMap.remove(interfaces[0]);
                }
            }
        }

    }



    @Override
    public Object getBean(String id) {
        return idBeanMap.get(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        return (T)getBean(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> clazz) {
        return (T)typeBeanMap.get(clazz);
    }
}
