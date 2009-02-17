//  Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * Fires the {@link org.apache.tapestry5.EventConstants#PASSIVATE} event on a page, and collects the result, converting
 * it to an array of objects.
 */
public interface PageActivationContextCollector
{
    /**
     * Fires the passivate event and collects the response, which is coerced to an object array. A page that does not
     * have an event handler for the passivate event will return an empty array.
     *
     * @param pageName to collect context from ; this should be the canonical page name
     * @return the activation context, or an empty array of the page does not provide a context
     */
    Object[] collectPageActivationContext(String pageName);
}
