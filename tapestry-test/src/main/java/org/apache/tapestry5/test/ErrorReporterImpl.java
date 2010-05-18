// Copyright 2009, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.testng.ITestContext;

import com.thoughtworks.selenium.CommandProcessor;

public class ErrorReporterImpl implements ErrorReporter
{
    private final CommandProcessor commandProcessor;

    private final ITestContext testContext;

    private int uid = 0;

    private final Set<String> previousNames = new HashSet<String>();

    public ErrorReporterImpl(CommandProcessor commandProcessor, ITestContext testContext)
    {
        this.commandProcessor = commandProcessor;
        this.testContext = testContext;
    }

    public void writeErrorReport()
    {
        String htmlSource = commandProcessor.getString("getHtmlSource", new String[]
        {});

        File dir = new File(testContext.getOutputDirectory());

        dir.mkdirs();

        Method testMethod = (Method) testContext.getAttribute(TapestryTestConstants.CURRENT_TEST_METHOD_ATTRIBUTE);

        String baseFileName = testMethod == null ? "Unknown-test" : testMethod.getDeclaringClass().getSimpleName()
                + "." + testMethod.getName();

        if (previousNames.contains(baseFileName))
        {
            baseFileName += "-" + uid++;
        }
        else
        {
            previousNames.add(baseFileName);
        }

        File report = new File(dir, baseFileName + "-page-content.html");

        System.err.println("Writing current page's HTML source to: " + report);

        try
        {
            FileWriter fw = new FileWriter(report);

            fw.write(htmlSource);

            fw.close();
        }
        catch (IOException ex)
        {
            // Ignore.
        }
    }

}
