// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

/**
 * For Tapestry IoC, Aspects provide a limited amount of advise, i.e., advising method invocations. That's the only join
 * point available (method invocations of service interface methods); full AOP systems such as AspectJ can do much, much
 * more, such as advising field access and even object construction.
 *
 * @see org.apache.tapestry5.ioc.services.AspectDecorator
 */
public interface MethodAdvice
{
    /**
     * Allows the Aspect to advise the invocation.  The Aspect is free to inspect and even replace parameters. Most
     * Aspects will then invoke {@link org.apache.tapestry5.ioc.Invocation#proceed()}.  The Aspect may then inspect and
     * replace any checked thrown exceptions. Some Aspects (for example, caching) may selectively decide to bypass the
     * invocation entirely, and instead invoke some other method or otherwise set a return value or thrown exception.
     *
     * @param invocation to advise
     */
    void advise(Invocation invocation);
}
