package org.example.app.services.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app.entities.User;
import org.example.app.services.UserDAO;

public class UserDAOImpl implements UserDAO
{
    @Inject
    @PersistenceUnit(unitName = "TestUnit")
    private EntityManager entityManager;

    public void add(final User user)
    {
        entityManager.persist(user);
    }

    @SuppressWarnings(
    { "unchecked" })
    public List<User> findAll()
    {
        return entityManager.createQuery("select u from User u order by u.id desc").getResultList();
    }

    public void delete(final User... users)
    {
        for (final User user : users)
            entityManager.remove(user);
    }

    public void deleteAll()
    {
        for (final User u : findAll())
        {
            entityManager.remove(u);
        }
    }
}
