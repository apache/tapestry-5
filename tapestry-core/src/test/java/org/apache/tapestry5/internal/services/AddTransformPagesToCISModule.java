package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.transform.ControlledPackageType;

public class AddTransformPagesToCISModule
{
    @Contribute(ComponentInstantiatorSource.class)
    public static void setupPackageForReload(MappedConfiguration<String, ControlledPackageType> configuration)
    {
        configuration.add("org.apache.tapestry5.internal.transform.pages", ControlledPackageType.COMPONENT);
    }

    /**
     * There used to be a t5internal module used for some tests and such, but that was refactored out in 5.4. Some
     * tests rely on it though.
     */
    @Contribute(ComponentClassResolver.class)
    public static void setupT5InternalLibrary(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("t5internal", "org.apache.tapestry5.internal.t5internal"));
    }

}
