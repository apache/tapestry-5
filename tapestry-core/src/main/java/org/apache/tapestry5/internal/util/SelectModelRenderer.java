// Copyright 2008-2024 The Apache Software Foundation
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

    private final boolean raw;

    private int optgroupIdx = -1;
    private boolean inOptgroup = false;

    public SelectModelRenderer(final MarkupWriter writer, ValueEncoder encoder, boolean raw)
    {
        this.writer = writer;
        this.encoder = encoder;
        this.raw = raw;
    }

    @Override
    public void beginOptionGroup(OptionGroupModel groupModel)
    {
        this.optgroupIdx++;
        this.inOptgroup = true;
        this.writer.element("optgroup", "label", groupModel.getLabel());

        writeDisabled(groupModel.isDisabled());
        writeAttributes(groupModel.getAttributes());
    }

    @Override
    public void endOptionGroup(OptionGroupModel groupModel)
    {
        this.inOptgroup = false;
        this.writer.end(); // select
    }

    @Override
    @SuppressWarnings("unchecked")
    public void option(OptionModel optionModel)
    {
        Object optionValue = optionModel.getValue();

        String clientValue = this.encoder.toClient(optionValue);

        this.writer.element("option", "value", clientValue);

        if (this.inOptgroup && this.optgroupIdx > -1)
        {
            this.writer.attributes("data-optgroup-idx", this.optgroupIdx);
        }

        if (isOptionSelected(optionModel, clientValue))
        {
            this.writer.attributes("selected", "selected");
        }

        writeDisabled(optionModel.isDisabled());
        writeAttributes(optionModel.getAttributes());


        if (this.raw)
        {
            this.writer.writeRaw(optionModel.getLabel());
        } else
        {
            this.writer.write(optionModel.getLabel());
        }

        this.writer.end();
    }

    private void writeDisabled(boolean disabled)
    {
        if (disabled)
        {
            this.writer.attributes("disabled", "disabled");
        }
    }

    private void writeAttributes(Map<String, String> attributes)
    {
        if (attributes == null)
        {
            return;
        }

        for (Map.Entry<String, String> e : attributes.entrySet())
        {
            this.writer.attributes(e.getKey(), e.getValue());
        }
    }

    /**
     * If true, then the selected attribute will be written. This implementation always returns false.
     */
    protected boolean isOptionSelected(OptionModel optionModel, String clientValue)
    {
        return false;
    }

}
