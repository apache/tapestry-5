// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;

/**
 * Page for demonstrating/testing the use of the BindParameter mixin demonstration.
 */
public class BindParameterDemo
{

    @Property
    private String myproperty;

    @Property
    private Integer myproperty2;

    @Property
    private String myproperty3;

    @Property
    private String myproperty4;

    @Property
    private String myproperty5;
    
    @Property
    private String myproperty6;
    
    @SetupRender
    void initMyprop()
    {
        myproperty="mypropertyvalue";
        myproperty2=10;
        myproperty3="hello";
        myproperty4="supervalue";
        myproperty5="goodbye";
        myproperty6="publishedvalue";
    }
}
