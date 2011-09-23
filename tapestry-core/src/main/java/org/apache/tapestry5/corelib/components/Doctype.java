// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;

/**
 * Overrides the DOCTYPE of the rendered document (via {@link org.apache.tapestry5.dom.Document#dtd(String, String, String)}
 * which can be useful when different component templates that render to the same document disagree about what the correct DOCTYPE
 * is.
 *
 * @tapestrydoc
 * @since 5.3
 */
public class Doctype
{
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private String name;

    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String publicId, systemId;

    boolean beginRender(MarkupWriter writer)
    {
        writer.getDocument().dtd(name, publicId, systemId);

        return false;
    }
}
