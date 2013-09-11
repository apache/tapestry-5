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
 *
 * @since 5.3
 */
class ComponentParameterTests extends App1TestCase
{
    @Test
    void event_handler_method_for_missing_component_is_error()
    {
        openLinks "Unmatched Component Id in Event Method Demo"

        assertTextPresent "Method org.apache.tapestry5.integration.app1.pages.EventMethodUnmatchedComponentId.onActionFromBaz() references component id 'Baz' which does not exist.",
                "Component EventMethodUnmatchedComponentId does not contain embedded component 'Baz'."

    }
}
