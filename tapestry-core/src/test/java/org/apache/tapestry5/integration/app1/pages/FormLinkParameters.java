// Copyright (c) 2013. The Apache Software Foundation
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

import java.net.URLEncoder;
import org.apache.commons.codec.net.URLCodec;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.http.Link;

public class FormLinkParameters {

    public final static String TEST_PARAM_NAME = "myparam";
    public final static String TEST_PARAM_VALUE = "!@#$%^&*()_+=";

    @Property
    private String val;

    @Persist("flash")
    @Property
    private String result;

    void onDecorateComponentEventLink(Link link) throws Exception
    {
        // Add parameter to the form submit link and the event link
        link.addParameter(TEST_PARAM_NAME, URLEncoder.encode(TEST_PARAM_VALUE, "UTF-8"));
    }

    void onMyAction(@RequestParameter(TEST_PARAM_NAME) String value)
    {
        result = value; // Expecting: value equals TEST_PARAM_VALUE
    }

    void onSuccessFromSimpleform(@RequestParameter(TEST_PARAM_NAME) String value)
    {
        result = value; // Expecting: value equals TEST_PARAM_VALUE
    }

}
