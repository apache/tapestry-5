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

package org.apache.tapestry5.upload.internal.services;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class ParameterValueTest
{

    @Test
    public void singleGivesConstructedParameterByDefault() throws Exception
    {
        ParameterValue value = new ParameterValue("foo");
        assertEquals(value.single(), "foo");
    }

    @Test
    public void multiReturnsArrayWithConstructedParameterByDefault() throws Exception
    {
        ParameterValue value = new ParameterValue("foo");
        assertEquals(value.multi(), new String[] { "foo" });
    }

    @Test
    public void singleGivesFirstValueOfMultiValue() throws Exception
    {
        ParameterValue value = new ParameterValue("foo", "blah");
        assertEquals(value.single(), "foo");
    }

    @Test
    public void multiGivesAllValuesOfMultiValue() throws Exception
    {
        ParameterValue value = new ParameterValue("foo");
        value.add("blah");
        assertEquals(value.multi(), new String[] { "foo", "blah" });
    }

    @Test
    public void isMultiIsFalseForSingleValue() throws Exception
    {
        ParameterValue value = new ParameterValue("foo");
        assertFalse(value.isMulti());
    }

    @Test
    public void isMultiIsTrueForMultiValue() throws Exception
    {
        ParameterValue value = new ParameterValue("foo");
        value.add("blah");
        assertTrue(value.isMulti());
    }

    @Test
    public void nullObjectGivesNullForSingleAndMulti() throws Exception
    {
        ParameterValue value = ParameterValue.NULL;
        assertNull(value.single());
        assertNull(value.multi());
        assertFalse(value.isMulti());
    }

}
