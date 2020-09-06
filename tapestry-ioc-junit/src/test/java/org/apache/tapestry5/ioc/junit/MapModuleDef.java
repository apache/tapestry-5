// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.junit;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;

/**
 * Test ModuleDef implementation based on a {@link Map}
 */
@SuppressWarnings("rawtypes")
public class MapModuleDef implements ModuleDef {
	private final Map<String, Object> map;

	public MapModuleDef(Map<String, Object> map) {
		super();
		this.map = map;
	}

	@Override
	public Class getBuilderClass() {
		return null;
	}

	@Override
	public Set<ContributionDef> getContributionDefs() {
		return Collections.emptySet();
	}

	@Override
	public Set<DecoratorDef> getDecoratorDefs() {
		return Collections.emptySet();
	}

	@Override
	public String getLoggerName() {
		return "MapModuleDef";
	}

	@Override
	public Set<String> getServiceIds() {
		return map.keySet();
	}

	@Override
	public ServiceDef getServiceDef(String serviceId) {
		return new MapServiceDef(map, serviceId);
	}

	public static class MapServiceDef implements ServiceDef {
		private final Map<String, Object> map;
		private final String serviceId;

		public MapServiceDef(Map<String, Object> map, String serviceId) {
			super();
			this.map = map;
			this.serviceId = serviceId;
		}

		@Override
		public ObjectCreator createServiceCreator(ServiceBuilderResources resources) {
			return new ObjectCreator() {
				@Override
				public Object createObject() {
					return map.get(serviceId);
				}
			};
		}

		@Override
		public String getServiceId() {
			return serviceId;
		}

		@Override
		public Set<Class> getMarkers() {
			return Collections.emptySet();
		}

		@Override
		public Class getServiceInterface() {
			return map.get(serviceId).getClass();
		}

		@Override
		public String getServiceScope() {
			return ScopeConstants.DEFAULT;
		}

		@Override
		public boolean isEagerLoad() {
			return false;
		}
	}
}
