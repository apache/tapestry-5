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

package org.apache.tapestry5.internal.services.javascript;

import java.util.List;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Used to generate a list of asset URL paths for the JavaScript libraries
 * of a JavaScript stack. This encapsulates much of the logic of {@linkplain SymbolConstants#COMBINE_SCRIPTS script
 * aggregation}.
 * 
 * @since 5.2.0
 * @see JavaScriptStack
 * @see JavaScriptStackSource
 * @see JavaScriptSupport
 */
public interface JavaScriptStackPathConstructor
{
    /**
     * Given a stack, by name, return a list of URL paths for the individual libraries in the stack.
     * If scripts are combined, this will be a single (combined) URL.
     * 
     * @param stackName
     *            name of {@link JavaScriptStack}
     */
    List<String> constructPathsForJavaScriptStack(String stackName);
}
