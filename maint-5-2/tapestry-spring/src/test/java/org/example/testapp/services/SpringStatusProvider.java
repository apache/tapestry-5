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

package org.example.testapp.services;

import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.services.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.SpringVersion;

public class SpringStatusProvider
{
    private final Context context;
    private final Upcase upcase;

    /**
     * Just wanted to get code coverage for using @InjectService here.
     */
    @Autowired
    public SpringStatusProvider(@InjectService("Context") Context context, Upcase upcase)
    {
        this.context = context;
        this.upcase = upcase;
    }

    public String getStatus()
    {
        return upcase.toUpperCase(String.format("Spring version %s: %s",
                                                SpringVersion.getVersion(),
                                                context.getAttribute("status-message")));
    }
}
