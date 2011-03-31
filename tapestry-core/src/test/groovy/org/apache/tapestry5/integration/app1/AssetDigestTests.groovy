// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

/**
 * Tests access to a "protected" asset, one that requires a digest in the URL in order to access.  Ensures
 * that such assets can only be accessed using a URL with the necessary digest.
 */
class AssetDigestTests extends TapestryCoreTestCase
{
    def tryBadPath(badPath) {
        def url = new URL("${baseURL}${badPath}")

        try {
            url.getText()
            unreachable()
        }
        catch (IOException ex) {
            assert ex.getMessage().contains("403")
        }
    }

    @Test
    void protected_asset() {
        openLinks "AssetDemo"

        def path = getText("propurl").substring(1) // Strip leading slash

        def url = new URL("${baseURL}${path}")

        def p = new Properties();

        p.load(url.newInputStream())

        printf("URL: %s\nProperties: %s\n",  url, p)

        assert p.getProperty("note") == "Should be protected via a MD5 checksum"

        assert path ==~ /.*\/AssetDemo\.\p{XDigit}+\.properties/
    }

    @Test
    void invalid_digest() {
        openLinks "AssetDemo"

        def path = getText("propurl").substring(1)

        tryBadPath ((path =~ /\.\p{XDigit}+\.properties/).replaceFirst(".abc.properties"))
    }


    @Test
    void missing_digest() {
        openLinks "AssetDemo"

        def path = getText("propurl").substring(1)

        tryBadPath( (path =~ /\.\p{XDigit}+\.properties/).replaceFirst(".properties"))
    }
}
