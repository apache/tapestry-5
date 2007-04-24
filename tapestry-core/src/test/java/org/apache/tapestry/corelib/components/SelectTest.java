// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.OptionGroupModel;
import org.apache.tapestry.OptionModel;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.dom.XMLMarkupModel;
import org.apache.tapestry.internal.OptionGroupModelImpl;
import org.apache.tapestry.internal.OptionModelImpl;
import org.apache.tapestry.internal.SelectModelImpl;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.internal.services.MarkupWriterImpl;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.testng.annotations.Test;

/**
 * Mostly, this is about how the Select component renders its {@link SelectModel}. The real nuts
 * and bolts are tested in the integration tests.
 */
public class SelectTest extends InternalBaseTestCase
{
    @Test
    public void empty_model()
    {
        Select select = new Select();

        select.setModel(new SelectModelImpl(null, null));

        select.options(null);
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

        return builder.toString();
    }

    @Test
    public void just_options() throws Exception
    {
        List<OptionModel> options = TapestryUtils
                .toOptionModels("fred=Fred Flintstone,barney=Barney Rubble");

        Select select = new Select();

        select.setModel(new SelectModelImpl(null, options));
        select.setValue("barney");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("just_options.html"));
    }

    @Test
    public void option_attributes() throws Exception
    {
        // Extra cast needed for Sun compiler, not Eclipse compiler.

        List<OptionModel> options = Arrays.asList((OptionModel) new OptionModelImpl("Fred", false,
                "fred", "class", "pixie"));

        Select select = new Select();

        select.setModel(new SelectModelImpl(null, options));
        select.setValue("barney");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_attributes.html"));
    }

    @Test
    public void disabled_option() throws Exception
    {
        // Extra cast needed for Sun compiler, not Eclipse compiler.

        List<OptionModel> options = CollectionFactory.newList((OptionModel) new OptionModelImpl(
                "Fred", true, "fred", "class", "pixie"));

        Select select = new Select();

        select.setModel(new SelectModelImpl(null, options));
        select.setValue("barney");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("disabled_option.html"));

    }

    @Test
    public void option_groups() throws Exception
    {
        OptionGroupModel husbands = new OptionGroupModelImpl("Husbands", false, TapestryUtils
                .toOptionModels("Fred,Barney"));
        OptionGroupModel wives = new OptionGroupModelImpl("Wives", true, TapestryUtils
                .toOptionModels("Wilma,Betty"));
        List<OptionGroupModel> groupModels = CollectionFactory.newList(husbands, wives);

        Select select = new Select();

        select.setModel(new SelectModelImpl(groupModels, null));
        select.setValue("Fred");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_groups.html"));
    }

    @Test
    public void option_groups_precede_ungroup_options() throws Exception
    {
        OptionGroupModel husbands = new OptionGroupModelImpl("Husbands", false, TapestryUtils
                .toOptionModels("Fred,Barney"));

        Select select = new Select();

        select.setModel(new SelectModelImpl(Collections.singletonList(husbands), TapestryUtils
                .toOptionModels("Wilma,Betty")));
        select.setValue("Fred");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_groups_precede_ungroup_options.html"));
    }

    @Test
    public void option_group_attributes() throws Exception
    {
        Map<String, String> attributes = Collections.singletonMap("class", "pixie");

        OptionGroupModel husbands = new OptionGroupModelImpl("Husbands", false, TapestryUtils
                .toOptionModels("Fred,Barney"), attributes);

        Select select = new Select();

        select.setModel(new SelectModelImpl(Collections.singletonList(husbands), null));
        select.setValue("Fred");

        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);

        writer.element("select");

        select.options(writer);

        writer.end();

        assertEquals(writer.toString(), read("option_group_attributes.html"));
    }
}
