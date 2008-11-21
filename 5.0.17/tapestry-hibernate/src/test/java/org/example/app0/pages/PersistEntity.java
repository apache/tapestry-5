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

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app0.entities.User;
import org.example.app0.services.UserDAO;
import org.hibernate.Session;

import java.util.List;

public class PersistEntity
{
    @Persist("entity")
    @Property
    private User user;

    @Inject
    private UserDAO userDAO;

    @Inject
    private Session session;

    @Inject
    private HibernateSessionManager manager;

    void onCreateEntity()
    {
        User user = new User();
        user.setFirstName("name");

        userDAO.add(user);

        this.user = user;
    }

    void onChangeName()
    {
        user.setFirstName("name2");

        // No commit, so no real change.
    }

    void onSetToTransient()
    {
        user = new User();
    }

    void onSetToNull()
    {
        user = null;
    }

    void onDelete()
    {
        List<User> users = userDAO.findAll();

        userDAO.delete(users.toArray(new User[0]));
    }
}
