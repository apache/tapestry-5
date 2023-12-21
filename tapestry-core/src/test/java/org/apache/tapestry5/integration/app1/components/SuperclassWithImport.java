package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;

@Import(stylesheet = "context:css/via-import.css")
public class SuperclassWithImport {
    
    @Cached
    public int getOther() 
    { 
        return 4; 
    }
    
    public void cleanupRender(MarkupWriter writer) {
        writer.element("p").text("Other: " + getOther() + " : " + getOther());
        writer.end();
    }

}
