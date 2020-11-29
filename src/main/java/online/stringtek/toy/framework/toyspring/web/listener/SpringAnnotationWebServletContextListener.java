package online.stringtek.toy.framework.toyspring.web.listener;


import online.stringtek.toy.framework.toyspring.factory.JavaAnnotationConfigApplicationContext;
import online.stringtek.toy.framework.toyspring.util.PackageUtil;
import online.stringtek.toy.framework.toyspring.util.WebApplicationContextUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

public class SpringAnnotationWebServletContextListener implements ServletContextListener {
    private final String basePackage;
    private final String webInfPath="WEB-INF"+ File.separator+"classes"+File.separator;
    public SpringAnnotationWebServletContextListener(String basePackage) {
        this.basePackage = basePackage;
    }
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JavaAnnotationConfigApplicationContext ctx;
        ctx = new JavaAnnotationConfigApplicationContext(basePackage);
        WebApplicationContextUtil.setCtxBeanFactoryMap(sce.getServletContext().getContextPath(),ctx);
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
