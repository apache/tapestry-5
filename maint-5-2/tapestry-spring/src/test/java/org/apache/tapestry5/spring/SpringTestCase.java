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

package org.apache.tapestry5.spring;

import org.apache.tapestry5.test.TapestryTestCase;

import javax.servlet.ServletContext;

/**
 * Base class for Spring Integration test cases.
 */
public class SpringTestCase extends TapestryTestCase
{
    protected final void train_getInitParameter(ServletContext context, String parameterName, String parameterValue)
    {
        expect(context.getInitParameter(parameterName)).andReturn(parameterValue).atLeastOnce();
    }

    protected final ServletContext mockServletContext()
    {
        return newMock(ServletContext.class);
    }

    protected final void train_getAttribute(ServletContext context, String attributeName, Object attributeValue)
    {
        expect(context.getAttribute(attributeName)).andReturn(attributeValue);
    }
}
