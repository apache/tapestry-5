// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.*;

import java.util.Map;

public class SelectModelRenderer implements SelectModelVisitor
{
    private final MarkupWriter writer;

    private final ValueEncoder encoder;

    public SelectModelRenderer(final MarkupWriter writer, ValueEncoder encoder)
    {
        this.writer = writer;
        this.encoder = encoder;
    }

    public void beginOptionGroup(OptionGroupModel groupModel)
    {
        writer.element("optgroup", "label", groupModel.getLabel());

        writeDisabled(groupModel.isDisabled());
        writeAttributes(groupModel.getAttributes());
    }

    public void endOptionGroup(OptionGroupModel groupModel)
    {
        writer.end(); // select
    }

    @SuppressWarnings("unchecked")
    public void option(OptionModel optionModel)
    {
        Object optionValue = optionModel.getValue();

        String clientValue = encoder.toClient(optionValue);

        writer.element("option", "value", clientValue);

        if (isOptionSelected(optionModel, clientValue)) writer.attributes("selected", "selected");

        writeDisabled(optionModel.isDisabled());
        writeAttributes(optionModel.getAttributes());

        writer.write(optionModel.getLabel());

        writer.end();
    }

    private void writeDisabled(boolean disabled)
    {
        if (disabled) writer.attributes("disabled", "disabled");
    }

    private void writeAttributes(Map<String, String> attributes)
    {
        if (attributes == null) return;

        for (Map.Entry<String, String> e : attributes.entrySet())
            writer.attributes(e.getKey(), e.getValue());
    }

    /**
     * If true, then the selected attribute will be written. This implementation always returns false.
     */
    protected boolean isOptionSelected(OptionModel optionModel, String clientValue)
    {
        return false;
    }

}
