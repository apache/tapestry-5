// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.pages;

import java.util.List;

import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.integration.app1.data.Track;
import org.apache.tapestry.integration.app1.services.MusicLibrary;

public class GridDemo
{
    @Inject
    private MusicLibrary _library;

    private Track _track;

    public Track getTrack()
    {
        return _track;
    }

    public void setTrack(Track track)
    {
        _track = track;
    }

    public List<Track> getTracks()
    {
        return _library.getTracks();
    }
}
