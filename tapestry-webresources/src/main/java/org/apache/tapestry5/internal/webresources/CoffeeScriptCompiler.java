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

package org.apache.tapestry5.internal.webresources;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class CoffeeScriptCompiler implements ResourceTransformer
{
    private final static Charset UTF8 = Charset.forName("utf-8");

    private final RhinoExecutorPool executorPool;

    @Override
    public ContentType getTransformedContentType()
    {
        return InternalConstants.JAVASCRIPT_CONTENT_TYPE;
    }

    public CoffeeScriptCompiler(@Path("classpath:org/apache/tapestry5/webresources/internal/coffee-script.js")
                                Resource mainCompiler,
                                @Path("classpath:org/apache/tapestry5/webresources/internal/invoke-coffeescript.js")
                                Resource shim,
                                OperationTracker tracker)
    {

        executorPool = new RhinoExecutorPool(tracker, toList(mainCompiler, shim));
    }

    private List<Resource> toList(Resource... resources)
    {
        List<Resource> list = CollectionFactory.newList();

        for (Resource r : resources)
        {
            list.add(r);
        }

        return list;
    }


    private static String getString(NativeObject object, String key)
    {
        return object.get(key).toString();
    }


    @Override
    public InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException
    {
        InputStream is = null;
        String content;

        try
        {
            is = source.openStream();
            content = IOUtils.toString(is, UTF8);
        } finally
        {
            InternalUtils.close(is);
        }

        RhinoExecutor executor = executorPool.get();

        try
        {

            NativeObject result = (NativeObject) executor.invokeFunction("compileCoffeeScriptSource", content, source.toString());

            if (result.containsKey("exception"))
            {
                throw new RuntimeException(getString(result, "exception"));
            }

            return IOUtils.toInputStream(getString(result, "output"), UTF8);

        } finally
        {
            executor.discard();
        }


    }
}
