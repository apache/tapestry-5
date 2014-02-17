// Copyright 2014 The Apache Software Foundation
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

import java.sql.SQLException;

import javax.persistence.PersistenceContext;

import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app6.AppConstants;
import org.example.app6.entities.User;

/**
 * Demos the CommitAfter annotation on component methods.
 */
public class CommitAfterDemo
{
    private User user;

    void onActivate(final User user)
    {
        this.user = user;
    }

    Object onPassivate()
    {
        return user;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(final User user)
    {
        this.user = user;
    }

    @CommitAfter
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void onChangeName()
    {
        user.setFirstName("Frank");
    }

    @CommitAfter
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void doChangeNameWithRuntimeException()
    {
        user.setFirstName("Bill");

        throw new RuntimeException("To avoid commit.");
    }

    void onChangeNameWithRuntimeException()
    {
        try
        {
            doChangeNameWithRuntimeException();
        }
        catch (final Exception ex)
        {
            // Ignore
        }
    }

    @CommitAfter
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void doChangeNameWithCheckedException() throws SQLException

    {
        user.setFirstName("Troy");

        throw new SQLException("Doesn't matter.");
    }

    void onChangeNameWithCheckedException()
    {
        try
        {
            doChangeNameWithCheckedException();
        }
        catch (final Exception ex)
        {
            // Ignore
        }
    }
}
