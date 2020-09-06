// Copyright 2011-2013 The Apache Software Foundation
//
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

package org.apache.tapestry5.integration.symbolparam.components

import org.apache.tapestry5.annotations.Import
import org.apache.tapestry5.annotations.Property
import org.apache.tapestry5.http.services.Request
import org.apache.tapestry5.http.services.Session
import org.apache.tapestry5.ioc.annotations.Inject

@Import(stack="core")
class Layout {

    @Inject
    @Property
    private Request request

    @Property
    private String attributeName

    Session getSession() {
        request.getSession(false)
    }
    
    void onActionFromReset()
    {
        request.getSession(true).invalidate()
    }
}
