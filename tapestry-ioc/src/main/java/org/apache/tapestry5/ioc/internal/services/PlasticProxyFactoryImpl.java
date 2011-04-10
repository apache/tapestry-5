package org.apache.tapestry5.ioc.internal.services;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PlasticMethod;

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

    public <T> T createProxy(final Class<T> interfaceType, final ObjectCreator<T> creator,
            final Class<? extends T> annotationSource, final String description)
    {
        assert creator != null;
        assert InternalUtils.isNonBlank(description);

        ClassInstantiator instantiator = createProxy(interfaceType, new PlasticClassTransformer()
        {
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField objectCreatorField = plasticClass.introduceField(ObjectCreator.class, "creator")
                        .inject(creator);

                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(interfaceType.getName(), "delegate",
                        null, null);

                delegateMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().getField(objectCreatorField);
                        builder.invoke(ObjectCreator.class, Object.class, "createObject");
                        builder.checkcast(interfaceType).returnResult();
                    }
                });

                for (Method method : interfaceType.getMethods())
                {
                    plasticClass.introduceMethod(method).delegateTo(delegateMethod);
                }

                plasticClass.addToString(description);

                if (annotationSource != null)
                    plasticClass.copyAnnotations(annotationSource);
            }
        });

        return interfaceType.cast(instantiator.newInstance());
    }

}
