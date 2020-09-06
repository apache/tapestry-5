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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.util.Arrays;
import java.util.List;

public class RequestParameterDemo
{
    private static final String PARAMETER_NAME = "gnip";

    private static final String EVENT_NAME = "frob";

    private static final String MULTIVALUED_PARAMETER_EVENT_NAME = "frobFrob";

    @Property
    @Persist
    private int value;

    @Property
    @Persist
    private List<Integer> values;

    @Inject
    private ComponentResources resources;

    public Link getWorkingLink()
    {
        Link link = resources.createEventLink(EVENT_NAME);
        link.addParameter(PARAMETER_NAME, "97");

        return link;
    }

    public Link getMultivaluedQueryParameterLink()
    {
        Link link = resources.createEventLink(MULTIVALUED_PARAMETER_EVENT_NAME);
        link.addParameter(PARAMETER_NAME, "97");
        link.addParameter(PARAMETER_NAME, "98");
        link.addParameter(PARAMETER_NAME, "99");

        return link;
    }

    public Link getBrokenLink()
    {
        Link link = resources.createEventLink(EVENT_NAME);
        link.addParameter(PARAMETER_NAME, "frodo");

        return link;
    }

    public Link getBlankAllowedLink()
    {
        return resources.createEventLink("emptyStringAllowed").addParameter(PARAMETER_NAME, "");
    }

    public Link getNullLink()
    {
        return resources.createEventLink(EVENT_NAME);
    }

    public Link getNullAllowedLink()
    {
        return resources.createEventLink("frobNullAllowed");
    }

    void onFrob(@RequestParameter(PARAMETER_NAME)
                int value)
    {
        this.value = value;
    }

    void onFrobFrob(@RequestParameter(PARAMETER_NAME)
                    Integer[] values)
    {
        this.values = Arrays.asList(values);
    }

    void onFrobNullAllowed(@RequestParameter(value = PARAMETER_NAME, allowBlank = true)
                           int value)
    {
        this.value = value;
    }

    void onEmptyStringAllowed(@RequestParameter(value = PARAMETER_NAME, allowBlank = true) Integer value)
    {
        if (value == null)
        {
            this.value = -1;
        } else
        {
            this.value = value.intValue();
        }

    }
}
