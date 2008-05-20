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

package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.services.ComponentLayer;
import org.apache.tapestry5.services.Request;

/**
 * Here's a component with a template, including a t:body element.   Really should rename this to "Layout" as that's the
 * T5 naming.
 */
@IncludeStylesheet({ "context:layout/style.css", "context:css/app.css" })
public class Border
{
    @Inject
    @Builtin
    private ClassFactory iocClassFactory;

    @Inject
    @ComponentLayer
    private ClassFactory componentClassFactory;

    @Inject
    private Request request;

    public ClassFactory getComponentClassFactory()
    {
        return componentClassFactory;
    }

    public ClassFactory getIocClassFactory()
    {
        return iocClassFactory;
    }

    public Request getRequest()
    {
        return request;
    }

    public String getSecure()
    {
        return request.isSecure() ? "secure" : "insecure";
    }

}
