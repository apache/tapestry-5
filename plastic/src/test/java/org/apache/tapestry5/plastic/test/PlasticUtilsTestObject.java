// Copyright 2023 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.plastic.test;

import org.apache.tapestry5.plastic.test_.Enumeration;

public class PlasticUtilsTestObject extends PlasticUtilsTestObjectSuperclass
{
    
    public static final String STRING = "A nice string";
    public static final int[] INT_ARRAY = new int[] {1, 42};
    public static final String OTHER_STRING = "Another nice string";
    public static final Enumeration ENUMERATION = Enumeration.FILE_NOT_FOUND;
    public static final boolean TRUE_OF_FALSE = true;
    
    public PlasticUtilsTestObject()
    {
    }
    
    public PlasticUtilsTestObject(String ignored, boolean b)
    {
        setString("?");
        setString(ignored);
        setTrueOrFalse(b);
    }
    
    
    private String string = STRING;
    
    private String otherString = OTHER_STRING;
    
    private String nullString = null;
    
    private Enumeration enumeration = ENUMERATION;
    
    private int[] intArray = INT_ARRAY;
    
    private boolean trueOrFalse = TRUE_OF_FALSE;

    public String getString() 
    {
        return string;
    }
    
    public void setString(String string) 
    {
        this.string = string;
    }

    public String getOtherString() 
    {
        return otherString;
    }
    
    public void setOtherString(String otherString) 
    {
        this.otherString = otherString;
    }
    
    public String getNullString() 
    {
        return nullString;
    }
    
    public void setNullString(String nullString) 
    {
        this.nullString = nullString;
    }
    
    public Enumeration getEnumeration() 
    {
        return enumeration;
    }
    
    public void setEnumeration(Enumeration enumeration) 
    {
        this.enumeration = enumeration;
    }
    
    public int[] getIntArray() 
    {
        return intArray;
    }
    
    public void setIntArray(int[] intArray) 
    {
        this.intArray = intArray;
    }
    
    public boolean isTrueOrFalse() 
    {
        return trueOrFalse;
    }
    
    public void setTrueOrFalse(boolean trueOrFalse) 
    {
        this.trueOrFalse = trueOrFalse;
    }
    
}
