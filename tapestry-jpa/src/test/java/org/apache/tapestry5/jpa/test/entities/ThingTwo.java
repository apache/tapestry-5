/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tapestry5.jpa.test.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.tapestry5.jpa.test.CommitCounter;

@Entity
@EntityListeners(CommitCounter.class)
public class ThingTwo
{
    private Integer id;

    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId()
    {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * @return Returns the name.
     */
    @Column(nullable = false)
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ThingTwo thing = (ThingTwo) o;

        return getId() != null ? getId().equals(thing.getId()) : thing.getId() == null;

    }

    @Override
    public int hashCode()
    {
        return (getId() != null ? getId().hashCode() : 0);
    }

    public String toString()
    {
        return getName();
    }

}
