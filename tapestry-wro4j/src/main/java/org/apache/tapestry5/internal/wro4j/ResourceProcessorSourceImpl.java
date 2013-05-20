// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.wro4j;

import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.services.CachingObjectCreator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.*;
import java.util.Map;

public class ResourceProcessorSourceImpl implements ResourceProcessorSource
{
    private final Map<String, ObjectCreator> configuration;

    private final Map<String, ResourceProcessor> cache = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, ResourceType> contentType2resourceType = CollectionFactory.newMap();


    public ResourceProcessorSourceImpl(Map<String, ObjectCreator> configuration)
    {
        this.configuration = configuration;

        contentType2resourceType.put("text/css", ResourceType.CSS);
        contentType2resourceType.put("text/javascript", ResourceType.JS);
    }

    // Not called very often so synchronized is easier.
    public synchronized ResourceProcessor getProcessor(String name)
    {
        ResourceProcessor result = cache.get(name);

        if (result == null)
        {
            result = create(name);
            cache.put(name, result);
        }

        return result;
    }

    private ResourceProcessor create(String name)
    {
        ObjectCreator<ResourcePreProcessor> creator = configuration.get(name);

        if (creator == null)
        {
            throw new UnknownValueException(String.format("Unknown resource processor '%s'.", name), new AvailableValues("configured processors", configuration));
        }

        final ObjectCreator<ResourcePreProcessor> lazyCreator = new CachingObjectCreator<ResourcePreProcessor>(creator);

        return new ResourceProcessor()
        {
            public InputStream process(String operationDescription, String inputURL, InputStream input, String contentType) throws IOException
            {
                Resource resource = Resource.create(inputURL, contentType2resourceType.get(contentType));

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5000);

                lazyCreator.createObject().process(resource, new InputStreamReader(input), new OutputStreamWriter(outputStream));

                return new BytestreamCache(outputStream).openStream();
            }
        };
    }
}
