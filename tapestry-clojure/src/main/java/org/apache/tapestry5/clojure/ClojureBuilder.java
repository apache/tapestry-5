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

/**
 * Creates a proxy for the interface that delegates each method to a Clojure function.
 */
public interface ClojureBuilder
{
    /**
     * Creates the proxy. Method names are converted to Clojure function names.
     *
     * @param interfaceType
     *         type of interface. Must have the {@link Namespace} annotation. Not null.
     * @return the proxy
     * @param <T> the interface type.
     * @see MethodToFunctionSymbolMapper
     */
    <T> T build(Class<T> interfaceType);
}
