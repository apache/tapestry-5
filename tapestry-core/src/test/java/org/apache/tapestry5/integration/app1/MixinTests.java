// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.testng.annotations.Test;

public class MixinTests extends App1TestCase
{
    @Test
    public void render_notification_mixin()
    {
        openLinks("RenderNotification Demo");

        assertTextSeries("//ul[@id='list']//li[%d]", 1, "before item render", "item body in template",
                "after item render");
    }

    @Test
    public void renderclientid_mixin()
    {
        openLinks("RenderClientId Mixin");

        assertText("divwithid", "Div Content");
    }

    @Test
    public void mixin_ordering()
    {
        // echo => <original>-before, temporaryvaluefromechovaluemixin,
        // <original>-after
        // echo2 => echo2-<original>-before, "3", echo2-<original>-after
        // echo3 => echo3-<original>-before, "world", echo3-<original>-after
        // order1: echo, echo2, echo3
        openLinks("Mixin Ordering Demo");

        assertMixinOrder(1, 0, 1, 2, 3, true);
        // order2: echo3, echo2, echo
        assertMixinOrder(2, 2, 3, 0, 1, true);
        // order3: echo2, echo3, echo
        assertMixinOrder(3, 3, 0, 2, 1, true);
        // order4: echo3, echo, echo2
        assertMixinOrder(4, 3, 1, 0, 2, true);
        // order5: echo2, echo, echo3
        assertMixinOrder(5, 2, 0, 1, 3, true);
        // order6: echo, echo3, echo2, TextOnlyOnDisabled
        assertMixinOrder(6, 0, 3, 1, 2, false);
        // make sure mixin after and mixin before constraints don't interfere...
        // order7: echo, echo2 <corecomponent> echoafter2, echoafter
        assertMixinOrder(7, 0, 1, -1, 2, true);
        assertText("order7_before_but_after",
                "afterrender_for_mixinafter_isreally_justbefore_corecomponent_afterrender-before");
        assertText("order7_after_but_before",
                "afterrender_for_mixinafter_isreally_justbefore_corecomponent_afterrender-after");
        // echoafter2 should have for its value at the point it renders
        // the value that echo2 sets, since the core component isn't changing
        // its value.
        assertText("order7_before_but_after2", "3-before");
        assertText("order7_after_but_before2", "3-after");
    }

    private void assertMixinOrder(int orderNum, int echo1From, int echo2From, int echo3From, int fieldFrom,
            boolean isField)
    {
        assertEchoMixins("order" + orderNum, "batman", echo1From, echo2From, echo3From, fieldFrom, isField);
    }

    /**
     * asserts that the "echo value" mixins are properly functioning (ie
     * 
     * @BindParameter, and mixin ordering).
     *                 each integer value specifies the echo mixin number (echovalue => 1,
     *                 echovalue2 => 2, echovalue3 => 3; 0 is the original value)
     *                 from which the specified echo mixin is expected to "receive" its value.
     *                 So if echo1From is 2, then the "original value"
     *                 printed by echo1 is expected to be the value set by echo2. If a given
     *                 "from" is < 0, checking the corresponding mixin values is disabled.
     */

    private void assertEchoMixins(String fieldName, String originalValue, int echo1From, int echo2From, int echo3From,
            int fieldFrom, boolean isField)
    {
        String[] vals =
        { originalValue, "temporaryvaluefromechovaluemixin", "3", "world" };
        String before = fieldName + "_before";
        String after = fieldName + "_after";
        if (echo1From > -1)
        {
            assertText(before, vals[echo1From] + "-before");
            assertText(after, vals[echo1From] + "-after");
        }
        if (echo2From > -1)
        {
            assertText(before + "2", "echo2-" + vals[echo2From] + "-before");
            assertText(after + "2", "echo2-" + vals[echo2From] + "-after");
        }
        if (echo3From > -1)
        {
            assertText(before + "3", "echo3-" + vals[echo3From] + "-before");
            assertText(after + "3", "echo3-" + vals[echo3From] + "-after");
        }
        if (isField)
            assertFieldValue(fieldName, vals[fieldFrom]);
        else
            assertText(fieldName, vals[fieldFrom]);
    }

}
