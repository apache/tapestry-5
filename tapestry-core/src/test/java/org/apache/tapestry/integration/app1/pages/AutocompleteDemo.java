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

import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.integration.app1.data.Track;
import org.apache.tapestry.integration.app1.services.MusicLibrary;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.Collections;
import java.util.List;

public class AutocompleteDemo
{
    @Inject
    private MusicLibrary _library;

    @Persist
    private String _title;

    List onProvideCompletionsFromTitle(String partialTitle) throws Exception
    {
        List<Track> matches = _library.findByMatchingTitle(partialTitle);

        List<String> result = CollectionFactory.newList();

        for (Track t : matches)
            result.add(t.getTitle());

        Collections.sort(result);

        // Thread.sleep(1000);

        return result;
    }

    public String getTitle()
    {
        return _title;
    }

    public void setTitle(String title)
    {
        _title = title;
    }
}
