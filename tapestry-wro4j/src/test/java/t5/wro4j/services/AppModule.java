package t5.wro4j.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;
import org.apache.tapestry5.wro4j.modules.WRO4JModule;

@SubModule(WRO4JModule.class)
public class AppModule
{
    @Contribute(Compatibility.class)
    public static void disableOldStuff(MappedConfiguration<Trait, Boolean> configuration)
    {
        configuration.add(Trait.INITIALIZERS, false);
        configuration.add(Trait.SCRIPTACULOUS, false);
    }

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void enableJQuery(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
    }
}
