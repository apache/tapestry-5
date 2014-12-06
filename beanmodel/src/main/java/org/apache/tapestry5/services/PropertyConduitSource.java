// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.PropertyConduit;

/**
 * A source for {@link org.apache.tapestry5.PropertyConduit}s, which can be thought of as a compiled property path
 * expression. PropertyConduits are the basis of the "prop:" binding factory, thus this service defines the expression
 * format used by the {@link org.apache.tapestry5.internal.bindings.PropBindingFactory}.
 */
public interface PropertyConduitSource
{
    /**
     * Returns a property conduit instance for the given expression. PropertyConduitSource caches the conduits it
     * returns, so despite the name, this method does not always create a <em>new</em> conduit. The cache is cleared if
     * a change to component classes is observed.
     * <p/>
     * Callers of this method should observe notifications from the {@link org.apache.tapestry5.services.InvalidationEventHub}
     * for {@link org.apache.tapestry5.services.ComponentClasses} and discard any aquired conduits; failure to do so
     * will create memory leaks whenever component classes change (the conduits will keep references to the old classes
     * and classloaders).
     *
     * @param rootType   the type of the root object to which the expression is applied
     * @param expression expression to be evaluated on instances of the root class
     * @return RuntimeException if the expression is invalid (poorly formed, references non-existent properties, etc.)
     */
    PropertyConduit create(Class rootType, String expression);
}
