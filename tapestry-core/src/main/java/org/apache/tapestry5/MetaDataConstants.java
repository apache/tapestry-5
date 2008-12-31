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

package org.apache.tapestry5;

/**
 * Meta-data keys that are applied to components and pages.  In addition, in many cases a {@linkplain
 * org.apache.tapestry5.SymbolConstants symbol constant key} is also a meta data key (where the symbol value is the
 * ultimate default).
 *
 * @see org.apache.tapestry5.services.MetaDataLocator
 * @see org.apache.tapestry5.MetaDataConstants
 */
public class MetaDataConstants
{
    /**
     * Meta data key applied to pages that sets the response content type. A factory default provides the value
     * "text/html" when not overridden.
     */
    public static final String RESPONSE_CONTENT_TYPE = "tapestry.response-content-type";

    /**
     * Meta data key applied to pages that may only be accessed via secure methods (HTTPS).
     */
    public static final String SECURE_PAGE = "tapestry.secure-page";
}
