package online.stringtek.toy.framework.toyspring.util;

import online.stringtek.toy.framework.toyspring.factory.BeanFactory;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebApplicationContextUtil {
    private static Map<String, BeanFactory> ctxBeanFactoryMap=new ConcurrentHashMap<>();
    public static BeanFactory getWebApplicationContext(String ctxName){
        return ctxBeanFactoryMap.get(ctxName);
    }
    public static BeanFactory getWebApplicationContext(ServletContext ctx){
        return getWebApplicationContext(ctx.getContextPath());
    }
    public static void setCtxBeanFactoryMap(String ctxName,BeanFactory beanFactory){
        ctxBeanFactoryMap.put(ctxName,beanFactory);
    }
}
