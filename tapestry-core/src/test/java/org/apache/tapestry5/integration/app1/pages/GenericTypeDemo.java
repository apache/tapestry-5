package org.apache.tapestry5.integration.app1.pages;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;

public class GenericTypeDemo {
	private Set<Long> setOfLongs;
	
	@Property
	private Map<String, String> mapOfStrings;
	
	public List<List<Date>> getListOfListOfDates() {
		List<Date> dates = Arrays.asList(new Date(Long.MIN_VALUE), new Date(0), new Date(Long.MAX_VALUE));
		return Arrays.asList(dates);
	}
	
	public void setSetOfLongs(Set<Long> setOfLongs) {
		this.setOfLongs = setOfLongs;
	}
	
	@SetupRender
	void setupRender() {
		mapOfStrings = new HashMap<String,String>();
		mapOfStrings.put("foo", "bar");
	}
}
