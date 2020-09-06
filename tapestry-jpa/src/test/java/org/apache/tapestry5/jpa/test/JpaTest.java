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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.tapestry5.http.services.ApplicationGlobals;
import org.apache.tapestry5.internal.jpa.JpaInternalUtils;
import org.apache.tapestry5.internal.jpa.PersistedEntity;
import org.apache.tapestry5.internal.test.PageTesterContext;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.modules.JpaModule;
import org.apache.tapestry5.jpa.test.entities.ThingOne;
import org.apache.tapestry5.jpa.test.entities.ThingTwo;
import org.apache.tapestry5.jpa.test.entities.VersionedThing;
import org.apache.tapestry5.modules.TapestryModule;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JpaTest
{

    private static Registry registry;
    private EntityManagerManager entityManagerManager;
    private TopLevelService topLevelService;

    // @BeforeSuite
    public final void setupRegistry()
    {
        RegistryBuilder builder = new RegistryBuilder();
        builder.add(TapestryModule.class);
        builder.add(JpaModule.class);
        builder.add(JpaTestModule.class);

        registry = builder.build();
        // set PageTesterContext, otherwise T5 tries to load classpath assets
        ApplicationGlobals globals = registry.getObject(ApplicationGlobals.class, null);
        globals.storeContext(new PageTesterContext(""));
        registry.performRegistryStartup();

        entityManagerManager = registry.getService(EntityManagerManager.class);
        topLevelService = registry.getService(TopLevelService.class);

    }

    private EntityManager getEntityManager()
    {
        return entityManagerManager.getEntityManagers().values().iterator().next();
    }

    // @AfterSuite
    public final void shutdownRegistry()
    {
        registry.cleanupThread();
        registry.shutdown();
        registry = null;
    }

    @BeforeMethod
    public final void beginTransaction()
    {
        setupRegistry();
        EntityTransaction tx = getEntityManager().getTransaction();
        if (!tx.isActive())
            tx.begin();
    }

    @AfterMethod
    public void rollbackLastTransactionAndClean() throws SQLException
    {
        EntityTransaction transaction = getEntityManager().getTransaction();
        if (transaction.isActive())
            transaction.rollback();
        clearDatabase();
        getEntityManager().clear();
        shutdownRegistry();
    }

    // based on http://www.objectpartners.com/2010/11/09/unit-testing-your-persistence-tier-code/
    private void clearDatabase() throws SQLException
    {
        EntityManager em = getEntityManager();
        em.clear();
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive())
            transaction.begin();
        Connection c = em.unwrap(Connection.class);
        Statement s = c.createStatement();
        s.execute("SET REFERENTIAL_INTEGRITY FALSE");
        Set<String> tables = new HashSet<String>();
        ResultSet rs = s.executeQuery("select table_name " + "from INFORMATION_SCHEMA.tables "
                + "where table_type='TABLE' and table_schema='PUBLIC'");
        while (rs.next())
        {
            // if we don't skip over the sequence table, we'll start getting
            // "The sequence table information is not complete"
            // exceptions
            if (!rs.getString(1).startsWith("DUAL_") && !rs.getString(1).equals("SEQUENCE"))
            {
                tables.add(rs.getString(1));
            }
        }
        rs.close();
        for (String table : tables)
        {
            s.executeUpdate("DELETE FROM " + table);
        }
        transaction.commit();
        s.execute("SET REFERENTIAL_INTEGRITY TRUE");
        s.close();
    }

    private <T> List<T> getInstances(final Class<T> type)
    {
        EntityManager em = getEntityManager();
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = qb.createQuery(type);
        query.select(query.from(type));
        return em.createQuery(query).getResultList();
    }

    @Test
    public void commitBothInNestedTransaction()
    {
        topLevelService.createThingOneAndTwo("one", "two");
        assertEquals(1, getInstances(ThingOne.class).size());
        assertEquals(1, getInstances(ThingTwo.class).size());
        assertTrue(getEntityManager().find(VersionedThing.class, 1).getVersion() > 0);
    }

    @Test(expectedExceptions = RollbackException.class)
    public void rollbackNestedFails()
    {
        topLevelService.createThingOneAndTwo("one", null);
    }

    @Test(expectedExceptions = RollbackException.class)
    public void rollbackTopFails()
    {
        topLevelService.createThingOneAndTwo(null, "two");
    }

    @Test
    public void sequentialCommitUsingInvokeAfterCommit()
    {
        topLevelService.createThingOneThenTwo("one", "two");
        assertEquals(1, getInstances(ThingOne.class).size());
        assertEquals(1, getInstances(ThingTwo.class).size());
        assertTrue(getEntityManager().find(VersionedThing.class, 1).getVersion() > 1);
    }

    @Test
    public void sequentialCommitUsingInvokeAfterCommitAndCommitAfterAnnotation()
    {
        topLevelService.createThingOneThenTwoWithNestedCommitAfter("one", "two");
        assertEquals(1, getInstances(ThingOne.class).size());
        assertEquals(1, getInstances(ThingTwo.class).size());
        assertTrue(getEntityManager().find(VersionedThing.class, 1).getVersion() > 1);
    }

    @Test
    public void sequentialRollbackAndAbortUsingInvokeAfterCommit()
    {
        try
        {
            topLevelService.createThingOneThenTwo(null, "two");
            Assert.fail();
        }
        catch (RollbackException e)
        {
        }
        assertEquals(0, getInstances(ThingOne.class).size());
        assertEquals(0, getInstances(ThingTwo.class).size());
    }

    @Test
    public void trySomething()
    {
        ThingOne thingOne = new ThingOne();
        thingOne.setId(1);
        PersistedEntity entity = JpaInternalUtils.convertApplicationValueToPersisted(
                entityManagerManager, thingOne);
    }

}
