// Copyright 2009 The Apache Software Foundation
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

import static org.easymock.EasyMock.isA;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.AssetSource;
import org.testng.annotations.Test;

public class ClientInfrastructureImplTest  extends InternalBaseTestCase
{
    @Test
    public void production_mode() throws Exception
    {
        SymbolSource symbolSource = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        ThreadLocale threadLocale = mockThreadLocale();
        
        train_constructor(symbolSource, assetSource, threadLocale);

        replay();
        
        ClientInfrastructureImpl infrastructure = new ClientInfrastructureImpl(symbolSource, assetSource, threadLocale, false);

        List<Asset> stack = infrastructure.getJavascriptStack();
        
        List<Asset> stylesheetStack = infrastructure.getStylesheetStack();
        
        verify();
        
        //blackbird is the only one asset to be excluded in production mode
        //for now it is ok to check only sizes of stacks
        assertEquals(stack.size(), 5);
        
        assertEquals(stylesheetStack.size(), 1);
    }
    
    @Test
    public void test_mode() throws Exception
    {
        SymbolSource symbolSource = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        ThreadLocale threadLocale = mockThreadLocale();
        
        train_constructor(symbolSource, assetSource, threadLocale);

        replay();
        
        ClientInfrastructureImpl infrastructure = new ClientInfrastructureImpl(symbolSource, assetSource, threadLocale, true);

        List<Asset> javascriptStack = infrastructure.getJavascriptStack();
        
        List<Asset> stylesheetStack = infrastructure.getStylesheetStack();
        
        verify();
        
        //blackbird is the only one asset to be excluded in production mode
        //for now it is ok to check only sizes of stacks
        assertEquals(javascriptStack.size(), 6);
        
        assertEquals(stylesheetStack.size(), 2);
    }
    
    private void train_constructor(SymbolSource symbolSource, AssetSource assetSource, ThreadLocale threadLocale)
    {
        expect(symbolSource.expandSymbols(isA(String.class))).andReturn("expanded").anyTimes();
        expect(assetSource.getAsset(null, "expanded",null)).andReturn(mockAsset()).anyTimes();
        train_getLocale(threadLocale, Locale.ENGLISH);
        expect(assetSource.getAsset(null, "org/apache/tapestry5/tapestry-messages.js",Locale.ENGLISH)).andReturn(mockAsset());
    }
}
