// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.internal.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.tapestry5.services.ComponentLibraryInfo;
import org.apache.tapestry5.services.ComponentLibraryInfoSource;
import org.apache.tapestry5.services.LibraryMapping;
import org.slf4j.Logger;

/**
 * {@link ComponentLibraryInfoSource} implementation based on the pom.xml and pom.properties files 
 * Maven places in the /META-INF/maven/[groupId]/[artifactId] folder.
 */
public class MavenComponentLibraryInfoSource implements ComponentLibraryInfoSource
{
    
    final private Logger logger;

    public MavenComponentLibraryInfoSource(Logger logger)
    {
        super();
        this.logger = logger;
    }

    @Override
    public ComponentLibraryInfo find(LibraryMapping libraryMapping)
    {
        
//        final File root = getRoot(libraryMapping);
//        
//        System.out.println(root);
        
        return null;
    }

//    private File getRoot(LibraryMapping libraryMapping)
//    {
//        final String rootPackageConverted = libraryMapping.getRootPackage().replace('.', '/');
//        final URL rootPackageUrl = getClass().getClassLoader().getResource(rootPackageConverted);
//        final String rootPath = "jar:" + rootPackageUrl.getPath().replace(rootPackageConverted, "") + "META-INF/maven/";
//        final URL rootUrl = getClass().getClassLoader().getResource(rootPath);
//        return root;
//    }
    
    private static InputStream open(String path)
    {
        return MavenComponentLibraryInfoSource.class.getClassLoader().getResourceAsStream(path);
    }

}
