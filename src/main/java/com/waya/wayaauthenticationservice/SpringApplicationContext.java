package com.waya.wayaauthenticationservice;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        CONTEXT = context;
    }

    public static Object getBean(String beanName) {
        return CONTEXT == null ? null : CONTEXT.getBean(beanName);
    }

    public static <T> T getBean(Class<T> beanClass) {
        return CONTEXT == null ? null : CONTEXT.getBean(beanClass);
    }

}