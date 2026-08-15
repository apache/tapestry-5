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

package org.apache.tapestry5.benchmarks.app.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Property;

/**
 * A page with enough embedded components to make page assembly, rather than page instantiation,
 * the dominant cost.
 *
 * Deliberately built from core components only, so that the benchmark measures Tapestry's
 * loading machinery and not a fixture of our own. The difference between loading this and
 * {@link SimplePage} is the per-component cost of assembly.
 */
public class RichPage
{
    @Property
    private String item;

    @Property
    private String text;

    @Property
    private boolean flag;

    public List<String> getItems()
    {
        List<String> items = new ArrayList<>();

        for (int i = 0; i < 20; i++)
        {
            items.add("Item " + i);
        }

        return items;
    }

    public String getTitle()
    {
        return "Rich";
    }
}
