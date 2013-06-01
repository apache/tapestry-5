package t5.wro4j.components

import org.apache.tapestry5.annotations.Import
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.services.ComponentClassResolver

@Import(stack="core", module = "bootstrap")
class Layout {

    @Inject
    private ComponentClassResolver resolver

    String name

    List<String> getPageNames() {
        resolver.pageNames.findAll { !it.startsWith("core/") }
    }


}
