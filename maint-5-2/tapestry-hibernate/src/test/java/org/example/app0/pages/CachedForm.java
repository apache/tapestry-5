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

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app0.entities.User;
import org.example.app0.services.UserDAO;
import org.hibernate.Session;

import java.util.List;

@SuppressWarnings("unused")
public class CachedForm
{
    @Property
    private String name;

    @Property
    private User user;

    @Property
    private int index;

    @Inject
    private Session session;

    @Inject
    private UserDAO userDAO;

    @CommitAfter
    void onSuccess()
    {
        User user = new User();
        user.setFirstName(name);

        session.save(user);
    }

    @SuppressWarnings("unchecked")
    @Cached
    public List<User> getUsers()
    {
        return session.createQuery("from User").list();
    }

    void onActionFromSetup()
    {
        userDAO.deleteAll();
    }


}
