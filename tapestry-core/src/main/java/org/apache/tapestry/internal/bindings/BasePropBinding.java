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

import java.lang.annotation.Annotation;

import org.apache.tapestry.AnnotationProvider;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;

/**
 * Base class for bindings created by the
 * {@link org.apache.tapestry.internal.bindings.PropBindingFactory}. A subclass of this is created
 * at runtime.
 */
public abstract class BasePropBinding extends AbstractBinding
{
    private final String _toString;

    private final Class _bindingType;

    private final AnnotationProvider _annotationProvider;

    public BasePropBinding(Class bindingType, String toString,
            AnnotationProvider annotationProvider, Location location)
    {
        super(location);

        _bindingType = bindingType;
        _toString = toString;
        _annotationProvider = annotationProvider;
    }

    /**
     * The default implementation of get() will throw a TapestryException (binding is write only).
     * The fabricated subclass <em>may</em> override this method (as well as set()).
     */
    public Object get()
    {
        throw new TapestryException(BindingsMessages.bindingIsWriteOnly(this), this, null);
    }

    @Override
    public String toString()
    {
        return _toString;
    }

    /** Returns false; these properties are always dynamic. */
    @Override
    public boolean isInvariant()
    {
        return false;
    }

    @Override
    public Class getBindingType()
    {
        return _bindingType;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return _annotationProvider.getAnnotation(annotationClass);
    }
}