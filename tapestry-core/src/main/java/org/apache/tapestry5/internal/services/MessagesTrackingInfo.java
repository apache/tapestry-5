// Copyright 2022 The Apache Software Foundation
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
package org.apache.tapestry5.internal.services;

import java.util.Objects;

import org.apache.tapestry5.commons.Resource;

/**
 * Class that holds information about a messages properties file for tracking.
 */
final public class MessagesTrackingInfo implements ClassNameHolder
{
    
    private Object bundleId;
    private Resource resource;
    private String className;

    public MessagesTrackingInfo(Resource resource, Object bundleId, String className) 
    {
        super();
        this.resource = resource;
        this.className = className;
        this.bundleId = bundleId;
    }
    
    public Object getBundleId() 
    {
        return bundleId;
    }
    
    public Resource getResource() 
    {
        return resource;
    }
    
    public String getClassName() 
    {
        return className;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bundleId, className, resource);
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
        MessagesTrackingInfo other = (MessagesTrackingInfo) obj;
        return Objects.equals(bundleId, other.bundleId)
                && Objects.equals(className, other.className)
                && Objects.equals(resource, other.resource);
    }

    @Override
    public String toString()
    {
        return "MessagesTrackingInfo [resource=" + resource + ", className=" + className
                + ", bundleId=" + bundleId + "]";
    }
    
}