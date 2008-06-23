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

import org.example.app0.entities.User;
import org.hibernate.Session;

import java.util.List;

public class UserDAOImpl implements UserDAO
{
    private final Session session;

    public UserDAOImpl(Session session)
    {
        this.session = session;
    }

    public void add(User user)
    {
        session.save(user);
    }

    @SuppressWarnings({ "unchecked" })
    public List<User> findAll()
    {
        return (List<User>) session.createQuery("from User").list();
    }

    public void delete(User... users)
    {
        for (User user : users) session.delete(user);
    }

    public void deleteAll()
    {
        for (User u : findAll())
        {
            session.delete(u);
        }
    }
}
