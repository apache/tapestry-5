// Copyright 2024 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.components;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;

public class SuperclassWithFinalCachedMethod
{
    
    @Property
    private int clientId = 1;
    
    private int counter = 0;
    
    @Cached(watch = "clientId")
    protected final List<?> getList() 
    {
        return createList();
    }

    protected List<?> createList() 
    {
        return Arrays.asList("superclass", String.valueOf(counter++));
    }
    
    public final List<?> getListPublic() 
    {
        return getList();
    }
    
}
