// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test.internal.services;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.slf4j.Logger;

public class StartupModule2
{
    public static boolean staticStartupInvoked;
    public static boolean instanceStartupInvoked;

    @Startup
    public static void foo(ObjectLocator locator)
    {
        staticStartupInvoked = true;
    }
    
    @Startup
    public void bar(ObjectLocator locator, Logger logger)
    {
        instanceStartupInvoked = true;
        
        logger.info("StartupModule2.bar invoked");
    }
}
