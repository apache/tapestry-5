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
package org.apache.tapestry5.ioc.services;

public interface Java15To17Service 
{
    /**
     * Tests Record Classes, regular ones (no interfaces, no records).
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    public void recordsWithoutInterfacesNorRecords();  
    
    /**
     * Tests Record Classes with interfaces but no records.
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    public void recordsWithInterfacesButNotRecords();

    /**
     * Tests Record Classes with interfaces and records.
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    public void recordsWithInterfacesAndRecords();
    
}