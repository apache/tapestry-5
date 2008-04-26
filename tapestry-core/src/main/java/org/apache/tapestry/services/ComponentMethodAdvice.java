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

package org.apache.tapestry.services;

/**
 * An object that receives control around an "advised" method of a component. The advise can query or even replace
 * method parameters.  After invoking {@link org.apache.tapestry.services.ComponentMethodInvocation#proceed()}, the
 * advise may query and override thrown exceptions or the return value of the invocation.
 *
 * @see ClassTransformation#advise(TransformMethodSignature, ComponentMethodAdvice)
 */
public interface ComponentMethodAdvice
{
    void advise(ComponentMethodInvocation invocation);
}
