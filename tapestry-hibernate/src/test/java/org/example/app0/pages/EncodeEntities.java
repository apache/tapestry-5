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

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app0.entities.User;
import org.hibernate.Session;

import java.util.List;

public class EncodeEntities
{
    @Inject
    private Session session;

    @SuppressWarnings("unused")
    @Property
    private User user;

    @CommitAfter
    void onCreate()
    {
        User user = new User();
        user.setFirstName("name");

        session.save(user);
    }

    @SuppressWarnings("unchecked")
    User onPassivate()
    {
        List<User> users = session.createQuery("from User").list();
        if (users.isEmpty())
            return null;

        return users.get(0);
    }

    void onActivate(User user)
    {
        this.user = user;
    }
}
