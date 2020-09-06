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
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.corelib.components.SelectTest.Platform;
import org.apache.tapestry5.corelib.data.BlankOption;
import org.apache.tapestry5.corelib.data.SecureOption;
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.internal.services.ContextValueEncoderImpl;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
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
    public void output_with_raw_enabled() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        List<OptionModel> options = TapestryInternalUtils.toOptionModels("bold=<b>Bold</b>,italic=<i>Italic</i>");

        Select select = new Select();

        train_getInput(tracker, select, null);

        replay();

        select.setModel(new SelectModelImpl(null, options));
        select.setValueEncoder(new StringValueEncoder());
        select.setValue("barney");
        select.setValidationTracker(tracker);
        select.setRaw(true);

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("output_with_raw_enabled.txt"));

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

    /**
     * TAP5-2204: When secure parameter is "always" there should be no
     * validation error if the model is NOT null.
     */
    @Test
    public void submitted_option_found_when_secure_always() throws ValidationException
    {
        checkSubmittedOption(true, SecureOption.ALWAYS, null);
    }

    /**
     * TAP5-2204: When secure parameter is "always" there should be a
     * validation error if the model is null.
     */
    @Test
    public void submitted_option_not_found_when_secure_always() throws ValidationException
    {
        checkSubmittedOption(false, SecureOption.ALWAYS, "is null when validating");
    }

    /**
     * TAP5-2204: When secure parameter is "never" there should be no
     * validation error if the model is NOT null.
     */
    @Test
    public void submitted_option_ok_when_secure_never() throws ValidationException
    {
        checkSubmittedOption(true, SecureOption.NEVER, null);
    }

    /**
     * TAP5-2204: When secure parameter is "never" there should be no
     * validation error if the model is null.
     */
    @Test
    public void submitted_option_ok_when_secure_never_no_model() throws ValidationException
    {
        checkSubmittedOption(false, SecureOption.NEVER, null);
    }

    /**
     * TAP5-2204: When secure parameter is "auto" there should be no
     * validation error if the model is NOT null.
     */
    @Test
    public void submitted_option_found_when_secure_auto() throws ValidationException
    {
        checkSubmittedOption(true, SecureOption.AUTO, null);
    }

    /**
     * TAP5-2204: When secure parameter is "auto" there should be no
     * validation error if the model is null.
     */
    @Test
    public void submitted_option_ok_when_secure_auto() throws ValidationException
    {
        checkSubmittedOption(false, SecureOption.AUTO, null);
    }

    /**
     * Utility for testing the "secure" option with various values and model
     * states. This avoids a lot of redundant test setup code.
     *
     * @param withModel whether there should be a model to test against
     * @param secureOption which "secure" option to test
     * @param expectedError the expected error message, nor null if no error
     * @throws ValidationException
     */
    private void checkSubmittedOption(boolean withModel, SecureOption secureOption,
            String expectedError) throws ValidationException
    {

        ValueEncoder<Platform> encoder = getService(ValueEncoderSource.class).getValueEncoder(Platform.class);

        ValidationTracker tracker = mockValidationTracker();
        Request request = mockRequest();
        Messages messages = mockMessages();
        FieldValidationSupport fvs = mockFieldValidationSupport();
        TypeCoercer typeCoercer = mockTypeCoercer();
        InternalComponentResources resources = mockInternalComponentResources();
        Binding selectModelBinding = mockBinding();

        expect(request.getParameter("xyz")).andReturn("MAC");

        expect(messages.contains(EasyMock.anyObject(String.class))).andReturn(false).anyTimes();

        expect(resources.getBinding("model")).andReturn(selectModelBinding);

        final Holder<SelectModel> modelHolder = Holder.create();

        expect(typeCoercer.coerce(EasyMock.or(EasyMock.isA(SelectModel.class), EasyMock.isNull()), EasyMock.eq(SelectModel.class)))
        .andAnswer(new IAnswer<SelectModel>() {

          @Override
          public SelectModel answer() throws Throwable {
            return modelHolder.get();
          }
        });


        expect(selectModelBinding.get()).andAnswer(new IAnswer<SelectModel>() {

          @Override
          public SelectModel answer() throws Throwable {
            return modelHolder.get();
          }
        });

        Select select = new Select();

        tracker.recordInput(select, "MAC");

        // when not failing we will expect to call the fvs.validate method
        if (expectedError == null)
        {
            fvs.validate(Platform.MAC, resources, null);
        }
        else
        {
            tracker.recordError(EasyMock.eq(select), EasyMock.contains(expectedError));
        }

        replay();

        if (withModel)
        {
          modelHolder.put(new EnumSelectModel(Platform.class, messages));
        }

        set(select, "encoder", encoder);
        set(select, "model", modelHolder.get());
        set(select, "request", request);
        set(select, "secure", secureOption);
        set(select, "beanValidationDisabled", true); // Disable BeanValidationContextSupport
        set(select, "tracker", tracker);
        set(select, "fieldValidationSupport", fvs);
        set(select, "typeCoercer", typeCoercer);
        set(select, "resources", resources);

        select.processSubmission("xyz");

        if (expectedError == null)
        {
            assertEquals(get(select, "value"), Platform.MAC);
        }

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
        TypeCoercer typeCoercer = mockTypeCoercer();
        InternalComponentResources resources = mockInternalComponentResources();
        Binding selectModelBinding = mockBinding();


        expect(request.getParameter("xyz")).andReturn("5");

        expect(messages.contains(EasyMock.anyObject(String.class))).andReturn(false).anyTimes();

        expect(resources.getBinding("model")).andReturn(selectModelBinding);

        final Holder<SelectModel> modelHolder = Holder.create();

        expect(typeCoercer.coerce(EasyMock.or(EasyMock.isA(SelectModel.class), EasyMock.isNull()), EasyMock.eq(SelectModel.class)
        ))
        .andAnswer(new IAnswer<SelectModel>() {

          @Override
          public SelectModel answer() throws Throwable {
            return modelHolder.get();
          }
        });


        expect(selectModelBinding.get()).andAnswer(new IAnswer<SelectModel>() {

          @Override
          public SelectModel answer() throws Throwable {
            return modelHolder.get();
          }
        });


        Select select = new Select();

        tracker.recordInput(select, "5");

        fvs.validate(5, resources, null);

        replay();

        // TAP5-2184 is triggered by the automatic String->SelectModel coercion, because the OptionModel
        // values are Strings even if the desired property type is not (Integer, here). Select has a little
        // hack to run the model values through the ValueEncoder for comparison.
        modelHolder.put(getService(TypeCoercer.class).coerce("1,5,10,20", SelectModel.class));

        set(select, "encoder", encoder);
        set(select, "model", modelHolder.get());
        set(select, "request", request);
        set(select, "secure", SecureOption.ALWAYS);
        set(select, "beanValidationDisabled", true); // Disable BeanValidationContextSupport
        set(select, "tracker", tracker);
        set(select, "fieldValidationSupport", fvs);
        set(select, "typeCoercer", typeCoercer);
        set(select, "resources", resources);

        select.processSubmission("xyz");

        verify();

        assertEquals(get(select, "value"), 5);

    }

    @Test
    public void context_that_needs_to_be_encoded() throws Exception
    {

        ValueEncoderSource valueEncoderSource = mockValueEncoderSource();

        TypeCoercer typeCoercer = getService(TypeCoercer.class);

        ContextValueEncoder contextValueEncoder = new ContextValueEncoderImpl(valueEncoderSource);

        ValueEncoder<Platform> platformEncoder = new ValueEncoder<SelectTest.Platform>() {

          @Override
          public Platform toValue(String clientValue) {
            return Platform.valueOf(clientValue.substring(10));
          }

          @Override
          public String toClient(Platform value) {
            return "Platform: "+value.name();
          }
        };

        InternalComponentResources resources = mockInternalComponentResources();
        expect(valueEncoderSource.getValueEncoder(Platform.class)).andReturn(platformEncoder).anyTimes();
        expect(valueEncoderSource.getValueEncoder(String.class)).andReturn(new StringValueEncoder()).anyTimes();

        expect(resources.triggerContextEvent(EasyMock.eq(EventConstants.VALUE_CHANGED), eqEventContext(null, Platform.LINUX), EasyMock.isA(ComponentEventCallback.class))).andReturn(true);


        Select select = new Select();

        set(select, "resources", resources);
        set(select, "encoder", new StringValueEncoder());
        set(select, "typeCoercer", typeCoercer);

        replay();

        select.onChange(new URLEventContext(contextValueEncoder, new String[]{platformEncoder.toClient(Platform.LINUX)}), null);

        verify();
    }

    private static EventContext eqEventContext(final Object ... expectedContext)
    {
      EasyMock.reportMatcher(new IArgumentMatcher() {

        @Override
        public boolean matches(Object argument)
        {
          EventContext context = (EventContext) argument;
          for (int i = 0; i < expectedContext.length; i++)
          {
            Object expected = expectedContext[i];
            Class expectedClass = expected == null ? Object.class : expected.getClass();

            if (!TapestryInternalUtils.isEqual(context.get(expectedClass, i), expected))
            {
                return false;
            }
          }
          return true;
        }

        @Override
        public void appendTo(StringBuffer buffer)
        {
          buffer.append("expected event context [");
          for (int i = 0; i < expectedContext.length; i++)
          {
            if (i != 0)
            {
              buffer.append(", ");
            }
            buffer.append(expectedContext[i]);
          }
          buffer.append("]");
        }
      });
      return null;
    }
}
