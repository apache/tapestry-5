package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;

@Import(stylesheet = "context:css/ie-only.css")
public class SubclassWithImport extends SuperclassWithImport {
    
    @Cached
    public int getInt() 
    { 
        return 2; 
    }
    
    public void setupRender(MarkupWriter writer) {
        writer.element("p").text("Int: " + getInt() + " : " + getInt());
        writer.end();
    }
}
