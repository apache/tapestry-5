// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.slf4j.Logger;

/**
 * Used to check the ability to inject service resources (including Logger) into
 * contribut methods, etc.
 */
public class InjectionCheckModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(InjectionCheck.class);
    }

    public static void contributeInjectionCheck(MappedConfiguration<String, Object> configuration, Logger logger)
    {
        configuration.add("logger", logger);
        configuration.addInstance("indirect-resources", IndirectResources.class);
    }
}
