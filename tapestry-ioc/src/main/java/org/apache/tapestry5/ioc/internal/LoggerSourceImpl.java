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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper around SLF4J's LoggerFactory. The concept here is that Log implementations could be provided that
 * promote warnings or errors upto thrown exceptions, for people who like their IOC container extra finicky. In
 * addition, the extra layer makes things a lot easier to mock.
 */
public class LoggerSourceImpl implements LoggerSource
{
    public Logger getLogger(Class clazz)
    {
        return LoggerFactory.getLogger(clazz);
    }

    public Logger getLogger(String name)
    {
        return LoggerFactory.getLogger(name);
    }

}
