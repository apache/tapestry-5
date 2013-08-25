package org.apache.tapestry5.internal.webresources;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.mozilla.javascript.NativeObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class CoffeeScriptCompiler implements ResourceTransformer
{
    private final static Charset UTF8 = Charset.forName("utf-8");

    private final RhinoExecutorPool executorPool;

    public String getTransformedContentType()
    {
        return "text/javascript";
    }

    public CoffeeScriptCompiler(@Path("classpath:org/apache/tapestry5/webresources/internal/coffeescript-1.6.3.js")
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


    private String getString(NativeObject object, String key)
    {
        return object.get(key).toString();
    }


    public InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException
    {
        String content = IOUtils.toString(source.openStream(), UTF8);

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
