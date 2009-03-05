// Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ClientDataSink;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Used to collection component actions, with the ultimate goal being the creation of a MIME-encoded string of the
 * serialization of those actions.
 */
public class ComponentActionSink
{
    private final Logger logger;

    private final ObjectOutputStream stream;

    private final ClientDataSink sink;

    public ComponentActionSink(Logger logger, ClientDataEncoder encoder)
    {
        this.logger = logger;

        sink = encoder.createSink();

        stream = sink.getObjectOutputStream();
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


    public String getClientData()
    {
        return sink.getClientData();
    }
}
