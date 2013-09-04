package t5.webresources.components

import org.apache.tapestry5.annotations.Import
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.services.ComponentClassResolver

@Import(stack="core", module = "bootstrap/dropdown")
class Layout {

    @Inject
    private ComponentClassResolver resolver

    String name

    List<String> getPageNames() {
        resolver.pageNames.findAll { !it.startsWith("core/") }
    }


}
