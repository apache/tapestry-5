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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for {@link ClassRefactor}.
 */
@Test(groups = "unit")
public class ClassRefactorTest 
{
    final private static String VALID = "valid";
    final private static String SIMPLE_CLASS_NAME = "Something";
    final private static String PACKAGE_NAME = "org.apache.tapestry5";
    final private static String CLASS_NAME = PACKAGE_NAME + "." + SIMPLE_CLASS_NAME;
    final private static String ARTIFACT_NAME = "tapestry-subproject";
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_first_parameter_check() 
    {
        new ClassRefactor(null, VALID, VALID, VALID);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_second_parameter_check() 
    {
        new ClassRefactor(VALID, null, VALID, VALID);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_third_parameter_check() 
    {
        new ClassRefactor(VALID, VALID, null, VALID);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_fourth_parameter_check() 
    {
        new ClassRefactor(VALID, VALID, VALID, null);
    }
    
    @Test
    public void is_moved_between_artifacts() 
    {
        
        Assert.assertTrue(
                new ClassRefactor(
                        CLASS_NAME, CLASS_NAME, 
                        ARTIFACT_NAME, ARTIFACT_NAME + "blah").isMovedBetweenArtifacts(), 
                "Artifact changed");

        Assert.assertFalse(
                new ClassRefactor(
                        CLASS_NAME, CLASS_NAME + "Blah", 
                        ARTIFACT_NAME, ARTIFACT_NAME).isMovedBetweenArtifacts(), 
                "Artifact not changed");
        
    }

    @Test
    public void is_renamed() 
    {
        
        Assert.assertTrue(
                new ClassRefactor(
                        CLASS_NAME, CLASS_NAME + "Blah", 
                        ARTIFACT_NAME, ARTIFACT_NAME).isRenamed(), 
                "Fully qualified class name changed");

        Assert.assertFalse(
                new ClassRefactor(
                        CLASS_NAME, CLASS_NAME, 
                        ARTIFACT_NAME, ARTIFACT_NAME + "blah").isRenamed(), 
                "Fully qualified class name changed");        
    }
    
    @Test
    public void get_simple_old_class_name() 
    {
        
        Assert.assertEquals(
                new ClassRefactor(
                        CLASS_NAME, CLASS_NAME, 
                        ARTIFACT_NAME, ARTIFACT_NAME).getSimpleOldClassName(), 
                SIMPLE_CLASS_NAME,
                "Wrong simple old class name.");
    }

    @Test
    public void get_new_package_name() 
    {
        
        Assert.assertEquals(
                new ClassRefactor(
                        CLASS_NAME, CLASS_NAME.replace("org.", "com."), 
                        ARTIFACT_NAME, ARTIFACT_NAME).getNewPackageName(), 
                PACKAGE_NAME,
                "Wrong new package name.");
    }

}
