// Copyright  2008 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages.music;


import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Track;

public class MusicDetails2
{   
    @Property
    @PageActivationContext
    private Track track;
    
    void onActivate(Track track)
    {
        throw new RuntimeException("onActivate invoked unexpectedly");
    }
    
    Object onPassivate()
    {
        throw new RuntimeException("onPassivate invoked unexpectedly");
    }
}
