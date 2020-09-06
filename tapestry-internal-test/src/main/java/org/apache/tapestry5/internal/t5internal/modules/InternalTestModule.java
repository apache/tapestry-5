package org.apache.tapestry5.internal.t5internal.modules;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.LibraryMapping;

/**
 * Provides the "t5internal" library, that provides common text utilities needed by some of the other
 * projects' integration tests. To normalize in-IDE development with command-line development, this module
 * is not set up to auto load via a manifest attribute, instead it is referenced via {@link org.apache.tapestry5.ioc.annotations.ImportModule}.
 *
 * @since 5.4
 */
public class InternalTestModule
{
    @Contribute(ComponentClassResolver.class)
    public static void provideT5InternalLibrary(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("t5internal", "org.apache.tapestry5.internal.t5internal"));
    }

}
