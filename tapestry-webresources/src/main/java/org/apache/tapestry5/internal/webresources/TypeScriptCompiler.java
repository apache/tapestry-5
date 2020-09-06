package org.apache.tapestry5.internal.webresources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.mozilla.javascript.NativeObject;

public class TypeScriptCompiler implements ResourceTransformer {
  private final static Charset UTF8 = StandardCharsets.UTF_8;

  private final RhinoExecutorPool executorPool;

  @Override
  public ContentType getTransformedContentType()
  {
      return InternalConstants.JAVASCRIPT_CONTENT_TYPE;
  }

  public TypeScriptCompiler(final OperationTracker tracker,
      @Path("classpath:org/apache/tapestry5/webresources/internal/typescript.js") final Resource typescript)
  {
      this.executorPool = new RhinoExecutorPool(tracker, Arrays.<Resource> asList(typescript,
          new ClasspathResource("org/apache/tapestry5/webresources/internal/invoke-typescript.js")));

  }

  private static String getString(final NativeObject object, final String key)
  {
      return object.get(key).toString();
  }

  @Override
  public InputStream transform(final Resource source, final ResourceDependencies dependencies) throws IOException
  {
      InputStream is = null;
      String content;
  
      try
      {
          is = source.openStream();
          content = IOUtils.toString(is, UTF8);
      } finally {
          InternalUtils.close(is);
      }
  
      RhinoExecutor executor = executorPool.get();
  
      try {
  
          NativeObject result = (NativeObject) executor.invokeFunction("transpile", content, source.toString());
  
          if (result.containsKey("exception")) {
              throw new RuntimeException(getString(result, "exception"));
          }
  
          return IOUtils.toInputStream(getString(result, "output"), UTF8);
  
        } finally {
          executor.discard();
        }

   }
}
