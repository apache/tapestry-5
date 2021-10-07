// Copyright 2021 The Apache Software Foundation
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

package org.apache.tapestry5.http.services;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.commons.services.TypeCoercer;

/**
 * Service which provides REST-related utilities.
 * @since 5.8.0
 */
public interface RestSupport
{
    
    /**
     * Is this request a GET?
     * @return <code>true</code> or <code>false</code>
     */
    boolean isHttpGet();
    
    /**
     * Is this request a POST?
     * @return <code>true</code> or <code>false</code>
     */
    boolean isHttpPost();

    /**
     * Is this request a HEAD?
     * @return <code>true</code> or <code>false</code>
     */
    boolean isHttpHead();

    /**
     * Is this request a PUT?
     * @return <code>true</code> or <code>false</code>
     */
    boolean isHttpPut();
    
    /**
     * Is this request a HEAD?
     * @return <code>true</code> or <code>false</code>
     */
    boolean isHttpDelete();

    /**
     * Is this request a HEAD?
     * @return <code>true</code> or <code>false</code>
     */
    boolean isHttpPatch();

    /**
     * Returns, if present, the body of the request body coerced to a given type. If the body is empty,
     * an empty {@linkplain Optional} is returned. Coercions are done through, which uses
     * {@linkplain TypeCoercer} as a fallback (coercing {@linkplain HttpServletRequest} to the target type).
     * @param <T> the type of the return value.
     * @param type the target type.
     * @return an <code>Optional</code> wrapping the resulting object.
     */
    <T> Optional<T> getRequestBodyAs(Class<T> type);
    
}
