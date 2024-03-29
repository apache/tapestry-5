//  Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;

public class LinkSubmitDemo
{
    @Property
    @Persist
    @Validate("required,minlength=5")
    private String name;

    @Property
    @Persist
    private String lastClicked;
    
    @Persist
    @Property
    private Double result;
    
    public Object[] getFormContext()
    {
        return new Object[]{Integer.valueOf(7), Double.valueOf(3.14159)};
     }

    void onSelectedFromFred(Integer first, Double second) 
    { 
        lastClicked = "Fred"; 
        
        result = first + second;
    }

    void onNeighbor() { lastClicked = "Barney"; }
    
    void onDaughter() { lastClicked = "Pebbles"; }
}
