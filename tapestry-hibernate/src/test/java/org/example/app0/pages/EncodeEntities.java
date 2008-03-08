package org.example.app0.pages;

import java.util.List;

import org.apache.tapestry.annotations.Property;
import org.apache.tapestry.ioc.annotations.Inject;
import org.example.app0.entities.User;
import org.hibernate.Session;

public class EncodeEntities {
	@Inject
	@Property
	private Session _session;
	
	@SuppressWarnings("unused")
	@Property
	private User _user;
	
	void onCreate() {
		User user = new User();
		user.setFirstName("name");
		_session.save(user);
	}

	@SuppressWarnings("unchecked")
	User onPassivate() {
		List<User> users = _session.createQuery("from User").list();
		if (users.isEmpty())
			return null;
		
		return users.get(0);
	}
	
	void onActivate(User user) {
		_user = user;
	}
}
