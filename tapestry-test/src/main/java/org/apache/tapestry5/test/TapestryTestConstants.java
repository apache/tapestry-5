// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.test;

import java.io.File;

public class TapestryTestConstants
{
    /**
     * The current working directory (i.e., property "user.dir").
     */
    public static final String CURRENT_DIR_PATH = System.getProperty("user.dir");
    /**
     * The Surefire plugin sets basedir but DOES NOT change the current working directory. When building across modules,
     * basedir changes for each module, but user.dir does not. This value should be used when referecing local files.
     * Outside of surefire, the "basedir" property will not be set, and the current working directory will be the
     * default.
     */
    public static final String MODULE_BASE_DIR_PATH = System.getProperty("basedir", CURRENT_DIR_PATH);

    /**
     * {@link #MODULE_BASE_DIR_PATH} as a file.
     */
    public static final File MODULE_BASE_DIR = new File(MODULE_BASE_DIR_PATH);
}
