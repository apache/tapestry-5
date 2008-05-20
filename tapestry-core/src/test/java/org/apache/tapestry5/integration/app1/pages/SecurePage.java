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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Secure;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

@Secure
public class SecurePage
{

    @Persist("flash")
    private String message;

    @Inject
    private Request request;

    @Inject
    @Path("context:images/tapestry_banner.gif")
    private Asset icon;

    @Inject
    @Path("nested/tapestry-button.png")
    private Asset button;

    public Asset getIcon()
    {
        return icon;
    }

    public Asset getButton()
    {
        return button;
    }

    public String getMessage()
    {
        return message;
    }

    void onActionFromSecureLink()
    {
        message = "Link clicked";
    }

    void onSubmit()
    {
        message = "Form submitted";
    }

    SecurePage initialize(String message)
    {
        this.message = message;

        return this;
    }
}
