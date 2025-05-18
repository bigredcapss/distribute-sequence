package distributesequence.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * @author peanut
 * @description SpringContextHolder
 */
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        Assert.notNull(applicationContext, "applicationContext属性未注入");
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        applicationContext = appContext;
    }

    public static <T> T getBean(String name) {
        Assert.notNull(applicationContext, "applicationContext属性未注入");
        return (T) applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) {
        Assert.notNull(applicationContext, "applicationContext属性未注入");
        return applicationContext.getBean(requiredType);
    }
}