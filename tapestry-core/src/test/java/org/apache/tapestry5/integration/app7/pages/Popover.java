package org.apache.tapestry5.integration.app7.pages;

import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class Popover {

	@Environmental
	JavaScriptSupport jsSupport;
	
	void afterRender() {
		jsSupport.require("app/Popover");
	}

	
}
