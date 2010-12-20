package org.apache.tapestry5.internal.structure;

import static org.apache.tapestry5.SymbolConstants.COMPONENT_RENDER_TRACING_ENABLED;
import static org.apache.tapestry5.SymbolConstants.PRODUCTION_MODE;

import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.Test;

public class ComponentPageElementImplTest extends TestBase {
	/** TAP5-742 */
	@Test public void component_render_tracing() {
		Request request = newMock(Request.class);
		SymbolSource symbolSource = newMock(SymbolSource.class);
		
		Page page = getMocksControl().createMock(Page.class);
		Instantiator instantiator = newMock(Instantiator.class);
		Location location = newMock(Location.class);
		ComponentPageElementResources elementResources = newMock(ComponentPageElementResources.class);
		ComponentModel model = newMock(ComponentModel.class);
		
		getMocksControl().resetToNice();

		expect(instantiator.getModel()).andReturn(model).anyTimes();
		
		// off by default
		expect(symbolSource.valueForSymbol(PRODUCTION_MODE)).andReturn("false");
		expect(symbolSource.valueForSymbol(COMPONENT_RENDER_TRACING_ENABLED)).andReturn("false");
		expect(request.getParameter("t:component-trace")).andReturn("false");
		
		// enable by query parameter
		expect(symbolSource.valueForSymbol(PRODUCTION_MODE)).andReturn("false");
		expect(symbolSource.valueForSymbol(COMPONENT_RENDER_TRACING_ENABLED)).andReturn("false");
		expect(request.getParameter("t:component-trace")).andReturn("true");
		
		// enable by symbol
		expect(symbolSource.valueForSymbol(PRODUCTION_MODE)).andReturn("false");
		expect(symbolSource.valueForSymbol(COMPONENT_RENDER_TRACING_ENABLED)).andReturn("true");
		expect(request.getParameter("t:component-trace")).andReturn("false");
		
		// off no matter what in production mode
		expect(symbolSource.valueForSymbol(PRODUCTION_MODE)).andReturn("true");
		expect(symbolSource.valueForSymbol(COMPONENT_RENDER_TRACING_ENABLED)).andReturn("true");
		expect(request.getParameter("t:component-trace")).andReturn("false");

		replay();
		ComponentPageElementImpl c;	// need to create every time because of changing symbols
		
		c = new ComponentPageElementImpl(page, null, "id", "nestedId", "completeid", "elementname", instantiator, location, elementResources, request, symbolSource);
		assertFalse(c.isRenderTracingEnabled());
		
		c = new ComponentPageElementImpl(page, null, "id", "nestedId", "completeid", "elementname", instantiator, location, elementResources, request, symbolSource);
		assertTrue(c.isRenderTracingEnabled());
		
		c = new ComponentPageElementImpl(page, null, "id", "nestedId", "completeid", "elementname", instantiator, location, elementResources, request, symbolSource);
		assertTrue(c.isRenderTracingEnabled());
		
		c = new ComponentPageElementImpl(page, null, "id", "nestedId", "completeid", "elementname", instantiator, location, elementResources, request, symbolSource);
		assertFalse(c.isRenderTracingEnabled());
	}
}
