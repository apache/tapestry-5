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

import org.apache.tapestry5.commons.OrderedConfiguration;

/**
 * Constructs order constraints for {@link OrderedConfiguration}.
 * 
 * @since 5.2.0.0
 */
public final class OrderConstraintBuilder
{
    /**
     * Adds an <i>after:id</i> constraint.
     */
    public static OrderConstraint after(String id)
    {
        return new OrderConstraint().after(id);
    }
    
    /**
     * Adds an <i>after:*</i> constraint.
     */
    public static OrderConstraint afterAll()
    {
        return new OrderConstraint().afterAll();
    }
    
    /**
     * Adds a <i>before:id</i> constraint.
     */
    public static OrderConstraint before(String id)
    {
        return new OrderConstraint().before(id);
    }
    
    /**
     * Adds a <i>before:*</i> constraint.
     */
    public static OrderConstraint beforeAll()
    {
        return new OrderConstraint().beforeAll();
    }
}
