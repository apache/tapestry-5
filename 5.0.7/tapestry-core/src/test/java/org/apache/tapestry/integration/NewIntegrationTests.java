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

package org.apache.tapestry.integration;

import org.apache.tapestry.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

/**
 * A kind of staging space for new tests that will be added to {@link IntegrationTests}. The
 * problem with IntegrationTests is that there's now about 35 tests that run. Rather than have to go
 * disable all of those when adding a new test, the new test is added and debugged here. Once it is
 * totally ready, it is moved up to IntegrationTests.
 */
@Test(timeOut = 50000, sequential = true, enabled = false, groups = {"integration"})
public class NewIntegrationTests extends AbstractIntegrationTestSuite
{
    public NewIntegrationTests()
    {
        super("src/test/app1");
    }


}
