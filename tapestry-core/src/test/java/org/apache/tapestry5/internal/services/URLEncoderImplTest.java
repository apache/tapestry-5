//  Copyright 2008 The Apache Software Foundation
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

import junit.framework.AssertionFailedError;
import org.apache.tapestry5.services.URLEncoder;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class URLEncoderImplTest extends Assert
{
    private final URLEncoder encoder = new URLEncoderImpl();

    @DataProvider
    public Object[][] encoder_inputs()
    {
        return new Object[][]
                {
                        { "simple", "simple" },
                        { "lettersAndNumbers123456", "lettersAndNumbers123456" },
                        { "simplePunctuation-_.", "simplePunctuation-_." },
                        { "a-lone-$", "a-lone-$$" },
                        { "a-slash-/", "a-slash-$002f" },
                        { "a-space_ _", "a-space_$0020_" },
                        { "unicode-\u027C-", "unicode-$027c-" },
                        { null, URLEncoderImpl.ENCODED_NULL },
                        { "", URLEncoderImpl.ENCODED_BLANK }
                };
    }

    @DataProvider
    public Object[][] failures()
    {
        return new Object[][]
                {
                        { "trailing-dollar-$",
                                "Input string 'trailing-dollar-$' is not valid; the '$' character at position 17 should be followed by another '$' or a four digit hex number (a unicode value)." },
                        { "not-hex-after-$xyzq-",
                                "Input string 'not-hex-after-$xyzq-' is not valid; the '$' character at position 15 should be followed by another '$' or a four digit hex number (a unicode value)." },
                        { "not-enough-after-$-ab",
                                "Input string 'not-enough-after-$-ab' is not valid; the '$' character at position 18 should be followed by another '$' or a four digit hex number (a unicode value)." },
                        { "unsafe-@-",
                                "Input string 'unsafe-@-' is not valid; the character '@' at position 8 is not valid." }
                };
    }

    @Test(dataProvider = "encoder_inputs")
    public void encode(String input, String expectedOutput)

    {
        String output = encoder.encode(input);

        assertEquals(output, expectedOutput);

        if (input != null && input.equals(output))
            assertSame(input, output, "When no change occurs, the input object should be passed through as is.");
    }

    @Test(dataProvider = "encoder_inputs")
    public void decode(String expectedDecodedOutput, String encodedInput)

    {
        String output = encoder.decode(encodedInput);

        assertEquals(output, expectedDecodedOutput);

        if (encodedInput.equals(output))
            assertSame(encodedInput, output,
                       "When no change occurs, the output object should be passed through as is.");
    }

    @Test(dataProvider = "failures")
    public void decode_failures(String input, String expectedMessage)
    {
        try
        {
            encoder.decode(input);
            throw new AssertionFailedError("This code should not be reachable.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), expectedMessage);
        }
    }
}
