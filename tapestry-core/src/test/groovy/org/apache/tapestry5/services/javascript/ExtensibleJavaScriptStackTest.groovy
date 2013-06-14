// Copyright 2011-2013 The Apache Software Foundation
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

package org.apache.tapestry5.services.javascript;


import org.apache.tapestry5.Asset
import org.apache.tapestry5.services.AssetSource
import org.apache.tapestry5.test.TapestryTestCase
import org.testng.annotations.Test

/** @since 5.3 */
class ExtensibleJavaScriptStackTest extends TapestryTestCase {

    @Test
    void contributed_libraries_are_expanded() {
        def lib1path = '${lib1}/lib.js'
        Asset lib1 = mockAsset()
        def lib2path = '${lib2}/lib.js'
        Asset lib2 = mockAsset()

        AssetSource mockSource = mockAssetSource()

        expect(mockSource.getExpandedAsset(lib1path)).andReturn(lib1)
        expect(mockSource.getExpandedAsset(lib2path)).andReturn(lib2)

        replay()

        ExtensibleJavaScriptStack stack = new ExtensibleJavaScriptStack(mockSource, [
            new StackExtension(StackExtensionType.LIBRARY, lib1path),
            new StackExtension(StackExtensionType.LIBRARY, lib2path),
        ])

        assert stack.stacks.empty
        assert stack.javaScriptLibraries == [lib1, lib2]
        assert stack.stylesheets.empty
        assert stack.initialization == null

        verify()
    }

    @Test
    void contributed_stack() {
        AssetSource mockSource = mockAssetSource()

        replay()

        ExtensibleJavaScriptStack stack = new ExtensibleJavaScriptStack(mockSource, [
            new StackExtension(StackExtensionType.STACK, "stacka"),
            new StackExtension(StackExtensionType.STACK, "stackb"),
        ])

        assert stack.stacks == ["stacka", "stackb"]
        assert stack.javaScriptLibraries.empty

        verify()
    }

    @Test
    void contributed_modules() {

        replay()

        ExtensibleJavaScriptStack stack = new ExtensibleJavaScriptStack(null, [
            new StackExtension(StackExtensionType.MODULE, "t5/core/dom"),
            new StackExtension(StackExtensionType.MODULE, "magic"),
        ])

        assert stack.getModules() == ["t5/core/dom", "magic"]

        verify()

    }

    @Test
    void contributed_stylesheets_are_expanded() {
        def stylesheet1path = '${stylesheet1}/stylesheet.css'
        def stylesheet1URL = "/foo/ss1/stylesheet.css"
        Asset stylesheet1 = mockAsset()
        def stylesheet2path = '${stylesheet2}/stylesheet.js'
        def stylesheet2URL = '/foo/ss2/stylesheet.css'

        Asset stylesheet2 = mockAsset()

        AssetSource mockSource = mockAssetSource()

        expect(mockSource.getExpandedAsset(stylesheet1path)).andReturn(stylesheet1)
        expect(mockSource.getExpandedAsset(stylesheet2path)).andReturn(stylesheet2)

        expect(stylesheet1.toClientURL()).andReturn(stylesheet1URL).atLeastOnce()
        expect(stylesheet2.toClientURL()).andReturn(stylesheet2URL).atLeastOnce()

        replay()

        ExtensibleJavaScriptStack stack = new ExtensibleJavaScriptStack(mockSource, [
            new StackExtension(StackExtensionType.STYLESHEET, stylesheet1path),
            new StackExtension(StackExtensionType.STYLESHEET, stylesheet2path),
        ])

        assert stack.stacks.empty
        assert stack.javaScriptLibraries.empty
        assert stack.stylesheets == [
            new StylesheetLink(stylesheet1),
            new StylesheetLink(stylesheet2)
        ]

        assert stack.initialization == null

        verify()
    }

    @Test
    void initializations_are_combined() {
        ExtensibleJavaScriptStack stack = new ExtensibleJavaScriptStack(null, [
            new StackExtension(StackExtensionType.INITIALIZATION, "doThis();"),
            new StackExtension(StackExtensionType.INITIALIZATION, "doThat();"),
        ])

        assert stack.initialization == "doThis();\ndoThat();"
    }
}
