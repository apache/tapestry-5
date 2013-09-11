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

class ClassTransformationTests extends App1TestCase
{
    /** TAP5-1222  */
    @Test
    void access_to_public_field_of_data_object_with_name_that_conflicts_with_component_field()
    {
        openLinks "Public Field Access Demo"

        assertText "message", "success"
    }

    @Test
    void tap5_1656_abstract_method_class_transformation_edge_case()
    {

        openLinks "TAP5-1656 Demo"

        // Prior to the bug fix, the request would fail with an exception
        assertText "subtitle", "Child A"
    }
}
