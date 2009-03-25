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

package org.apache.tapestry5.ioc;

/**
 * Extension to {@link org.apache.tapestry5.ioc.ServiceLifecycle} that adds an additional method.
 */
public interface ServiceLifecycle2   extends ServiceLifecycle
{
    /**
     * If true, then lifecycle requires a proxy, meaning it is only useable with services that properly define a service
     * interface. The default (singleton) scope does not require a proxy, but most other service scopes do. The default
     * (when wrapping a {@link org.apache.tapestry5.ioc.ServiceLifecycle} as a {@link
     * org.apache.tapestry5.ioc.ServiceLifecycle2} is to return true.
     *
     * @return true if proxying is necesssary, false otherwise
     */
    boolean requiresProxy();
}
