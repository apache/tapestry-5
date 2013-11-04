package org.apache.tapestry5.integration.app3

import org.apache.tapestry5.corelib.components.BeanEditor
import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.apache.tapestry5.integration.app3.services.AppModule
import org.apache.tapestry5.test.TapestryTestConfiguration

/**
 * Tests for the {@link BeanEditor} component CSS overrides
 */
@TapestryTestConfiguration(webAppFolder = "src/test/app3")
public class BeanEditorTestsWithOverridenCssClassesTests extends TapestryCoreTestCase {

    /** TAP5-2182 */
    public void bean_editor_overriden_markup() {

        def formGroupLocator = "//form[@id='form']/div[@class='${AppModule.FORM_GROUP_WRAPPER_CSS_CLASS_VALUE}']"
        def divCssLocator = "$formGroupLocator/@class"
        def labelCssLocator = "$formGroupLocator/label/@class"
        def inputCssLocator = String.format("%s/%s[@class='%s']/input/@class",
            formGroupLocator,
            AppModule.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME_VALUE,
            AppModule.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS_VALUE)

        open("/beaneditorwithoverridencssclassesdemo")

        assertEquals AppModule.FORM_GROUP_WRAPPER_CSS_CLASS_VALUE, getAttribute(divCssLocator)
        assertEquals AppModule.FORM_GROUP_LABEL_CSS_CLASS_VALUE, getAttribute(labelCssLocator)
        assertEquals AppModule.FORM_FIELD_CSS_CLASS_VALUE, getAttribute(inputCssLocator)
    }

}
