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

import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;

/**
 * Constants needed by end-user classes.
 * 
 * @since 5.2.0
 */
public class TapestryConstants
{

    /**
     * The extension used for Tapestry component template files, <em>T</em>apestry <em>M</em>arkup <em>L</em>anguage.
     * Template files are well-formed XML files.
     */
    public static final String TEMPLATE_EXTENSION = "tml";

    /**
     * Name of query parameter that is placed on "loopback" links (page render links for the same
     * page). This mostly includes the redirects sent after a component event request. Page render
     * requests
     * that do <em>not</em> have the LOOPBACK query parameter will trigger a {@linkplain PageResetListener reset
     * notification} after the initialization event; the
     * LOOPBACK
     * prevents this reset notification.
     * 
     * @since 5.2.0
     * @see ComponentEventLinkEncoder#createPageRenderLink(org.apache.tapestry5.services.PageRenderRequestParameters)
     * @see ComponentEventLinkEncoder#decodePageRenderRequest(org.apache.tapestry5.services.Request)
     * @see PageResetListener
     */
    public static final String PAGE_LOOPBACK_PARAMETER_NAME = "t:lb";

}
