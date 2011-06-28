// Copyright 2008, 2010, 2011 The Apache Software Foundation
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

/**
 * An object that receives control around an "advised" method of a component. The advise can query or even replace
 * method parameters. After invoking {@link org.apache.tapestry5.services.ComponentMethodInvocation#proceed()}, the
 * advice may query and override thrown exceptions or the return value of the invocation.
 * 
 * @see TransformMethod#addAdvice(ComponentMethodAdvice)
 * @see ComponentInstanceOperation
 * @deprecated Deprecated in 5.3
 * @see org.apache.tapestry5.plastic.PlasticClass
 * @see org.apache.tapestry5.plastic.PlasticMethod
 * @see org.apache.tapestry5.plastic.MethodAdvice
 */
@SuppressWarnings({"deprecation"})
public interface ComponentMethodAdvice
{
    void advise(ComponentMethodInvocation invocation);
}
