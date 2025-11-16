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

package org.example.app0.services;

import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.example.app0.entities.User;
import org.hibernate.Session;

import java.util.List;

public class UserDAOImpl implements UserDAO
{
    private final HibernateSessionManager sessionManager;

    public UserDAOImpl(HibernateSessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    @Override
    public void add(User user)
    {
        sessionManager.getSession().save(user);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public List<User> findAll()
    {
        return (List<User>) sessionManager.getSession().createQuery("from User").list();
    }

    @Override
    public void delete(User... users)
    {
        for (User user : users)
            sessionManager.getSession().delete(user);
    }

    @Override
    public void deleteAll()
    {
        for (User u : findAll())
        {
            sessionManager.getSession().delete(u);
        }
    }
}
