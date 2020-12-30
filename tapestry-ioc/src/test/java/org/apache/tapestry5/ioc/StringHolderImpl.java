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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.internal.services.SimpleAnnotation;

/**
 *
 */
@SimpleAnnotation("StringHolderImpl")
public class StringHolderImpl implements StringHolder
{
    private String value;

    @Override
    @SimpleAnnotation("StringHolderImpl#getValue()")
    public String getValue()
    {
        return value;
    }

    @Override
    public void setValue(String value)
    {
        this.value = value;
    }
}