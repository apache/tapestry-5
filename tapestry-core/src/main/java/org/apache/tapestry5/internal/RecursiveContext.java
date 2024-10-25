// Licensed to the Apache License, Version 2.0 (the "License");
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.commons.RecursiveValue;
import org.apache.tapestry5.corelib.components.Recursive;
import org.apache.tapestry5.corelib.components.RecursiveBody;
import org.apache.tapestry5.dom.Element;

/**
 * <p>
 * Class that makes the link between {@link Recursive} and {@link RecursiveBody}. 
 * </p>
 * <p>
 * This was contributed by <a href="https://www.pubfactory.com">KGL PubFactory</a>.
 * </p>
 * @since 5.9.0
 */
final public class RecursiveContext
{

    private final Provider provider;

    public RecursiveContext(Provider provider) 
    {
        this.provider = provider;
    }

    public String getId() 
    {
        return provider.getClientIdForCurrent();
    }

    public RecursiveValue<?> getCurrent() 
    {
        return provider.getCurrent();
    }

    public Object getValue() 
    {
        return provider.getCurrent().getValue();
    }

    public void registerPlaceholder(Element element) 
    {
        provider.registerPlaceholder(element);
    }

    public static interface Provider 
    {
        RecursiveValue<?> getCurrent();

        String getClientIdForCurrent();

        void registerPlaceholder(Element element);
    }
    
}