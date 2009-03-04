// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.dom;

import java.io.PrintWriter;
import java.util.Map;

/**
 * A node that represents a comment within the DOM.
 */
public final class Comment extends Node
{
    private final String comment;

    Comment(Element container, String comment)
    {
        super(container);

        this.comment = comment;
    }

    @Override
    void toMarkup(Document document, PrintWriter writer, Map<String, String> namespaceURIToPrefix)
    {
        writer.print("<!-- ");
        writer.print(comment);
        writer.print(" -->");
    }
}
