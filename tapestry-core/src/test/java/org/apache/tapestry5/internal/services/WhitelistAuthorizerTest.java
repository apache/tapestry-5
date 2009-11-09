// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/*
 * Created on Jul 28, 2007
 * 
 * 
 */
package org.apache.tapestry5.internal.services;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.tapestry5.internal.services.WhitelistAuthorizer;
import org.apache.tapestry5.services.AssetPathAuthorizer;

public class WhitelistAuthorizerTest extends Assert {
    
    @Test
    public void run()
    {
        WhitelistAuthorizer auth = new WhitelistAuthorizer(Arrays.asList("foo"));
        assertEquals(auth.order().get(0), AssetPathAuthorizer.Order.ALLOW);
        assertEquals(auth.order().get(1), AssetPathAuthorizer.Order.DENY);
        assertEquals(auth.order().size(),2);
        assertTrue(auth.accessAllowed("foo"));
        assertFalse(auth.accessDenied("foo"));
        
        assertFalse(auth.accessAllowed("bar"));
        assertTrue(auth.accessDenied("bar"));
    }

}
