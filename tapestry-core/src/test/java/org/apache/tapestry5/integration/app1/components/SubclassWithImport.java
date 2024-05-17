package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;

@Import(stylesheet = "context:css/ie-only.css")
public class SubclassWithImport extends SuperclassWithImport {
    
    @Property int getIntCallCount;
    
    // Just to test a bug happening when a parameter is an array type
    @Parameter
    private Zone[][][][][] zones;
    
    // Just to test CachedWorker with a watch expression
    @Cached(watch = "counter")
    public String getInt() 
    { 
        getIntCallCount++;
        return getCounter() + " from subclass";
    }
    
    @Cached(watch = "counter")
    public String getOther() 
    { 
        return getCounter() + " " + " from superclass"; 
    }
    
    public void setupRender(MarkupWriter writer) {
        getIntCallCount = 0;
        writer.element("p").text("Int: " + getInt() + " : " + getInt());
        writer.end();
    }

}
