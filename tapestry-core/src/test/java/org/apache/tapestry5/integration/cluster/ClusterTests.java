//  Copyright 2011 The Apache Software Foundation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.apache.tapestry5.integration.cluster;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import io.github.bonigarcia.wdm.FirefoxDriverManager;

import org.apache.tapestry5.test.JettyRunner;
import org.apache.tapestry5.test.TapestryTestConstants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.JDBCSessionIdManager;
import org.eclipse.jetty.server.session.JDBCSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * User: josh_canfield Date: 6/24/11
 */
public class ClusterTests
{
    private static final String FIREFOX_BROWSER_CMD = "*firefox";
    private static final int SERVER_A_PORT = 9091;
    private static final int SERVER_B_PORT = 9092;
    private static final String SERVER_A_NAME = "server_A";
    private static final String SERVER_B_NAME = "server_B";

    private static final String CREATE_1 = "//a[contains(text(),'create1')]";
    private static final String VALUE_1 = "value1";

    private static final String CREATE_2 = "//a[contains(text(),'create2')]";
    private static final String VALUE_2 = "value2";

    private static final String UPDATE_1 = "//a[contains(text(),'update1')]";
    private static final String VALUE_3 = "value3";

    private static final String UPDATE_2 = "//a[contains(text(),'update2')]";
    private static final String VALUE_4 = "value4";

    private static final String CLEAR = "//a[contains(text(),'Clear')]";

    JettyRunner serverA;

    JettyRunner serverB;

    Selenium selenium;

    @BeforeClass
    void setupServers(XmlTest xmlTest) throws Exception
    {
        createJettySessionsTable();

        serverA = configureClusteredJetty(SERVER_A_NAME, SERVER_A_PORT);
        serverB = configureClusteredJetty(SERVER_B_NAME, SERVER_B_PORT);

        FirefoxDriverManager.getInstance().setup();
        FirefoxDriver driver = new FirefoxDriver();

        selenium = new WebDriverBackedSelenium(driver, "http://localhost:9091/");

        selenium.start();
    }

    @AfterClass
    void stopServers()
    {
        serverA.stop();
        serverB.stop();
        selenium.stop();
    }

    @Test
    public void mutable_pojo_as_session_state_is_always_shared()
    {
        // Expect all object changes to be transferred to the other server.
        String[][] click = {
                {CREATE_1, VALUE_1, VALUE_1},
                {CLEAR, "", ""},
                {CREATE_2, VALUE_2, VALUE_2},
                {UPDATE_1, VALUE_3, VALUE_3},
                {UPDATE_2, VALUE_4, VALUE_4}
        };

        evaluate("PersistedMutablePojoDemo", click);
    }

    @Test
    public void immutable_session_persisted_object() throws InterruptedException
    {
        // expect only create links to transfer over (We've told tapestry it's immutable)
        String[][] data = {
                {CREATE_1, VALUE_1, VALUE_1},
                {UPDATE_1, VALUE_3, VALUE_1},
                {CLEAR, "", ""},
                {CREATE_2, VALUE_2, VALUE_2},
                {UPDATE_2, VALUE_4, VALUE_2},
                {UPDATE_1, VALUE_3, VALUE_2},
        };

        evaluate("ImmutableSessionPersistedObjectDemo", data);
    }

    @Test
    public void session_persisted_object_analyzer() throws InterruptedException
    {
        // special cased so that only UPDATE_2 marks as dirty, creates/deletes still transfer
        String[][] data = {
                {CREATE_1, VALUE_1, VALUE_1}, // created, session transferred
                {UPDATE_1, VALUE_3, VALUE_1}, // update-1 doesn't transfer
                {CLEAR, "", ""},
                {CREATE_2, VALUE_2, VALUE_2}, // create-2, session transferred
                {UPDATE_2, VALUE_4, VALUE_4}, // update-2, session transferred
                {UPDATE_1, VALUE_3, VALUE_4}, // update-1, doesn't transfer
        };

        evaluate("SessionPersistedObjectAnalyzerDemo", data);
    }

    private void evaluate(String page, String[][] expect)
    {
        for (String[] strings : expect)
        {
            openOnServerA(page);

            clickAndWait(strings[0]);

            assertText("value", strings[1]);

            openOnServerB(page);

            assertText("value", strings[2]);
        }

        clickAndWait(CLEAR);
        assertText("value", "");
    }

    private void openOnServerA(String page)
    {
        selenium.open("http://localhost:" + SERVER_A_PORT + "/" + page);
        assertServerName(SERVER_A_NAME);
    }

    private void openOnServerB(String page)
    {
        selenium.open("http://localhost:" + SERVER_B_PORT + "/" + page);
        assertServerName(SERVER_B_NAME);
    }

    private void assertServerName(String serverName)
    {
        assertTrue(selenium.isElementPresent("//h1[@id='serverName' and text()='" + serverName + "']"));
    }

    private void assertText(String locator, String expected)
    {
        assertEquals(expected, selenium.getText(locator));
    }

    private void clickAndWait(String s)
    {
        selenium.click(s);
        selenium.waitForPageToLoad("5000");
    }

    private JettyRunner configureClusteredJetty(String name, int port) throws Exception
    {
        JettyRunner runner = new JettyRunner();

        runner.configure("src/test/cluster", "", port, port + 100);

        JDBCSessionIdManager idMgr = new JDBCSessionIdManager(runner.getServer());
        idMgr.setWorkerName(name);
        idMgr.setDriverInfo("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:clustertest");

        Server server = runner.getServer();
        server.setSessionIdManager(idMgr);

        WebAppContext wac = (WebAppContext) server.getHandler();

        JDBCSessionManager jdbcMgr = new JDBCSessionManager();
        jdbcMgr.setIdManager(server.getSessionIdManager());

        // force the session to be read from the database with no delay
        // This is an incorrectly documented feature.
        jdbcMgr.setSaveInterval(0);

        wac.setSessionHandler(new SessionHandler(jdbcMgr));
        wac.getServletContext().setInitParameter("cluster.name", name);
        runner.start();
        return runner;
    }

    private void createJettySessionsTable() throws ClassNotFoundException, SQLException
    {
        Class.forName("org.hsqldb.jdbcDriver");

        Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:clustertest", "sa", "");
        String sql = "create table JettySessions (" +
                "rowId varchar(60), " +
                "sessionId varchar(60)," +
                "contextPath varchar(60)," +
                "virtualHost varchar(60)," +
                "lastNode varchar(60)," +
                "accessTime bigint," +
                "lastAccessTime bigint," +
                "createTime bigint," +
                "cookieTime bigint," +
                "lastSavedTime bigint," +
                "expiryTime bigint," +
                "map longvarbinary" +
                ");";
        Statement statement = c.createStatement();
        statement.execute(sql);
        statement.close();
    }
}
