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

import javax.persistence.EntityManager;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.apache.tapestry5.jpa.test.entities.ThingOne;

public class TopLevelServiceImpl implements TopLevelService
{

    private final EntityManager em;
    private final NestedService nestedService;
    private final EntityTransactionManager entityTransactionManager;

    public TopLevelServiceImpl(EntityManager em, NestedService nestedService,
            EntityTransactionManager transactionalUnits)
    {
        this.em = em;
        this.nestedService = nestedService;
        this.entityTransactionManager = transactionalUnits;
    }

    @Override
    @CommitAfter
    public void createThingOneAndTwo(String nameOne, String nameTwo)
    {
        ThingOne thingOne = new ThingOne();
        thingOne.setName(nameOne);
        em.persist(thingOne);
        nestedService.createThingTwo(nameTwo);
    }

    @Override
    @CommitAfter
    public void createThingOneThenTwo(final String nameOne, final String nameTwo)
    {
        entityTransactionManager.invokeAfterCommit(null, new Invokable<Boolean>()
        {
            @Override
            public Boolean invoke()
            {
                nestedService.createThingTwo(nameTwo);
                return true;
            }
        });
        ThingOne thingOne = new ThingOne();
        thingOne.setName(nameOne);
        em.persist(thingOne);
    }

    @Override
    @CommitAfter
    public void createThingOneThenTwoWithNestedCommitAfter(final String nameOne,
            final String nameTwo)
    {
        entityTransactionManager.runInTransaction(null, new Runnable()
        {
            @Override
            public void run()
            {
                entityTransactionManager.invokeAfterCommit(null, new Invokable<Boolean>()
                {
                    @Override
                    public Boolean invoke()
                    {
                        nestedService.createThingTwo(nameTwo);
                        return true;
                    }
                });
                ThingOne thingOne = new ThingOne();
                thingOne.setName(nameOne);
                em.persist(thingOne);
            }
        });

    }
}
