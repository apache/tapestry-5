// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class MessageCatalogResource extends VirtualResource
{
    private final Locale locale;

    private final ComponentMessagesSource messagesSource;

    private final boolean compactJSON;

    private volatile byte[] bytes;

    public MessageCatalogResource(Locale locale, ComponentMessagesSource messagesSource, final ResourceChangeTracker changeTracker, boolean compactJSON)
    {
        this.locale = locale;
        this.messagesSource = messagesSource;
        this.compactJSON = compactJSON;

        messagesSource.getInvalidationEventHub().addInvalidationCallback(new Runnable()
        {
            public void run()
            {
                bytes = null;

                // What's all this then?  This MessageCatalogResource is converted, as if it was a real file, into
                // a StreamableResource by the StreamableResourceService service; as a virtual resource, we don't have
                // any date-time-modified information for the application message catalog (as if that was possible,
                // given that its composed of multiple files). The ComponentMessagesSource can tell us when
                // *some* properties file has changed (not necessarily one used in the application message catalog,
                // but that's the breaks). When that occurs, we tell the ResourceChangeTracker to fire its invalidation
                // event. That flushes out all the assets it has cached, including StreamableResources for JavaScript files,
                // including the one created here to represent the application message catalog.
                changeTracker.forceInvalidationEvent();
            }
        });
    }

    @Override
    public String toString()
    {
        return String.format("MessageCatalogResource[%s]", locale);
    }

    /**
     * StreamableResourceSourceImpl needs a file, and a suffix, to determine the content type.
     */
    @Override
    public String getFile()
    {
        return String.format("virtual-%s.js", locale);
    }

    /**
     * To work as a streamable resource, must return a value (or null) from toURL.
     */
    @Override
    public URL toURL()
    {
        return null;
    }

    public InputStream openStream() throws IOException
    {
        return toInputStream(getBytes());
    }

    private byte[] getBytes() throws IOException
    {
        if (bytes == null)
        {
            bytes = assembleCatalog().getBytes(UTF8);
        }

        return bytes;
    }

    private String assembleCatalog()
    {
        Messages messages = messagesSource.getApplicationCatalog(locale);

        JSONObject catalog = new JSONObject();

        for (String key : messages.getKeys())
        {
            if (key.startsWith("private-"))
            {
                continue;
            }

            String value = messages.get(key);

            if (value.contains("%"))
            {
                continue;
            }

            catalog.put(key, value);
        }

        StringBuilder builder = new StringBuilder(2000);

        builder.append("define(").append(catalog.toString(compactJSON)).append(");");

        return builder.toString();
    }
}
