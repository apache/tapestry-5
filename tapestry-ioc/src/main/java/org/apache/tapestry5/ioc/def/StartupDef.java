// Copyright 2014 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.def;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.slf4j.Logger;

/**
 * Represents a public module method (static or instance) with a
 * {@link org.apache.tapestry5.ioc.annotations.Startup} annotation. Such methods are invoked (possibly triggering side effects such as instantiating
 * services and proxies).
 *
 * @since 5.4
 */
public interface StartupDef
{
    /**
     * Invoke the startup method, which includes computing any parameters to the method.
     */
    void invoke(ModuleBuilderSource moduleBuilderSource,
                OperationTracker tracker,
                ObjectLocator locator,
                Logger logger);
}
