package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.transform.ControlledPackageType;

public class AddTransformPagesToCISModule
{
    @Contribute(ComponentInstantiatorSource.class)
    public static void setupPackageForReload(MappedConfiguration<String, ControlledPackageType> configuration)
    {
        configuration.add("org.apache.tapestry5.internal.transform.pages", ControlledPackageType.COMPONENT);
    }
}
