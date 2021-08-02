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

package org.apache.tapestry5.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Represents an HTTP content type. Allows to set various elements like the MIME type, the character set, and other
 * parameters. This is similar to a number of other implementations of the same concept in JAF, etc. We have created
 * this simple implementation to avoid including the whole libraries.
 *
 * As of Tapestry 5.4, this is now an immutable data type.
 */
public final class ContentType
{
    private final String baseType;

    private final String subType;

    private final Map<String, String> parameters;

    private static final Pattern PATTERN = Pattern.compile("^(.+)/([^;]+)(;(.+=[^;]+))*$");

    /**
     * Creates a new content type from the argument. The format of the argument has to be basetype/subtype(;key=value)*
     *
     * @param contentType
     *         the content type that needs to be represented
     */
    public ContentType(String contentType)
    {
        Matcher matcher = PATTERN.matcher(contentType);

        if (!matcher.matches())
        {
            throw new IllegalArgumentException(String.format("Not a parseable content type '%s'.", contentType));
        }

        this.baseType = matcher.group(1);
        this.subType = matcher.group(2);
        this.parameters = parseKeyValues(matcher.group(4));
    }

    private ContentType(String baseType, String subType, Map<String, String> parameters)
    {
        this.baseType = baseType;
        this.subType = subType;
        this.parameters = parameters;
    }


    private static Map<String, String> parseKeyValues(String keyValues)
    {
        if (keyValues == null)
        {
            return Collections.emptyMap();
        }

        Map<String, String> parameters = CollectionFactory.newCaseInsensitiveMap();

        StringTokenizer tk = new StringTokenizer(keyValues, ";");

        while (tk.hasMoreTokens())
        {
            String token = tk.nextToken();
            int sep = token.indexOf('=');

            parameters.put(token.substring(0, sep), token.substring(sep + 1));
        }

        return parameters;
    }

    /**
     * Returns true only if the other object is another instance of ContentType, and has the same baseType, subType and
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

    @Override
    public int hashCode() 
    {
        return Objects.hash(baseType, subType, parameters);
    }

    /**
     * @return the base type of the content type
     */
    public String getBaseType()
    {
        return baseType;
    }

    /**
     * @return the sub-type of the content type
     */
    public String getSubType()
    {
        return subType;
    }

    /**
     * @return the MIME type of the content type (the base type and the subtype, seperated with a '/').
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
     * @return the character set (the "charset" parameter) or null.
     */
    public String getCharset()
    {
        return getParameter(TapestryHttpInternalConstants.CHARSET_CONTENT_TYPE_PARAMETER);
    }

    /**
     * @param key
     *         the name of the content type parameter
     * @return the value of the content type parameter
     */
    public String getParameter(String key)
    {
        assert key != null;
        return parameters.get(key);
    }

    private String unparse()
    {
        StringBuilder buffer = new StringBuilder(getMimeType());

        for (String parameterName : getParameterNames())
        {
            buffer.append(';');
            buffer.append(parameterName);
            buffer.append('=');
            buffer.append(parameters.get(parameterName));
        }

        return buffer.toString();
    }

    /**
     * Returns a new content type with the indicated parameter.
     *
     * @since 5.4
     */
    public ContentType withParameter(String key, String value)
    {
        assert InternalUtils.isNonBlank(key);
        assert InternalUtils.isNonBlank(value);

        Map<String, String> newParameters = CollectionFactory.newCaseInsensitiveMap();

        newParameters.putAll(parameters);
        newParameters.put(key, value);

        return new ContentType(baseType, subType, newParameters);
    }

    public ContentType withCharset(String charset)
    {
        return withParameter(TapestryHttpInternalConstants.CHARSET_CONTENT_TYPE_PARAMETER, charset);
    }

    /**
     * @return the string representation of this content type.
     */
    @Override
    public String toString()
    {
        return unparse();
    }

    /**
     * @return true if the content type includes parameters (such as 'charset').
     * @since 5.4
     */
    public boolean hasParameters()
    {
        return !parameters.isEmpty();
    }
}
