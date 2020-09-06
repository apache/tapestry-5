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

package org.apache.tapestry5.versionmigrator.internal;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.versionmigrator.ClassRefactor;
import org.apache.tapestry5.versionmigrator.FileRefactorCommitParser;

/**
 * Parses lines like this, in which just the package is changed:
 * <code>commons/src/main/java/org/apache/tapestry5/{ioc =&gt; commons}/Messages.java (98%)</code>. 
 */
public class PackageChangeRefactorCommitParser implements FileRefactorCommitParser {

    final public static String EXAMPLE = "commons/src/main/java/org/apache/tapestry5/{ioc => commons}/Messages.java (98%)";
    
    final private static Pattern PATTERN = 
            Pattern.compile("([^/]*)/(.*)" + Pattern.quote("{") + "(.*)\\s=>\\s(.*)" + Pattern.quote("}/") + "([^\\.]*).*");
    
    @Override
    public Optional<ClassRefactor> apply(String line) 
    {
        final Matcher matcher = PATTERN.matcher(line);
        ClassRefactor move = null;
        if (matcher.matches()) 
        {
            String newPackageNameSuffix = matcher.group(4);
            String oldPackageNameSuffix = matcher.group(3);
//                    System.out.printf("1(%s) 2(%s) 3(%s) 4(%s) 5(%s)\n", matcher.group(1), matcher.group(2), oldPackageNameSuffix, newPackageNameSuffix, matcher.group(5));
            
            final String rootPackageName = matcher.group(2)
                    .replace("src/main/java/", "")
                    .replace("/", ".");
            final String className = matcher.group(5);
            final String newClassName = FileRefactorCommitParser.buildClassName(rootPackageName, newPackageNameSuffix, className);
            final String oldClassName = FileRefactorCommitParser.buildClassName(rootPackageName, oldPackageNameSuffix, className);
            final String artifactName = matcher.group(1);
            
            move = new ClassRefactor(newClassName, oldClassName, artifactName, artifactName);
        }
        return Optional.ofNullable(move);
    }

}
