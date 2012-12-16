// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.alerts;

/**
 * The severity of an {@link Alert}, used primarily to control how it is decorated when presented to the user on the client side.
 * 
 * @since 5.3
 */
public enum Severity
{
    /**
     * @since 5.3.6
     */
    SUCCESS, 
    /**
     * @since 5.3
     */
    INFO, 
    /**
     * @since 5.3
     */
    WARN, 
    /**
     * @since 5.3
     */
    ERROR;

    /**
     * The CSS class to be used for the client list element.
     *
     * @deprecated Deprecated in Tapestry 5.4 with no replacement (beyond the name of the value itself).
     */
    public final String cssClass;

    private Severity()
    {
        cssClass = name().toLowerCase();
    }
}
