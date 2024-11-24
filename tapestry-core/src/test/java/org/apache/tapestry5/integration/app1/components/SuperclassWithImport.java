package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;

@Import(stylesheet = "context:css/via-import.css")
public class SuperclassWithImport extends SuperclassWithoutImport {
    
    // Just to test CachedWorker with a watch expression 
    @Persist
    private int counter;
    
    @Persist
    private int secondCounter;
    
    @Cached(watch = "counter")
    public String getOther() 
    { 
        return getCounter() + " " + " from superclass"; 
    }
    
    @Cached(watch = "secondCounter")
    public String getNonOverriden() 
    { 
        return getCounter() + " " + " non-overriden"; 
    }
    
    public void cleanupRender(MarkupWriter writer) {
        writer.element("p").text("Other: " + getOther() + " : " + getOther() + 
                " nonOverriden " + getNonOverriden() + " nonOverriden " + getNonOverriden());
        writer.write(" yeah!!!");
        writer.end();
        counter++;
    }
    
    public int getCounter() {
        return counter;
    }
    
    public int getSecondCounter() {
        return secondCounter;
    }

}
