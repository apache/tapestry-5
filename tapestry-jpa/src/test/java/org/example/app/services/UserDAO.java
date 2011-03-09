package org.example.app.services;

import java.util.List;

import org.example.app.entities.User;

public interface UserDAO
{
    void add(User user);

    List<User> findAll();

    void delete(User... users);

    void deleteAll();
}
