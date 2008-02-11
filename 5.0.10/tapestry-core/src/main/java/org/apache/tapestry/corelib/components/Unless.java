// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.annotations.Parameter;

/**
 * A close relative of the {@link org.apache.tapestry.corelib.components.If} component that inverts the meaning of its
 * test.  This is easier than an If component with the negate parameter set to true.
 */
public class Unless
{
    /**
     * If true, then the body of the If component is rendered. If false, the body is omitted.
     */
    @Parameter(required = true)
    private boolean _test;

    /**
     * Returns the inversion of it's test parameter. Therefore, when test is true, nothing is rendered. When test is
     * false, the component (which is to say, the body of the component, as that's the point) is rendered.
     */
    boolean beginRender()
    {
        return !_test;
    }
}
