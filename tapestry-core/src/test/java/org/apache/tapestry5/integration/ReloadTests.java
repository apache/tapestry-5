// Copyright 2008, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration;

import javassist.*;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Integration tests designed to test Tapestry's ability to dynamically reload component classes, templates and message
 * catalogs.
 */
public class ReloadTests extends AbstractIntegrationTestSuite
{
    private final File webappDir;
    private final File webinfDir;
    private final File classesDir;
    private final File pagesDir;

    private static final String PACKAGE = "org.apache.tapestry5.integration.reload.pages";

    public ReloadTests() throws Exception
    {
        String uid = Long.toHexString(System.currentTimeMillis());

        webappDir = new File(System.getProperty("java.io.tmpdir"), uid);

        webinfDir = new File(webappDir, "WEB-INF");

        classesDir = new File(webinfDir, "classes");

        pagesDir = new File(classesDir, PACKAGE.replace('.', '/'));

        pagesDir.mkdirs();

        copy("web.xml", webinfDir, "web.xml");
        copy("Index.1.tml", webappDir, "Index.tml");
        copy("Index.1.properties", pagesDir, "Index.properties");

        createIndexClass(100);

        setWebappRoot(webappDir.getAbsolutePath());

        System.err.println("Created: " + webappDir);
    }

    private void createIndexClass(int number) throws NotFoundException, CannotCompileException, IOException
    {
        ClassPool pool = new ClassPool(null);

        pool.appendSystemPath();

        CtClass ctClass = pool.makeClass(PACKAGE + ".Index");

        CtMethod method = new CtMethod(pool.get("int"), "getNumber", null, ctClass);

        method.setBody("return " + number + ";");

        ctClass.addMethod(method);

        ctClass.writeFile(classesDir.getAbsolutePath());
    }


    /**
     * Copies a source file (from the classpath) to a directory as a new file name.
     *
     * @param sourceFile source file (within in the reload package)
     * @param dir        directory to copy to
     * @param targetFile name of file   to be created or overwritten
     */
    private void copy(String sourceFile, File dir, String targetFile) throws IOException
    {
        File output = new File(dir, targetFile);

        FileOutputStream fos = new FileOutputStream(output);

        InputStream in = getClass().getResourceAsStream("reload/" + sourceFile);

        copy(in, fos);

        in.close();
        fos.close();
    }

    private void copy(InputStream in, FileOutputStream fos) throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(in);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        TapestryInternalUtils.copy(bis, bos);
    }

    @Test
    public void reload_class() throws Exception
    {
        open(BASE_URL);

        assertText("property", "100");

        createIndexClass(200);

        open(BASE_URL);

        assertText("property", "200");
    }

    @Test
    public void reload_template() throws Exception
    {
        open(BASE_URL);

        assertText("template", "Initial Template Version");

        copy("Index.2.tml", webappDir, "Index.tml");

        open(BASE_URL);

        assertText("template", "Updated Template Version");
    }

    @Test
    public void reload_message_catalog() throws Exception
    {
        open(BASE_URL);

        assertText("message", "Initial Message");

        copy("Index.2.properties", pagesDir, "Index.properties");

        open(BASE_URL);

        assertText("message", "Updated Message");
    }
}
