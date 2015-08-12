// Copyright 2007, 2008, 2011, 2012 The Apache Software Foundation
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
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

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

    private static final Pattern SPECIAL_CONTENT = Pattern.compile("(?:</?(\\p{Alpha}+)>)|(?:&\\p{Alpha}+;)");
    private static final Set<String> PASS_THROUGH_TAGS = CollectionFactory.newSet("b", "em", "i", "code", "strong");


    public ParameterDescription(final FieldDoc fieldDoc, final String name, final String type, final String defaultValue, final String defaultPrefix,
            final boolean required, final boolean allowNull, final boolean cache, final String since, final boolean deprecated)
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

    /**
     * Extracts the description, converting Text and @link nodes as needed into markup text.
     *
     * @return markup text, ready for writing
     * @throws IOException
     */
    public String extractDescription() throws IOException
    {
        StringBuilder builder = new StringBuilder();

        for (Tag tag : field.inlineTags())
        {
            if (tag.name().equals("Text"))
            {
                appendContentSafe(builder, tag.text());
                continue;
            }

            if (tag.name().equals("@link"))
            {
                SeeTag seeTag = (SeeTag) tag;

                String label = seeTag.label();
                if (label != null && !label.equals(""))
                {
                    builder.append(StringEscapeUtils.escapeHtml(label));
                    continue;
                }

                if (seeTag.referencedClassName() != null)
                    builder.append(StringEscapeUtils.escapeHtml(seeTag.referencedClassName()));

                if (seeTag.referencedMemberName() != null)
                {
                    builder.append('#');
                    builder.append(StringEscapeUtils.escapeHtml(seeTag.referencedMemberName()));
                }
            }
            else if (tag.name().equals("@code"))
            {
                builder.append("<code>");
                builder.append(StringEscapeUtils.escapeHtml(tag.text()));
                builder.append("</code>");
            }
        }

        String text = builder.toString();

        // Fix it up a little.

        // Remove any simple open or close tags found in the text, as well as any XML entities.

        return text.trim();
    }

    private static void appendContentSafe(final StringBuilder sb, final String string){
        Matcher m = SPECIAL_CONTENT.matcher(string);
        int index = 0;
        while (index < string.length()){
            boolean match = m.find(index);
            if (match){
                if (index != m.start()){
                    sb.append(StringEscapeUtils.escapeHtml(string.substring(index, m.start())));
                }
                String tagName = m.group(1);
                if (tagName!= null){
                    if(PASS_THROUGH_TAGS.contains(tagName.toLowerCase(Locale.US))){
                        sb.append(m.group());
                    }
                }else{
                    sb.append(m.group());
                }
                index = m.end();
            }else{
                sb.append(StringEscapeUtils.escapeHtml(string.substring(index)));
                break;
            }
        }
    }
}
