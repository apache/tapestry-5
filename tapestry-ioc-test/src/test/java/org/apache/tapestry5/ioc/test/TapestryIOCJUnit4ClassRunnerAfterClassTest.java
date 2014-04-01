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

import static org.apache.tapestry5.ioc.test.RegistryShutdownType.AFTER_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.test.TapestryIOCJUnit4ClassRunnerAfterClassTest.AfterClassTestModule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TapestryIOCJUnit4ClassRunner.class)
@Registry(modules=AfterClassTestModule.class, shutdown=AFTER_CLASS)
public class TapestryIOCJUnit4ClassRunnerAfterClassTest {
	public static class AfterClassTestModule {
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
