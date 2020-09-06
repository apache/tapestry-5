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
package org.apache.tapestry5.internal.beanvalidator;

import org.apache.tapestry5.beanvalidator.ClientConstraintDescriptor;
import org.apache.tapestry5.json.JSONObject;

import static org.apache.tapestry5.commons.util.CollectionFactory.newSet;

import java.util.Set;

/**
 * Describes a single client-side constraint.
 */
public abstract class BaseCCD implements ClientConstraintDescriptor
{
    private final Class annotationClass;

    private final Set<String> attributes;

    /**
     * Creates a {@link BaseCCD}.
     *
     * @param annotationClass
     *         Type of the constraint annotation
     * @param attributes
     *         Attribute names of the constraint annotation to be passed (along with their values) to the JavaScript validator
     *         function as an {@link JSONObject}.
     */
    public BaseCCD(Class annotationClass, String... attributes)
    {
        this.annotationClass = annotationClass;
        this.attributes = newSet(attributes);
    }

    /**
     * Returns the annotation describing the constraint declaration.
     */
    @Override
    public Class getAnnotationClass()
    {
        return annotationClass;
    }


    /**
     * Attribute names of the constraint annotation to be passed (along with their values) to the JavaScript validator
     * function as an {@link JSONObject}.
     */
    @Override
    public Set<String> getAttributes()
    {
        return attributes;
    }
}