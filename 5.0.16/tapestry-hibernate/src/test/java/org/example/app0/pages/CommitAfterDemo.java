package org.example.app0.pages;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.example.app0.entities.User;

import java.sql.SQLException;

/**
 * Demos the CommitAfter annotation on component methods.
 */
public class CommitAfterDemo
{
    private User user;

    void onActivate(User user)
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

    public void setUser(User user)
    {
        this.user = user;
    }


    @CommitAfter
    void onChangeName()
    {
        user.setFirstName("Frank");
    }

    @CommitAfter
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
        catch (Exception ex)
        {
            // Ignore
        }
    }

    @CommitAfter
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
        catch (Exception ex)
        {
            // Ignore
        }
    }
}
