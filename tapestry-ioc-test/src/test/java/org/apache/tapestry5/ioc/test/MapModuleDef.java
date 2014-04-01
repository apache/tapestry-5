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

package org.apache.tapestry5.ioc.test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;

public class MapModuleDef implements ModuleDef {
	private final Map<String, Object> map;
	
    public MapModuleDef(Map<String, Object> map) {
		super();
		this.map = map;
	}
	public Class getBuilderClass() {
        return null;
    }
    public Set<ContributionDef> getContributionDefs() {
    	return Collections.emptySet();
    }
    public Set<DecoratorDef> getDecoratorDefs() {
    	return Collections.emptySet();
    }
    public String getLoggerName() {
    	return "MapModuleDef";
    }
    public Set<String> getServiceIds() {
    	return map.keySet();
    }
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

    	public ObjectCreator createServiceCreator(ServiceBuilderResources resources) {
    		return new ObjectCreator() {
    			public Object createObject() {
    				return map.get(serviceId);
    			}
    		};
    	}

    	public String getServiceId() {
    		return serviceId;
    	}

    	public Set<Class> getMarkers() {
    		return Collections.emptySet();
    	}

    	public Class getServiceInterface() {
    		return map.get(serviceId).getClass();
    	}

    	public String getServiceScope() {
    		return ScopeConstants.DEFAULT;
    	}

    	public boolean isEagerLoad() {
    		return false;
    	}
    }
}

