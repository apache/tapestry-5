// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.util.Locale;

import org.apache.tapestry.Asset;
import org.apache.tapestry.dom.Document;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.Environment;
import org.testng.annotations.Test;

public class InjectStandardStylesheetCommandTest extends InternalBaseTestCase
{
    @Test
    public void no_head_element()
    {
        Document d = new Document();
        ThreadLocale tl = newThreadLocale();
        AssetSource source = newAssetSource();
        Environment env = newEnvironment();

        d.newRootElement("foo");
        String initial = d.toString();

        train_peek(env, Document.class, d);

        replay();

        new InjectStandardStylesheetCommand(tl, source).cleanup(env);

        assertEquals(d.toString(), initial, "Document structure should not change.");

        verify();
    }

    @Test
    public void head_element_found()
    {
        Document d = new Document();
        ThreadLocale tl = newThreadLocale();
        AssetSource source = newAssetSource();
        Environment env = newEnvironment();
        Asset asset = newAsset();
        Locale l = Locale.FRENCH;

        d.newRootElement("html").element("head");

        train_peek(env, Document.class, d);
        train_getLocale(tl, l);

        train_getClasspathAsset(source, "org/apache/tapestry/default.css", l, asset);
        toClientURL(asset, "{clientURL}");

        replay();

        new InjectStandardStylesheetCommand(tl, source).cleanup(env);

        verify();

        assertEquals(
                d.toString(),
                "<html><head><link href=\"{clientURL}\" rel=\"stylesheet\" type=\"text/css\"></head></html>");
    }
}
