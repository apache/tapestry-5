// Copyright 2023 The Apache Software Foundation
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

package org.example.app6.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.example.app6.entities.User;

public class Sign
{
	public static final String GREETING = "Signed as ";
	@Inject
	private ApplicationStateManager applicationStateManager;
	
	@InjectPage
	private Login login;
	
	public String getGreeting()
	{
		User user = applicationStateManager.getIfExists(User.class);
		return user != null ? GREETING + user.getFirstName() : null;
	}
	
	public boolean isLoggedIn()
	{
		return applicationStateManager.exists(User.class);
	}
	
	public boolean isNotLogged()
	{
		return !isLoggedIn();
	}
	
	Object onActionFromSignIn()
	{
		return login;
	}
}