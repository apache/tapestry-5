package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.integration.app1.services.MusicLibrary;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.util.List;

public class SelectModelCoercionDemo
{
    @Inject
    private MusicLibrary library;
    
    @Property
    @Persist
    private Track track;
    
    public List<Track> getTracks(){
        return library.getTracks();
    }
    
    
}
