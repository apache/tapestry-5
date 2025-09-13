package org.apache.tapestry5.clojure.tests;

import org.apache.tapestry5.clojure.ClojureBuilder;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;

public class TestModule
{
    @ApplicationDefaults
    @Contribute(SymbolProvider.class)
    public static void contributeTestApplicationDefaults(MappedConfiguration<String, String> conf)
    {
        conf.add(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "test");
    }

    public static Fixture buildFixture(ClojureBuilder builder)
    {
        return builder.build(Fixture.class);
    }
}
