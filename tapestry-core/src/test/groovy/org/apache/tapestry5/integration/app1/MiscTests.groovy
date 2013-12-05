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

        assertSourcePresent "<label for=\"firstName\" class=\"control-label\">First Name</label>",
                            "<label for=\"lastName\" class=\"control-label dummyClassName\">Last Name</label>"

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

}
