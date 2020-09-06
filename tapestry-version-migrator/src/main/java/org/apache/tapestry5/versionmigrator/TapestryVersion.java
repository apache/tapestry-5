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
 * Represents a Tapestry version that needs a migration due to names that moved from
 * one package to another.
 */
public enum TapestryVersion 
{
    
    TAPESTRY_5_7_0("5.7.0", "HEAD", "39041bfa2c6c030f2f2e55d1c88c3ed836ff758b" /* 5.6.1 */);
    
    final private String number;
    final private String previousVersionGitHash;
    final private String versionGitHash;
    
    private TapestryVersion(String number, String versionGitHash, String previousVersionGitHash) 
    {
        this.number = number;
        this.versionGitHash = versionGitHash;
        this.previousVersionGitHash = previousVersionGitHash;
    }

    /**
     * Returns the version number.
     */
    public String getNumber() 
    {
        return number;
    }

    /**
     * Returns the Git hash or tag representing the version previous to this one.
     */
    public String getPreviousVersionGitHash() 
    {
        return previousVersionGitHash;
    }

    /**
     * Represents the Git hash or tag representing this version.
     * @return
     */
    public String getVersionGitHash() 
    {
        return versionGitHash;
    }
    
    public String toString() 
    {
        return String.format("Apache Tapestry %s", getNumber());
    }

}