// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.internal.bindings.PropBindingFactory;

/**
 * A source for {@link PropertyConduit}s, which can be thought of as a compiled property path
 * expression. PropertyConduits are the basis of the "prop:" binding factory, thus this service
 * defines the expression format used by the {@link PropBindingFactory}.
 * <p/>
 * The expression consist of one or more terms, seperated by periods. Each term may be either the
 * name of a JavaBean property, or the name of a method (a method that takes no parameters). Method
 * names are distinguished from property names by appending empty parens. Using a method term as the
 * final term will make the expression read-only.
 */
public interface PropertyConduitSource
{
    /**
     * Creates a new property conduit instance for the given expression.
     *
     * @param rootClass  the class of the root object to which the expression is applied
     * @param expression
     * @return RuntimeException if the expression is invalid (poorly formed, references non-existent
     *         properties, etc.)
     */
    PropertyConduit create(Class rootClass, String expression);
}
