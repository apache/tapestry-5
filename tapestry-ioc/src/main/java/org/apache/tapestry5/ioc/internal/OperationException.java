// Copyright 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.internal.util.TapestryException;

/**
 * An exception caught and reported by an {@link org.apache.tapestry5.ioc.OperationTracker}; the trace property
 * identifies what operations were active at the time of the exception.
 */
public class OperationException extends TapestryException
{
    private static final long serialVersionUID = -7555673473832355909L;

    private final String[] trace;

    public OperationException(Throwable cause, String[] trace)
    {
        super(cause.getMessage(), cause);

        this.trace = trace;
    }

    public String[] getTrace()
    {
        return trace;
    }
}
