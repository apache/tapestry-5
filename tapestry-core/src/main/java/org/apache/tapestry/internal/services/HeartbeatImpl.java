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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newStack;

import java.util.List;

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.util.Stack;
import org.apache.tapestry.services.Heartbeat;

public class HeartbeatImpl implements Heartbeat
{
    private final Stack<List<Runnable>> _stack = newStack();

    public void begin()
    {
        List<Runnable> beat = CollectionFactory.newList();

        _stack.push(beat);
    }

    public void defer(Runnable command)
    {
        Defense.notNull(command, "command");

        _stack.peek().add(command);

    }

    public void end()
    {
        List<Runnable> beat = _stack.pop();

        for (Runnable r : beat)
            r.run();
    }

}
