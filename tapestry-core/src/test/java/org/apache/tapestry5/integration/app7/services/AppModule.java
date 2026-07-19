package org.apache.tapestry5.integration.app7.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.modules.Bootstrap4Module;

@ImportModule(Bootstrap4Module.class)
public class AppModule {

    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "hmac passphrase for testing");
    }
}
