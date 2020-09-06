package org.apache.tapestry5.corelib.components

import org.apache.tapestry5.ComponentResources
import org.apache.tapestry5.MarkupWriter
import org.apache.tapestry5.http.Link
import org.apache.tapestry5.services.PageRenderLinkSource
import org.apache.tapestry5.test.TapestryTestCase
import org.testng.annotations.Test


class PageLinkTest extends TapestryTestCase {

    @Test
    void parameters_parameter_overrides_ARP() {

        Link link = newMock Link
        ComponentResources resources = newMock ComponentResources
        PageRenderLinkSource source = newMock PageRenderLinkSource
        MarkupWriter writer = newMock MarkupWriter

        expect(resources.isBound("context")).andReturn false
        expect(source.createPageRenderLink("Target")).andReturn link

        expect(resources.isBound("parameters")).andReturn true

        // TAP5-2126:
        expect(link.removeParameter("foo")).andReturn link

        expect(link.addParameterValue("foo", "bar")).andReturn link

        expect(link.toURI()).andReturn "/xyz"

        expect(writer.element("a", "href", "/xyz")).andReturn null
        writer.attributes();

        resources.renderInformalParameters(writer);

        replay()

        PageLink component = new PageLink(
            resources: resources,
            page: "Target",
            linkSource: source)


        // Not sure why this is necessary; should be able to set it as with others
        // above!
        set component, "parameters", [foo: "bar"]

        component.beginRender writer

        verify()
    }
}
