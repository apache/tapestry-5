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

package org.apache.tapestry5.services.javascript;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.ioc.util.UnknownValueException;

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
     *             if no such stack
     */
    JavaScriptStack getStack(String name);

    /**
     * Returns the names of all stacks, in sorted order.
     * 
     * @since 5.2.1
     */
    List<String> getStackNames();
}
