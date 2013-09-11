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

package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test

/**
 * @since 5.3
 */
class ParameterTests extends App1TestCase
{

    /**
     * https://issues.apache.org/jira/browse/TAP5-1227
     */
    @Test
    void null_bound_to_primitive_field_is_an_error()
    {
        openLinks "Null Bound to Primitive Demo"

        assertTextPresent "Parameter 'value' of component NullBindingToPrimitive:showint is bound to null. This parameter is not allowed to be null."
    }

    /**
     * https://issues.apache.org/jira/browse/TAP5-1428
     */
    @Test
    void parameter_specified_with_component_annotation_must_match_a_formal_parameter()
    {
        openLinks "Unmatched Formal Parameter with @Component"

        assertTextPresent "Component InvalidFormalParameterDemo:counter does not include a formal parameter 'step' (and does not support informal parameters).",
                "Formal parameters", "end", "start", "value"

    }

    /**
     * https://issues.apache.org/jira/browse/TAP5-1675
     */
    @Test
    void parameter_conflict_with_base_class_is_error()
    {
        openLinks "Parameter Conflict Demo"

        assertTextPresent "Parameter 'value' of component class org.apache.tapestry5.integration.app1.components.ParameterSubClass conflicts with the parameter defined by the org.apache.tapestry5.integration.app1.base.ParameterBaseClass base class."
    }

    /**
     * https://issues.apache.org/jira/browse/TAP5-1680
     */
    @Test
    void use_component_class_name_to_disambiguate_informal_parameter()
    {
        openLinks "Mixin Parameter vs. Informal Parameter"

        // Why frog?  I don't know.

        assertAttribute "//a[@id='frog']/@alt", "Alt Title"
        assertAttribute "//a[@id='frog']/@title", "Frog Title"
    }

/** https://issues.apache.org/jira/browse/TAP5-1642    */
    @Test
    void mixin_parameter_with_default_no_longer_causes_spurious_exception()
    {
        openLinks "Mixin Parameter with Default"

        // This proves the mixin was added and did its job.
        assertAttribute "//a[@class='testsubject']/@alt", "Default title"
    }
}
