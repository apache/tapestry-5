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

package org.apache.tapestry5.services.transform;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;

/**
 * Provides some form of injection when the value for an {@link org.apache.tapestry5.ioc.annotations.Inject} annotation is
 * present. In this case, the provider is responsible for determining the value to be injected from the field name and
 * field type.
 * 
 * This interface will be used as part of a {@link org.apache.tapestry5.ioc.services.ChainBuilder chain of command}.
 */
@UsesOrderedConfiguration(InjectionProvider2.class)
public interface InjectionProvider2
{
    /**
     * Perform the injection, if possible. Most often, this will result in a call to {@link
     * org.apache.tapestry5.plastic.PlasticField#inject(Object)}. The caller is responsible for invoking {@link
     * org.apache.tapestry5.plastic.PlasticField#claim(Object)}.
     *
     * @param field          that has the {@link org.apache.tapestry5.ioc.annotations.Inject} annotation
     * @param locator        allows services to be located
     * @param componentModel defines the relevant aspects of the component
     * @return true if an injection has been made (terminates the command chain), false to continue down the chain
     */
    boolean provideInjection(PlasticField field, ObjectLocator locator,
                             MutableComponentModel componentModel);
}

