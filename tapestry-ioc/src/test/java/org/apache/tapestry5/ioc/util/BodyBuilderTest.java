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

package org.apache.tapestry5.ioc.util;

import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class BodyBuilderTest extends IOCTestCase
{
    @Test
    public void simple_nesting_and_indentation()
    {
        BodyBuilder b = new BodyBuilder();

        b.begin();
        b.addln("invoke();");
        b.end();

        assertEquals(b.toString(), join("{", "  invoke();", "}"));
    }

    @Test
    public void block_nesting()
    {
        BodyBuilder b = new BodyBuilder();

        b.begin();

        b.add("while(true)");
        b.begin();
        b.add("_i += 1;");
        b.end();

        b.end();

        assertEquals(b.toString(), join("{", "  while(true)", "  {", "    _i += 1;", "  }", "}"));
    }

    @Test
    public void addln_idents_subsequent_line()
    {
        BodyBuilder b = new BodyBuilder();

        b.begin();
        b.addln("invoke(fred);");
        b.addln("invoke(barney);");
        b.end();

        assertEquals(b.toString(), join("{", "  invoke(fred);", "  invoke(barney);", "}"));
    }

    @Test
    public void clear()
    {
        BodyBuilder b = new BodyBuilder();

        b.begin();
        b.add("fred");
        b.end();

        assertEquals(b.toString(), "{\n  fred\n}\n");

        b.clear();

        b.begin();
        b.add("barney");
        b.end();

        assertEquals(b.toString(), "{\n  barney\n}\n");
    }

    @Test
    public void add_with_format_and_args()
    {
        BodyBuilder b = new BodyBuilder();

        b.add("%s = %d;", "i", 3);

        assertEquals(b.toString(), "i = 3;");
    }

    @Test
    public void addln_with_format_and_args()
    {
        BodyBuilder b = new BodyBuilder();

        b.addln("%s = %d;", "i", 3);

        assertEquals(b.toString(), "i = 3;\n");
    }

    @Test
    public void indent_only_on_new_line()
    {
        BodyBuilder b = new BodyBuilder();

        b.begin();
        b.add("if");
        b.addln(" (debug)");
        b.add("  log.debug(\"%s\"", "foo");
        b.addln(");");
        b.addln("while (true)");
        b.begin();
        b.addln("if (%s > 10)", "i");
        b.addln("  return;");
        b.add("%s++;", "i");
        b.end();
        b.end();

        assertEquals(b.toString(), join("{", "  if (debug)", "    log.debug(\"foo\");", "  while (true)", "  {",
                                        "    if (i > 10)", "      return;", "    i++;", "  }", "}"

        ));

    }
}
