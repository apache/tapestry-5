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

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;

import java.io.IOException;

/**
 * Attempts to match resources against a {@link org.apache.tapestry5.services.javascript.JavaScriptStack}, and
 * possibly disabled minimization based on the stack.
 *
 * @since 5.4
 */
public class JavaScriptStackMinimizeDisabler extends DelegatingSRS
{

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
        } finally
        {
            request.setAttribute(TapestryConstants.DISABLE_JAVASCRIPT_MINIMIZATION, null);
        }
    }
}
