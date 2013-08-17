// Copyright 2010-2013  The Apache Software Foundation
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

import org.apache.tapestry5.SelectModel;

import java.util.List;

/**
 * Used to create an {@link org.apache.tapestry5.SelectModel}.
 * 
 * @since 5.2.0
 */
public interface SelectModelFactory
{
    /**
     * Creates a {@link org.apache.tapestry5.SelectModel} from a list of objects of the same type and a label property name.
     * The returned model creates for every object in the list a selectable option and relies on existing 
     * {@link org.apache.tapestry5.ValueEncoder} for the object type. The value of the label property is used as user-presentable label for the option.
     * 
     * @param objects objects to create model from
     * @param labelProperty property for the client-side value
     * @return the model
     */
    public SelectModel create(List<?> objects, String labelProperty);

    /**
     * Creates a {@link org.apache.tapestry5.SelectModel} from a list of objects of the same type.
     * 
     * The returned model creates for every object in the list a selectable option and relies on existing 
     * {@link org.apache.tapestry5.ValueEncoder} for the object type.
     * 
     * @param objects objects to create model from
     * @return the model
     * 
     * @since 5.4
     */
    public SelectModel create(List<?> objects);
}
