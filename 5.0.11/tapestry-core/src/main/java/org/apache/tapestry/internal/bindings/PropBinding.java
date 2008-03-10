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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;

import java.lang.annotation.Annotation;

/**
 * Base class for bindings created by the {@link org.apache.tapestry.internal.bindings.PropBindingFactory}. A subclass
 * of this is created at runtime.
 */
public class PropBinding extends AbstractBinding
{
    private final Object _root;

    private final PropertyConduit _conduit;

    private final String _toString;

    public PropBinding(final Object root, final PropertyConduit conduit, final String toString,
                       final Location location)
    {
        super(location);

        _root = root;
        _conduit = conduit;
        _toString = toString;
    }

    /**
     * The default implementation of get() will throw a TapestryException (binding is write only). The fabricated
     * subclass <em>may</em> override this method (as well as set()).
     */
    public Object get()
    {
        try
        {
            return _conduit.get(_root);
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
            _conduit.set(_root, value);
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), getLocation(), ex);
        }
    }

    @Override
    public String toString()
    {
        return _toString;
    }

    /**
     * Returns false; these properties are always dynamic.
     */
    @Override
    public boolean isInvariant()
    {
        return false;
    }

    @Override
    public Class getBindingType()
    {
        return _conduit.getPropertyType();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return _conduit.getAnnotation(annotationClass);
    }

}