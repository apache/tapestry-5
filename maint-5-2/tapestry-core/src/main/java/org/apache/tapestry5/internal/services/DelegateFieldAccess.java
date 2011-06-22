// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.TransformField;

/**
 * A temporary version of {@link FieldAccess} returned in some circumstances from {@link TransformField#getAccess()}.
 * {@linkplain TransformField#replaceAccess(org.apache.tapestry5.ioc.services.FieldValueConduit) replaced} (with a
 * {@link FieldValueConduit}), the delegate of this class may be pointed directly at the FieldValueConduit (through an
 * adapter) and no extra classes or static methods (on the component class) will need to be constructed.
 * 
 * @since 5.2.0
 */
class DelegateFieldAccess implements FieldAccess
{
    // Technically, this field should be volatile or synchronized; I hope it falls under the proper
    // synchronization umbrella by the time its actually used in a live component class.
    
    FieldAccess delegate;

    public Object read(Object instance)
    {
        return delegate.read(instance);
    }

    public void write(Object instance, Object value)
    {
        delegate.write(instance, value);
    }
}
