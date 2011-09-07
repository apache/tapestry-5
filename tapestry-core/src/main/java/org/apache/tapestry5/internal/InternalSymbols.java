// Copyright 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

public class InternalSymbols
{
    /**
     * The name of the application (i.e., the name of the application filter). Used, for example, to
     * select additional resources related to the application.
     */
    public static final String APP_NAME = "tapestry.app-name";

    /**
     * The application package converted to a path ('.' becomes '/'). Useful for finding resources
     * on the classpath relevant to the application.
     *
     * @since 5.1.0.0
     */
    public static final String APP_PACKAGE_PATH = "tapestry.app-package-path";

    /**
     * Comma-separated list of pre-allocated Form component control names. Basically, this exists to
     * work around name collisions on the client side. Starting in 5.3, these names are
     * also pre-allocated as ids.
     *
     * @since 5.2.0
     */
    public static final String PRE_SELECTED_FORM_NAMES = "tapestry.pre-selected-form-names";
}
