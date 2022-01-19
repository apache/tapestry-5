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

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Tests for Tapestry-IoC and Java language features introduced in Java SE 14: pattern matching for the
 * <code>instanceof</code> operator and records.
 */
public class Java14NewFeatureTests 
{
    
    private Registry registry;
    
    public static class Java14Module 
    {
        public static void bind(ServiceBinder binder)
        {
            binder.bind(Java14Service.class, Java14ServiceImpl.class);
            binder.bind(Java14ConcreteService.class);
        }
        public static IntTuple buildIntTuple()
        {
            return new IntTuple(10, 15);
        }
    }
    
    private Java14Service java14Service;
    
    private Java14ConcreteService java14ConcreteService;
    
    @BeforeSuite
    public void setup() 
    {
        registry = RegistryBuilder.buildAndStartupRegistry(Java14Module.class);
        java14Service = registry.getService(Java14Service.class);
        java14ConcreteService = registry.getService(Java14ConcreteService.class);
    }

    /**
     * For testing Pattern Matching for the instanceof Operator
     * http://www.oracle.com/pls/topic/lookup?ctx=javase14&id=GUID-843060B5-240C-4F47-A7B0-95C42E5B08A7
     */
    @Test
    public void patternMatchingForTheInstanceOfOperator() 
    {
        java14Service.patternMatchingForTheInstanceOfOperator();
        java14ConcreteService.patternMatchingForTheInstanceOfOperator();
    }

    /**
     * For testing Records
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    @Test
    public void records() 
    {
        process(java14Service.records());
        process(java14ConcreteService.records());
    }
    
    /**
     * For testing services whose type is a record.
     */
    @Test
    public void recordsAsServices()
    {
        process(registry.getService(IntTuple.class));
    }

    private void process(IntTuple record) {
        LoggerFactory.getLogger(this.getClass()).info("Record: " + record);
    }
    
    /**
     * For testing Local Record Classes
     * GUID-6699E26F-4A9B-4393-A08B-1E47D4B2D263__GUID-FB8EDC85-2C6A-4591-8E00-248DA900723A
     */
    @Test
    public void localRecord() 
    {
        java14Service.localRecord();
        java14ConcreteService.localRecord();
    }

}
