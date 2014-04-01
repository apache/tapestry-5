package org.apache.tapestry5.ioc.test;

import static org.apache.tapestry5.ioc.test.RegistryShutdownType.AFTER_METHOD;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.test.TapestryIOCJUnit4ClassRunnerModuleDefTest.ModuleDefTestModule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TapestryIOCJUnit4ClassRunner.class)
@Registry(modules=ModuleDefTestModule.class, shutdown=AFTER_METHOD)
public class TapestryIOCJUnit4ClassRunnerModuleDefTest {
	@ModuleDef
	public static MapModuleDef createMapModuleDef() {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("foo", new Date(100));
		map.put("bar", new Long(999));
		
		return new MapModuleDef(map);
	}

	public static class ModuleDefTestModule {
		public Integer buildBaz() {
			return new Integer(666);
		}
	}

	@Inject
	private Date foo;
	
	@Inject
	private Long bar;
	
	@Inject
	private Integer baz;
	
	@Test
	public void testModuleDefInject() {
		Assert.assertEquals(new Date(100), foo);
		Assert.assertEquals(new Long(999), bar);
		Assert.assertEquals(new Integer(666), baz);
	}
}
