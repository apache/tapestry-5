package org.apache.tapestry5.internal;

import org.apache.tapestry5.services.BeanEditContext;

import java.lang.annotation.Annotation;

public class BeanEditContextImpl implements BeanEditContext
{
    private Class<?> beanClass;

    public BeanEditContextImpl(Class<?> beanClass)
    {
        this.beanClass = beanClass;
    }

    public Class<?> getBeanClass()
    {
        return beanClass;
    }

    public <T extends Annotation> T getAnnotation(Class<T> type)
    {
        return beanClass.getAnnotation(type);
    }

}
