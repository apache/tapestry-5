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

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app0.entities.User;

public class EncodeEntities
{
    @Inject
    private HibernateSessionManager sessionManager;

    @SuppressWarnings("unused")
    @Property
    private User user;

    @CommitAfter
    void onCreate()
    {
        User user = new User();
        user.setFirstName("name");

        sessionManager.getSession().save(user);
    }

    @SuppressWarnings("unchecked")
    User onPassivate()
    {
    	// Use ordering so that we get the most recently inserted users first.    	
        List<User> users = sessionManager.getSession().createQuery("from User order by id desc")
                .list();
        if (users.isEmpty())
            return null;

        return users.get(0);
    }

    void onActivate(User user)
    {
        this.user = user;
    }
}
