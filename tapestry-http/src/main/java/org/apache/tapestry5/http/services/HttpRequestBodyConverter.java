// Copyright 2011 The Apache Software Foundation
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
package org.apache.tapestry5.http.services;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Service that converts the body of an HTTP request to a given target class.
 * Each implementation, which should be contributed to the {@link HttpRequestBodyConverter} service,
 * should check whether it can actually handled that request. If not, it should return <code>null</code>,
 * which means trying the next HttpRequestBodyConverter instance.
 */
@UsesOrderedConfiguration(HttpRequestBodyConverter.class)
public interface HttpRequestBodyConverter
{

    /**
     * Converts the body of this request. If this implementation cannot handle this request,
     * probably by not handling its content type, it should return <code>null</code>.
     * In addition, if the request body is empty, this method should also return
     * <code>null</code>.
     * @param request an {@linkplain HttpServletRequest}.
     * @param type the target type.
     * @return an object of the target type or <code>null</code>.
     */
    <T> T convert(HttpServletRequest request, Class<T> type);
    
}
