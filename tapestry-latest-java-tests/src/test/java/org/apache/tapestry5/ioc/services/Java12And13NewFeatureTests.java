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

import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Tests for Tapestry-IoC and the Java language features introduced in Java SE 12 and 13,
 * switch expressiosn and text blocks.
 */
public class Java12And13NewFeatureTests 
{
    
    public static class Java12And13Module 
    {
        public static void bind(ServiceBinder binder)
        {
            binder.bind(Java12And13Service.class, Java12And13ServiceImpl.class);
            binder.bind(Java12And13ConcreteService.class);
        }
    }
    
    private Java12And13Service java12And13Service;
    
    private Java12And13ConcreteService java12And13ConcreteService;
    
    @BeforeSuite
    public void setup() 
    {
        var registry = RegistryBuilder.buildAndStartupRegistry(Java12And13Module.class);
        java12And13Service = registry.getService(Java12And13Service.class);
        java12And13ConcreteService = registry.getService(Java12And13ConcreteService.class);
    }

    /**
     * Tests Text Blocks
     * https://docs.oracle.com/en/java/javase/17/language/text-blocks.html
     */
    @Test
    public void textBlocks() throws Exception 
    {
        java12And13Service.textBlocks();
        java12And13ConcreteService.textBlocks();
    }

    /**
     * Tests Switch Expressions
     * https://docs.oracle.com/en/java/javase/17/language/switch-expressions.html
     */
    @Test
    public void switchExpressions() throws Exception 
    {
        java12And13Service.switchExpressions();
        java12And13ConcreteService.switchExpressions();
    }

}
