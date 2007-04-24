// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.test.pagelevel;

import java.util.Collections;

import org.apache.tapestry.services.Session;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SessionHolderForPageTesterTest extends Assert
{
    @Test
    public void not_created()
    {
        SessionHolderForPageTester holder = new SessionHolderForPageTester();
        assertNull(holder.getSession(false));
    }

    @Test
    public void create()
    {
        SessionHolderForPageTester holder = new SessionHolderForPageTester();
        Session session = holder.getSession(true);
        assertNotNull(session);
        assertEquals(session.getAttributeNames(), Collections.EMPTY_LIST);
    }

}
