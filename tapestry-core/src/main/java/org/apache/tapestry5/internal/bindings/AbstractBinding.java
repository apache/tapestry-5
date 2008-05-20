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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ioc.BaseLocatable;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;

import java.lang.annotation.Annotation;

/**
 * Abstract base class for bindings. Assumes that the binding is read only and invariant. Subclasses must provide an
 * implementation of {@link Binding#get()}.
 */
public abstract class AbstractBinding extends BaseLocatable implements Binding
{
    public AbstractBinding()
    {
        this(null);
    }

    protected AbstractBinding(Location location)
    {
        super(location);
    }

    /**
     * @throws TapestryException always
     */
    public void set(Object value)
    {
        throw new TapestryException(BindingsMessages.bindingIsReadOnly(this), this, null);
    }

    /**
     * Returns true. Subclasses that do not supply a fixed, read-only value should override this method to return
     * false.
     */
    public boolean isInvariant()
    {
        return true;
    }

    /**
     * Returns the actual class, by invoking {@link Binding#get()}. Subclasses may override this method to work more
     * efficiently (say, when the binding type is known statically).
     */
    public Class getBindingType()
    {
        return get().getClass();
    }

    /**
     * Always returns null. Bindings that provide access to a method or field will override this method to return the
     * appropriate annotation.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return null;
    }

}
