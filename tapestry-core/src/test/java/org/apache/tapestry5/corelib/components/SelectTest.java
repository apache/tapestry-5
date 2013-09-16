// Copyright 2007-2013 The Apache Software Foundation
//
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.corelib.data.BlankOption;
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.util.EnumSelectModel;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mostly, this is about how the Select component renders its {@link SelectModel}. The real nuts and bolts are tested in
 * the integration tests.
 */
public class SelectTest extends InternalBaseTestCase
{

    @Test
    public void empty_model()
    {
        ValidationTracker tracker = mockValidationTracker();

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(null, null));
        select.setValidationTracker(tracker);

        select.options(null);

        verify();
    }

    private String fix(Object input)
    {
        return input.toString().replaceAll("\r\n", "\n");
    }

    private String read(String file) throws Exception
    {
        InputStream is = getClass().getResourceAsStream(file);
        Reader reader = new InputStreamReader(new BufferedInputStream(is));

        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1000];

        while (true)
        {
            int length = reader.read(buffer);

            if (length < 0)
                break;

            builder.append(buffer, 0, length);
        }

        reader.close();

        return fix(builder);
    }

    @Test
    public void just_options() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        List<OptionModel> options = TapestryInternalUtils.toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(null, options));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("barney");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("just_options.txt"));

        verify();
    }

    @Test
    public void just_options_with_blank_label_enabled() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        List<OptionModel> options = TapestryInternalUtils.toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(null, options));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("barney");
        select.setValidationTracker(tracker);
        select.setBlankOption(BlankOption.ALWAYS, "Make a selection");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("blank_label.txt"));

        verify();
    }

    @Test
    public void current_selection_from_validation_tracker() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        List<OptionModel> options = TapestryInternalUtils.toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

        Select select = new Select();

        train_getInput(tracker, select, "fred");

        replay();

        select.setModel(new SelectModelImpl(null, options));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("barney");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        // fred will be selected, not barney, because the validation tracker
        // takes precendence.

        assertEquals(writer.toString(), read("current_selection_from_validation_tracker.txt"));

        verify();
    }

    @Test
    public void option_attributes() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        // Extra cast needed for Sun compiler, not Eclipse compiler.

        List<OptionModel> options = Arrays.asList((OptionModel) new OptionModelImpl("Fred", "fred")
        {
            @Override
            public Map<String, String> getAttributes()
            {
                return Collections.singletonMap("class", "pixie");
            }
        });

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(null, options));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("barney");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_attributes.txt"));

        verify();
    }

    @Test
    public void disabled_option() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        // Extra cast needed for Sun compiler, not Eclipse compiler.

        List<OptionModel> options = Arrays.asList((OptionModel) new OptionModelImpl("Fred", "fred")
        {
            @Override
            public boolean isDisabled()
            {
                return true;
            }

            @Override
            public Map<String, String> getAttributes()
            {
                return Collections.singletonMap("class", "pixie");
            }
        });

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(null, options));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("barney");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("disabled_option.txt"));

        verify();
    }

    @Test
    public void option_groups() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        OptionGroupModel husbands = new OptionGroupModelImpl("Husbands", false,
                TapestryInternalUtils.toOptionModels("Fred,Barney"));
        OptionGroupModel wives = new OptionGroupModelImpl("Wives", true,
                TapestryInternalUtils.toOptionModels("Wilma,Betty"));
        List<OptionGroupModel> groupModels = CollectionFactory.newList(husbands, wives);

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(groupModels, null));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("Fred");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_groups.txt"));

        verify();
    }

    @Test
    public void option_groups_precede_ungroup_options() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        OptionGroupModel husbands = new OptionGroupModelImpl("Husbands", false,
                TapestryInternalUtils.toOptionModels("Fred,Barney"));

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(Collections.singletonList(husbands), TapestryInternalUtils
                .toOptionModels("Wilma,Betty")));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("Fred");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_groups_precede_ungroup_options.txt"));

        verify();
    }

    @Test
    public void option_group_attributes() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        Map<String, String> attributes = Collections.singletonMap("class", "pixie");

        OptionGroupModel husbands = new OptionGroupModelImpl("Husbands", false,
                TapestryInternalUtils.toOptionModels("Fred,Barney"), attributes);

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(Collections.singletonList(husbands), null));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("Fred");
        select.setValidationTracker(tracker);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_group_attributes.txt"));

        verify();
    }

    enum Platform
    {
        WINDOWS, MAC, LINUX;
    }

    @Test
    public void submitted_option_found_when_secure() throws ValidationException
    {

        ValueEncoder<Platform> encoder = getService(ValueEncoderSource.class).getValueEncoder(Platform.class);

        ValidationTracker tracker = mockValidationTracker();
        Request request = mockRequest();
        Messages messages = mockMessages();
        FieldValidationSupport fvs = mockFieldValidationSupport();

        expect(request.getParameter("xyz")).andReturn("MAC");

        expect(messages.contains(EasyMock.anyObject(String.class))).andReturn(false).anyTimes();

        Select select = new Select();

        tracker.recordInput(select, "MAC");

        fvs.validate(Platform.MAC, null, null);

        replay();

        SelectModel model = new EnumSelectModel(Platform.class, messages);

        set(select, "encoder", encoder);
        set(select, "model", model);
        set(select, "request", request);
        set(select, "secure", true);
        set(select, "beanValidationDisabled", true); // Disable BeanValidationContextSupport
        set(select, "tracker", tracker);
        set(select, "fieldValidationSupport", fvs);

        select.processSubmission("xyz");

        verify();

        assertEquals(get(select, "value"), Platform.MAC);
    }

    @Test
    public void submitted_option_not_found_when_secure() throws ValidationException
    {

        ValueEncoder<Platform> encoder = getService(ValueEncoderSource.class).getValueEncoder(Platform.class);

        ValidationTracker tracker = mockValidationTracker();
        Request request = mockRequest();
        Messages messages = mockMessages();

        expect(request.getParameter("xyz")).andReturn("MAC");

        expect(messages.contains(EasyMock.anyObject(String.class))).andReturn(false).anyTimes();

        Select select = new Select();

        tracker.recordInput(select, "MAC");

        tracker.recordError(EasyMock.eq(select), EasyMock.contains("option is not listed"));

        replay();

        SelectModel model = new EnumSelectModel(Platform.class, messages, new Platform[]{Platform.WINDOWS, Platform.LINUX});

        set(select, "encoder", encoder);
        set(select, "model", model);
        set(select, "request", request);
        set(select, "secure", true);
        set(select, "beanValidationDisabled", true); // Disable BeanValidationContextSupport
        set(select, "tracker", tracker);

        select.processSubmission("xyz");

        verify();
    }

    /** This a test for TAP5-2184 */
    @Test
    public void submitted_option_matches_against_value_encoded_option_model_value() throws ValidationException {
        ValueEncoder<Integer> encoder = getService(ValueEncoderSource.class).getValueEncoder(Integer.class);

        ValidationTracker tracker = mockValidationTracker();
        Request request = mockRequest();
        Messages messages = mockMessages();
        FieldValidationSupport fvs = mockFieldValidationSupport();

        expect(request.getParameter("xyz")).andReturn("5");

        expect(messages.contains(EasyMock.anyObject(String.class))).andReturn(false).anyTimes();

        Select select = new Select();

        tracker.recordInput(select, "5");

        fvs.validate(5, null, null);

        replay();

        // TAP5-2184 is triggered by the automatic String->SelectModel coercion, because the OptionModel
        // values are Strings even if the desired property type is not (Integer, here). Select has a little
        // hack to run the model values through the ValueEncoder for comparison.
        SelectModel model = getService(TypeCoercer.class).coerce("1,5,10,20", SelectModel.class);

        set(select, "encoder", encoder);
        set(select, "model", model);
        set(select, "request", request);
        set(select, "secure", true);
        set(select, "beanValidationDisabled", true); // Disable BeanValidationContextSupport
        set(select, "tracker", tracker);
        set(select, "fieldValidationSupport", fvs);

        select.processSubmission("xyz");

        verify();

        assertEquals(get(select, "value"), 5);

    }

}
