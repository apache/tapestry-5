package t5.webresources.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptAggregationStrategy;
import org.apache.tapestry5.services.javascript.StackExtension;
import org.apache.tapestry5.services.javascript.StackExtensionType;
import org.apache.tapestry5.webresources.modules.WebResourcesModule;

@ImportModule(WebResourcesModule.class)
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
    public static void setupEnvironment(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
        configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
        configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "context:bootstrap");
    }

    @Contribute(JavaScriptStack.class)
    @Core
    public static void overrideBootstrapCSS(OrderedConfiguration<StackExtension> configuration)
    {
        // configuration.add("ForTestingOnly", StackExtension.javascriptAggregation(JavaScriptAggregationStrategy.DO_NOTHING));

        configuration.override("bootstrap.css",
                new StackExtension(StackExtensionType.STYLESHEET, "context:bootstrap/less/bootstrap.less"), "before:tapestry.css");
    }
}
