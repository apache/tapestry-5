// Copyright 2006, 2007, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

/**
 * Allows for a number of anonymous injections based on the type of field that is to be injected.
 */
public class CommonResourcesInjectionProvider implements InjectionProvider
{
    private static ComponentValueProvider<ComponentResourceSelector> selectorProvider = new ComponentValueProvider<ComponentResourceSelector>()
    {
        public ComponentResourceSelector get(ComponentResources resources)
        {
            return resources.getResourceSelector();
        }
    };

    private static ComponentValueProvider<Messages> messagesProvider = new ComponentValueProvider<Messages>()
    {

        public Messages get(ComponentResources resources)
        {
            return resources.getMessages();
        }
    };

    private static ComponentValueProvider<Locale> localeProvider = new ComponentValueProvider<Locale>()
    {

        public Locale get(ComponentResources resources)
        {
            return resources.getLocale();
        }
    };

    private static ComponentValueProvider<Logger> loggerProvider = new ComponentValueProvider<Logger>()
    {

        public Logger get(ComponentResources resources)
        {
            return resources.getLogger();
        };
    };

    private static ComponentValueProvider<String> completeIdProvider = new ComponentValueProvider<String>()
    {
        public String get(ComponentResources resources)
        {
            return resources.getCompleteId();
        }
    };

    private static final Map<Class, ComponentValueProvider> configuration = CollectionFactory.newMap();

    {
        configuration.put(ComponentResourceSelector.class, selectorProvider);
        configuration.put(Messages.class, messagesProvider);
        configuration.put(Locale.class, localeProvider);
        configuration.put(Logger.class, loggerProvider);
        configuration.put(String.class, completeIdProvider);
    }

    @SuppressWarnings("unchecked")
    public boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
            ClassTransformation transformation, MutableComponentModel componentModel)
    {
        ComponentValueProvider provider = configuration.get(fieldType);

        if (provider == null)
            return false;

        transformation.getField(fieldName).injectIndirect(provider);

        return true;
    }
}
