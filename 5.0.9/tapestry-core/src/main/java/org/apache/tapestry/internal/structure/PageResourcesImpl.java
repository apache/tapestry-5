// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.internal.services.ComponentClassCache;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.services.ComponentMessagesSource;

import java.util.Locale;

public class PageResourcesImpl implements PageResources
{
    private final Locale _locale;

    private final ComponentMessagesSource _componentMessagesSource;

    private final TypeCoercer _typeCoercer;

    private final ComponentClassCache _componentClassCache;

    public PageResourcesImpl(Locale locale, ComponentMessagesSource componentMessagesSource, TypeCoercer typeCoercer,
                             ComponentClassCache componentClassCache)
    {
        _componentMessagesSource = componentMessagesSource;
        _locale = locale;
        _typeCoercer = typeCoercer;
        _componentClassCache = componentClassCache;
    }

    public Messages getMessages(ComponentModel componentModel)
    {
        return _componentMessagesSource.getMessages(componentModel, _locale);
    }

    public <S, T> T coerce(S input, Class<T> targetType)
    {
        return _typeCoercer.coerce(input, targetType);
    }

    public Class toClass(String className)
    {
        return _componentClassCache.forName(className);
    }
}
