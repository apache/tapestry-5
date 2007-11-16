package org.apache.tapestry.corelib.internal;

import org.apache.tapestry.*;
import org.apache.tapestry.runtime.ComponentEventException;
import org.apache.tapestry.test.TapestryTestCase;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class ComponentValidatorWrapperTest extends TapestryTestCase
{
    @Test
    public void render_is_a_pass_thru()
    {
        ComponentResources resources = mockComponentResources();
        FieldValidator fv = mockFieldValidator();
        MarkupWriter writer = mockMarkupWriter();

        fv.render(writer);

        replay();

        FieldValidator wrapper = new ComponentValidatorWrapper(resources, fv);

        wrapper.render(writer);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void event_triggered_after_delegate_invoked() throws Exception
    {
        getMocksControl().checkOrder(true);

        ComponentResources resources = mockComponentResources();
        FieldValidator fv = mockFieldValidator();

        Object value = new Object();

        fv.validate(value);


        ComponentEventHandler handler = null;

        expect(resources.triggerEvent(EasyMock.eq(ComponentValidatorWrapper.VALIDATE_EVENT),
                                      EasyMock.aryEq(new Object[]{value}), EasyMock.eq(handler))).andReturn(true);


        replay();

        FieldValidator wrapper = new ComponentValidatorWrapper(resources, fv);

        wrapper.validate(value);

        verify();
    }

    @SuppressWarnings({"unchecked", "ThrowableInstanceNeverThrown"})
    @Test
    public void event_trigger_throws_validation_exception() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        FieldValidator fv = mockFieldValidator();

        Object value = new Object();

        ValidationException ve = new ValidationException("Bah!");
        ComponentEventException cee = new ComponentEventException(ve.getMessage(), null, ve);

        ComponentEventHandler handler = null;

        fv.validate(value);

        expect(resources.triggerEvent(EasyMock.eq(ComponentValidatorWrapper.VALIDATE_EVENT),
                                      EasyMock.aryEq(new Object[]{value}), EasyMock.eq(handler))).andThrow(cee);


        replay();

        FieldValidator wrapper = new ComponentValidatorWrapper(resources, fv);


        try
        {
            wrapper.validate(value);
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertSame(ex, ve);
        }
        verify();
    }
}
