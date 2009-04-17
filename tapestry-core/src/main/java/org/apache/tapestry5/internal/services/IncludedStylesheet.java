//  Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.dom.Element;

/**
 * A stylesheet included in the rendered document by the {@link org.apache.tapestry5.internal.services.DocumentLinker}.
 */
class IncludedStylesheet
{
    private final String url;

    private final String media;

    IncludedStylesheet(String url, String media)
    {
        this.url = url;
        this.media = media;
    }


    /**
     * Invoked to add the stylesheet link to a container element.
     *
     * @param container to add the new element to
     */
    void add(Element container)
    {
        container.element("link",
                          "href", url,
                          "rel", "stylesheet",
                          "type", "text/css",
                          "media", media);
    }
}
