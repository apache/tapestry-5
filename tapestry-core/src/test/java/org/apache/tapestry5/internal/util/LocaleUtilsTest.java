// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Locale;

/**
 * This, too, was adapted from commons-lang code.  Could be cleaned up a bit to better take advantage of TestNG.
 */
@SuppressWarnings({ "EmptyCatchBlock" })
public class LocaleUtilsTest extends Assert
{
    /**
     * Pass in a valid language, test toLocale.
     *
     * @param language the language string
     */
    private void assertValidToLocale(String language)
    {
        Locale locale = LocaleUtils.toLocale(language);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        //country and variant are empty
        assertTrue(locale.getCountry() == null || locale.getCountry().length() == 0);
        assertTrue(locale.getVariant() == null || locale.getVariant().length() == 0);
    }

    /**
     * Pass in a valid language, test toLocale.
     *
     * @param localeString to pass to toLocale()
     * @param language     of the resulting Locale
     * @param country      of the resulting Locale
     */
    private void assertValidToLocale(String localeString, String language, String country)
    {
        Locale locale = LocaleUtils.toLocale(localeString);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
        //variant is empty
        assertTrue(locale.getVariant() == null || locale.getVariant().length() == 0);
    }

    /**
     * Pass in a valid language, test toLocale.
     *
     * @param localeString to pass to toLocale()
     * @param language     of the resulting Locale
     * @param country      of the resulting Locale
     * @param variant      of the resulting Locale
     */
    private void assertValidToLocale(
            String localeString, String language,
            String country, String variant)
    {
        Locale locale = LocaleUtils.toLocale(localeString);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
        assertEquals(variant, locale.getVariant());

    }

    @Test
    public void toLocale_just_language()
    {
        assertEquals(null, LocaleUtils.toLocale(null));

        assertValidToLocale("us");
        assertValidToLocale("fr");
        assertValidToLocale("de");
        assertValidToLocale("zh");
        // Valid format but lang doesnt exist, should make instance anyway
        assertValidToLocale("qq");

        try
        {
            LocaleUtils.toLocale("Us");
            fail("Should fail if not lowercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("US");
            fail("Should fail if not lowercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("uS");
            fail("Should fail if not lowercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("u#");
            fail("Should fail if not lowercase");
        }
        catch (IllegalArgumentException iae)
        {
        }

        try
        {
            LocaleUtils.toLocale("u");
            fail("Must be 2 chars if less than 5");
        }
        catch (IllegalArgumentException iae)
        {
        }

        try
        {
            LocaleUtils.toLocale("uuu");
            fail("Must be 2 chars if less than 5");
        }
        catch (IllegalArgumentException iae)
        {
        }

        try
        {
            LocaleUtils.toLocale("uu_U");
            fail("Must be 2 chars if less than 5");
        }
        catch (IllegalArgumentException iae)
        {
        }
    }


    @Test
    public void toLocale_language_and_country()
    {
        assertValidToLocale("us_EN", "us", "EN");
        //valid though doesnt exist
        assertValidToLocale("us_ZH", "us", "ZH");

        try
        {
            LocaleUtils.toLocale("us-EN");
            fail("Should fail as not underscore");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("us_En");
            fail("Should fail second part not uppercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("us_en");
            fail("Should fail second part not uppercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("us_eN");
            fail("Should fail second part not uppercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("uS_EN");
            fail("Should fail first part not lowercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("us_E3");
            fail("Should fail second part not uppercase");
        }
        catch (IllegalArgumentException iae)
        {
        }
    }

    /**
     * Test toLocale() method.
     */
    @Test
    public void toLocale_with_variant()
    {
        assertValidToLocale("us_EN_A", "us", "EN", "A");

        assertValidToLocale("us_EN_a", "us", "EN", "a");
        assertValidToLocale("us_EN_SFsafdFDsdfF", "us", "EN", "SFsafdFDsdfF");

        try
        {
            LocaleUtils.toLocale("us_EN-a");
            fail("Should fail as not underscore");
        }
        catch (IllegalArgumentException iae)
        {
        }
        try
        {
            LocaleUtils.toLocale("uu_UU_");
            fail("Must be 3, 5 or 7+ in length");
        }
        catch (IllegalArgumentException iae)
        {
        }
    }

}
