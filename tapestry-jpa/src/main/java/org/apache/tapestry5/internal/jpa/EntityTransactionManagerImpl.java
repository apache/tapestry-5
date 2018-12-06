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
package org.apache.tapestry5.internal.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.slf4j.Logger;

@Scope(ScopeConstants.PERTHREAD)
public class EntityTransactionManagerImpl implements EntityTransactionManager
{

    private final Logger logger;
    private final EntityManagerManager entityManagerManager;

    private final Map<String, PersistenceContextSpecificEntityTransactionManager> transactionManagerMap;

    public EntityTransactionManagerImpl(Logger logger, EntityManagerManager entityManagerManager)
    {
        this.logger = logger;
        this.entityManagerManager = entityManagerManager;
        transactionManagerMap = new HashMap<>(entityManagerManager.getEntityManagers().size());
    }

    private EntityManager getEntityManager(String unitName)
    {
        // EntityManager em = JpaInternalUtils.getEntityManager(entityManagerManager, unitName);
        // FIXME we should simply incorporate the logic in JpaInternalUtils.getEntityManager to
        // EntityManagerManager.getEntityManager(unitName)
        if (unitName != null)
            return entityManagerManager.getEntityManager(unitName);
        else
        {
            Map<String, EntityManager> entityManagers = entityManagerManager.getEntityManagers();
            if (entityManagers.size() == 1)
                return entityManagers.values().iterator().next();
            else
                throw new RuntimeException(
                        "Unable to locate a single EntityManager. "
                                + "You must provide the persistence unit name as defined in the persistence.xml using the @PersistenceContext annotation.");
        }
    }

    /*
     * (non-Javadoc)
     * @see net.satago.tapestry5.jpa.EntityTransactionManager#runInTransaction(java.lang.String,
     * java.lang.Runnable)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void runInTransaction(final String unitName, final Runnable runnable)
    {
        getPersistenceContextSpecificEntityTransactionManager(unitName).invokeInTransaction(
                new VoidInvokable(runnable));
    }

    /*
     * (non-Javadoc)
     * @see net.satago.tapestry5.jpa.EntityTransactionManager#invokeInTransaction(java.lang.String,
     * org.apache.tapestry5.ioc.Invokable)
     */
    @Override
    public <T> T invokeInTransaction(String unitName, Invokable<T> invokable)
    {
        return getPersistenceContextSpecificEntityTransactionManager(unitName).invokeInTransaction(
                invokable);
    }

    private PersistenceContextSpecificEntityTransactionManager getPersistenceContextSpecificEntityTransactionManager(
            String unitName)
    {
        if (!transactionManagerMap.containsKey(unitName))
        {
            PersistenceContextSpecificEntityTransactionManager transactionManager = new PersistenceContextSpecificEntityTransactionManager(
                    logger, getEntityManager(unitName));
            transactionManagerMap.put(unitName, transactionManager);
            return transactionManager;
        }
        else
            return transactionManagerMap.get(unitName);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.satago.tapestry5.jpa.EntityTransactionManager#invokeBeforeCommit(org.apache.tapestry5
     * .ioc.Invokable, java.lang.String)
     */
    @Override
    public void invokeBeforeCommit(String unitName, Invokable<Boolean> invokable)
    {
        getPersistenceContextSpecificEntityTransactionManager(unitName).addBeforeCommitInvokable(
                invokable);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.satago.tapestry5.jpa.EntityTransactionManager#invokeAfterCommit(org.apache.tapestry5.
     * ioc.Invokable, java.lang.String)
     */
    @Override
    public void invokeAfterCommit(String unitName, Invokable<Boolean> invokable)
    {
        getPersistenceContextSpecificEntityTransactionManager(unitName).addAfterCommitInvokable(
                invokable);

    }
}
