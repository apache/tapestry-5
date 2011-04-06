package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticManager;

public class PlasticProxyFactoryImpl implements PlasticProxyFactory
{
    private final PlasticManager manager;

    public PlasticProxyFactoryImpl(ClassLoader parentClassLoader)
    {
        manager = new PlasticManager(parentClassLoader);
    }

    public ClassLoader getClassLoader()
    {
        return manager.getClassLoader();
    }

    public ClassInstantiator createProxy(Class interfaceType, PlasticClassTransformer callback)
    {
        return manager.createProxy(interfaceType, callback);
    }

    public PlasticClassTransformation createProxyTransformation(Class interfaceType)
    {
        return manager.createProxyTransformation(interfaceType);
    }

}
