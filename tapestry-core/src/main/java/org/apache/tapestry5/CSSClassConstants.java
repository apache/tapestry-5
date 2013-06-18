// Copyright 2008-2013 The Apache Software Foundation
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

/**
 * Constants used when rendering a CSS class attribute.
 *
 * @deprecated Deprecated in 5.4 with no replacement.
 */
public class CSSClassConstants
{
    /**
     * CSS class name that causes a rendered element to be invisible on the client side.
     *
     * @deprecated Removed in Tapestry 5.4 with no replacement.
     */
    public static final String INVISIBLE = "t-invisible";

    /**
     * All purpose CSS class name for anything related to Tapestry errors.
     *
     * @deprecated Deprecated in 5.4 with no replacement; decoration of fields with validation errors
     *             has moved to the client.
     */
    public static final String ERROR = "error";

    /**
     * CSS class name for individual validation errors.
     *
     * @since 5.2.0
     * @deprecated Deprecated in 5.4 with no replacement; decoration of fields with validation errors
     *             has moved to the client.
     */
    public static final String ERROR_SINGLE = "t-error-single";
}
