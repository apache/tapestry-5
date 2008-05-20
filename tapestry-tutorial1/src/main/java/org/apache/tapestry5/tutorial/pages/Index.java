// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.tutorial.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.tutorial.entities.Address;
import org.hibernate.Session;

import java.util.List;
import java.util.Random;

public class Index
{
    private final Random random = new Random();

    @InjectPage
    private Guess guess;

    @Inject
    private Session session;

    Object onAction()
    {
        int target = random.nextInt(10) + 1;

        return guess.initialize(target);
    }

    @SuppressWarnings({ "unchecked" })
    public List<Address> getAddresses()
    {
        return session.createCriteria(Address.class).list();
    }
}
