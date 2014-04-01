// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
