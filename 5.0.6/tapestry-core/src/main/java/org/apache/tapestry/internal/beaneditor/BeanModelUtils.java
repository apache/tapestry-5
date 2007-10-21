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

package org.apache.tapestry.internal.beaneditor;

import org.apache.tapestry.beaneditor.BeanModel;

/**
 * Utilities used in a few places to modify an existing {@link BeanModel}.
 */
public final class BeanModelUtils
{
    /**
     * Removes properties from the bean model.
     * 
     * @param model
     * @param propertyNames
     *            comma-separated list of property names
     * @see BeanModel#remove(String...)
     */
    public static void remove(BeanModel model, String propertyNames)
    {
        model.remove(split(propertyNames));
    }

    /**
     * Reorders properties within the bean model.
     * 
     * @param model
     * @param propertyNames
     *            comma-separated list of property names
     * @see BeanModel#reorder(String...)
     */
    public static void reorder(BeanModel model, String propertyNames)
    {
        model.reorder(split(propertyNames));
    }

    static String[] split(String propertyNames)
    {
        String trimmed = propertyNames.trim();

        if (trimmed.length() == 0) return new String[0];

        return trimmed.split("\\s*,\\s*");
    }
}
