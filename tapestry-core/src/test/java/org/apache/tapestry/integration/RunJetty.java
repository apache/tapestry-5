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

import org.apache.tapestry.test.JettyRunner;

/**
 * A "shim" to run Demo App #1 inside IntelliJ.  I still haven't found a way to get IntelliJ to
 * export test classes and resources into a web facet.
 */
public class RunJetty
{
    public static void main(String[] args) throws InterruptedException
    {
        new JettyRunner("/", 8080, "src/test/app1");
    }
}
