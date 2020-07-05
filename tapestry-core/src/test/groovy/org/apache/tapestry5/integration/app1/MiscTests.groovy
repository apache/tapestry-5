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

package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test

class MiscTests extends App1TestCase {

  @Test
  void operation_tracking_via_annotation() {
    openLinks "Operation Worker Demo", "throw exception"

    assertTitle "Application Exception"

    assertTextPresent "[Operation Description]"
  }

    @Test
    void meta_tag_identifying_page_name_is_present()
    {
        openLinks "Zone Demo"

        assertAttribute "//meta[@name='tapestry-page-name']/@content", "nested/ZoneDemo"
    }

    @Test
    void FormGroup_mixin() {
        openLinks "Autocomplete Mixin Demo"

        assertText "css=div.form-group > label", "Title"

        // Using Geb, we could do a lot more. Sigh.
    }

    // TAP5-2045
    // No longer such a big deal, as Label no longer has a formal "class" parameter
    @Test
    void label_class_override()
    {
        openLinks "Override Label Class Demo"

        assertSourcePresent "<label for=\"firstName\" id=\"firstName-label\" class=\"control-label\">First Name</label>",
                            "<label for=\"lastName\" id=\"lastName-label\" class=\"control-label dummyClassName\">Last Name</label>"

    }

    @Test
    void missing_asset_reports_location_under_meta_inf() {
        openLinks "Missing Asset Demo"

        assertTextPresent "Unable to locate asset 'does-not-exist.txt' for component MissingAssetDemo. It should be located at META-INF/assets/does-not-exist.txt."
    }

    @Test
    void local_date_formatting() {
        openLinks "LocalDate Demo"

        def defaultFormat = getText "local-date-default"
        def customFormat = getText "local-date-formatted"

        // Because there are so many variables: the date formatted, the locale and time zone of the
        // server running the tests, etc., all we can really test for is that the
        // client-side code successfully kicked in and formatted these differently.

        assertNotEquals defaultFormat, customFormat
    }

    @Test
    void timeinterval_component() {
        openLinks "TimeInterval Demo", "English"
        def timeSincePrevRenderText = getText("time-since-prev-render")
        def timeSincePrevRenderIntervalEnd = getAttribute("time-since-prev-render@data-timeinterval-end")

        assert timeSincePrevRenderText.endsWith(" ago"), "Time since previous render ($timeSincePrevRenderText) does not end with ' ago', the interval ends at $timeSincePrevRenderIntervalEnd"
        assert getText("jacob-age").contains("years")
        assert getText("jacob-vote").startsWith("in ")
    }

    @Test
    void strict_mixin_parameters() {
        openLinks "Strict Mixin Parameters"

        // Seems like this message could be a bit improved to make it clearer that it is a mixin parameter that is not bound.
        // However, the point is, that title is now not ambiguous; it is an informal parameter of ActionLink, even though
        // it matches the name of a formal parameter of AltTitle.
        assertTextPresent "Parameter(s) 'AltTitle.title' are required for org.apache.tapestry5.corelib.components.ActionLink, but have not been bound."
    }

}
