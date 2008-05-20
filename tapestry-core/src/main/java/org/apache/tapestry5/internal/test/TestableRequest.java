// Copyright 2007 The Apache Software Foundation
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

import java.util.Map;

/**
 * An extended version of {@link Request} that allows the {@link PageTester} to control and override behavior,
 * effectively simulating the portions of {@link Request} that are provided normally by a servlet container.
 */
public interface TestableRequest extends Request
{
    /**
     * Clears the internal parameters map.
     */
    void clear();

    /**
     * Loads new parameter/value pairs into the map.
     */
    void loadParameters(Map<String, String> parameterValues);

    /**
     * Loads a single parameter/value pair.
     */
    void loadParameter(String parameterName, String parameterValue);
}
