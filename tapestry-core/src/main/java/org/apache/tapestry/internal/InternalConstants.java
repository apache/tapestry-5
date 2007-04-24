// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal;

public final class InternalConstants
{
    /**
     * Init parameter used to identify the package from which application classes are loaded. Such
     * classes are in the pages, components and mixins sub-packages.
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = "tapestry.app-package";

    /** Binding expression prefix used for literal strings. */
    public static final String LITERAL_BINDING_PREFIX = "literal";

    /** Binding expression prefix used to bind to a property of the component. */
    public static final String PROP_BINDING_PREFIX = "prop";

    /**
     * The extension used for Tapestry component template files. Template files are well-formed XML
     * files. This is also used as the extension for page render requests (perhaps these will be
     * split into two concepts later, especially if the we designate a particular extension for
     * Tapestry template files, such as .tsp).
     */
    public static final String TEMPLATE_EXTENSION = "html";

    /** All purpose CSS class name for anything related to Tapestry errors. */
    public static final String TAPESTRY_ERROR_CLASS = "t-error";

    private InternalConstants()
    {
    }
}
