package org.apache.tapestry5.internal.structure

import org.apache.tapestry5.http.internal.services.RequestGlobalsImpl
import org.apache.tapestry5.http.services.Request
import org.apache.tapestry5.test.ioc.TestBase
import org.testng.annotations.Test

class ComponentPageElementResourcesImplTest extends TestBase {

    def create(productionMode, tracing, request) {
        RequestGlobalsImpl rg = new RequestGlobalsImpl()

        rg.storeRequestResponse(request, null)

        new ComponentPageElementResourcesImpl(null, null, null, null, null, null, null, null, null, null, null, productionMode, tracing, rg)
    }

    @Test
    void production_mode_prevents_render_tracing() {
        def r = create true, true, null

        assertFalse r.renderTracingEnabled
    }

    @Test
    void request_not_checked_if_tracing_enabled() {
        def r = create false, true, null

        assertTrue r.renderTracingEnabled
    }

    @Test
    void tracing_enabled_by_request_parameter() {

        def request = newMock Request
        def r = create false, false, request

        expect(request.getParameter("t:component-trace")).andReturn("true")

        replay()

        assertTrue r.renderTracingEnabled

        verify()
    }

    @Test
    void tracing_not_enabled_by_request_parameter() {
        def request = newMock Request
        def r = create false, false, request

        expect(request.getParameter("t:component-trace")).andReturn(null)

        replay()

        assertFalse r.renderTracingEnabled

        verify()
    }

    @Test
    void tracing_not_enabled_if_no_request() {
        def r = create false, false, null

        assertFalse r.renderTracingEnabled
    }
}
