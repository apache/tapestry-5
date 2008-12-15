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

package org.apache.tapestry5.internal.transform.pages;

public class MethodPrefixTarget
{
    // If this is final, then the read is inlined, defeating the test.
    private int _targetField = 42;

    public int getTargetValue()
    {
        return _targetField;
    }

    // Again, necessary to defeat inlining of the value.
    public void setTargetField(int value)
    {
        _targetField = value;
    }
}
