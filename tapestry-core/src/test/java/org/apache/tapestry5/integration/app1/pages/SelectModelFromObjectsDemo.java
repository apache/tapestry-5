package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.integration.app1.services.MusicLibrary;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.SelectModelFactory;

public class SelectModelFromObjectsDemo
{
    @Inject
    private MusicLibrary library;
    
    @Inject
    private SelectModelFactory modelFactory;
    
    @Property
    private SelectModel model;
    
    @Property
    @Persist
    private Track track;
    
    void onPrepare()
    {
        model = modelFactory.create(library.getTracks());
    }
    
    
}
