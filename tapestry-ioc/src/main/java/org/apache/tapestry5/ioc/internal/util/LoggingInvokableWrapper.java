// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Invokable;
import org.slf4j.Logger;

/**
 * @since 5.3
 */
public class LoggingInvokableWrapper<T> implements Invokable<T>
{
    private final Logger logger;

    private final String message;

    private final Invokable<T> delegate;

    public LoggingInvokableWrapper(Logger logger, String message, Invokable<T> delegate)
    {
        this.logger = logger;
        this.message = message;
        this.delegate = delegate;
    }

    @Override
    public T invoke()
    {
        logger.debug(message);

        return delegate.invoke();
    }
}
