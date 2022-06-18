package org.apache.tapestry5.integration.app7.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.modules.Bootstrap4Module;

@ImportModule(Bootstrap4Module.class)
public class AppModule {

	@Contribute(SymbolProvider.class)
	@ApplicationDefaults
	public static void configureTapestry(MappedConfiguration<String, Object> conf) {
		conf.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
	}

}
