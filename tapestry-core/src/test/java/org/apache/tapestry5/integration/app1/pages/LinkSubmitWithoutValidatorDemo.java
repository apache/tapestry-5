package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.EventConstants;

/**
 * Created by IntelliJ IDEA.
 * User: steent
 * Date: 2009-aug-12
 * Time: 19:19:35
 * To change this template use File | Settings | File Templates.
 */
public class LinkSubmitWithoutValidatorDemo {
    @Property
    private String searchString;

    @Property
    @Persist
    private String result;

    @OnEvent(component = "searchForm", value = EventConstants.SUCCESS)
    void onSearchSuccess()
    {
        result = "" + searchString + " not found!";
    }
}
