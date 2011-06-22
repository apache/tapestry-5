// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.Resource;

/**
 * An Asset is any kind of resource that can be exposed to the client web browser. Although quite often an Asset is a
 * resource in a web application's context folder, within Tapestry, Assets may also be resources on the classpath (i.e.,
 * packaged inside JARs).
 * <p/>
 * An Asset's toString() will return the URL for the resource (the same value as {@link #toClientURL()}).
 * <p/>
 * Release 5.1.0.0 introduced {@link org.apache.tapestry5.Asset2}, which extends this interface with an additional
 * method.
 */
public interface Asset
{
    /**
     * Returns a URL that can be passed, unchanged, to the client in order for it to access the resource. The same value
     * is returned from <code>toString()</code>.
     * <p/>
     * Tapestry's built-in asset types (context and classpath) always incorporate a version number as part of the path,
     * and alternate implementations are encouraged to do so as well. In addition, Tapestry ensures that context and
     * classpath assets have a far-future expires header (to ensure aggresive caching by the client).
     * <p/>
     */
    String toClientURL();

    /**
     * Returns the underlying Resource for the Asset.
     */
    Resource getResource();

}
