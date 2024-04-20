package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.Zone;

@Import(stylesheet = "context:css/ie-only.css")
public class SubclassWithImport extends SuperclassWithImport {
    
    // Just to test a bug happening when a parameter is an array type
    @Parameter
    private Zone[][][][][] zones;
    
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
