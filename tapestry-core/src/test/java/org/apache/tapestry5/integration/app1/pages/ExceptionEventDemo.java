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

import org.apache.tapestry5.annotations.Persist;

public class ExceptionEventDemo
{
    @Persist("flash")
    private String message;

    @Persist
    private boolean intercept;

    public Object getInvalidContext()
    {
        return "abc";
    }

    void onActivate(float context)
    {
        message = "Activation context: " + context;
    }

    void onActionFromFail(float context)
    {
        message = "Event context: " + context;
    }

    Object onException(Throwable exception)
    {
        if (!intercept) return null;

        message = "Exception: " + exception.getMessage();

        return this;
    }


    void onActionFromEnable()
    {
        intercept = true;
    }

    void onActionFromDisable()
    {
        intercept = false;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isIntercept()
    {
        return intercept;
    }
}
