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

import org.slf4j.LoggerFactory;

public class Java14ConcreteService
{
    
    public Java14ConcreteService()
    {
        patternMatchingForTheInstanceOfOperator();
        records();
        localRecord();  
    }

    /**
     * For testing Pattern Matching for the instanceof Operator
     * http://www.oracle.com/pls/topic/lookup?ctx=javase14&id=GUID-843060B5-240C-4F47-A7B0-95C42E5B08A7
     */
    public void patternMatchingForTheInstanceOfOperator() 
    {
        handleVideogame(new HomeConsole());
        handleVideogame(new ConsolePortableHybrid());
    }
    
    @SuppressWarnings("preview")
    private void handleVideogame(Videogame videogame)
    {
        if (videogame instanceof HomeConsole homeConsole)
        {
            homeConsole.turnConnectedTVOn();
        }
        if (videogame instanceof ConsolePortableHybrid hybrid)
        {
            hybrid.turnEmbeddedScreenOn();
        }
    }

    /**
     * For testing Records
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    public IntTuple records() 
    {
        return new IntTuple(1, 1);
    }
    
    /**
     * For testing Local Record Classes
     * GUID-6699E26F-4A9B-4393-A08B-1E47D4B2D263__GUID-FB8EDC85-2C6A-4591-8E00-248DA900723A
     */
    public void localRecord() 
    {
        
        record Local(String name)
        {
            
        }
        
        LoggerFactory.getLogger(this.getClass()).info("Local record " + new Local("test"));
    }
    
}