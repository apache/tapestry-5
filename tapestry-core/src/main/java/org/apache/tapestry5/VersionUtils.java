// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility methods related to managing framework version numbers.
 */
public class VersionUtils
{

    /**
     * Reads a version number from a properties file on the classpath.  These files are generally created by Maven.  For
     * example, tapestry-core's properties file is <code>META-INF/maven/org.apache.tapestry/tapestry-core/pom.properties</code>.
     * The Maven generated properties files include the artifact id and group id as well as the version.
     * <p/>
     * The resource is located using the Thread's context class loader.
     *
     * @param resourcePath the complete path to the resource, including a leading slash.
     * @return the version number read from the properties file, or "UNKNOWN" if the version number is not present or
     *         the file can not be opened
     */
    public static String readVersionNumber(String resourcePath)
    {
        String result = "UNKNOWN";

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                resourcePath);


        if (stream != null)
        {
            Properties properties = new Properties();


            try
            {
                stream = new BufferedInputStream(stream);

                properties.load(stream);
            }
            catch (IOException ex)
            {
                // Just ignore it.
            }

            String version = properties.getProperty("version");

            // Since the file, if it exists, is created by Maven and will have the key, I can't see
            // how version would EVER be null, unless there's a problem reading the properties.

            if (version != null) result = version;
        }

        return result;
    }
}
