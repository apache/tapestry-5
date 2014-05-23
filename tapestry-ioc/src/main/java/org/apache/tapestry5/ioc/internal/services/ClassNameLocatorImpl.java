// Copyright 2007, 2008, 2010, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.services.ClasspathMatcher;
import org.apache.tapestry5.ioc.services.ClasspathScanner;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

public class ClassNameLocatorImpl implements ClassNameLocator
{
    private final ClasspathScanner scanner;

    // This matches normal class files but not inner class files (which contain a '$'.

    private final Pattern CLASS_NAME_PATTERN = Pattern.compile("^\\p{javaJavaIdentifierStart}[\\p{javaJavaIdentifierPart}&&[^\\$]]*\\.class$", Pattern.CASE_INSENSITIVE);

    /**
     * Matches paths that are classes, but not for inner classes, or the package-info.class psuedo-class (used for package-level annotations).
     */
    private final ClasspathMatcher CLASS_NAME_MATCHER = new ClasspathMatcher()
    {
        @Override
        public boolean matches(String packagePath, String fileName)
        {
            if (!CLASS_NAME_PATTERN.matcher(fileName).matches())
            {
                return false;
            }

            // Filter out inner classes.

            if (fileName.contains("$") || fileName.equals("package-info.class"))
            {
                return false;
            }

            return true;
        }
    };

    /**
     * Maps a path name ("foo/bar/Baz.class") to a class name ("foo.bar.Baz").
     */
    private final Mapper<String, String> CLASS_NAME_MAPPER = new Mapper<String, String>()
    {
        @Override
        public String map(String element)
        {
            return element.substring(0, element.length() - 6).replace('/', '.');
        }
    };


    public ClassNameLocatorImpl(ClasspathScanner scanner)
    {
        this.scanner = scanner;
    }

    /**
     * Synchronization should not be necessary, but perhaps the underlying ClassLoader's are sensitive to threading.
     */
    @Override
    public synchronized Collection<String> locateClassNames(String packageName)
    {
        String packagePath = packageName.replace('.', '/') + "/";

        try
        {
            Collection<String> matches = scanner.scan(packagePath, CLASS_NAME_MATCHER);

            return F.flow(matches).map(CLASS_NAME_MAPPER).toSet();

        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }


}
