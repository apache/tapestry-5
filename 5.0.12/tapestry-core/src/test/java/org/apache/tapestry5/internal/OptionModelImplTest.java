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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.OptionModel;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionModelImplTest extends Assert
{
    @Test
    public void basics()
    {
        OptionModel model = new OptionModelImpl("Label", this);

        assertEquals(model.getLabel(), "Label");
        assertFalse(model.isDisabled());
        assertSame(model.getValue(), this);
        assertNull(model.getAttributes());

        model = new OptionModelImpl("Fred", "fred");


        assertEquals(model.toString(), "OptionModel[Fred fred]");
    }
}
