// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1.mixins;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.annotations.Parameter;

/**
 * Mixin demonstrating the use of the BindParameter annotation, using implicit parent-parameter naming.
 * It echos the current value, changes said value for the duration of the component
 * render, then changes it back and re-echos it.
 */
public class EchoValueWithId
{
    @Parameter
    private String id;
    
    @BindParameter
    private String value;

    private String temp;

    @BeginRender
    void beginRender(MarkupWriter writer)
    {
        writer.element("div","id", id + "_before");
        writer.writeRaw(value + "-before");
        writer.end();
        temp = value;
        value = "publishedvaluetemporaryvalue";
    }

    @AfterRender
    void afterRender(MarkupWriter writer) {
        value = temp;
        writer.element("div","id", id + "_after");
        writer.writeRaw(value + "-after");
        writer.end();
    }
}
