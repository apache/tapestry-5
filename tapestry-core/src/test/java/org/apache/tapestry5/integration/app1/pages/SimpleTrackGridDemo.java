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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.integration.app1.data.SimpleTrack;
import org.apache.tapestry.integration.app1.data.Track;
import org.apache.tapestry.integration.app1.services.MusicLibrary;
import org.apache.tapestry.ioc.annotation.Inject;
import org.apache.tapestry.services.BeanModelSource;

import java.util.List;

public class SimpleTrackGridDemo
{
    @Inject
    private MusicLibrary library;

    @Inject
    private BeanModelSource beanModelSource;

    @Inject
    private ComponentResources resources;

    private SimpleTrack track;

    public SimpleTrack getTrack()
    {
        return track;
    }

    public void setTrack(SimpleTrack track)
    {
        this.track = track;
    }

    public List<Track> getTracks()
    {
        return library.getTracks();
    }

    public BeanModel getSimpleTrackModel()
    {
        return beanModelSource.create(SimpleTrack.class, false, resources);
    }
}
