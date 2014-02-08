// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.kaptcha.components;

import java.util.List;

import org.apache.tapestry5.internal.test.TestableResponse;
import org.apache.tapestry5.test.PageTester;
import org.junit.Test;

public class KaptchaUnitTest
{
    
    @SuppressWarnings("unchecked")
    @Test
    public void cache_control_header()
    {
        PageTester tester = new PageTester("kaptcha.demo", "app");
        final TestableResponse response = tester.renderPageAndReturnResponse("KaptchaDemo");
        final List<String> headers = (List<String>) response.getHeaders("Cache-Control");
        assert "no-store, no-cache, must-revalidate".equals(headers.get(0));
        assert "post-check=0, pre-check=0".equals(headers.get(1));
    }

}
