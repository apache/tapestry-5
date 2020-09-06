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
package org.apache.tapestry5.versionmigrator;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Interface that defines a file refactor parser.
 */
public interface FileRefactorCommitParser extends Function<String, Optional<ClassRefactor>> 
{
    /**
     * Extracts a package or class name from a string which may contain <code>src/main/java/</code>
     * or <code>src/test/java/</code>
     */
    static String extractPackageOrClassName(String string) 
    {
        return string
                .replace("src/main/java/", "")
                .replace("src/test/java/", "")
                .replace("/", ".");
    }
    
    /**
     * Builds a class name given some parts.
     */
    static String buildClassName(final String rootPackageName, String packageNameSuffix, final String className) 
    {
        return (rootPackageName + packageNameSuffix.replace("/", ".") + "." + className.replace("/", "."))
                .replaceAll(Pattern.quote(".."), ".");
    }

    
}
