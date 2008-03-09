package org.apache.tapestry.integration.app1.pages;

public class CachedPage2 extends CachedPage {

	@Override
	public int getValue() {
		return super.getValue()+1;
	}
	
}
