// Copyright 2011, 2012, 2022 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.services.ClassNameHolder;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.slf4j.Logger;

public class ResourceChangeTrackerImpl extends InvalidationEventHubImpl implements ResourceChangeTracker,
        UpdateListener
{
    private final URLChangeTracker<ResourceInfo> tracker;
    
    private final ThreadLocal<String> currentClassName;
    
    private final Logger logger;

    /**
     * Used in production mode as the last modified time of any resource exposed to the client. Remember that
     * all exposed assets include a URL with a version number, and each new deployment of the application should change
     * that version number.
     */
    private final long fixedLastModifiedTime = Math.round(System.currentTimeMillis() / 1000d) * 1000L;

    public ResourceChangeTrackerImpl(ClasspathURLConverter classpathURLConverter,
                                     @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                     boolean productionMode, Logger logger)
    {
        super(productionMode, logger);
        this.logger = logger;

        // Use granularity of seconds (not milliseconds) since that works properly
        // with response headers for identifying last modified. Don't track
        // folder changes, just changes to actual files.
        tracker = productionMode ? null : new URLChangeTracker<ResourceInfo>(classpathURLConverter, true, false);
        currentClassName = productionMode ? null : new ThreadLocal<>();
    }
    
    @PostInjection
    public void registerWithUpdateListenerHub(UpdateListenerHub hub)
    {
        hub.addUpdateListener(this);
    }


    public long trackResource(Resource resource)
    {
        if (tracker == null)
        {
            return fixedLastModifiedTime;
        }
        
        return tracker.add(resource.toURL(), new ResourceInfo(resource.toString(), currentClassName.get()));
    }

    public void addDependency(Resource dependency)
    {
        trackResource(dependency);
    }

    public void forceInvalidationEvent()
    {
        fireInvalidationEvent();

        if (tracker != null)
        {
            tracker.clear();
        }
    }

    public void checkForUpdates()
    {
        if (tracker != null)
        {
            final Set<ResourceInfo> changedResources = tracker.getChangedResourcesInfo();
            if (!changedResources.isEmpty())
            {
                logger.info("Changed resources: {}", changedResources.stream()
                        .map(ResourceInfo::getResource)
                        .collect(Collectors.joining(", ")));
            }
            
            boolean applicationLevelChange = false;
            
            for (ResourceInfo info : changedResources) 
            {
                
                // An application-level file was changed, so we need to invalidate everything.
                if (info.getClassName() == null)
                {
                    forceInvalidationEvent();
                    applicationLevelChange = true;
                    break;
                }
                    
            }
            
            if (!changedResources.isEmpty() && !applicationLevelChange)
            {
                List<String> resources = new ArrayList<>(4);
                resources.addAll(changedResources.stream()
                        .filter(Objects::nonNull)
                        .map(ResourceInfo::getResource)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
                resources.addAll(changedResources.stream()
                        .filter(Objects::nonNull)
                        .map(ClassNameHolder::getClassName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
                fireInvalidationEvent(resources);
            }
        }
    }

    @Override
    public void setCurrentClassName(String className) 
    {
        if (currentClassName != null)
        {
            currentClassName.set(className);
        }
    }

    @Override
    public void clearCurrentClassName() 
    {
        currentClassName.set(null);
    }
    
    private static class ResourceInfo implements ClassNameHolder
    {
        private String resource;
        private String className;

        public ResourceInfo(String resource, String className) 
        {
            super();
            this.className = className;
            this.resource = resource;
        }

        @Override
        public int hashCode() 
        {
            return Objects.hash(className, resource);
        }

        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ResourceInfo other = (ResourceInfo) obj;
            return Objects.equals(className, other.className) && Objects.equals(resource, other.resource);
        }

        @Override
        public String toString() {
            return "ResourceInfo [path=" + resource + ", className=" + className + "]";
        }
        
        public String getResource()
        {
            return resource;
        }
        
        public String getClassName() 
        {
            return className;
        }
                
    }
    
}
