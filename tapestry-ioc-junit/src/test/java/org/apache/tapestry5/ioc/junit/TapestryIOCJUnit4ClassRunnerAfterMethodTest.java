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

package org.apache.tapestry5.ioc.junit;

import static org.apache.tapestry5.ioc.junit.RegistryShutdownType.AFTER_METHOD;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.junit.Registry;
import org.apache.tapestry5.ioc.junit.TapestryIOCJUnit4ClassRunner;
import org.apache.tapestry5.ioc.junit.TapestryIOCJUnit4ClassRunnerAfterMethodTest.AfterMethodTestModule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TapestryIOCJUnit4ClassRunner.class)
@Registry(modules=AfterMethodTestModule.class, shutdown=AFTER_METHOD)
@FixMethodOrder(NAME_ASCENDING) // guarantees test ordering
public class TapestryIOCJUnit4ClassRunnerAfterMethodTest {
	public static class AfterMethodTestModule {
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
		assertArrayEquals(new Object[] { "foo" }, list.toArray());

		list.add("bar");
	}

	@Test
	public void testInjectB() {
		assertArrayEquals(new Object[] { "foo" }, list.toArray());
		
		list.add("baz");
	}

	@Test
	public void testInjectC() {
		assertArrayEquals(new Object[] { "foo" }, list.toArray());
	}
}
