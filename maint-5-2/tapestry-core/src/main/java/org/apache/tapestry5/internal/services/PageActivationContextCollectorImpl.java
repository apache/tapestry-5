//  Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.InvalidationListener;

import java.util.Map;

public class PageActivationContextCollectorImpl implements PageActivationContextCollector, InvalidationListener
{
    private final Object[] EMPTY = new Object[0];

    private final TypeCoercer typeCoercer;

    private final ComponentModelSource modelSource;

    private final RequestPageCache requestPageCache;

    /**
     * Keyed on *canonical* page name, value indicates whether the page has a passivate event handler.
     */
    private final Map<String, Boolean> cache = CollectionFactory.newConcurrentMap();

    public PageActivationContextCollectorImpl(TypeCoercer typeCoercer, RequestPageCache requestPageCache,
                                              ComponentModelSource modelSource)
    {
        this.typeCoercer = typeCoercer;
        this.requestPageCache = requestPageCache;
        this.modelSource = modelSource;
    }

    public void objectWasInvalidated()
    {
        cache.clear();
    }

    public Object[] collectPageActivationContext(String pageName)
    {
        Boolean hasHandler = cache.get(pageName);

        if (hasHandler == null)
        {
            ComponentModel model = modelSource.getPageModel(pageName);

            hasHandler = model.handlesEvent(EventConstants.PASSIVATE);

            cache.put(pageName, hasHandler);
        }

        // If no handler for the event, then no need to fire the event (and more importantly,
        // no need to obtain a page instance!)

        if (!hasHandler)
            return EMPTY;

        // Get or create a page instance and trigger the event.

        Page page = requestPageCache.get(pageName);

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
