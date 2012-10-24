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

import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
            @Override
            public void run()
            {
                bytes = null;

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

    @Override
    public InputStream openStream() throws IOException
    {
        return new ByteArrayInputStream(getBytes());
    }

    private byte[] getBytes()
    {
        if (bytes != null)
        {
            return bytes;
        }

        try
        {
            bytes = assembleCatalog().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

        return bytes;
    }

    private String assembleCatalog()
    {
        Messages messages = messagesSource.getApplicationCatalog(locale);

        JSONObject catalog = new JSONObject();

        for (String key : messages.getKeys())
        {
            catalog.put(key, messages.get(key));
        }

        StringBuilder builder = new StringBuilder(2000);

        builder.append("define(").append(catalog.toString(compactJSON)).append(");");

        return builder.toString();
    }
}
