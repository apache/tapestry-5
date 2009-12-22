// Copyright 2006, 2008 The Apache Software Foundation
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
 * The source for the module builder instance needed by most (but not all) service builders, service contributors and
 * service decorators. Allows the creation of the moduleBuilder instance to be deferred until actually needed; in
 * practical terms, when the builder/decorator/contributor is a <em>static</em> method on the module builder class, then
 * a module builder instance is not needed. This allows Tapestry IOC to work around a tricky chicken-and-the-egg
 * problem, whereby the constructor of a module builder instance requires contributions that originate in the same
 * module.
 */
public interface ModuleBuilderSource
{
    /**
     * Returns the instantiated version of the Tapestry IoC module class.
     */
    Object getModuleBuilder();
}
