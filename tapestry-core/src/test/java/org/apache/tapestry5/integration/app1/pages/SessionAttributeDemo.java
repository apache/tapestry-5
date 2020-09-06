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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.ioc.annotations.Inject;

public class SessionAttributeDemo
{
    @Inject
    private Request request;
    
    @SessionAttribute
    private Track favoriteTrack;
    
    @SessionAttribute("track_in_session")
    private Track anotherTrack;

    void onActivate()
    {
        favoriteTrack = new Track();
        favoriteTrack.setTitle("Foo");
        
        anotherTrack = new Track();
        anotherTrack.setTitle("Bar");
    }
    
    public Track getFavoriteTrack(){
        return (Track) request.getSession(true).getAttribute("favoriteTrack");
    }
    
    public Track getAnotherTrack(){
        return (Track) request.getSession(true).getAttribute("track_in_session");
    }
}
