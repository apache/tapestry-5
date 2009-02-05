// Copyright 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.test.PageTester;

import java.util.Locale;

/**
 * An extended version of {@link Request} that allows the {@link PageTester} to control and override behavior,
 * effectively simulating the portions of {@link Request} that are provided normally by a servlet container.
 */
public interface TestableRequest extends Request
{
    /**
     * Clears the internal parameters map.
     *
     * @return the request for further configuration
     */
    TestableRequest clear();

    /**
     * Sets the path; the path should begin with a "/" character and contain everything from there to the start of query
     * parameters (if any).
     *
     * @param path
     * @return the request for further configuration
     */
    TestableRequest setPath(String path);

    /**
     * Sets the locale requested by "the browser".
     *
     * @returns the request for further configuration
     */
    TestableRequest setLocale(Locale locale);

    /**
     * Loads a single parameter/value pair. This may define a new parameter, or add a value to a list of parameters.
     *
     * @return the request for further configuration
     */
    TestableRequest loadParameter(String parameterName, String parameterValue);

    /**
     * Overrides a parameter to the specific value, regardless of how the parameter was previously set.
     */
    TestableRequest overrideParameter(String parameterName, String parameterValue);
}
