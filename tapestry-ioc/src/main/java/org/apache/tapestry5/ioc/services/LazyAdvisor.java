// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;

/**
 * An advisor that identifies methods which can be evaluated lazily and advises them. A method can be evaluated lazily
 * if it returns an interface type and if it throws no checked exceptions. Lazy evaluation should be handled carefully,
 * as if any of the parameters to a method are mutable, or the internal state of the invoked service changes, the lazily
 * evaluated results may not match the immediately evaluated result. This effect is greatly exaggerated if the lazy
 * return object is evaluated in a different thread than when it was generated.
 * <p/>
 * Another consideration is that exceptions that would occur immediately in the non-lazy case are also deferred, often
 * losing much context in the process.
 * <p/>
 * Use laziness with great care.
 * <p/>
 * Use the {@link org.apache.tapestry5.ioc.annotations.NotLazy} annotation on methods that should not be advised.
 *
 * @since 5.1.0.0
 */
public interface LazyAdvisor
{
    void addLazyMethodInvocationAdvice(MethodAdviceReceiver methodAdviceReceiver);

}
