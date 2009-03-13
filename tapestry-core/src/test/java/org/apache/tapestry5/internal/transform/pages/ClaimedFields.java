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

package org.apache.tapestry5.internal.transform.pages;

public class ClaimedFields
{
    // Make sure results are sorted by putting this first
    // but expecting them last.

    private int _zzfield;

    private int _field1;

    private String _field4;

    public final int getField1()
    {
        return _field1;
    }

    public final void setField1(int field1)
    {
        _field1 = field1;
    }

    public final String getField4()
    {
        return _field4;
    }

    public final void setField4(String field4)
    {
        _field4 = field4;
    }

    public final int getZzfield()
    {
        return _zzfield;
    }

    public final void setZzfield(int zzfield)
    {
        _zzfield = zzfield;
    }

}
