// Copyright 2007, 2008 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class InheritedBindingTest extends TapestryTestCase
{
    private static final String BINDING_DESCRIPTION = "binding description";

    private static final String MESSAGE = "Exception in inner.";

    private static final Throwable exception = new RuntimeException(MESSAGE);

    @Test
    public void to_string_and_location()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        assertSame(binding.toString(), BINDING_DESCRIPTION);
        assertSame(binding.getLocation(), l);

        verify();
    }

    @Test
    public void get_success()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        Object expected = new Object();

        train_get(inner, expected);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        assertSame(binding.get(), expected);

        verify();
    }

    @Test
    public void get_failure()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();

        expect(inner.get()).andThrow(exception);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        try
        {
            binding.get();
            unreachable();
        }
        catch (TapestryException ex)
        {
            checkException(ex, l);
        }

        verify();
    }

    @Test
    public void get_binding_type_success()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        Class expected = Runnable.class;

        expect(inner.getBindingType()).andReturn(expected);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        assertSame(binding.getBindingType(), expected);

        verify();
    }

    @Test
    public void get_binding_type_failure()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        String description = BINDING_DESCRIPTION;

        expect(inner.getBindingType()).andThrow(exception);

        replay();

        InheritedBinding binding = new InheritedBinding(description, inner, l);

        try
        {
            binding.getBindingType();
            unreachable();
        }
        catch (TapestryException ex)
        {
            checkException(ex, l);
        }

        verify();
    }

    private void checkException(TapestryException ex, Location l)
    {
        assertEquals(ex.getMessage(), MESSAGE);
        assertEquals(ex.getLocation(), l);
        assertSame(ex.getCause(), exception);
    }

    @Test
    public void is_invariant_success()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        boolean expected = true;

        expect(inner.isInvariant()).andReturn(expected);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        assertEquals(binding.isInvariant(), expected);

        verify();
    }

    @Test
    public void is_invariant_failure()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        expect(inner.isInvariant()).andThrow(exception);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        try
        {
            binding.isInvariant();
            unreachable();
        }
        catch (TapestryException ex)
        {
            checkException(ex, l);
        }

        verify();
    }

    @Test
    public void set_success()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        String description = BINDING_DESCRIPTION;
        Object parameter = new Object();

        inner.set(parameter);

        replay();

        InheritedBinding binding = new InheritedBinding(description, inner, l);

        binding.set(parameter);

        verify();
    }

    @Test
    public void set_failure()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        Object parameter = new Object();

        inner.set(parameter);
        setThrowable(exception);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        try
        {
            binding.set(parameter);
            unreachable();
        }
        catch (TapestryException ex)
        {
            checkException(ex, l);
        }

        verify();
    }

    @Test
    public void get_annotation_success()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();
        Inject inject = newMock(Inject.class);

        train_getAnnotation(inner, Inject.class, inject);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        assertSame(binding.getAnnotation(Inject.class), inject);

        verify();
    }

    @Test
    public void get_annotation_failure()
    {
        Binding inner = mockBinding();
        Location l = mockLocation();

        expect(inner.getAnnotation(Inject.class)).andThrow(exception);

        replay();

        InheritedBinding binding = new InheritedBinding(BINDING_DESCRIPTION, inner, l);

        try
        {
            binding.getAnnotation(Inject.class);
            unreachable();
        }
        catch (TapestryException ex)
        {
            checkException(ex, l);
        }

        verify();
    }
}
