// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.BlueMarker;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.RedMarker;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;

public class ContributeViaAnnotationWithMarkerModule
{
    
    public static void bind(ServiceBinder binder)
    {
        binder.bind(FoeService.class, FoeServiceImpl.class).withId("BlueAndRed")
            .withMarker(new Class[]{RedMarker.class, BlueMarker.class});
    }
    
    @Marker(RedMarker.class)
    public static FoeService build()
    { 
        return new FoeService(){

            public int foe()
            {

                return 0;
            }};
    }
    
    public static FoeService buildWithoutMarker()
    { 
        return new FoeService(){

            public int foe()
            {

                return 0;
            }};
    }

    
    @Contribute(FoeService.class)
    @Marker({BlueMarker.class, RedMarker.class})
    public void contributeMyService(MappedConfiguration configuration)
    {
        
    }
    
    public class FoeServiceImpl implements FoeService{

        public int foe()
        {
            return 0;
        }
        
    }
}
