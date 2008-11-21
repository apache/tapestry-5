// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import java.net.URL;

/**
 * Used by {@link org.apache.tapestry5.ioc.services.ClassNameLocator} to convert URLs from one protocol to another. This
 * is a hook for supporting OSGi, allowing "bundleresource" and "bundleentry" protocols to be converted to "jar:" or
 * "file:".
 */
public interface ClasspathURLConverter
{
    /**
     * Passed a URL provided by {@link ClassLoader#getResources(String)} to check to see if can be converted.
     *
     * @param url to check
     * @return the url, or an equivalent url of type jar: or file:
     */
    URL convert(URL url);
}
