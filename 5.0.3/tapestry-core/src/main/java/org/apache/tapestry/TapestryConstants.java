// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry;

/**
 * Collection of common constant values used throughout Tapestry.
 */
public final class TapestryConstants
{
    /** Default client event name, "action", used in most situations. */
    public static final String ACTION_EVENT = "action";

    /**
     * Event triggered when a page is activated (for rendering). The component event handler will be
     * passed the context provided by the passivate event.
     */
    public static final String ACTIVATE_EVENT = "activate";

    /**
     * Event triggered when a link for a page is generated. The event handler for the page may
     * provide an object, or an array of objects, as the context for the page. These values will
     * become part of the page's context, and will be provided back when the page is activated.
     */
    public static final String PASSIVATE_EVENT = "passivate";

    /**
     * Request path prefix that identifies an internal (on the classpath) asset.
     */
    public static final String ASSET_PATH_PREFIX = "/assets/";

    /** Binding expression prefix used for literal strings. */
    public static final String LITERAL_BINDING_PREFIX = "literal";

    /** Binding expression prefix used to bind to a property of the component. */
    public static final String PROP_BINDING_PREFIX = "prop";

    private TapestryConstants()
    {
    }
}
