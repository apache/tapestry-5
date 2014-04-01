package org.apache.tapestry5.ioc.test;

import static org.apache.tapestry5.ioc.test.RegistryShutdownType.AFTER_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.test.TapestryIOCJUnit4ClassRunnerAfterClassTest.TestModule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TapestryIOCJUnit4ClassRunner.class)
@Registry(modules=TestModule.class, shutdown=AFTER_CLASS)
public class TapestryIOCJUnit4ClassRunnerAfterClassTest {
	public static class TestModule {
		public List<String> buildList() {
			List<String> list = new ArrayList<String>();
			list.add("foo");
			return list;
		}
	}
	
	@Inject
	private List<String> list;
	
	@Test
	public void testInjectA() {
		Assert.assertEquals(1, list.size());
		list.add("bar");
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void testInjectB() {
		Assert.assertEquals(2, list.size());
	}
}
