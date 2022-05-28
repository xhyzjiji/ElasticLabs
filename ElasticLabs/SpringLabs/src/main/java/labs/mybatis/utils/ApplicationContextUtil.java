package labs.mybatis.utils;

import org.springframework.context.ApplicationContext;

public class ApplicationContextUtil {
    private static ApplicationContext applicationContext;

    public static void setContext(ApplicationContext applicationContext) {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    public <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    public <T> T getBeanByType(Class<?> clazz) {
        return (T) applicationContext.getBean(clazz);
    }

    public String getActiveProfile() {
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }

}
