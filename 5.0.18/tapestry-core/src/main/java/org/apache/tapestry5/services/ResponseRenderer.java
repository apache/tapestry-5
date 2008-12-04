// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ContentType;

import java.io.IOException;

/**
 * Public facade around internal services related to rendering a markup response.
 */
public interface ResponseRenderer
{
    /**
     * Renders a markup response by rendering the named page.
     *
     * @param pageName logical name of page to provide the markup
     */
    void renderPageMarkupResponse(String pageName) throws IOException;


    /**
     * Finds the content type for the page containing the indicated component.
     *
     * @param component a component within a page
     * @return the content type
     * @throws IllegalArgumentException if the component parameter is not a component
     */
    ContentType findContentType(Object component);
}
