// Copyright 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.mixins;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Renders a plain-text version of a value where
 */
public class TextOnlyOnDisabled
{
    @BindParameter
    private Object value;

    @BindParameter
    private boolean disabled;

    @BindParameter
    private FieldTranslator translate;

    @Inject
    private TypeCoercer coercer;

    @InjectContainer
    private ClientElement field;

    @Inject
    private ComponentResources resources;

    Boolean beginRender(MarkupWriter writer)
    {
        if (disabled)
        {
            // We can short-circuit the text field's beginRender phase, but
            // not it's afterRender phase, and TextField calls writer.end()
            // in end render. So we add a dummy element to provide an element to end.
            writer.element("span", "id", field.getClientId());
            writer.write(translate.toClient(value));

            return false;
        }
        return null;
    }

}
