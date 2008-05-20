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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Persist;

public class EventMethodTranslate
{
    @Persist
    private int count;

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    String onToClientFromCount()
    {
        if (count == 0) return "zero";

        // Get the default behavior
        return null;
    }

    Object onParseClientFromCount(String input) throws ValidationException
    {
        if (input == null || input.equals("")) return null;

        // And it gets tricky because we probably should trim spaces!

        if (input.equalsIgnoreCase("zero")) return 0;

        if (input.equalsIgnoreCase("i")) throw new ValidationException("Rational numbers only, please.");

        // Get the default behavior.

        return null;
    }

    void onValidateFromCount(Integer count) throws ValidationException
    {
        // count may be null
        if (count == null) return;

        if (count.equals(13)) throw new ValidationException("Thirteen is an unlucky number.");
    }
}
