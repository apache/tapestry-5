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

package org.apache.tapestry.integration.app4.pages;

public class Destination
{
    private String _message;

    private String _value;

    public String getMessage()
    {
        return _message;
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }

    String onPassivate()
    {
        return _value;
    }

    void onActivate(String value)
    {
        addMessage("onActivate(String) invoked");

        _value = value;
    }

    void onActivate()
    {
        addMessage("onActivate() invoked");
    }

    private void addMessage(String text)
    {
        if (_message == null)
        {
            _message = text;
            return;
        }

        _message += " - " + text;
    }


}
