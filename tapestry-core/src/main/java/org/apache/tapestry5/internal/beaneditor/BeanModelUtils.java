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

package org.apache.tapestry5.internal.beaneditor;

import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Utilities used in a few places to modify an existing {@link BeanModel}.
 */
public final class BeanModelUtils
{

    /**
     * Performs standard set of modifications to a {@link org.apache.tapestry5.beaneditor.BeanModel}. First new
     * properties may be added, then properties removed, then properties reordered.
     *
     * @param model                to modifiy
     * @param addPropertyNames     comma seperated list of property names to add, or null
     * @param includePropertyNames comma seperated list of property names to include
     * @param excludePropertyNames comma seperated list of property names to exclude, or null
     * @param reorderPropertyNames comma seperated list of property names to reorder, or null
     */
    public static void modify(BeanModel model, String addPropertyNames, String includePropertyNames,
                              String excludePropertyNames,
                              String reorderPropertyNames)
    {
        if (addPropertyNames != null) add(model, addPropertyNames);

        if (includePropertyNames != null) include(model, join(includePropertyNames, addPropertyNames));

        if (excludePropertyNames != null) exclude(model, excludePropertyNames);

        if (reorderPropertyNames != null) reorder(model, reorderPropertyNames);
    }

    private static final String join(String firstList, String optionalSecondList)
    {
        if (InternalUtils.isBlank(optionalSecondList)) return firstList;

        return firstList + "," + optionalSecondList;
    }

    /**
     * Adds empty properties to the bean model.  New properties are added with a <em>null</em> {@link
     * org.apache.tapestry5.PropertyConduit}. `
     *
     * @param model         to be modified
     * @param propertyNames comma-separated list of property names
     */
    public static void add(BeanModel model, String propertyNames)
    {
        for (String name : split(propertyNames))
        {
            model.add(name, null);
        }
    }

    /**
     * Removes properties from the bean model.
     *
     * @param model
     * @param propertyNames comma-separated list of property names
     * @see BeanModel#exclude(String...)
     */
    public static void exclude(BeanModel model, String propertyNames)
    {
        model.exclude(split(propertyNames));
    }

    /**
     * Selects a subset of the properties to keep, and reorders them.
     */
    public static void include(BeanModel model, String propertyNames)
    {
        model.include(split(propertyNames));
    }

    /**
     * Reorders properties within the bean model.
     *
     * @param model
     * @param propertyNames comma-separated list of property names
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
