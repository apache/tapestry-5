package t5.webresources.components;

import java.util.List;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentClassResolver;

@Import(module = "bootstrap/dropdown")
public class Layout {

    @Property
    private String name;
    
    @Inject
    private ComponentClassResolver componentClassResolver;
    
    public List<String> getPageNames() {
        return componentClassResolver.getPageNames();
    }
    
}
