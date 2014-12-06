// Copyright 2006-2014 The Apache Software Foundation
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
package org.apache.tapestry5.integration.app1.components;

import java.lang.reflect.Type;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Outputs the type and genericType of the 'value' binding in a div
 */
public class GenericTypeDisplay {
    @Inject
    private ComponentResources resources;
    
    @Parameter(required=true, defaultPrefix=BindingConstants.LITERAL)
    private String description;
    
    @Parameter(required=true)
    private Object value;
    
    void afterRender(MarkupWriter writer) {
        writer.element("div");
        Class<?> type = resources.getBoundType("value");
        Type genericType = resources.getBoundGenericType("value");
        String text = String.format("description=%s,type=%s,genericType=%s", description, type.getName(), genericType.toString());
        writer.write(text);
        writer.end();
    }
}
