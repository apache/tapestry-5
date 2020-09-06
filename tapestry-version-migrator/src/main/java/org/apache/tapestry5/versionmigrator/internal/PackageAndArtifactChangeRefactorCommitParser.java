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
 * Parses lines like this, in which both artifact and package are changed:
 * <code>{tapestry-ioc/src/main/java/org/apache/tapestry5/ioc =&gt; commons/src/main/java/org/apache/tapestry5/commons}/ObjectProvider.java</code>. 
 */
public class PackageAndArtifactChangeRefactorCommitParser implements FileRefactorCommitParser {

    final public static String EXAMPLE = "{tapestry-ioc/src/main/java/org/apache/tapestry5/ioc => commons/src/main/java/org/apache/tapestry5/commons}/ObjectProvider.java";
    
    final private static Pattern PATTERN = 
            Pattern.compile("\\{([^\\/]+)\\/([^\\s]+)\\s=>\\s([^\\/]+)\\/([^}]+)}([^\\.]+).*");
    
    @Override
    public Optional<ClassRefactor> apply(String line) 
    {        
        ClassRefactor move = null;
          final Matcher matcher = PATTERN.matcher(line);
          if (matcher.matches()) 
          {
//              System.out.printf("1(%s) 2(%s) 3(%s) 4(%s) 5(%s)\n", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
              String newPackageNameSuffix = FileRefactorCommitParser.extractPackageOrClassName(matcher.group(4));
              String oldPackageNameSuffix = FileRefactorCommitParser.extractPackageOrClassName(matcher.group(2));
              final String className = matcher.group(5);
              final String newClassName = FileRefactorCommitParser.buildClassName("", newPackageNameSuffix, className);
              final String oldClassName = FileRefactorCommitParser.buildClassName("", oldPackageNameSuffix, className);
              final String sourceArtifactName = matcher.group(1);
              final String destinationArtifactName = matcher.group(3);
              
              move  = new ClassRefactor(newClassName, oldClassName, sourceArtifactName, destinationArtifactName);
          }
        return Optional.ofNullable(move);

    }

}
