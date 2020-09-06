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

/**
 * Class that represents information about one class being renamed and/or moved
 * between artifacts (JARs) and/or packages.
 */
final public class ClassRefactor 
{
    
    final private String newClassName;
    final private String oldClassName;
    final private String sourceArtifact;
    final private String destinationArtifact;
    
    /**
     * Constructor for classes being moved from one artifact to another
     * and possibly being renamed or moved between packages.
     */
    public ClassRefactor(String newClassName, String oldClassName, String sourceArtifact, String destinationArtifact) 
    {
        super();
        verifyNotBlank("newClassName", newClassName);
        verifyNotBlank("oldClassName", oldClassName);
        verifyNotBlank("sourceArtifact", sourceArtifact);
        verifyNotBlank("destinationArtifact", destinationArtifact);
        this.newClassName = newClassName;
        this.oldClassName = oldClassName;
        this.sourceArtifact = sourceArtifact;
        this.destinationArtifact = destinationArtifact;
    }
    
    /**
     * Returns the new fully-qualified class name.
     */
    public String getNewClassName() 
    {
        return newClassName;
    }
    
    /**
     * Returns the old fully-qualified class name.
     */
    public String getOldClassName() 
    {
        return oldClassName;
    }
    
    /**
     * Returns the artifact where the class was located.
     */
    public String getSourceArtifact() 
    {
        return sourceArtifact;
    }
    
    /**
     * Returns the artifact where the class is now located.
     */
    public String getDestinationArtifact() 
    {
        return destinationArtifact;
    }
    
    /**
     * Returns whether the class was moved between artifacts.
     */
    public boolean isMovedBetweenArtifacts() 
    {
        return !sourceArtifact.equals(destinationArtifact);
    }

    /**
     * Returns whether the class had its fully qualified class name changed.
     * This includes package changes.
     */
    public boolean isRenamed() 
    {
        return !oldClassName.equals(newClassName);
    }

    @Override
    public String toString() {
        return "ClassMoveInformation [newClassName=" + newClassName + ", oldClassName=" + oldClassName + ", sourceArtifact=" + sourceArtifact
                + ", destinationArtifact=" + destinationArtifact + "]";
    }
 
    final static boolean isNotBlank(String string)
    {
        return string != null && string.trim().length() > 0;
    }
    
    final static void verifyNotBlank(String parameterName, String parameterValue)
    {
        if (!isNotBlank(parameterValue))
        {
            throw new IllegalArgumentException(
                    String.format("Parameter %s cannot be null nor blank", parameterName));
        }
    }
 
    /**
     * Returns the simple old class name.
     */
    public String getSimpleOldClassName() {
        return oldClassName.substring(oldClassName.lastIndexOf(".") + 1);
    }
    
    /**
     * Returns whether the class is internal or not.
     */
    public boolean isInternal()
    {
        return oldClassName.contains(".internal.");
    }
    
    /**
     * Returns the new package location.
     */
    public String getNewPackageName() 
    {
        return newClassName.substring(0, newClassName.lastIndexOf("."));
    }

}