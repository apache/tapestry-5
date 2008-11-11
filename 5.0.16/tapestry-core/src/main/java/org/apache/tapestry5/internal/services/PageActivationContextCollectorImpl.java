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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.services.TypeCoercer;

public class PageActivationContextCollectorImpl implements PageActivationContextCollector
{
    private final Object[] EMPTY = new Object[0];

    private final TypeCoercer typeCoercer;

    public PageActivationContextCollectorImpl(TypeCoercer typeCoercer)
    {
        this.typeCoercer = typeCoercer;
    }

    public Object[] collectPageActivationContext(Page page)
    {
        ComponentPageElement element = page.getRootElement();

        final Holder<Object[]> holder = Holder.create();

        ComponentEventCallback callback = new ComponentEventCallback()
        {
            public boolean handleResult(Object result)
            {
                holder.put(typeCoercer.coerce(result, Object[].class));

                // We've got the value, stop the event.

                return true;
            }
        };

        element.triggerEvent(EventConstants.PASSIVATE, null, callback);

        if (!holder.hasValue()) return EMPTY;

        return holder.get();
    }
}
