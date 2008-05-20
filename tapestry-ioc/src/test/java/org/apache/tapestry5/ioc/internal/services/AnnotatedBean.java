// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.annotations.Scope;

public class AnnotatedBean
{
    public String getReadWrite()
    {
        return null;
    }

    public void setReadWrite(String value)
    {
    }

    public String getAnnotationOnWrite()
    {
        return null;
    }

    @Scope("onwrite")
    public void setAnnotationOnWrite(String value)
    {
    }

    @Scope("onread")
    public String getAnnotationOnRead()
    {
        return null;
    }

    @Scope("onwrite")
    public void setAnnotationOnRead(String value)
    {
    }

    public String getReadOnly()
    {
        return null;
    }
}
