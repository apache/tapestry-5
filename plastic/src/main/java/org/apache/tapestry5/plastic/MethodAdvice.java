// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic;

/**
 * MethodAdvice is a special callback that is threaded into the implementation of a method.
 * The advice recieves a {@link MethodInvocation}, which gives the advice the ability to change
 * parameters or return values or thrown exceptions. In many cases, new behavior is added around the method invocation
 * with affecting it; common examples include logging, null checks, transaction management, or security checks.
 */
public interface MethodAdvice
{
    /**
     * Advise the method, usually invoking {@link MethodInvocation#proceed()} at some point.
     * The advice is free to inspect and even replace parameters. Most
     * Aspects will then invoke {@link MethodInvocation#proceed()}. The advice may then inspect and
     * replace any checked thrown exceptions. Some advice (for example, caching) may selectively decide to bypass the
     * invocation entirely, and instead invoke some other method or otherwise set a return value or thrown exception.
     * 
     * @param invocation
     *            identifies the method being invoked, including parameters
     */
    void advise(MethodInvocation invocation);
}
