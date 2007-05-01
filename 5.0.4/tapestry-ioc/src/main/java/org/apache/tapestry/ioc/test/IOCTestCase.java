// Copyright 2006, 2007 The Apache Software Foundation
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
import org.apache.tapestry.ioc.AnnotationProvider;
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
import org.apache.tapestry.ioc.ObjectLocator;
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

    protected final AnnotationProvider mockAnnotationProvider()
    {
        return newMock(AnnotationProvider.class);
    }

    @SuppressWarnings("unchecked")
    protected final <T> Configuration<T> mockConfiguration()
    {
        return newMock(Configuration.class);
    }

    protected final ContributionDef mockContributionDef()
    {
        return newMock(ContributionDef.class);
    }

    protected final DecoratorDef mockDecoratorDef()
    {
        return newMock(DecoratorDef.class);
    }

    protected final Location mockLocation()
    {
        return newMock(Location.class);
    }

    protected final Log mockLog()
    {
        return newMock(Log.class);
    }

    @SuppressWarnings("unchecked")
    protected final <K, V> MappedConfiguration<K, V> mockMappedConfiguration()
    {
        return newMock(MappedConfiguration.class);
    }

    protected final MessageFormatter mockMessageFormatter()
    {
        return newMock(MessageFormatter.class);
    }

    protected final Messages mockMessages()
    {
        return newMock(Messages.class);
    }

    protected final ModuleDef mockModuleDef()
    {
        return newMock(ModuleDef.class);
    }

    protected final ObjectCreator mockObjectCreator()
    {
        return newMock(ObjectCreator.class);
    }

    protected final ObjectProvider mockObjectProvider()
    {
        return newMock(ObjectProvider.class);
    }

    @SuppressWarnings("unchecked")
    protected final <T> OrderedConfiguration<T> mockOrderedConfiguration()
    {
        return newMock(OrderedConfiguration.class);
    }

    protected final Resource mockResource()
    {
        return newMock(Resource.class);
    }

    /** Frequently used as a placeholder for an arbitrary service (but its nice and simple). */
    protected final Runnable mockRunnable()
    {
        return newMock(Runnable.class);
    }

    protected final ServiceBuilderResources mockServiceBuilderResources()
    {
        return newMock(ServiceBuilderResources.class);
    }

    protected final ServiceDecorator mockServiceDecorator()
    {
        return newMock(ServiceDecorator.class);
    }

    protected final ServiceDef mockServiceDef()
    {
        return newMock(ServiceDef.class);
    }

    protected final ObjectLocator mockObjectLocator()
    {
        return newMock(ObjectLocator.class);
    }

    protected final ServiceResources mockServiceResources()
    {
        return newMock(ServiceResources.class);
    }

    protected final SymbolSource mockSymbolSource()
    {
        return newMock(SymbolSource.class);
    }

    protected final ThreadLocale mockThreadLocale()
    {
        return newMock(ThreadLocale.class);
    }

    protected final TypeCoercer mockTypeCoercer()
    {
        return newMock(TypeCoercer.class);
    }

    protected final void stub_contains(Messages messages, boolean contained)
    {
        expect(messages.contains(isA(String.class))).andStubReturn(contained);
    }

    protected <S, T> void train_coerce(TypeCoercer coercer, S input, Class<T> expectedType,
            T coercedValue)
    {
        expect(coercer.coerce(input, expectedType)).andReturn(coercedValue);
    }

    protected final void train_contains(Messages messages, String key, boolean result)
    {
        expect(messages.contains(key)).andReturn(result).atLeastOnce();
    }

    protected final void train_createInterceptor(ServiceDecorator decorator, Object coreObject,
            Object interceptor)
    {
        expect(decorator.createInterceptor(coreObject)).andReturn(interceptor);
    }

    protected final void train_createObject(ObjectCreator creator, Object service)
    {
        expect(creator.createObject()).andReturn(service);
    }

    protected final void train_expandSymbols(SymbolSource source, String input)
    {
        train_expandSymbols(source, input, input);

    }

    protected final void train_expandSymbols(SymbolSource source, String input, String expanded)
    {
        expect(source.expandSymbols(input)).andReturn(expanded);
    }

    protected final void train_forFile(Resource resource, String relativePath, Resource file)
    {
        expect(resource.forFile(relativePath)).andReturn(file);
    }

    protected final void train_forLocale(Resource base, Locale locale, Resource resource)
    {
        expect(base.forLocale(locale)).andReturn(resource);
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

    protected final void train_getLocale(ThreadLocale threadLocale, Locale locale)
    {
        expect(threadLocale.getLocale()).andReturn(locale);
    }

    protected final void train_getLog(LogSource source, String serviceId, Log log)
    {
        expect(source.getLog(serviceId)).andReturn(log).atLeastOnce();
    }

    protected final void train_getMessageFormatter(Messages messages, String key,
            MessageFormatter formatter)
    {
        expect(messages.getFormatter(key)).andReturn(formatter).atLeastOnce();
    }

    protected final void train_getPath(Resource r, String path)
    {
        expect(r.getPath()).andReturn(path).atLeastOnce();
    }

    protected final <T> void train_getService(ObjectLocator locator, Class<T> serviceInterface,
            T service)
    {
        expect(locator.getService(serviceInterface)).andReturn(service);
    }

    protected final <T> void train_getService(ObjectLocator locator, String serviceId,
            Class<T> serviceInterface, T service)
    {
        expect(locator.getService(serviceId, serviceInterface)).andReturn(service);
    }

    protected final void train_getServiceId(ServiceDef def, String serviceId)
    {
        expect(def.getServiceId()).andReturn(serviceId).atLeastOnce();
    }

    protected final void train_getServiceId(ServiceResources resources, String serviceId)
    {
        expect(resources.getServiceId()).andReturn(serviceId).atLeastOnce();

    }

    protected final void train_getServiceInterface(ServiceDef def, Class serviceInterface)
    {
        expect(def.getServiceInterface()).andReturn(serviceInterface).atLeastOnce();
    }

    protected final void train_getServiceInterface(ServiceResources resources,
            Class serviceInterface)
    {
        expect(resources.getServiceInterface()).andReturn(serviceInterface).atLeastOnce();
    }

    protected final void train_getServiceLog(ServiceResources resources, Log log)
    {
        expect(resources.getServiceLog()).andReturn(log).atLeastOnce();

    }

    protected final void train_isDebugEnabled(Log log, boolean debugEnabled)
    {
        expect(log.isDebugEnabled()).andReturn(debugEnabled);
    }

    protected final void train_matches(DecoratorDef decoratorDef, ServiceDef serviceDef,
            boolean matches)
    {
        expect(decoratorDef.matches(serviceDef)).andReturn(matches);
    }

    protected final <T> void train_provide(ObjectProvider provider, Class<T> objectType,
            AnnotationProvider annotationProvider, ObjectLocator locator, T object)
    {
        expect(provider.provide(objectType, annotationProvider, locator)).andReturn(object);
    }

    protected final void train_toURL(Resource resource, URL url)
    {
        expect(resource.toURL()).andReturn(url).atLeastOnce();
    }
}
