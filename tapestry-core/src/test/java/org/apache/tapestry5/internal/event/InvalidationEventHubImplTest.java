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
package org.apache.tapestry5.internal.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.internal.services.ComponentTemplateSourceImplTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the parts of {@link InvalidationEventHubImpl} that {@link ComponentTemplateSourceImplTest}
 * doesn't. This is mostly for the resource-specific invalidations in
 * {@link InvalidationEventHubImpl#addInvalidationCallback(java.util.function.Function)}
 */
public class InvalidationEventHubImplTest 
{

    /**
     * Tests {@link InvalidationEventHubImpl#addInvalidationCallback(java.util.function.Function)}.
     */
    @Test
    public void add_invalidation_callback_with_parameter() 
    {
        InvalidationEventHubImpl invalidationEventHub = new InvalidationEventHubImpl(false);
        final String firstInitialElement = "a";
        final String secondInitialElement = "b";
        final List<String> initialResources = Arrays.asList(firstInitialElement, secondInitialElement);
        final AtomicInteger callCount = new AtomicInteger(0);
        Function<List<String>, List<String>> callback = (r) -> {
            callCount.incrementAndGet();
            if (r.size() == 2 && r.get(0).equals(firstInitialElement) && r.get(1).equals(secondInitialElement)) {
                return Arrays.asList(firstInitialElement.toUpperCase(), secondInitialElement.toUpperCase());
            }
            else if (r.size() == 2 && r.get(0).equals(firstInitialElement.toUpperCase()) && r.get(1).equals(secondInitialElement.toUpperCase())) {
                return Arrays.asList("something", "else");
            }
            else {
                return Collections.emptyList();
            }
        };
        
        invalidationEventHub.addInvalidationCallback(callback);
        invalidationEventHub.fireInvalidationEvent(initialResources);
        Assert.assertEquals(callCount.get(), 3, "Wrong call count");
        
    }
    
    @Test(expectedExceptions = TapestryException.class)
    public void null_check_for_callback_method() 
    {
        InvalidationEventHubImpl invalidationEventHub = new InvalidationEventHubImpl(false);
        invalidationEventHub.addInvalidationCallback((s) -> null);
        invalidationEventHub.fireInvalidationEvent();
    }
    
}
