// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;

public class DateFieldStack implements JavaScriptStack
{
    private final ThreadLocale threadLocale;

    private final boolean compactJSON;

    private final List<Asset> javaScriptStack;

    private final List<StylesheetLink> stylesheetStack;

    public DateFieldStack(ThreadLocale threadLocale, @Symbol(SymbolConstants.COMPACT_JSON)
    boolean compactJSON, final AssetSource assetSource)
    {
        this.threadLocale = threadLocale;
        this.compactJSON = compactJSON;

        Mapper<String, Asset> pathToAsset = new Mapper<String, Asset>()
        {
            public Asset map(String path)
            {
                return assetSource.getExpandedAsset(path);
            }
        };

        Mapper<String, StylesheetLink> pathToStylesheetLink = F.combine(pathToAsset,
                TapestryInternalUtils.assetToStylesheetLink);

        javaScriptStack = F
                .flow("${tapestry.datepicker}/js/datepicker.js", "org/apache/tapestry5/corelib/components/datefield.js")
                .map(pathToAsset).toList();

        stylesheetStack = F.flow("${tapestry.datepicker}/css/datepicker.css").map(pathToStylesheetLink).toList();
    }

    public String getInitialization()
    {
        Locale locale = threadLocale.getLocale();

        JSONObject spec = new JSONObject();

        DateFormatSymbols symbols = new DateFormatSymbols(locale);

        spec.put("months", new JSONArray((Object[])symbols.getMonths()));

        StringBuilder days = new StringBuilder();

        String[] weekdays = symbols.getWeekdays();

        Calendar c = Calendar.getInstance(locale);

        int firstDay = c.getFirstDayOfWeek();

        // DatePicker needs them in order from monday to sunday.

        for (int i = Calendar.MONDAY; i <= Calendar.SATURDAY; i++)
        {
            days.append(weekdays[i].substring(0, 1));
        }

        days.append(weekdays[Calendar.SUNDAY].substring(0, 1));

        spec.put("days", days.toString().toLowerCase(locale));

        // DatePicker expects 0 to be monday. Calendar defines SUNDAY as 1, MONDAY as 2, etc.

        spec.put("firstDay", firstDay == Calendar.SUNDAY ? 6 : firstDay - 2);

        // TODO: Skip localization if locale is English?

        return String.format("Tapestry.DateField.initLocalization(%s);", spec.toString(compactJSON));
    }

    public List<Asset> getJavaScriptLibraries()
    {
        return javaScriptStack;
    }

    public List<StylesheetLink> getStylesheets()
    {
        return stylesheetStack;
    }

    public List<String> getStacks()
    {
        return Collections.emptyList();
    }
}
