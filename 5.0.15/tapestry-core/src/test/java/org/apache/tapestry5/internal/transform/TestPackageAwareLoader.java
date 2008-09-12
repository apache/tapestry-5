// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import javassist.ClassPool;
import javassist.Loader;

public class TestPackageAwareLoader extends Loader
{
    public TestPackageAwareLoader(ClassLoader parent, ClassPool cp)
    {
        super(parent, cp);
    }

    @Override
    public Class findClass(String className) throws ClassNotFoundException
    {
        int lastdotx = className.lastIndexOf('.');
        String packageName = className.substring(0, lastdotx);

        if (packageName.startsWith("org.apache.tapestry5.internal.transform.")) return super.findClass(className);

        // Returning null forces delegation to the parent class loader.

        return null;
    }
}
