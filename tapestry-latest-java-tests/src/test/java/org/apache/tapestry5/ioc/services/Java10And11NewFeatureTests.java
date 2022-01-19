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
 * Tests for Tapestry-IoC and the only Java language feature introduced in Java SE 10 and 11,
 * local variable type inference (introduced in 10 and improved in 11).
 */
public class Java10And11NewFeatureTests 
{
    
    public static class Java10And11Module 
    {
        public static void bind(ServiceBinder binder)
        {
            binder.bind(Java10And11Service.class, Java10And11ServiceImpl.class);
            binder.bind(Java10And11ConcreteService.class);
        }
    }
    
    private Java10And11Service java10And11Service;
    
    private Java10And11ConcreteService java10And11ConcreteService;
    
    @BeforeSuite
    public void setup() 
    {
        var registry = RegistryBuilder.buildAndStartupRegistry(Java10And11Module.class);
        java10And11Service = registry.getService(Java10And11Service.class);
        java10And11ConcreteService = registry.getService(Java10And11ConcreteService.class);
    }

    @Test
    public void localVariableTypeInference() throws Exception 
    {
        java10And11Service.localVariableTypeInference();
        java10And11ConcreteService.localVariableTypeInference();;
    }
    
}
