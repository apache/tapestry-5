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

import org.apache.tapestry.internal.services.SessionHolder;
import org.apache.tapestry.services.Session;

public class SessionHolderForPageTester implements SessionHolder
{
    private Session _session;

    public Session getSession(boolean create)
    {
        if (_session != null)
        {
            return _session;
        }
        if (!create)
        {
            return null;
        }
        _session = new SessionForPageTester();
        return _session;
    }

}
