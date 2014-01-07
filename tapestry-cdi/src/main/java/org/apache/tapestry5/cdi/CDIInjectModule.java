// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.cdi;

import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.services.transform.InjectionProvider2;

/**
 * This module provides an InjectionProvider and an ObjectProvider to handle CDI beans
 *
 */
public final class CDIInjectModule {
	
	public static void bind(ServiceBinder binder) {
		binder.bind(ObjectProvider.class, CDIObjectProvider.class);
	} 
	
	@Contribute(value=MasterObjectProvider.class)
	public static void provideMasterObjectProvider(
			@Local ObjectProvider cdiProvider,
			OrderedConfiguration<ObjectProvider> configuration) {
		configuration.add("cdiProvider", cdiProvider, "after:*");
	}
	
	@Contribute(InjectionProvider2.class)
	public static void provideStandardInjectionProviders(final OrderedConfiguration<InjectionProvider2> configuration) {
		configuration.addInstance("CDI", CDIInjectionProvider.class, "before:*");
	}
}
