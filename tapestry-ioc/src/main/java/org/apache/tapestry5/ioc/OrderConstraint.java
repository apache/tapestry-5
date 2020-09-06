// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.ioc;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.commons.OrderedConfiguration;

/**
 * Represents an order constraints for {@link OrderedConfiguration}.
 * 
 * @since 5.2.0.0
 */
public class OrderConstraint
{
    private static final String ALL = "*";
    
    private List<String> constraints = new ArrayList<String>();
    
    /**
     * Adds an <i>after:id</i> constraint.
     */
    public OrderConstraint after(String id)
    {
        constraints.add("after:" + id);
        
        return this;
    }
    
    /**
     * Adds an <i>after:*</i> constraint.
     */
    public OrderConstraint afterAll()
    {
        return after(ALL);
    }
    /**
     * Adds a <i>before:id</i> constraint.
     */
    public OrderConstraint before(String id)
    {
        constraints.add("before:" + id);
        
        return this;
    }
    
    /**
     * Adds a <i>before:*</i> constraint.
     */
    public OrderConstraint beforeAll()
    {
        return before(ALL);
    }
    
    /**
     * Returns all constraints as array of strings.
     */
    public String[] build()
    {
        return constraints.toArray(new String[]{});
    }
}
