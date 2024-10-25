// Licensed to the Apache License, Version 2.0 (the "License");
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

package org.apache.tapestry5.integration.app1.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Category;

public class RecursiveDemo
{
    
    @Property
    private Category category;
    
    public List<Category> getCategories()
    {
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++)
        {
            final String name = "Category " + i;
            categories.add(new Category(name,
                    new Category(name + ".1", new Category(name + ".1.1")), 
                    new Category(name + ".2")));
        }
        
        return categories;
        
    }

}
