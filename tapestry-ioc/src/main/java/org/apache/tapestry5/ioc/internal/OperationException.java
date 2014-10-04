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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.util.Stack;

/**
 * An exception caught and reported by an {@link org.apache.tapestry5.ioc.OperationTracker}; the trace property
 * identifies what operations were active at the time of the exception.
 */
public class OperationException extends TapestryException
{
    private static final long serialVersionUID = -7555673473832355909L;

    private final Stack<String> operations = CollectionFactory.newStack();

    public OperationException(Throwable cause, String description)
    {
        super(cause.getMessage(), cause);

        operations.push(description);
    }

    public String[] getTrace()
    {
        Object[] snapshot = operations.getSnapshot();

        String[] trace = new String[snapshot.length];

        for (int i = 0; i < snapshot.length; i++)
        {
            trace[i] = snapshot[i].toString();
        }

        return trace;
    }

    /**
     * Invoked while unwinding the stack to add descriptions for each OperationTracker run/invoke/perform
     * operation.
     *
     */
    public void push(String description)
    {
        operations.push(description);
    }
}
