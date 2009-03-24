//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.URLEncoder;

public class ContextPathEncoderImpl implements ContextPathEncoder
{
    private static final int BUFFER_SIZE = 100;

    private final ContextValueEncoder valueEncoder;

    private final URLEncoder urlEncoder;

    private final TypeCoercer typeCoercer;

    private final EventContext EMPTY = new EmptyEventContext();

    public ContextPathEncoderImpl(ContextValueEncoder valueEncoder, URLEncoder urlEncoder, TypeCoercer typeCoercer)
    {
        this.valueEncoder = valueEncoder;
        this.urlEncoder = urlEncoder;
        this.typeCoercer = typeCoercer;
    }

    public String encodeIntoPath(Object[] context)
    {
        if (context == null || context.length == 0) return "";

        return encodeIntoPath(new ArrayEventContext(typeCoercer, context));       
    }

    public String encodeIntoPath(EventContext context)
    {
        Defense.notNull(context, "context");

        int count = context.getCount();
        
        StringBuilder output = new StringBuilder(BUFFER_SIZE);

        for (int i = 0; i < count; i++)
        {
            Object raw = context.get(Object.class, i);

            String valueEncoded = raw == null ? null : valueEncoder.toClient(raw);

            String urlEncoded = urlEncoder.encode(valueEncoded);

            if (i > 0) output.append("/");

            output.append(urlEncoded);
        }

        return output.toString();
    }

    public EventContext decodePath(String path)
    {
        if (InternalUtils.isBlank(path)) return EMPTY;

        String[] split = TapestryInternalUtils.splitPath(path);

        for (int i = 0; i < split.length; i++)
        {
            split[i] = urlEncoder.decode(split[i]);
        }

        return new URLEventContext(valueEncoder, split);
    }
}
