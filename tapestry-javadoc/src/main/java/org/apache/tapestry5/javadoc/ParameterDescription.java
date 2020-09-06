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

import com.sun.source.doctree.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.commons.util.CollectionFactory;

import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterDescription
{
    public final VariableElement field;

    public final String name;

    public final String type;

    public final String defaultValue;

    public final String defaultPrefix;

    public final boolean required;

    public final boolean allowNull;

    public final boolean cache;

    public final String since;

    public final boolean deprecated;

    private final DocCommentTreeProvider docCommentTreeProvider;

    private static final Pattern SPECIAL_CONTENT = Pattern.compile("(?:</?(\\p{Alpha}+)>)|(?:&\\p{Alpha}+;)");
    private static final Set<String> PASS_THROUGH_TAGS = CollectionFactory.newSet("b", "em", "i", "code", "strong");


    public ParameterDescription(final VariableElement fieldDoc, final String name, final String type, final String defaultValue, final String defaultPrefix,
                                final boolean required, final boolean allowNull, final boolean cache, final String since, final boolean deprecated,
                                final DocCommentTreeProvider docCommentTreeProvider)
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
        this.docCommentTreeProvider = docCommentTreeProvider;
    }

    /**
     * Extracts the description, converting Text and @link nodes as needed into markup text.
     *
     * @return markup text, ready for writing
     * @throws IOException if some error occurs.
     */
    public String extractDescription() throws IOException
    {
        final DocCommentTree tree = docCommentTreeProvider.getDocCommentTree(field);

        if (tree == null)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (com.sun.source.doctree.DocTree tag : tree.getFullBody())
        {
            if (tag.getKind() == DocTree.Kind.TEXT)
            {
                TextTree textTree = (TextTree) tag;
                appendContentSafe(builder, textTree.getBody());
                continue;
            }

            if (tag.getKind() == DocTree.Kind.LINK)
            {
                LinkTree seeTag = (LinkTree) tag;
                String label = seeTag.getLabel().toString();
                if (StringUtils.isNotEmpty(label))
                {
                    builder.append(StringEscapeUtils.escapeHtml(label));
                    continue;
                }

                if (seeTag.getReference() != null)
                    builder.append(StringEscapeUtils.escapeHtml(seeTag.getReference().getSignature()));
            }
            else if (tag.getKind() == DocTree.Kind.CODE)
            {
                LiteralTree codeTag = (LiteralTree) tag;
                builder.append("<code>");
                builder.append(StringEscapeUtils.escapeHtml(codeTag.getBody().getBody()));
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
