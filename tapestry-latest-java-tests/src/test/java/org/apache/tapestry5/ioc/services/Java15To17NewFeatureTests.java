// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions To
// limitations under the License.
package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Tests for Tapestry-IoC To the only Java language feature introduced in Java SE 10 To 11,
 * local variable type inference (introduced in 10 To improved in 11).
 */
public class Java15To17NewFeatureTests 
{
    
    // TODO: test ChainBuilder and company
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Java15To17NewFeatureTests.class);
    
    public static class Java15To17Module 
    {
        
        public static void bind(ServiceBinder binder)
        {
            binder.bind(Java15To17Service.class, Java15To17ServiceImpl.class);
            binder.bind(Java15To17ConcreteService.class);
        }
        
        // Two service for each case of sealed classes (no record and no interface, interface 
        // and no record, interface and record): one for the sealed class itself,
        // another for the subclasses.
        // 
        @Marker(Primary.class)        
        public static Appliance buildAppliance()
        {
            return new Freezer();
        }

        public static Refrigerator buildRefridgerator()
        {
            return new Refrigerator();
        }

        @Marker(Primary.class)
        public static BinaryExpression buildBinaryExpression()
        {
            return new OrExpression(new ConstantBinaryExpression(false), new ConstantBinaryExpression(true));
        }
        
        public static ConstantBinaryExpression buildConstantBinaryExpression()
        {
            return new ConstantBinaryExpression(true);
        }

        @Marker(Primary.class)
        public static IntExpression buildIntExpression()
        {
            return new SquareExpression(new ConstantIntExpression(42));
        }
        
        public static ConstantIntExpression buildConstantIntExpression()
        {
            return new ConstantIntExpression(3);
        }

    }
    
    private Java15To17Service java15To17Service;
    
    private Java15To17ConcreteService java15To17ConcreteService;

    private Registry registry;
    
    @BeforeSuite
    public void setup() 
    {
        registry = RegistryBuilder.buildAndStartupRegistry(Java15To17Module.class);
        java15To17Service = registry.getService(Java15To17Service.class);
        java15To17ConcreteService = registry.getService(Java15To17ConcreteService.class);
    }

    /**
     * Tests Record Classes, regular ones (no interfaces, no records).
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    @Test
    public void recordsWithoutInterfacesNorRecords()
    {
        java15To17Service.recordsWithoutInterfacesNorRecords();
        java15To17ConcreteService.recordsWithoutInterfacesNorRecords();
    }
    
    /**
     * Tests Record Classes with interfaces but no records.
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    @Test
    public void recordsWithInterfacesButNotRecords()
    {
        java15To17Service.recordsWithInterfacesButNotRecords();
        java15To17ConcreteService.recordsWithInterfacesButNotRecords();
    }

    /**
     * Tests Record Classes with interfaces and records.
     * https://docs.oracle.com/en/java/javase/17/language/records.html
     */
    @Test
    public void recordsWithInterfacesAndRecords()
    {
        java15To17Service.recordsWithInterfacesAndRecords();
        java15To17ConcreteService.recordsWithInterfacesAndRecords();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void recordsWithoutInterfacesNorRecords_service_sealedClass()
    {
        LOGGER.info(registry.getService(Appliance.class, Primary.class).getName());
    }
    
    @Test
    public void recordsWithoutInterfacesNorRecords_service_subclass()
    {
        LOGGER.info(registry.getService(Refrigerator.class).getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void recordsWithInterfacesButNotRecords_service_sealedClass()
    {
        LOGGER.info(String.valueOf(registry.getService(BinaryExpression.class, Primary.class).evaluate()));
    }
    
    @Test
    public void recordsWithInterfacesButNotRecords_service_subclass()
    {
        LOGGER.info(String.valueOf(registry.getService(ConstantBinaryExpression.class).evaluate()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void recordsWithInterfacesAndRecords_service_sealedClass()
    {
        LOGGER.info(String.valueOf(registry.getService(IntExpression.class, Primary.class).evaluate()));
    }
    
    @Test
    public void recordsWithInterfacesAndRecords_service_subclass()
    {
        LOGGER.info(String.valueOf(registry.getService(ConstantIntExpression.class).evaluate()));
    }

}
