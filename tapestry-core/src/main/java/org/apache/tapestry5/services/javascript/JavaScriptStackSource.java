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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.util.List;

/**
 * Manages the available {@link JavaScriptStack}s, each of which has a unique name.
 *
 * @since 5.2.0
 */
@UsesMappedConfiguration(JavaScriptStack.class)
public interface JavaScriptStackSource
{
    /**
     * Gets a stack by name (ignoring case).
     *
     * @return named stack
     * @throws UnknownValueException
     *         if no such stack
     */
    JavaScriptStack getStack(String name);

    /**
     * Gets a stack by name (ignoring case).
     *
     * @return named stack, or null if not found
     * @since 5.4
     */
    JavaScriptStack findStack(String name);

    /**
     * Returns the names of all stacks, in sorted order.
     *
     * @since 5.2.1
     */
    List<String> getStackNames();

    /**
     * Attempts to find the stack containing the indicated JavaScript library.
     *
     * @param resource identifies a potential JavaScript Library
     * @return the stack if found, or null
     * @since 5.4
     * @see JavaScriptStack#getJavaScriptLibraries()
     */
    JavaScriptStack findStackForJavaScriptLibrary(Resource resource);
}
