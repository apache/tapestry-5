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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used to lookup meta data concerning a particular component. The primary source of meta data is the meta data defined
 * for the component, accessed via {@link org.apache.tapestry5.model.ComponentModel#getMeta(String)}. This includes meta
 * data defined by base classes. When meta-data for a particular component can not be found, a search works up the
 * containment hierarchy (to the component's container, and the container's container, and so on). If <em>that</em>
 * proves unfruitful, a system of defaults is provided by configuration and matched against the containing page's
 * logical name.
 * <p/>
 * Finally, if no metadata is available, then {@link org.apache.tapestry5.ioc.services.SymbolSource#valueForSymbol(String)}
 * is used to obtain a value. Generally speaking, if you are going to use this service to look up meta data, your should
 * also make a contribution to the FactoryDefaults service; otherwise, you risk a runtime exception if a meta-data key
 * can not be resolved.
 */
@UsesMappedConfiguration(String.class)
public interface MetaDataLocator
{
    /**
     * Searches for the value for the corresponding key.  The value, if located, will have symbols expanded, and will be
     * type coerced to the desired type.
     *
     * @param key       the key used to locate the meta data (case insensitive)
     * @param resources the resources of the initial component used in the search
     * @return the value if found (in the component, the component's container, etc. or via a folder default)
     * @throws RuntimeException if the value for the key is not present as meta data of the component, as an override,
     *                          or as a symbol
     */
    <T> T findMeta(String key, ComponentResources resources, Class<T> expectedType);

    /**
     * Searches for the value for the corresponding key for a particular page name (the page will <em>not</em> be
     * loaded).  The value, if located, will have symbols expanded, and will be type coerced to the desired type.
     *
     * @param key      the key used to locate the meta data (case insensitive)
     * @param pageName the name of the page to search
     * @return the value if found
     * @throws RuntimeException if the value for the key is not present as meta data of the page, as an override, or as
     *                          a symbol
     */
    <T> T findMeta(String key, String pageName, Class<T> expectedType);
}
