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
package org.apache.tapestry5.http.internal;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CoercionNotFoundException;
import org.apache.tapestry5.http.services.HttpRequestBodyConverter;

final public class TypeCoercerHttpRequestBodyConverter implements HttpRequestBodyConverter
{
    
    final private TypeCoercer typeCoercer;

    public TypeCoercerHttpRequestBodyConverter(TypeCoercer typeCoercer) 
    {
        super();
        this.typeCoercer = typeCoercer;
    }

    @Override
    public <T> T convert(HttpServletRequest request, Class<T> type) 
    {
        T value;
        try
        {
            value = typeCoercer.coerce(request, type);
        } catch (CoercionNotFoundException e)
        {
            throw new TapestryException(
                    String.format("Couldn't find a coercion from InputStream to %s "
                            + " since no %s converted it", type.getName(), HttpRequestBodyConverter.class.getSimpleName())
                    , e);
        }
        return value;
    }
    
}