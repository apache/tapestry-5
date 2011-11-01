// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.reload;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.plastic.asm.ClassWriter;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;
import org.apache.tapestry5.internal.services.ClassCreationHelper;
import org.apache.tapestry5.test.TapestryTestConstants;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.io.*;

import static org.apache.tapestry5.internal.plastic.asm.Opcodes.*;

/**
 * Integration tests designed to test Tapestry's ability to dynamically reload component classes,
 * templates and message catalogs.
 */
public class ReloadTests extends TapestryCoreTestCase
{
    private File webappDir;
    private File webinfDir;
    private File classesDir;
    private File pagesDir;
    private ClassCreationHelper helper;

    private static final String PACKAGE = "org.apache.tapestry5.integration.reload.pages";

    @BeforeTest(groups = {"beforeStartup"})
    public void beforeStartup(XmlTest xmlTest) throws Exception
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

        helper = new ClassCreationHelper(classesDir.getAbsolutePath());

        createIndexClass(100);

        // overwrite the web-app-folder parameter
        xmlTest.addParameter(TapestryTestConstants.WEB_APP_FOLDER_PARAMETER, webappDir.getAbsolutePath());
    }

    private void createIndexClass(int number) throws Exception
    {
        String className = PACKAGE + ".Index";

        ClassWriter cw = helper.createWriter(className, "java.lang.Object");

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getNumber", "()I", null, null);
        mv.visitCode();
        mv.visitLdcInsn(number);
        mv.visitInsn(IRETURN);
        cw.visitEnd();

        cw.visitEnd();

        helper.writeFile(cw, className);
    }

    /**
     * Copies a source file (from the classpath) to a directory as a new file name.
     *
     * @param sourceFile source file (within in the reload package)
     * @param dir        directory to copy to
     * @param targetFile name of file to be created or overwritten
     */
    private void copy(String sourceFile, File dir, String targetFile) throws IOException
    {
        File output = new File(dir, targetFile);

        FileOutputStream fos = new FileOutputStream(output);

        InputStream in = getClass().getResourceAsStream(sourceFile);

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
        openBaseURL();

        assertText("property", "100");

        createIndexClass(200);

        openBaseURL();

        assertText("property", "200");
    }

    @Test
    public void reload_template() throws Exception
    {
        openBaseURL();

        assertText("template", "Initial Template Version");

        copy("Index.2.tml", webappDir, "Index.tml");

        openBaseURL();

        assertText("template", "Updated Template Version");
    }

    @Test
    public void reload_message_catalog() throws Exception
    {
        openBaseURL();

        assertText("message", "Initial Message");

        copy("Index.2.properties", pagesDir, "Index.properties");

        openBaseURL();

        assertText("message", "Updated Message");
    }
}
