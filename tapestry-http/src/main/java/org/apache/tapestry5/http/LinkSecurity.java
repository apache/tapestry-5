// Copyright 2010-2013 The Apache Software Foundation
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

package org.apache.tapestry5.http;

/**
 * Identifies how a {@link Link} should handle security.
 * 
 * @since 5.2.2
 */
public enum LinkSecurity
{
    /** The request was insecure, but the targeted page was secure, so the URI should be absolute and secure. */
    FORCE_SECURE,

    /** The request was was secure but the targeted page is not, so the URI should be absolute and insecure. */
    FORCE_INSECURE,

    /**
     * The request is insecure, which matches the targeted page security, so there's no explicit need for an absolute
     * URI.
     */
    INSECURE,

    /**
     * The request is secure, which matches the targeted page security, so there's no explicit need for an absolute
     * URI.
     */
    SECURE;

    /** Promotes to either {@link #FORCE_SECURE} or {@link #FORCE_INSECURE}. */
    public LinkSecurity promote()
    {
        switch (this)
        {
            case SECURE:
            case FORCE_SECURE:
                return FORCE_SECURE;

            default:
                return FORCE_INSECURE;
        }
    }

    /** Does this value indicate forcing an absolute URI (one that includes scheme and hostname)? */
    public boolean isAbsolute()
    {
        return this == FORCE_SECURE || this == FORCE_INSECURE;
    }
}
