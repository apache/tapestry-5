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

package org.apache.tapestry5.jpa.test;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.apache.tapestry5.jpa.test.entities.VersionedThing;

public class CommitCounter
{

    @Inject
    private EntityManager entityManager;

    @Inject
    private EntityTransactionManager transactionManager;

    @PostPersist
    @PostUpdate
    private void updateVersion(Object entity)
    {
        transactionManager.runInTransaction(null, new Runnable()
        {
            @Override
            public void run()
            {
                VersionedThing versionedThing = entityManager.find(VersionedThing.class, 1);
                if (versionedThing == null)
                    versionedThing = new VersionedThing();
                versionedThing.setId(1);
                versionedThing.setLastTouched(new Date());

                entityManager.merge(versionedThing);
            }
        });
    }

}
