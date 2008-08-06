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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
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

    private String read(String file) throws Exception
    {
        InputStream is = getClass().getResourceAsStream(file);
        Reader reader = new InputStreamReader(new BufferedInputStream(is));

        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1000];

        while (true)
        {
            int length = reader.read(buffer);

            if (length < 0) break;

            builder.append(buffer, 0, length);
        }

        reader.close();

        return builder.toString();
    }

    @Test
    public void just_options() throws Exception
    {
        ValidationTracker tracker = mockValidationTracker();

        List<OptionModel> options = TapestryInternalUtils
                .toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

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

        List<OptionModel> options = TapestryInternalUtils
                .toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

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

        List<OptionModel> options = TapestryInternalUtils
                .toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

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

        List<OptionModel> options = Arrays.asList(
                (OptionModel) new OptionModelImpl("Fred", "fred")
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

        List<OptionModel> options = Arrays.asList(
                (OptionModel) new OptionModelImpl("Fred", "fred")
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
        OptionGroupModel wives = new OptionGroupModelImpl("Wives", true, TapestryInternalUtils
                .toOptionModels("Wilma,Betty"));
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

        select.setModel(new SelectModelImpl(Collections.singletonList(husbands),
                                            TapestryInternalUtils.toOptionModels("Wilma,Betty")));
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
                                                             TapestryInternalUtils.toOptionModels("Fred,Barney"),
                                                             attributes);

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

}
