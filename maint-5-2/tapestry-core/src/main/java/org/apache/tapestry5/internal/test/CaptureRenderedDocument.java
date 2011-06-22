// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.services.MarkupRendererFilter;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.MarkupWriter;

/**
 * Used to capture the rendered document from a traditional page render. Invokes {@link
 * org.apache.tapestry5.internal.test.TestableResponse#setRenderedDocument(org.apache.tapestry5.dom.Document)}.
 *
 * @since 5.1.0.0
 */
public class CaptureRenderedDocument implements MarkupRendererFilter
{
    private final TestableResponse testableResponse;

    public CaptureRenderedDocument(TestableResponse testableResponse)
    {
        this.testableResponse = testableResponse;
    }


    public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
    {
        renderer.renderMarkup(writer);

        testableResponse.setRenderedDocument(writer.getDocument());
    }
}
