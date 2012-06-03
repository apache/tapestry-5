// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.clojure;

import clojure.lang.Symbol;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

import java.lang.reflect.Method;

/**
 * Maps a method from a service interface to a fully-qualified Clojure function name, as a Clojure
 * {@link clojure.lang.Symbol}. This service is itself a chain of command, to support adding or overriding
 * the mapping.
 */
@UsesOrderedConfiguration(MethodToFunctionSymbolMapper.class)
public interface MethodToFunctionSymbolMapper
{
    /**
     * @param namespace
     *         namespace for the service (from {@link Namespace} annotation)
     * @param method
     *         method for which a function name is desired.
     * @return Symbol for this method, or null (to drop down to next mapper)
     */
    Symbol mapMethod(String namespace, Method method);
}
