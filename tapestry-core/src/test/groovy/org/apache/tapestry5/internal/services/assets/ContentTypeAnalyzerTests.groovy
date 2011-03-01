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

package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.testng.annotations.Test

class ContentTypeAnalyzerTests extends InternalBaseTestCase {

    def configuration = [ "txt" : "application/text", "pdf" : "adobe/pdf" ]

    def mockResource(String file) {
        def r = mockResource()

        expect (r.getFile()).andReturn(file).atLeastOnce()

        return r
    }

    @Test
    void file_type_in_configuration() {
        def r = mockResource("foo.txt")

        replay()

        def cta = new ContentTypeAnalyzerImpl (null, configuration)

        assert cta.getContentType(r) == "application/text"

        verify()
    }

    @Test
    void file_type_via_context() {

        def r = mockResource("foo.png")
        def context = mockContext()

        expect(context.getMimeType("foo.png")).andReturn("image/png")

        replay()

        def cta = new ContentTypeAnalyzerImpl (context, configuration)

        assert cta.getContentType(r) == "image/png"

        verify()
    }

    @Test
    void default_is_octect_stream() {
        def r = mockResource("bar.unknown")
        def context = mockContext()

        expect(context.getMimeType("bar.unknown")).andReturn(null)

        replay()

        def cta = new ContentTypeAnalyzerImpl (context, configuration)

        assert cta.getContentType(r) == "application/octet-stream"

        verify()
    }
}
