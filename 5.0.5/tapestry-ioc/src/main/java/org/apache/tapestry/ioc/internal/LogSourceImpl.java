// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.ioc.LogSource;

/**
 * Simple wrapper around {@link org.apache.commons.logging.LogFactory}. The concept here is that
 * Log implementations could be provided that promote warnings or errors upto thrown exceptions, for
 * people who like their IOC container extra finicky.
 * 
 * 
 */
public class LogSourceImpl implements LogSource
{
    public Log getLog(Class clazz)
    {
        return LogFactory.getLog(clazz);
    }

    public Log getLog(String name)
    {
        return LogFactory.getLog(name);
    }

}
