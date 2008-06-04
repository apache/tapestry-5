// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.integration.app1.services.DearGodWhyMeException;
import org.apache.tapestry5.integration.app1.services.ReverseStrings;

public class MethodAdviceDemo
{
    @Persist
    private String text;

    @Validate("required")
    public String getText()
    {
        return text;
    }

    @ReverseStrings
    public void setText(String text)
    {
        this.text = text;
    }

    @ReverseStrings
    public String getMessage()
    {
        return "Hello!";
    }

    @ReverseStrings
    public int getVersion()
    {
        return 5;
    }

    @ReverseStrings
    public int[] getIntArray()
    {
        return null;
    }

    @ReverseStrings
    public void setIntArray(int[] array)
    {
    }

    @ReverseStrings
    public String[] getStringArray()
    {
        return null;
    }

    @ReverseStrings
    public void setStringArray(String[] array)
    {
    }

    @ReverseStrings
    public String getCranky() throws DearGodWhyMeException
    {
        throw new DearGodWhyMeException();
    }

}
