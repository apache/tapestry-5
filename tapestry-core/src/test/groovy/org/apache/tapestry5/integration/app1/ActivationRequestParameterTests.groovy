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

class ActivationRequestParameterTests extends App1TestCase
{
    @Test
    void basic_links() {
        openLinks "ActivationRequestParameter Annotation Demo"
        
        assertText "click-count", ""
        assertText "click-count-set", "false"
        assertText "message", ""

        // Part of this is testing that @ActivationRequestParameter processing occurs before ordinary event handler method
        // invocations.
        clickAndWait "link=increment count"
        
        assertText "click-count", "1"
        assertText "click-count-set", "true"
        
        clickAndWait "link=set message"
        
        assertText "click-count", "1"
        assertText "click-count-set", "true"
        assertText "message", "Link clicked!"
    }

    @Test
    void special_chars() {
        openLinks "ActivationRequestParameter Annotation Demo"

        clickAndWait "link=set special message"

        assertText "message", "!#\$&'()*+,/:;=?@[]"
    }
    
    @Test
    public void form_components_do_not_conflict_with_mapped_field_names() {
        
        openLinks "ActivationRequestParameter Annotation Demo"
        
        clickAndWait "link=increment count"
        
        select "clickCount", "two"
        
        clickAndWait SUBMIT
        
        assertText "click-count", "1"
        assertText "selected-click-count", "2"        
    }

    @Test
    public void required_arp_with_missing_parameter_is_error() {

        openLinks "Missing Query Parameter for @ActivationRequestParameter"

        assertTextPresent"Activation request parameter field org.apache.tapestry5.integration.app1.pages.MissingRequiredARP.missingARP is marked as required, but query parameter 'missingARP' is null."

    }
}
