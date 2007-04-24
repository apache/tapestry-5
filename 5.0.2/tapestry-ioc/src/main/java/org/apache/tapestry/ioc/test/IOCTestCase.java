// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.test;

import static org.easymock.EasyMock.isA;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.LogSource;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.ioc.services.TypeCoercer;

/** Add factory and trainer methods for the public interfaces of Tapestry IOC. */
public class IOCTestCase extends TestBase
{
    protected final Registry buildRegistry(Class... moduleClasses)
    {
        RegistryBuilder builder = new RegistryBuilder();

        builder.add(moduleClasses);

        return builder.build();
    }

    protected final Method findMethod(Class clazz, String methodName)
    {
        for (Method method : clazz.getMethods())
        {
            if (method.getName().equals(methodName)) return method;
        }

        throw new IllegalArgumentException(String.format(
                "Class %s does not provide a method named '%s'.",
                clazz.getName(),
                methodName));
    }

    protected final Method findMethod(Object subject, String methodName)
    {
        return findMethod(subject.getClass(), methodName);
    }

    protected final Method findMethod(String methodName)
    {
        return findMethod(this, methodName);
    }

    protected final Resource newResource()
    {
        return newMock(Resource.class);
    }

    protected final ServiceLocator newServiceLocator()
    {
        return newMock(ServiceLocator.class);
    }

    protected final ContributionDef newContributionDef()
    {
        return newMock(ContributionDef.class);
    }

    @SuppressWarnings("unchecked")
    protected final <T> OrderedConfiguration<T> newOrderedConfiguration()
    {
        return newMock(OrderedConfiguration.class);
    }

    protected final void train_getServiceInterface(ServiceDef def, Class serviceInterface)
    {
        expect(def.getServiceInterface()).andReturn(serviceInterface).atLeastOnce();
    }

    protected final void train_getServiceId(ServiceDef def, String serviceId)
    {
        expect(def.getServiceId()).andReturn(serviceId).atLeastOnce();
    }

    protected final ServiceResources newServiceResources()
    {
        return newMock(ServiceResources.class);
    }

    protected final ServiceDef newServiceDef()
    {
        return newMock(ServiceDef.class);
    }

    protected final void train_getServiceLog(ServiceResources resources, Log log)
    {
        expect(resources.getServiceLog()).andReturn(log).atLeastOnce();

    }

    protected final <T> void train_getService(ServiceLocator locator, String serviceId,
            Class<T> serviceInterface, T service)
    {
        expect(locator.getService(serviceId, serviceInterface)).andReturn(service);
    }

    protected final <T> void train_getService(ServiceLocator locator, Class<T> serviceInterface,
            T service)
    {
        expect(locator.getService(serviceInterface)).andReturn(service);
    }

    protected final void train_createObject(ObjectCreator creator, Object service)
    {
        expect(creator.createObject()).andReturn(service);
    }

    protected final ObjectProvider newObjectProvider()
    {
        return newMock(ObjectProvider.class);
    }

    protected final <T> void train_provide(ObjectProvider provider, String expression,
            Class<T> objectType, ServiceLocator locator, T object)
    {
        expect(provider.provide(expression, objectType, locator)).andReturn(object);
    }

    protected final ObjectCreator newObjectCreator()
    {
        return newMock(ObjectCreator.class);
    }

    protected final void train_getServiceInterface(ServiceResources resources,
            Class serviceInterface)
    {
        expect(resources.getServiceInterface()).andReturn(serviceInterface).atLeastOnce();
    }

    protected final void train_getServiceId(ServiceResources resources, String serviceId)
    {
        expect(resources.getServiceId()).andReturn(serviceId).atLeastOnce();

    }

    protected final void train_createInterceptor(ServiceDecorator decorator, Object coreObject,
            Object interceptor)
    {
        expect(decorator.createInterceptor(coreObject)).andReturn(interceptor);
    }

    protected final ServiceBuilderResources newServiceCreatorResources()
    {
        return newMock(ServiceBuilderResources.class);
    }

    protected final ServiceDecorator newServiceDecorator()
    {
        return newMock(ServiceDecorator.class);
    }

    protected final void train_getLog(LogSource source, String serviceId, Log log)
    {
        expect(source.getLog(serviceId)).andReturn(log).atLeastOnce();
    }

    protected final void train_getModuleId(ModuleDef def, String moduleId)
    {
        expect(def.getModuleId()).andReturn(moduleId).atLeastOnce();
    }

    /** Frequently used as a placeholder for an arbitrary service (but its nice and simple). */
    protected final Runnable newRunnable()
    {
        return newMock(Runnable.class);
    }

    protected final ModuleDef newModuleDef()
    {
        return newMock(ModuleDef.class);
    }

    protected final void train_matches(DecoratorDef decoratorDef, ServiceDef serviceDef,
            boolean matches)
    {
        expect(decoratorDef.matches(serviceDef)).andReturn(matches);
    }

    protected final DecoratorDef newDecoratorDef()
    {
        return newMock(DecoratorDef.class);
    }

    protected final Location newLocation()
    {
        return newMock(Location.class);
    }

    /** Combines a series of lines by forming a string with a line separator after each line. */
    protected final String join(String... lines)
    {
        StringBuilder result = new StringBuilder();

        for (String line : lines)
        {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }

    protected <S, T> void train_coerce(TypeCoercer coercer, S input, Class<T> expectedType,
            T coercedValue)
    {
        expect(coercer.coerce(input, expectedType)).andReturn(coercedValue);
    }

    protected final TypeCoercer newTypeCoercer()
    {
        return newMock(TypeCoercer.class);
    }

    @SuppressWarnings("unchecked")
    protected final <T> Configuration<T> newConfiguration()
    {
        return newMock(Configuration.class);
    }

    @SuppressWarnings("unchecked")
    protected final <K, V> MappedConfiguration<K, V> newMappedConfiguration()
    {
        return newMock(MappedConfiguration.class);
    }

    protected final void train_expandSymbols(SymbolSource source, String input)
    {
        train_expandSymbols(source, input, input);

    }

    protected final void train_expandSymbols(SymbolSource source, String input, String expanded)
    {
        expect(source.expandSymbols(input)).andReturn(expanded);
    }

    protected final SymbolSource newSymbolSource()
    {
        return newMock(SymbolSource.class);
    }

    protected final ThreadLocale newThreadLocale()
    {
        return newMock(ThreadLocale.class);
    }

    protected final Messages newMessages()
    {
        return newMock(Messages.class);
    }

    protected final void train_toURL(Resource resource, URL url)
    {
        expect(resource.toURL()).andReturn(url).atLeastOnce();
    }

    protected final void train_getPath(Resource r, String path)
    {
        expect(r.getPath()).andReturn(path).atLeastOnce();
    }

    protected final void train_getLocale(ThreadLocale threadLocale, Locale locale)
    {
        expect(threadLocale.getLocale()).andReturn(locale);
    }

    protected final Log newLog()
    {
        return newMock(Log.class);
    }

    protected final void train_isDebugEnabled(Log log, boolean debugEnabled)
    {
        expect(log.isDebugEnabled()).andReturn(debugEnabled);
    }

    protected final void train_contains(Messages messages, String key, boolean result)
    {
        expect(messages.contains(key)).andReturn(result).atLeastOnce();
    }

    protected final void train_getMessageFormatter(Messages messages, String key,
            MessageFormatter formatter)
    {
        expect(messages.getFormatter(key)).andReturn(formatter).atLeastOnce();
    }

    protected final MessageFormatter newMessageFormatter()
    {
        return newMock(MessageFormatter.class);
    }

    /** Have to put the result before the varargs. */
    protected void train_format(MessageFormatter formatter, String result, Object... arguments)
    {
        expect(formatter.format(arguments)).andReturn(result);
    }

    protected final void train_get(Messages messages, String key, String message)
    {
        expect(messages.get(key)).andReturn(message).atLeastOnce();
    }

    protected final void stub_contains(Messages messages, boolean contained)
    {
        expect(messages.contains(isA(String.class))).andStubReturn(contained);
    }
}
