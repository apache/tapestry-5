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

/**
 * Class that holds information about a template for tracking.
 */
final public class TemplateTrackingInfo implements ClassNameHolder
{
    
    private String template;
    private String className;
    
    public TemplateTrackingInfo(String template, String className) 
    {
        super();
        this.template = template;
        this.className = className;
    }
    
    public String getTemplate() 
    {
        return template;
    }
    
    public String getClassName() 
    {
        return className;
    }
    
    @Override
    public int hashCode() 
    {
        return Objects.hash(className, template);
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TemplateTrackingInfo)) 
        {
            return false;
        }
        TemplateTrackingInfo other = (TemplateTrackingInfo) obj;
        return Objects.equals(className, other.className) && Objects.equals(template, other.template);
    }

    @Override
    public String toString() 
    {
        return "TemplateTrackingInfo [template=" + template + ", className=" + className + "]";
    }
    
}