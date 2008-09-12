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

package org.apache.tapestry5.ioc;

import org.slf4j.Logger;

/**
 * A wrapper around SLF4J's LoggerFactory that exists to allow particular projects to "hook" the creation of Logger
 * instances.
 */
public interface LoggerSource
{
    /**
     * Creates or retrieves a log based on Class. This is rarely used in Tapestry IOC.
     */
    Logger getLogger(Class clazz);

    /**
     * Creates or retrieves a log based on name. Typically, the name will be a service id.
     */
    Logger getLogger(String name);
}
