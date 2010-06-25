// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.integration.TapestryCoreTestCase 
import org.testng.annotations.Test 

class QueryParameterMappedTests extends TapestryCoreTestCase
{
    @Test
    void basic_links()
    {
        clickThru "@QueryParameterMapped Demo"
        
        assertText("clickCount", "")
        assertText("clickCountSet", "false")
        assertText("message", "")
        
        clickAndWait "link=increment count"
        
        assertText("clickCount", "1")
        assertText("clickCountSet", "true")
        
        clickAndWait "link=set message"
        
        assertText("clickCount", "1")
        assertText("clickCountSet", "true")
        assertText("message", "Link clicked!")        
    }
}
