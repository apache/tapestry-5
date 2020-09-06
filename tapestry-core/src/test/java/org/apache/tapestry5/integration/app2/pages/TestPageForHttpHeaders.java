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
package org.apache.tapestry5.integration.app2.pages;

import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.Inject;

public class TestPageForHttpHeaders
{
    public static final String STRING_HEADER_NAME = "test-string-header";
    public static final String INT_HEADER_NAME = "test-int-header";
    public static final String DATE_HEADER_NAME = "test-date-header";
    
    @Inject
    private Response response;

    void onActivate()
    {
        response.setDateHeader(DATE_HEADER_NAME, 12345L);
        response.setIntHeader(INT_HEADER_NAME, 6789);
        response.setHeader(STRING_HEADER_NAME, "foo-bar-baz-barney");
    }
}
