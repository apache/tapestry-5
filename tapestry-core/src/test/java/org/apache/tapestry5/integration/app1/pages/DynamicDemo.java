// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

public class DynamicDemo
{
    @Persist
    private Integer selected;

    void onSelect(int value)
    {
        selected = value;
    }

    void pageReset()
    {
        selected = null;
    }

    @Inject
    @Path("context:dynamic1.xml")
    private Asset template1;

    @Inject
    @Path("dynamic2.xml")
    private Asset template2;

    public Asset getSelectedTemplate()
    {
        if (selected == null || selected == 1)
            return template1;

        return template2;
    }
}
