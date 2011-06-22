// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5;

import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.corelib.components.Zone;

/**
 * Defines constants for common query parameters passed in requests from the client.
 * 
 * @see RequestParameter
 */
public class QueryParameterConstants
{
    /**
     * The client-side id of the element being updated in an Ajax request. This is very useful when writing
     * new content that may update the same zone dynamically, even when the {@link Zone} has allocated a dynamic
     * client-side id during initial render.
     */
    public static final String ZONE_ID = "t:zoneid";
}
