// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.corelib.mixins;

import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.InjectComponent;
import org.apache.tapestry.annotations.MixinAfter;

@ComponentClass
@MixinAfter
public class RenderDisabled
{
    @InjectComponent
    private Field _field;

    @BeginRender
    void begin(MarkupWriter writer)
    {
        if (_field.isDisabled())
            writer.attributes("disabled", "disabled");
    }
}
