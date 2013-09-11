// Copyright 2010-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test

class PageActivationContextAnnotationTests extends App1TestCase
{
    @Test
    void pac_fields_set_before_activate_event_handler_method_invoked()
    {
        openLinks "PageActivationContext Demo"
        
        assertText "count", ""
        assertText "count-set", "false"
        
        clickAndWait "link=activate page with context"
        
        assertText "count", "99"
        assertText "count-set", "true"
    }
}
