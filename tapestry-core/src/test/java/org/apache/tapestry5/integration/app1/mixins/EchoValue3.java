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

import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ClientElement;

/**
 * Mixin demonstrating the use of BindParameter, using explicit naming of the parent parameter.
 */
public class EchoValue3
{
    @BindParameter(value = {"object","value"})
    private Object boundParameter;

    @InjectContainer
    private ClientElement element;

    private Object temp;

    void beginRender(MarkupWriter writer)
    {
        writer.element("div","id",element.getClientId() + "_before3");
        writer.writeRaw("echo3-" + boundParameter + "-before");
        writer.end();
        temp = boundParameter;
        boundParameter = "world";
    }

    void afterRender(MarkupWriter writer)
    {
        boundParameter = temp;
        writer.element("div","id",element.getClientId() + "_after3");
        writer.writeRaw("echo3-" + boundParameter + "-after");
        writer.end();
    }
}