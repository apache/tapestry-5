package org.apache.tapestry5.integration.appfolder.services

import org.apache.tapestry5.SymbolConstants
import org.apache.tapestry5.commons.MappedConfiguration
import org.apache.tapestry5.ioc.annotations.Contribute
import org.apache.tapestry5.ioc.services.ApplicationDefaults
import org.apache.tapestry5.ioc.services.SymbolProvider

class AppModule
{

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    static void applicationDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.PRODUCTION_MODE, false)
        configuration.add(SymbolConstants.APPLICATION_FOLDER, "t5app")
    }
}
