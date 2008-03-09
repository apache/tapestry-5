// Copyright 2008 The Apache Software Foundation
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

package org.example.app0.pages;

import java.util.List;

import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.annotations.Property;
import org.apache.tapestry.ioc.annotations.Inject;
import org.example.app0.entities.User;
import org.hibernate.Session;

public class PersistEntity {
	@Persist("entity")
	@Property
	private User _user;
	
	@Inject
	private Session _session;
		
	void onCreateEntity() {
		User user = new User();
		user.setFirstName("name");
		_session.save(user);
		_user = user;
	}
	
	void onChangeName() {
		_user.setFirstName("name2");
		// avoid having the changes saved if the request transaction is committed
		_session.evict(_user);
	}
	
	void onSetToTransient() {
		_user = new User();
	}
	
	void onSetToNull() {
		_user = null;
	}
	
	void onDelete() {
		for(User user : (List<User>)_session.createQuery("from User").list())
			_session.delete(user);
	}
}
