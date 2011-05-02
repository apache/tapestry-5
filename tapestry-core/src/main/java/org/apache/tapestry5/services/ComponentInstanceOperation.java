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

package org.apache.tapestry5.services;

import org.apache.tapestry5.runtime.Component;

/**
 * An operation that requires an instance of a component.
 * This is a simpler alternative to a {@link ComponentMethodAdvice} that avoids
 * the most common error case: forgetting to invoke {@link ComponentMethodInvocation#proceed()} (which
 * can be very difficult to track down!).
 * 
 * @since 5.2.0
 * @see TransformMethod#addOperationAfter(ComponentInstanceOperation)
 * @see TransformMethod#addOperationBefore(ComponentInstanceOperation)
 * @deprecated Deprecated in 5.3 with no replacement
 */
public interface ComponentInstanceOperation
{
    /** Called to perform the desired operation on a component instance. */
    public void invoke(Component instance);
}
