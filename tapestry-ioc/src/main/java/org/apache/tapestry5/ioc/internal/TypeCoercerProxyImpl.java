// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.services.TypeCoercer;

/**
 * A proxy for the {@link TypeCoercer}
 * 
 * @since 5.3
 */
public final class TypeCoercerProxyImpl implements TypeCoercerProxy
{
    private final ObjectLocator locator;

    private TypeCoercer delegate;

    public TypeCoercerProxyImpl(ObjectLocator locator)
    {
        this.locator = locator;
    }

    private TypeCoercer delegate()
    {
        if (delegate == null)
            delegate = locator.getService(TypeCoercer.class);

        return delegate;
    }

    @Override
    public <S, T> T coerce(S input, Class<T> targetType)
    {
        if (targetType.isInstance(input))
            return targetType.cast(input);

        return delegate().coerce(input, targetType);
    }

}
