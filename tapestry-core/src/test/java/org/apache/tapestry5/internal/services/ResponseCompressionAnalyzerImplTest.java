// Copyright 2009 Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class ResponseCompressionAnalyzerImplTest extends Assert
{
    @DataProvider
    public Object[][] compression_search_data()
    {
        return new Object[][]
                {
                        { "is/inlist", false },
                        { "Is/InList", false },
                        { "nope/not", true },
                        { "is/InList;xyz", false }
                };
    }

    @Test(dataProvider = "compression_search_data")
    public void compression_search(String contentType, boolean expected)
    {
        List<String> configuration = CollectionFactory.newList("is/inlist");

        ResponseCompressionAnalyzer analyzer = new ResponseCompressionAnalyzerImpl(null, configuration, true);

        assertEquals(analyzer.isCompressable(contentType), expected);
    }
}
