//  Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.upload.internal.services;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.internal.services.ComponentResultProcessorWrapper;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadEvents;

import java.io.IOException;

/**
 * Determines if there was an {@link org.apache.commons.fileupload.FileUploadException} processing the request and, if
 * so, triggers an exception event on the page. If the page fails to respond to the event, then
 */
@Events(UploadEvents.UPLOAD_EXCEPTION + " when a exception occur processing the upload")
public class UploadExceptionFilter implements ComponentEventRequestFilter
{
    private final MultipartDecoder decoder;

    private final ComponentEventResultProcessor resultProcessor;

    private ComponentSource componentSource;

    public UploadExceptionFilter(MultipartDecoder decoder,
                                 @Traditional @Primary ComponentEventResultProcessor resultProcessor,
                                 ComponentSource componentSource)
    {
        this.decoder = decoder;
        this.resultProcessor = resultProcessor;
        this.componentSource = componentSource;
    }

    public void handle(ComponentEventRequestParameters parameters, ComponentEventRequestHandler handler)
            throws IOException
    {
        FileUploadException uploadException = decoder.getUploadException();

        if (uploadException != null)
        {
            Component page = componentSource.getPage(parameters.getActivePageName());

            ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(resultProcessor);

            page.getComponentResources().triggerEvent(UploadEvents.UPLOAD_EXCEPTION, new Object[] { uploadException },
                                                      callback);

            // If an event handler exists and returns a value, then the callback will be aborted and a response
            // (typically a redirect) will already have been sent to the client.

            if (callback.isAborted()) return;

            // If the page does not properly handle the exception, then we throw it now.

            throw new RuntimeException(UploadMessages.unableToDecode(), uploadException);
        }


        handler.handle(parameters);
    }
}
