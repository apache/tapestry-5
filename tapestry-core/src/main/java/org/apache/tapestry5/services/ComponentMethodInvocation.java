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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Invocation;

/**
 * Encapsulates the parameters, thrown exceptions, and result of a method invocation, allowing a {@link
 * org.apache.tapestry5.services.ComponentMethodAdvice} to encapsulate the invocation.
 */
public interface ComponentMethodInvocation extends Invocation
{
    /**
     * Returns the component resources for the component whose method is being intercepted.
     */
    ComponentResources getComponentResources();
}
