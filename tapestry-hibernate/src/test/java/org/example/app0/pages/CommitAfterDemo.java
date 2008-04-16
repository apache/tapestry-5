package org.example.app0.pages;

import org.apache.tapestry.hibernate.annotations.CommitAfter;
import org.example.app0.entities.User;

import java.sql.SQLException;

/**
 * Demos the CommitAfter annotation on component methods.
 */
public class CommitAfterDemo
{
    private User _user;

    void onActivate(User user)
    {
        _user = user;
    }

    Object onPassivate()
    {
        return _user;
    }

    public User getUser()
    {
        return _user;
    }

    public void setUser(User user)
    {
        _user = user;
    }


    @CommitAfter
    void onChangeName()
    {
        _user.setFirstName("Frank");
    }

    @CommitAfter
    void doChangeNameWithRuntimeException()
    {
        _user.setFirstName("Bill");

        throw new RuntimeException("To avoid commit.");
    }

    void onChangeNameWithRuntimeException()
    {
        try
        {
            doChangeNameWithRuntimeException();
        }
        catch (Exception ex)
        {
            // Ignore
        }
    }

    @CommitAfter
    void doChangeNameWithCheckedException() throws SQLException

    {
        _user.setFirstName("Troy");

        throw new SQLException("Doesn't matter.");
    }

    void onChangeNameWithCheckedException()
    {
        try
        {
            doChangeNameWithCheckedException();
        }
        catch (Exception ex)
        {
            // Ignore
        }
    }
}
