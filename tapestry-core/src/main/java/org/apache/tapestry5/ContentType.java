// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Represents an HTTP content type. Allows to set various elements like the mime type, the character set, and other
 * parameters. This is similar to a number of other implementations of the same concept in JAF, etc. We have created
 * this simple implementation to avoid including the whole libraries.
 */
public final class ContentType
{
    private String baseType = "";

    private String subType = "";

    private final Map<String, String> parameters = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Creates a new empty content type.
     */
    public ContentType()
    {
    }

    /**
     * Creates a new content type from the argument. The format of the argument has to be basetype/subtype(;key=value)*
     *
     * @param contentType the content type that needs to be represented
     */
    public ContentType(String contentType)
    {
        parse(contentType);
    }

    /**
     * Creates a new content type with the given MIME type and charset
     */
    public ContentType(String contentType, String charset)
    {
        this(contentType);

        setParameter(InternalConstants.CHARSET_CONTENT_TYPE_PARAMETER, charset);
    }


    /**
     * Returns true only if the other object is another instance of ContentType, and has the ssame baseType, subType and
     * set of parameters.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;

        if (o.getClass() != this.getClass()) return false;

        ContentType ct = (ContentType) o;

        return baseType.equals(ct.baseType) && subType.equals(ct.subType) && parameters.equals(ct.parameters);
    }

    /**
     * @return the base type of the content type
     */
    public String getBaseType()
    {
        return baseType;
    }

    /**
     * @param baseType
     */
    public void setBaseType(String baseType)
    {
        Defense.notNull(baseType, "baseType");

        this.baseType = baseType;
    }

    /**
     * @return the sub-type of the content type
     */
    public String getSubType()
    {
        return subType;
    }

    /**
     * @param subType
     */
    public void setSubType(String subType)
    {
        Defense.notNull(subType, "subType");

        this.subType = subType;
    }

    /**
     * @return the MIME type of the content type
     */
    public String getMimeType()
    {
        return baseType + "/" + subType;
    }

    /**
     * @return the list of names of parameters in this content type, in alphabetical order.
     */
    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(parameters);
    }

    /**
     * @return the character set (the  "charset" parameter) or null.
     */
    public String getCharset()
    {
        return getParameter(InternalConstants.CHARSET_CONTENT_TYPE_PARAMETER);
    }

    /**
     * @param key the name of the content type parameter
     * @return the value of the content type parameter
     */
    public String getParameter(String key)
    {
        Defense.notNull(key, "key");

        return parameters.get(key);
    }

    /**
     * @param key   the name of the content type parameter
     * @param value the value of the content type parameter
     */
    public void setParameter(String key, String value)
    {
        Defense.notNull(key, "key");
        Defense.notNull(value, "value");

        parameters.put(key, value);
    }

    /**
     * Parses the argument and configures the content type accordingly. The format of the argument has to be
     * type/subtype(;key=value)*
     *
     * @param contentType the content type that needs to be represented
     */
    public void parse(String contentType)
    {
        baseType = "";
        subType = "";
        parameters.clear();

        StringTokenizer tokens = new StringTokenizer(contentType, ";");
        if (!tokens.hasMoreTokens()) return;

        String mimeType = tokens.nextToken();
        StringTokenizer mimeTokens = new StringTokenizer(mimeType, "/");
        setBaseType(mimeTokens.hasMoreTokens() ? mimeTokens.nextToken() : "");
        setSubType(mimeTokens.hasMoreTokens() ? mimeTokens.nextToken() : "");

        while (tokens.hasMoreTokens())
        {
            String parameter = tokens.nextToken();

            StringTokenizer parameterTokens = new StringTokenizer(parameter, "=");
            String key = parameterTokens.hasMoreTokens() ? parameterTokens.nextToken() : "";
            String value = parameterTokens.hasMoreTokens() ? parameterTokens.nextToken() : "";
            setParameter(key, value);
        }
    }

    /**
     * @return the string representation of this content type
     */
    public String unparse()
    {
        StringBuilder buffer = new StringBuilder(getMimeType());

        for (String parameterName : getParameterNames())
        {
            buffer.append(";");
            buffer.append(parameterName);
            buffer.append("=");
            buffer.append(parameters.get(parameterName));
        }

        return buffer.toString();
    }

    /**
     * @return the string representation of this content type. Same as unparse().
     */
    @Override
    public String toString()
    {
        return unparse();
    }
}
