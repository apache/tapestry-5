// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.cdi.test.pages;

import javax.inject.Inject;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.cdi.test.beans.StatefulEJBBean;

public class StatefulPage {

	@Inject
	private StatefulEJBBean statefulBean;

	@BeginRender
	public void rend(MarkupWriter mw) {
		
		mw.writeRaw(String.valueOf(statefulBean.num()) + String.valueOf(statefulBean.inc())
				+ String.valueOf(statefulBean.num())+"stateful");//prepend XXXstateful at the root of the page

	}

	@CleanupRender
	public void clean() {
		
	}

}
