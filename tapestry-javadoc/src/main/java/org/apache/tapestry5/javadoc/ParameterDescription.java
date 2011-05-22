// Copyright 2007, 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.javadoc;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

public class ParameterDescription
{
    public final FieldDoc field;

    public final String name;

    public final String type;

    public final String defaultValue;

    public final String defaultPrefix;

    public final boolean required;

    public final boolean allowNull;

    public final boolean cache;

    public final String since;

    public final boolean deprecated;

    private static final Pattern STRIPPER = Pattern.compile("(<.*?>|&.*?;)", Pattern.DOTALL);

    public ParameterDescription(FieldDoc fieldDoc, String name, String type, String defaultValue, String defaultPrefix,
            boolean required, boolean allowNull, boolean cache, String since, boolean deprecated)
    {
        this.field = fieldDoc;
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.defaultPrefix = defaultPrefix;
        this.required = required;
        this.allowNull = allowNull;
        this.cache = cache;
        this.since = since;
        this.deprecated = deprecated;
    }

    public void writeDescription(Writer writer) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        for (Tag tag : field.inlineTags())
        {
            if (tag.name().equals("Text"))
            {
                builder.append(tag.text());
                continue;
            }

            if (tag.name().equals("@link"))
            {
                SeeTag seeTag = (SeeTag) tag;

                String label = seeTag.label();
                if (label != null && !label.equals(""))
                {
                    builder.append(label);
                    continue;
                }

                if (seeTag.referencedClassName() != null)
                    builder.append(seeTag.referencedClassName());

                if (seeTag.referencedMemberName() != null)
                {
                    builder.append("#");
                    builder.append(seeTag.referencedMemberName());
                }
            }
        }

        String text = builder.toString();

        // Fix it up a little.

        // Remove any simple open or close tags found in the text, as well as any XML entities.

        String stripped = STRIPPER.matcher(text).replaceAll("");

        writer.write(stripped);
    }
}
