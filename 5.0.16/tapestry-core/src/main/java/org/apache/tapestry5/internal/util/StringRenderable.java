//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Renderable;

/**
 * Renders a string using {@link MarkupWriter#write(String)}.
 */
public class StringRenderable implements Renderable
{
    private final String text;

    public StringRenderable(String text)
    {
        this.text = text;
    }

    public void render(MarkupWriter writer)
    {
        writer.write(text);
    }

    @Override
    public String toString()
    {
        return String.format("Renderable[%s]", text);
    }
}
