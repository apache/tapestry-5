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

import org.apache.tapestry5.versionmigrator.ClassRefactor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for {@link PackageAndArtifactChangeRefactorCommitParser}.
 */
@Test(groups = "unit")
public class PackageAndArtifactChangeRefactorCommitParserTest 
{
    @Test
    public void valid_line() 
    {
        // {tapestry-ioc/src/main/java/org/apache/tapestry5/ioc => commons/src/main/java/org/apache/tapestry5/commons}/ObjectProvider.java
        PackageAndArtifactChangeRefactorCommitParser parser = new PackageAndArtifactChangeRefactorCommitParser();
        Optional<ClassRefactor> optionalRefactor = parser.apply(PackageAndArtifactChangeRefactorCommitParser.EXAMPLE);
        Assert.assertTrue(optionalRefactor.isPresent(), "Line not detected as a change of package and artifact.");
        ClassRefactor refactor = optionalRefactor.get();
        Assert.assertEquals(refactor.getNewClassName(), "org.apache.tapestry5.commons.ObjectProvider");
        Assert.assertEquals(refactor.getOldClassName(), "org.apache.tapestry5.ioc.ObjectProvider");
        Assert.assertEquals(refactor.getDestinationArtifact(), "commons");
        Assert.assertEquals(refactor.getSourceArtifact(), "tapestry-ioc");
    }

}
