// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.internal;

/**
 * Defines a context for validating beans.
 * 
 * @since 5.2.0
 */
public interface BeanValidationContext
{
    /**
     * Returns the type of the object to validate. This method is needed for client side validation.
     */
    Class getBeanType();
    
    /**
     * Return the object to validate.
     */
    Object getBeanInstance();
    
    /**
     * Returns name of the property to validate. The current name is overwritten by every form field.
     */
    String getCurrentProperty();
    
    /**
     * Sets name of the property to validate.
     * 
     * @param propertyName name of the property
     */
    void setCurrentProperty(String propertyName);
}
