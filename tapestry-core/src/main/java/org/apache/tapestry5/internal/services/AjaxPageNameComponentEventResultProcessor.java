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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.services.Ajax;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

import java.io.IOException;

/**
 * A {@link org.apache.tapestry5.services.ComponentEventResultProcessor}, used for Ajax requests, for a String value
 * that is interpreted as a logical page name.
 *
 * @see org.apache.tapestry5.internal.services.PageNameComponentEventResultProcessor
 */
public class AjaxPageNameComponentEventResultProcessor implements ComponentEventResultProcessor<String>
{
    private final ComponentEventResultProcessor masterProcessor;

    private final LinkSource linkSource;

    public AjaxPageNameComponentEventResultProcessor(@Ajax ComponentEventResultProcessor masterProcessor,
                                                     LinkSource linkSource)
    {
        this.masterProcessor = masterProcessor;
        this.linkSource = linkSource;
    }

    /**
     * Obtains a page render {@link org.apache.tapestry5.Link} for the named, then builds a JSON reponse for the
     * client.
     *
     * @param value page name
     * @throws IOException
     */
    public void processResultValue(String value) throws IOException
    {
        Link link = linkSource.createPageRenderLink(value, false);

        masterProcessor.processResultValue(link);
    }
}
