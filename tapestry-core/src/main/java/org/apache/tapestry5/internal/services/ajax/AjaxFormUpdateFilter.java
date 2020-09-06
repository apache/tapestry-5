// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.ajax;

import java.io.IOException;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Ajax;
import org.apache.tapestry5.services.ComponentEventRequestFilter;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;

/**
 * Filter for the {@link Ajax} {@link ComponentEventRequestHandler} that informs the {@link AjaxFormUpdateController}
 * about the form's client id and component id. Partial renders work with the
 * AjaxFormUpdateController to ensure that the Form data, if any, is collected and rendered
 * as part of the response.
 * 
 * @since 5.2.0
 */
public class AjaxFormUpdateFilter implements ComponentEventRequestFilter
{
    private final Request request;

    private final AjaxFormUpdateController ajaxFormUpdateController;

    public AjaxFormUpdateFilter(Request request, AjaxFormUpdateController ajaxFormUpdateController)
    {
        this.request = request;
        this.ajaxFormUpdateController = ajaxFormUpdateController;
    }

    public void handle(ComponentEventRequestParameters parameters, ComponentEventRequestHandler handler)
            throws IOException
    {
        String formClientId = request.getParameter(RequestConstants.FORM_CLIENTID_PARAMETER);
        String formComponentId = request.getParameter(RequestConstants.FORM_COMPONENTID_PARAMETER);

        if (InternalUtils.isNonBlank(formClientId) && InternalUtils.isNonBlank(formComponentId))
            ajaxFormUpdateController.initializeForForm(formComponentId, formClientId);

        handler.handle(parameters);
    }
}
