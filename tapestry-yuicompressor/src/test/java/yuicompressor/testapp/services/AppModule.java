package yuicompressor.testapp.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.t5internal.modules.InternalTestModule;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.yuicompressor.services.YuiCompressorModule;

@SubModule({YuiCompressorModule.class, InternalTestModule.class})
public class AppModule
{
    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void setupConfiguration(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, true);
        configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
        configuration.add(SymbolConstants.PRODUCTION_MODE, false);
    }
}
