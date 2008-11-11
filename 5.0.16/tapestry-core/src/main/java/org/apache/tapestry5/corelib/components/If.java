// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Parameter;

/**
 * Conditionally renders its body.
 */
public class If
{
    /**
     * If true, then the body of the If component is rendered. If false, the body is omitted.
     */
    @Parameter(required = true)
    private boolean test;

    /**
     * Optional parameter to invert the test. If true, then the body is rendered when the test parameter is false (not
     * true).
     *
     * @see Unless
     */
    @Parameter
    private boolean negate;

    /**
     * An alternate {@link org.apache.tapestry5.Block} to render if the test parameter is false. The default, null,
     * means render nothing in that situation.
     */
    @Parameter(name = "else", defaultPrefix = BindingConstants.LITERAL)
    private Block elseBlock;

    /**
     * Returns null if the test parameter is true, which allows normal rendering (of the body). If the test parameter is
     * false, returns the else parameter (this may also be null).
     */
    Object beginRender()
    {
        return test != negate ? null : elseBlock;
    }

    /**
     * If the test parameter is true, then the body is rendered, otherwise not. The component does not have a template
     * or do any other rendering besides its body.
     */
    boolean beforeRenderBody()
    {
        return test != negate;
    }

    void setup(boolean test, boolean negate, Block elseBlock)
    {
        this.test = test;
        this.negate = negate;
        this.elseBlock = elseBlock;
    }
}
