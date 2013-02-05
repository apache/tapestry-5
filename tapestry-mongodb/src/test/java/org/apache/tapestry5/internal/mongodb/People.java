package org.apache.tapestry5.internal.mongodb;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 *
 */
public class People
{
    private ObjectId _id;

    private String name;
    private String surname;
    private Date birthDate;


    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}