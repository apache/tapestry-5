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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
public class VersionedThing
{
    @Id
    // @GeneratedValue(strategy = GenerationType.AUTO)
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @Column(name = "id", insertable = true, updatable = true, unique = true, nullable = false)
    private Integer id;

    @Version
    private Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastTouched;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public int getVersion()
    {
        return version;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VersionedThing thing = (VersionedThing) o;

        return getId() != null ? getId().equals(thing.getId()) : thing.getId() == null;

    }

    @Override
    public int hashCode()
    {
        return (getId() != null ? getId().hashCode() : 0);
    }

    public Date getLastTouched()
    {
        return lastTouched;
    }

    public void setLastTouched(Date lastTouched)
    {
        this.lastTouched = lastTouched;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

}
