// Copyright 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.services.ExceptionTracker;
import org.apache.tapestry5.ioc.services.LoggingAdvisor;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.slf4j.Logger;

@PreventServiceDecoration
public class LoggingAdvisorImpl implements LoggingAdvisor
{
    private final ExceptionTracker exceptionTracker;

    public LoggingAdvisorImpl(ExceptionTracker exceptionTracker)
    {
        this.exceptionTracker = exceptionTracker;
    }

    @Override
    public void addLoggingAdvice(Logger logger, MethodAdviceReceiver receiver)
    {
        MethodAdvice advice = new LoggingAdvice(logger, exceptionTracker);

        receiver.adviseAllMethods(advice);
    }
}
