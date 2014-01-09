// Copyright 2014 The Apache Software Foundation
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

import org.apache.tapestry5.services.PartialTemplateRenderer;
import org.testng.annotations.Test
import org.junit.Assert;

/**
 * Tests the {@link PartialTemplateRenderer} service.
 *
 * @since 5.4
 */
class PartialTemplateRendererTests extends App1TestCase {

    @Test
    void render_block() {
        
        open "/partialtemplaterendererdemo"

        assertEquals(getAttribute("css=#original dl@class"), getAttribute("css=#serviceRenderedBlock dl@class"))
        
        // checking whether service-rendered block is the same as the original
        for (int i = 1; i <= getXpathCount("//div[@id='original']/dl/*"); i++) {
            
            assertEquals(
                getAttribute("//div[@id='original']/dl/*[" + i + "]@class"),
                getAttribute("//div[@id='serviceRenderedBlock']/dl/*[" + i + "]/@class")) 

            assertEquals(
                getText("//div[@id='original']/dl/*[" + i + "]"),
                getText("//div[@id='serviceRenderedBlock']/dl/*[" + i + "]"))

        } 

        // checking whether service-rendered component is the same as the original
        for (int i = 1; i <= getXpathCount("//div[@id='original']/dl/*"); i++) {
            
            assertEquals(
                getAttribute("//div[@id='originalBeanDisplay']/dl/*[" + i + "]@class"),
                getAttribute("//div[@id='serviceRenderedComponent']/dl/*[" + i + "]/@class"))

            assertEquals(
                getText("//div[@id='originalBeanDisplay']/dl/*[" + i + "]"),
                getText("//div[@id='serviceRenderedComponent']/dl/*[" + i + "]"))

        }

    }

}
