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
 * Used to automatically include a CSS stylesheet when rendering the page.  The value is an asset reference; relative
 * paths are relative to the Java class, or a "context:" prefix can be used to reference resources in the web
 * application.
 * <p/>
 * This saves the work of injecting the asset into a field and injecting the PageRenderSupport environmental service,
 * and invoking the method.
 * <p/>
 * Does not support setting a media type; if that is required. use {@link org.apache.tapestry5.RenderSupport#addStylesheetLink(org.apache.tapestry5.Asset,
 * String)} directly.
 *
 * @see org.apache.tapestry5.annotations.Path
 * @see org.apache.tapestry5.annotations.IncludeJavaScriptLibrary
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IncludeStylesheet
{
    /**
     * One or more paths to be injected. Symbols in the path will be expanded. The stylesheets may be localized.
     */
    String[] value();
}
