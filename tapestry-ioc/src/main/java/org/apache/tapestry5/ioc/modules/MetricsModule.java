package org.apache.tapestry5.ioc.modules;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.internal.services.metrics.MetricCollectorImpl;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.metrics.MetricCollector;

@Marker(Builtin.class)
public class MetricsModule
{

    public static void bind(ServiceBinder binder)
    {
        binder.bind(MetricCollector.class, MetricCollectorImpl.class);
    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void provideDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(MetricsSymbols.RRD_DB_DIR, "");
    }
}
