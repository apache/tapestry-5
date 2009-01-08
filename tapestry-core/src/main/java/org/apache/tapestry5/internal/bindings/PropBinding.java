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

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.internal.services.Invariant;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;

import java.lang.annotation.Annotation;

/**
 * Base class for bindings created by the {@link org.apache.tapestry5.internal.bindings.PropBindingFactory}. A subclass
 * of this is created at runtime.
 */
public class PropBinding extends AbstractBinding
{
    private final Object root;

    private final PropertyConduit conduit;

    private final String toString;

    private boolean invariant;

    public PropBinding(final Location location, final Object root, final PropertyConduit conduit, final String toString
    )
    {
        super(location);

        this.root = root;
        this.conduit = conduit;
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
     * Almost always returns false, unless the conduit provides the {@link org.apache.tapestry5.internal.services.Invariant}
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

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return conduit.getAnnotation(annotationClass);
    }
}
