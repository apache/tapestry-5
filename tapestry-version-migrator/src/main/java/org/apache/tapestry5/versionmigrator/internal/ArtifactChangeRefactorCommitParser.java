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
 * Parses lines like this, in which just the artifact is changed:
 * <code>{tapestry-ioc =&gt; tapestry5-annotations}/src/main/java/org/apache/tapestry5/ioc/annotations/Advise.java (100%)</code>. 
 */
public class ArtifactChangeRefactorCommitParser implements FileRefactorCommitParser {

    final public static String EXAMPLE = "{tapestry-ioc => tapestry5-annotations}/src/main/java/org/apache/tapestry5/ioc/annotations/Advise.java";
    
    final private static Pattern PATTERN = 
            Pattern.compile("\\{(.*)\\s=>\\s([^}]*)}\\/([^\\.]*).*");
    
    @Override
    public Optional<ClassRefactor> apply(String line) {
        final Matcher matcher = PATTERN.matcher(line);
        ClassRefactor move = null;
        if (matcher.matches()) 
        {
//            System.out.printf("1(%s) 2(%s) 3(%s)\n", otherMatcher.group(1), otherMatcher.group(2), otherMatcher.group(3));
            final String className = FileRefactorCommitParser.extractPackageOrClassName(matcher.group(3));
            final String sourceArtifactName = matcher.group(1);
            final String destinationArtifactName = matcher.group(2);
            
            move  = new ClassRefactor(className, className, sourceArtifactName, destinationArtifactName);
        }
        return Optional.ofNullable(move);
    }

}
