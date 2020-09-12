// Copyright 2013 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.test.internal;

import org.apache.tapestry5.beaneditor.ReorderProperties;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.IntermediateType;

@ReorderProperties("reorder") // no meaning, just for testing whether the proxy will have it
public class NonAnnotatedServiceInterfaceImpl implements NonAnnotatedServiceInterface
{

    @Override
    @Advise(id = "id", serviceInterface = NonAnnotatedServiceInterface.class)
    public String execute(@IntermediateType(String.class) int i) // annotation just for checking too
    { 

        // a try/catch block, as it caused some troubles with proxy class generation
        try
        {
            System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;

    }
    
    @Advise(id = "right")
    public String duplicatedAnnotation(@IntermediateType(String.class) String parameter)
    {
        return null;
    }

}
