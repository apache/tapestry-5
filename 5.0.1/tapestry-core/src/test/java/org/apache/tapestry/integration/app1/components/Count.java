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

package org.apache.tapestry.integration.app1.components;

import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.internal.util.IntegerRange;

/**
 * A component that can count up or count down.
 * <p>
 * This is useful as a demonstration; now that the prop binding supports
 * {@link IntegerRange integer ranges}, it's much less necessary.
 */
@ComponentClass
public class Count
{
    @Parameter
    private int _start = 1;

    @Parameter(required = true)
    private int _end;

    @Parameter
    private int _value;

    private boolean _increment;

    @SetupRender
    void initializeValue()
    {
        _value = _start;

        _increment = _start < _end;
    }

    @AfterRender
    boolean next()
    {
        if (_increment)
        {
            int newValue = _value + 1;

            if (newValue <= _end)
            {
                _value = newValue;
                return true; // re-render body
            }
        }
        else
        {
            int newValue = _value - 1;

            if (newValue >= _end)
            {
                _value = newValue;
                return true; // re-render body
            }
        }

        return false;
    }
}
