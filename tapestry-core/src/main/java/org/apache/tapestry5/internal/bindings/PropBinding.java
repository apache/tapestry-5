// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.PropertyConduit2;
import org.apache.tapestry5.beanmodel.internal.services.Invariant;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.internal.TapestryInternalUtils;

/**
 * Base class for bindings created by the {@link org.apache.tapestry5.internal.bindings.PropBindingFactory}. A subclass
 * of this is created at runtime.
 */
public class PropBinding extends AbstractBinding implements InternalPropBinding
{
    private final Object root;

    private final PropertyConduit conduit;

    private final String toString;

    private boolean invariant;
    
    private final String expression;

    public PropBinding(final Location location, final Object root, final PropertyConduit conduit, final String expression, final String toString)
    {
        super(location);

        this.root = root;
        this.conduit = conduit;
        this.expression = expression.intern();
        this.toString = toString;

        invariant = conduit.getAnnotation(Invariant.class) != null;
    }

    /**
     * The default implementation of get() will throw a TapestryException (binding is write only). The fabricated
     * subclass <em>may</em> override this method (as well as set()).
     */
    public Object get()
    {
        try
        {
            return conduit.get(root);
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), getLocation(), ex);
        }
    }

    @Override
    public void set(Object value)
    {
        try
        {
            conduit.set(root, value);
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), getLocation(), ex);
        }
    }

    @Override
    public String toString()
    {
        return toString;
    }

    /**
     * Almost always returns false, unless the conduit provides the {@link org.apache.tapestry5.beanmodel.internal.services.Invariant}
     * annotation.
     */
    @Override
    public boolean isInvariant()
    {
        return invariant;
    }

    @Override
    public Class getBindingType()
    {
        return conduit.getPropertyType();
    }
    
    /**
     * Get the generic type from the underlying property
     * 
     * @see PropertyConduit2#getPropertyGenericType()
     */
    @Override
    public Type getBindingGenericType()
    {
        if (conduit instanceof PropertyConduit2) {
            return ((PropertyConduit2) conduit).getPropertyGenericType();
        }
        return conduit.getPropertyType();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return conduit.getAnnotation(annotationClass);
    }

    public String getPropertyName() 
    {
        return TapestryInternalUtils.toInternalPropertyConduit(conduit).getPropertyName();
    }

    public String getExpression()
    {
        return expression;
    }
    
}
