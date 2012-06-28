package org.apache.tapestry5.internal.util

import org.apache.tapestry5.Field
import org.apache.tapestry5.FieldFocusPriority
import org.apache.tapestry5.ValidationDecorator
import org.apache.tapestry5.ValidationTracker
import org.apache.tapestry5.services.javascript.JavaScriptSupport
import org.apache.tapestry5.test.TapestryTestCase
import org.testng.annotations.Test

class AutofocusValidationDecoratorTest extends TapestryTestCase {

    @Test
    void field_is_disabled() {
        Field field = mockField()
        ValidationDecorator delegate = mockValidationDecorator()
        ValidationTracker tracker = mockValidationTracker()
        JavaScriptSupport jsSupport = mockJavaScriptSupport()

        delegate.insideField(field)

        expect(field.disabled).andReturn true

        replay()

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, jsSupport)

        decorator.insideField(field)

        verify()
    }

    @Test
    void field_is_in_error() {
        Field field = mockField()
        ValidationDecorator delegate = mockValidationDecorator()
        ValidationTracker tracker = mockValidationTracker()
        JavaScriptSupport jsSupport = mockJavaScriptSupport()

        delegate.insideField(field)

        expect(field.disabled).andReturn false
        expect(tracker.inError(field)).andReturn true

        expect(field.clientId).andReturn "foo"

        expect(jsSupport.autofocus(FieldFocusPriority.IN_ERROR, "foo")).andReturn(jsSupport)

        replay()

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, jsSupport)

        decorator.insideField(field)

        verify()
    }

    @Test
    void field_is_required() {
        Field field = mockField()
        ValidationDecorator delegate = mockValidationDecorator()
        ValidationTracker tracker = mockValidationTracker()
        JavaScriptSupport jsSupport = mockJavaScriptSupport()

        delegate.insideField(field)

        expect(field.disabled).andReturn false
        expect(tracker.inError(field)).andReturn false
        expect(field.required).andReturn true
        expect(field.clientId).andReturn "foo"

        expect(jsSupport.autofocus(FieldFocusPriority.REQUIRED, "foo")).andReturn(jsSupport)

        replay()

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, jsSupport)

        decorator.insideField(field)

        verify()
    }

    @Test
    void field_is_optional() {
        Field field = mockField()
        ValidationDecorator delegate = mockValidationDecorator()
        ValidationTracker tracker = mockValidationTracker()
        JavaScriptSupport jsSupport = mockJavaScriptSupport()

        delegate.insideField(field)

        expect(field.disabled).andReturn false
        expect(tracker.inError(field)).andReturn false
        expect(field.required).andReturn false
        expect(field.clientId).andReturn "foo"

        expect(jsSupport.autofocus(FieldFocusPriority.OPTIONAL, "foo")).andReturn(jsSupport)

        replay()

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, jsSupport)

        decorator.insideField(field)

        verify()
    }

}
