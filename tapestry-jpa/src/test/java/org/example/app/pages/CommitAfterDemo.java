package org.example.app.pages;

import java.sql.SQLException;

import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.jpa.CommitAfter;
import org.example.app.AppConstants;
import org.example.app.entities.User;

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
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void onChangeName()
    {
        user.setFirstName("Frank");
    }

    @CommitAfter
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
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
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
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
