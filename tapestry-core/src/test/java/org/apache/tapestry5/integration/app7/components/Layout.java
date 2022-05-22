package org.apache.tapestry5.integration.app7.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

public class Layout {

	@Parameter
	@Property
	String pageTitle;
	
	@Property
	String appTitle;
	
	void setupRender() {
		appTitle = "Tapestry Integration App 7";
	}
	
}
