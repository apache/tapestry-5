package org.apache.tapestry5.integration.app1.pages;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.annotations.Property;

public class GenericTypeDemo {
    private Set<Long> setOfLongs;
    
    @Property
    private Map<String, String> mapOfStrings;
    
    public List<List<Date>> getListOfListOfDates() {
        throw new UnsupportedOperationException();
    }
    
    public void setSetOfLongs(Set<Long> setOfLongs) {
        throw new UnsupportedOperationException();
    }
}
