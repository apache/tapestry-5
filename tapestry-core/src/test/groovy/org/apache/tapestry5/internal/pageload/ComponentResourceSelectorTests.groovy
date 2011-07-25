// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload

import org.apache.tapestry5.ioc.annotations.AnnotationUseContext
import org.apache.tapestry5.services.pageload.ComponentResourceSelector
import org.testng.Assert
import org.testng.annotations.Test

class ComponentResourceSelectorTests extends Assert
{
    ComponentResourceSelector english = new ComponentResourceSelector(Locale.ENGLISH)
    ComponentResourceSelector french = new ComponentResourceSelector(Locale.FRENCH)

    @Test
    void mismatch_if_locales_not_same() {
        assert english != french
    }

    @Test
    void equals_this_is_true() {
        assert english == english
    }

    @Test
    void equals_null_is_false() {
        assert english != null
    }

    @Test
    void to_string() {
        assert english.toString() == "ComponentResourceSelector[en]";

        assert english.withAxis(AnnotationUseContext.class, AnnotationUseContext.COMPONENT).toString() == "ComponentResourceSelector[en org.apache.tapestry5.ioc.annotations.AnnotationUseContext=COMPONENT]"
    }

    @Test
    void with_axis_returns_new_instance() {
        def withAxis = english.withAxis(AnnotationUseContext.class, AnnotationUseContext.SERVICE)

        assert ! english.is(withAxis)

        assert english.getAxis(AnnotationUseContext.class) == null

        assert withAxis.getAxis(AnnotationUseContext.class).is(AnnotationUseContext.SERVICE)
    }

    @Test
    void cant_add_same_axis_type_again() {

        def withAxis = english.withAxis(AnnotationUseContext.class, AnnotationUseContext.SERVICE)

        try {
            withAxis.withAxis(AnnotationUseContext.class, AnnotationUseContext.COMPONENT)
        }
        catch (IllegalArgumentException ex) {
            assert ex.message == "Axis type org.apache.tapestry5.ioc.annotations.AnnotationUseContext is already specified as SERVICE."
        }
    }
}
