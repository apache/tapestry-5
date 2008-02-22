// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.components;

import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.services.Builtin;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.services.ComponentLayer;
import org.apache.tapestry.services.Request;

/**
 * Here's a component with a template, including a t:body element.   Really should rename this to "Layout" as that's the
 * T5 naming.
 */
@IncludeStylesheet({ "context:layout/style.css", "context:css/app.css" })
public class Border
{
    @Inject
    @Builtin
    private ClassFactory _iocClassFactory;

    @Inject
    @ComponentLayer
    private ClassFactory _componentClassFactory;

    @Inject
    private Request _request;

    public ClassFactory getComponentClassFactory()
    {
        return _componentClassFactory;
    }

    public ClassFactory getIocClassFactory()
    {
        return _iocClassFactory;
    }

    public Request getRequest()
    {
        return _request;
    }

    public String getSecure()
    {
        return _request.isSecure() ? "secure" : "insecure";
    }

}
