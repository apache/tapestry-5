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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.integration.app1.components.Count;

public class Countdown
{
    @SuppressWarnings("unused")
    @Component(parameters =
            { "start=10", "end=1", "value=countValue" })
    private Count count;

    private int countValue;

    public int getCountValue()
    {
        return countValue;
    }

    public void setCountValue(int countValue)
    {
        this.countValue = countValue;
    }
}
