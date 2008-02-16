// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.ioc.services.PerthreadManager;
import org.apache.tapestry.services.Request;

/**
 * Because this service implementation needs to be a LinkFactoryListener, it could not use
 * the perthread scope.  Instead, we intract directly with the PerthreadManager service
 * to store request-scoped data (the UID extracted from the Request).
 */
public class AjaxUIDManagerImpl implements LinkFactoryListener, AjaxUIDManager
{
    public static final String AJAX_UID_PARAMETER_NAME = "t:uid";

    public static final String KEY = "AjaxUIDManager.UID";

    private final Request _request;

    private final PerthreadManager _perthreadManager;

    public AjaxUIDManagerImpl(Request request, PerthreadManager perthreadManager)
    {
        _request = request;
        _perthreadManager = perthreadManager;
    }

    public String getAjaxUID()
    {
        String result = (String) _perthreadManager.get(KEY);

        if (result == null)
        {
            String requestUID = _request.getParameter(AJAX_UID_PARAMETER_NAME);

            long asLong = requestUID == null ? 0 : Long.parseLong(requestUID);

            result = Long.toString(asLong + 1);

            _perthreadManager.put(KEY, result);
        }

        return result;
    }

    /**
     * Does nothing.
     */
    public void createdPageLink(Link link)
    {
    }

    /**
     * Adds the Ajax UID to this request, if the request is an Ajax request.
     */
    public void createdActionLink(Link link)
    {
        if (_request.isXHR())
            link.addParameter(AJAX_UID_PARAMETER_NAME, getAjaxUID());
    }
}
