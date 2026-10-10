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

/**
 * The floor of the page-load benchmarks: markup and one expansion, no embedded components.
 *
 * Whatever this page costs to load is what Tapestry charges for a page before any component
 * work happens at all — class transformation, template parse, page assembly. Read
 * {@link RichPage} against it.
 */
public class SimplePage
{
    public String getTitle()
    {
        return "Simple";
    }
}
