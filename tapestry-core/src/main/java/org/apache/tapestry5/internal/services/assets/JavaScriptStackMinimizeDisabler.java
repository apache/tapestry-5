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

package org.apache.tapestry5.internal.services.assets;

import java.io.IOException;

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to match resources against a {@link org.apache.tapestry5.services.javascript.JavaScriptStack}, and
 * possibly disabled minimization based on the stack.
 *
 * @since 5.4
 */
public class JavaScriptStackMinimizeDisabler extends DelegatingSRS
{

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptStackMinimizeDisabler.class);
    
    private final JavaScriptStackSource javaScriptStackSource;

    private final Request request;

    public JavaScriptStackMinimizeDisabler(StreamableResourceSource delegate, JavaScriptStackSource javaScriptStackSource, Request request)
    {
        super(delegate);

        this.javaScriptStackSource = javaScriptStackSource;
        this.request = request;
    }


    @Override
    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies) throws IOException
    {
        JavaScriptStack stack = javaScriptStackSource.findStackForJavaScriptLibrary(baseResource);

        if (stack != null && !stack.getJavaScriptAggregationStrategy().enablesMinimize())
        {
            request.setAttribute(TapestryConstants.DISABLE_JAVASCRIPT_MINIMIZATION, true);
        }

        try
        {
            return delegate.getStreamableResource(baseResource, processing, dependencies);
        } 
        catch (RuntimeException e)
        {
            if (processing != StreamableResourceProcessing.FOR_AGGREGATION)
            {
                // We know our current minimizer, Google Closure Compiler,
                // doesn't support ES modules
                if (LOGGER.isWarnEnabled() && 
                        !baseResource.toString().contains("/es-modules/") &&
                        !baseResource.toString().toLowerCase().contains("es module wrapper"))
                {
                    LOGGER.warn("Exception happened while processing " + baseResource + 
                            "Trying again without compression nor minification.", e);
                }
                request.setAttribute(TapestryConstants.DISABLE_JAVASCRIPT_MINIMIZATION, true);
                return delegate.getStreamableResource(baseResource, StreamableResourceProcessing.FOR_AGGREGATION, dependencies);
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            request.setAttribute(TapestryConstants.DISABLE_JAVASCRIPT_MINIMIZATION, null);
        }
    }
}
