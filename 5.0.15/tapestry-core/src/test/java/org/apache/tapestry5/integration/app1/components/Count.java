// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.internal.util.IntegerRange;

/**
 * A component that can count up or count down.
 * <p/>
 * This is useful as a demonstration; now that the prop binding supports {@link IntegerRange integer ranges}, it's much
 * less necessary.
 */
public class Count
{
    @Parameter
    private int start = 1;

    @Parameter(required = true)
    private int end;

    @Parameter
    private int value;

    private boolean increment;

    @SetupRender
    void initializeValue()
    {
        value = start;

        increment = start < end;
    }

    @AfterRender
    boolean next()
    {
        if (increment)
        {
            int newValue = value + 1;

            if (newValue <= end)
            {
                value = newValue;
                return false; // re-render body
            }
        }
        else
        {
            int newValue = value - 1;

            if (newValue >= end)
            {
                value = newValue;
                return false; // re-render body
            }
        }

        return true;
    }
}
