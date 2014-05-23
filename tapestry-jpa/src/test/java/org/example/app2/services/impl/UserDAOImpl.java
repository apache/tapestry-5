package org.example.app2.services.impl;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app2.services.UserDAO;

import javax.persistence.EntityManager;

public class UserDAOImpl implements UserDAO
{
    @Inject
    private EntityManager entityManager;

    @Override
    public void persist(Object entity)
    {
        entityManager.persist(entity);
    }
}
