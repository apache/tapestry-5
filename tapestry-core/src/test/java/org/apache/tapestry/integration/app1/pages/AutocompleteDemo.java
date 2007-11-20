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
