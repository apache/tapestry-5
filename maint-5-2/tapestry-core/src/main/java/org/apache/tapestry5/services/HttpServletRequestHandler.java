// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The first step in handing an incoming request to the {@linkplain org.apache.tapestry5.TapestryFilter servlet filter},
 * this constructed as a {@linkplain org.apache.tapestry5.ioc.services.PipelineBuilder pipeline}.  The main
 * implementation hands off to the {@link org.apache.tapestry5.services.RequestHandler} service.
 */
@UsesOrderedConfiguration(HttpServletRequestFilter.class)
public interface HttpServletRequestHandler
{
    /**
     * Returns true if the request was handled, false otherwise.
     */
    boolean service(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
