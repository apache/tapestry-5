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

package org.apache.tapestry.internal.bindings;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.tapestry.AnnotationProvider;
import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.corelib.components.BeanEditForm;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.services.BeanEditorModelSource;
import org.apache.tapestry.services.BindingFactory;

/**
 * Binding factory for reading and updating JavaBean properties. Uses
 * {@link org.apache.tapestry.ioc.services.PropertyAccess} to analyze the properties, and generates
 * a binding class using the component {@link org.apache.tapestry.ioc.services.ClassFactory}.
 * <p>
 * The {@link Binding} object that's returned is from a runtime-genereated class. The property
 * expression is turned into type-safe Java code.
 * <p>
 * The expression is a dotted sequence of <em>terms</em>. Each term is either a property name, or
 * the name of a public method. In the latter case, the term includes open and close parenthesis
 * (the method must take no parameters and throw no checked exceptions). A method name is treated as
 * if it were a <em>read-only property</em>.
 * <p>
 * Example property expressions:
 * <ul>
 * <li>userName</li>
 * <li>userData.name</li>
 * <li>usreList.size()</li>
 * </ul>
 * <p>
 * Everything works in terms of the property or methods <em>declared type</em>, not the
 * <em>actual type</em>. This means that, for example, if a property is type Collection, you may
 * <em>not</em> reference the listIterator() method, even if the actual value returned is of type
 * List. This is only really bites you when a method's return type is Object.
 * <p>
 * TODO: Split most of this out into a new service that generates a PropertyConduit. This will allow
 * the {@link BeanEditForm}/{@link BeanEditorModelSource} make use of the same expressions that
 * are supported here.
 */
public class PropBindingFactory implements BindingFactory, InvalidationListener
{
    private static final String PARENS = "()";

    private final PropertyAccess _access;

    private final ClassFactory _classFactory;

    // The key is a combination of class name and property path.
    private final Map<String, BindingConstructor> _cache = newThreadSafeMap();

    private static class BindingConstructor
    {
        private final Class _propertyType;

        private final Constructor _constructor;

        private final AnnotationProvider _annotationProvider;

        BindingConstructor(Class propertyType, AnnotationProvider annotationProvider,
                Constructor constructor)
        {
            _propertyType = propertyType;
            _constructor = constructor;
            _annotationProvider = annotationProvider;
        }

        Binding newBindingInstance(Object target, String toString, Location location)
                throws Exception
        {
            return (Binding) _constructor.newInstance(
                    target,
                    _propertyType,
                    toString,
                    _annotationProvider,
                    location);
        }
    }

    private static final MethodSignature GET_SIGNATURE = new MethodSignature(Object.class, "get",
            null, null);

    private static final MethodSignature SET_SIGNATURE = new MethodSignature(void.class, "set",
            new Class[]
            { Object.class }, null);

    public PropBindingFactory(PropertyAccess propertyAccess, ClassFactory classFactory)
    {
        _access = propertyAccess;
        _classFactory = classFactory;
    }

    public Binding newBinding(String description, ComponentResources container,
            ComponentResources component, String expression, Location location)
    {
        Object target = container.getComponent();
        Class targetClass = target.getClass();

        try
        {
            BindingConstructor cons = findCachedConstructor(targetClass, expression);

            String toString = String.format("PropBinding[%s %s(%s)]", description, container
                    .getCompleteId(), expression);

            return cons.newBindingInstance(target, toString, location);
        }
        catch (Throwable ex)
        {
            throw new TapestryException(ex.getMessage(), location, ex);
        }
    }

    /**
     * Searches for a cached binding constructor for the given target class and property name. As
     * necessary, a new cached constructor is created.
     * 
     * @param targetClass
     *            the class of the component which contains the property
     * @param propertyName
     *            the name of the property to expose as a binding
     * @return
     */
    private BindingConstructor findCachedConstructor(Class targetClass, String propertyName)
    {
        // The only problem with this key is if we can get in a situation where two different
        // versions of the class (from the default class loader, and the component class loader)
        // are accessed in this way at the same time. I'm pretty sure that can't happen.

        String key = targetClass.getName() + ":" + propertyName;

        BindingConstructor result = _cache.get(key);

        if (result == null)
        {
            result = createConstructor(key, targetClass, propertyName);
            _cache.put(key, result);
        }

        return result;
    }

    private BindingConstructor createConstructor(String key, Class targetClass,
            String propertyExpression)
    {
        // Race condition: simultaneous calls to createConstructor() for the same
        // targetClass/propertyExpression combination may result in duplicate binding classes being
        // created, which causes no great harm.

        StringBuilder builder = new StringBuilder("_target");

        Class step = targetClass;
        String[] terms = propertyExpression.split("\\.");

        for (int i = 0; i < terms.length - 1; i++)
        {
            String term = terms[i];

            boolean isMethodReference = term.endsWith(PARENS);

            step = isMethodReference ? extendWithMethodTerm(step, term, propertyExpression, builder)
                    : extendWithPropertyTerm(step, term, propertyExpression, builder);
        }

        String terminal = terms[terms.length - 1];

        Class bindingType = null;
        Method readMethod = null;
        Method writeMethod = null;

        if (terminal.endsWith(PARENS))
        {
            readMethod = validateMethodName(step, terminal, propertyExpression);
            bindingType = readMethod.getReturnType();
        }
        else
        {
            PropertyAdapter adapter = _access.getAdapter(step).getPropertyAdapter(terminal);

            if (adapter == null)
                throw new RuntimeException(BindingsMessages.noSuchProperty(
                        step,
                        terminal,
                        propertyExpression));

            bindingType = adapter.getType();
            readMethod = adapter.getReadMethod();
            writeMethod = adapter.getWriteMethod();
        }

        Class bindingClass = createBindingClass(
                targetClass,
                builder.toString(),
                terminal,
                bindingType,
                readMethod,
                writeMethod);

        // The fabricated class is only going to have the one constructor. This is the easiest
        // way to access it.

        return new BindingConstructor(bindingType, new PropBindingAnnotationProvider(readMethod,
                writeMethod), bindingClass.getConstructors()[0]);
    }

    private Class extendWithMethodTerm(Class inClass, String term, String propertyExpression,
            StringBuilder builder)
    {
        Method method = validateMethodName(inClass, term, propertyExpression);

        builder.append(".");
        builder.append(term);

        return method.getReturnType();
    }

    private Method validateMethodName(Class inClass, String term, String propertyExpression)
    {
        String methodName = term.substring(0, term.length() - PARENS.length());

        try
        {
            // Find a public method that takes no parameters.

            Method method = inClass.getMethod(methodName);

            if (method.getReturnType().equals(void.class))
                throw new RuntimeException(BindingsMessages.methodIsVoid(
                        term,
                        inClass,
                        propertyExpression));

            return method;

        }
        catch (NoSuchMethodException ex)
        {
            throw new RuntimeException(BindingsMessages.methodNotFound(
                    term,
                    inClass,
                    propertyExpression), ex);
        }
    }

    private Class extendWithPropertyTerm(Class inClass, String term, String propertyExpression,
            StringBuilder builder)
    {
        PropertyAdapter pa = _access.getAdapter(inClass).getPropertyAdapter(term);

        if (pa == null)
            throw new RuntimeException(BindingsMessages.noSuchProperty(
                    inClass,
                    term,
                    propertyExpression));

        Method m = pa.getReadMethod();

        if (m == null)
            throw new RuntimeException(BindingsMessages.writeOnlyProperty(
                    term,
                    inClass,
                    propertyExpression));

        builder.append(".");
        builder.append(m.getName());
        builder.append(PARENS);

        return pa.getType();
    }

    /**
     * @param targetClass
     *            root class of expression
     * @param pathExpression
     *            Javassist expression to navigate to the terminal property, starting with the
     *            _target instance variable
     * @param propertyName
     *            the name of the terminal property
     * @return Class that implements Binding
     */
    private Class createBindingClass(Class targetClass, String pathExpression, String propertyName,
            Class propertyType, Method readMethod, Method writeMethod)
    {
        String name = ClassFabUtils.generateClassName("PropBinding");

        ClassFab classFab = _classFactory.newClass(name, BasePropBinding.class);

        classFab.addField("_target", targetClass);

        classFab
                .addConstructor(
                        new Class[]
                        { targetClass, Class.class, String.class, AnnotationProvider.class,
                                Location.class },
                        null,
                        "{ super($2, $3, $4, $5); _target = $1; }");

        if (readMethod != null)
        {
            String body = String.format("return ($w) %s.%s();", pathExpression, readMethod
                    .getName());

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, body);
        }

        if (writeMethod != null)
        {
            BodyBuilder builder = new BodyBuilder();
            builder.begin();

            String propertyTypeName = propertyType.getName();
            builder.add("%s value = ", propertyTypeName);

            if (propertyType.isPrimitive())
            {
                String wrapperType = ClassFabUtils.getWrapperTypeName(propertyTypeName);
                String unwrapMethod = ClassFabUtils.getUnwrapMethodName(propertyTypeName);

                // Cast the value to the wrapper type, and then extract the primitive
                // value from that.

                builder.addln("((%s) $1).%s();", wrapperType, unwrapMethod);
            }
            else
                builder.addln("(%s) $1;", propertyTypeName);

            builder.addln("%s.%s(value);", pathExpression, writeMethod.getName());
            builder.end();

            classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());
        }

        return classFab.createClass();
    }

    /**
     * The cache contains references to classes loaded by the Tapestry class loader. When that
     * loader is invalidated (due to class file changes), the cache is cleared and rebuilt.
     */
    public void objectWasInvalidated()
    {
        _cache.clear();
    }

}
