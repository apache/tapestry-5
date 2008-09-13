// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.internal.util.Base64ObjectOutputStream;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.runtime.Component;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Used to collection component actions, with the ultimate goal being the creation of a MIME-encoded string of the
 * serialization of those actions.
 */
public class ComponentActionSink
{
    private final Logger logger;

    private final Base64ObjectOutputStream stream;

    public ComponentActionSink(Logger logger)
    {
        this.logger = logger;

        try
        {
            stream = new Base64ObjectOutputStream();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public <T> void store(T component, ComponentAction<T> action)
    {
        Component castComponent = Defense.cast(component, Component.class, "component");
        Defense.notNull(action, "action");

        String completeId = castComponent.getComponentResources().getCompleteId();

        logger.debug("Storing action: {} {}", completeId, action);

        try
        {
            // Writing the complete id is not very efficient, but the GZip filter
            // should help out there.
            stream.writeUTF(completeId);
            stream.writeObject(action);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(InternalMessages.componentActionNotSerializable(completeId, ex), ex);
        }
    }


    public String toBase64()
    {
        try
        {
            stream.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        return stream.toBase64();
    }
}
