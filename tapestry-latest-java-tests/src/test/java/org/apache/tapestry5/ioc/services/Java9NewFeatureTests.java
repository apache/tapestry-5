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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for Tapestry-IoC and Java language features introduced in Java SE 9.
 */
public class Java9NewFeatureTests 
{
    
    public static class Java9Module 
    {
        public static void bind(ServiceBinder binder)
        {
            binder.bind(Java9Service.class, Java9ServiceImpl.class);
            binder.bind(Java9ConcreteService.class);
        }
    }
    
    private static Java9Service java9Service;
    
    private static Java9ConcreteService java9ConcreteService;
    
    @BeforeAll
    public static void setup()
    {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(Java9Module.class);
        java9Service = registry.getService(Java9Service.class);
        java9ConcreteService = registry.getService(Java9ConcreteService.class);
    }

    @Test
    public void safevarargs() 
    {
        java9Service.callSafeVarArgs();
        java9ConcreteService.callSafeVarArgs();
    }
    
    @Test
    public void tryWithResources() 
    {
        java9Service.tryWithResources();
        java9ConcreteService.tryWithResources();
    }
    
}
