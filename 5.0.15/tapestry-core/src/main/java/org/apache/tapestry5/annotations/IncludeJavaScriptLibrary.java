// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import java.lang.annotation.*;


/**
 * Allows for the inclusion of one or more JavaScript libraries.  The libraries are assets, usually (but not always)
 * stored on the classpath with the component.
 *
 * @see org.apache.tapestry5.annotations.IncludeStylesheet
 * @see org.apache.tapestry5.annotations.Path
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IncludeJavaScriptLibrary
{
    /**
     * The paths to the JavaScript library assets.  Symbols in the paths are expanded.  The library may be localized.
     */
    String[] value();
}
