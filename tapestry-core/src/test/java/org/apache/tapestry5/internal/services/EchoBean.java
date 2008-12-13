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

package org.apache.tapestry5.internal.services;

import java.util.List;

public class EchoBean
{
    public int storedInt;

    private double storedDouble;

    private String storedString;

    private StringSource stringSource;

    public StringSource getStringSource()
    {
        return stringSource;
    }

    public void setStringSource(StringSource stringSource)
    {
        this.stringSource = stringSource;
    }

    public int echoInt(int value, int multiplyBy)
    {
        return value * multiplyBy;
    }

    public double echoDouble(double value, double multiplyBy)
    {
        return value * multiplyBy;
    }

    public int getStoredInt()
    {
        return storedInt;
    }

    public void setStoredInt(int storedInt)
    {
        this.storedInt = storedInt;
    }

    public double getStoredDouble()
    {
        return storedDouble;
    }

    public void setStoredDouble(double storedDouble)
    {
        this.storedDouble = storedDouble;
    }

    public String getStoredString()
    {
        return storedString;
    }

    public void setStoredString(String storedString)
    {
        this.storedString = storedString;
    }

    public String echoString(String value, String before, String after)
    {
        return String.format("%s - %s - %s", before, value, after);
    }

    public List echoList(List input)
    {
        return input;
    }
}
