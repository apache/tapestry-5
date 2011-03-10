package org.example.app.services;

import java.util.List;

import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.jpa.CommitAfter;
import org.example.app.AppConstants;
import org.example.app.entities.User;

public interface UserDAO
{
    @CommitAfter
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void add(User user);

    List<User> findAll();

    @CommitAfter
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void delete(User... users);

    void deleteAll();
}
