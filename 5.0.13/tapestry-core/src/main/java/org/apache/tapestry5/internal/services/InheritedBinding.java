// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ioc.BaseLocatable;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;

import java.lang.annotation.Annotation;

/**
 * Wraps another binding, adjusting the description of the binding and the location of the binding (as reported in any
 * thrown exceptions).
 */
public class InheritedBinding extends BaseLocatable implements Binding
{
    private final String toString;

    private final Binding binding;

    public InheritedBinding(String toString, Binding binding, Location location)
    {
        super(location);

        this.toString = toString;
        this.binding = binding;
    }

    @Override
    public String toString()
    {
        return toString;
    }

    public Object get()
    {
        try
        {
            return binding.get();
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), this, ex);
        }
    }

    public Class getBindingType()
    {
        try
        {
            return binding.getBindingType();
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), this, ex);
        }
    }

    public boolean isInvariant()
    {
        try
        {
            return binding.isInvariant();
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), this, ex);
        }
    }

    public void set(Object value)
    {
        try
        {
            binding.set(value);
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), this, ex);
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        try
        {
            return binding.getAnnotation(annotationClass);
        }
        catch (Exception ex)
        {
            throw new TapestryException(ex.getMessage(), this, ex);
        }
    }

}
