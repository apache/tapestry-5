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
package org.apache.tapestry5.ioc.services;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Java12And13ServiceImpl implements Java12And13Service 
{
    
    private static final String DAY_OF_WEEK_STRING = "Work!";
    private static final String WEEKEND_STRING = "Rest!";
    private static final Logger LOGGER = LoggerFactory.getLogger(Java12And13ServiceImpl.class);
    
    public Java12And13ServiceImpl()
    {
        textBlocks();
        switchExpressions();
    }

    /**
     * Tests Text Blocks
     * https://docs.oracle.com/en/java/javase/17/language/text-blocks.html
     */
    @Override
    public void textBlocks() 
    {
        String string = """
                <html>
                    <body>
                        <p>Hello, Tapestry world!>/p>
                    </body>
                </html>
                """;
        LOGGER.info("Text block: " + string);
    }

    @Override
    public void switchExpressions() 
    {
        LocalDate date = LocalDate.now();
        String string = switch(date.getDayOfWeek())
        {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> 
            {
                LOGGER.info(DAY_OF_WEEK_STRING);
                yield DAY_OF_WEEK_STRING;
            }
            default -> 
            {
                LOGGER.info(WEEKEND_STRING);
                yield WEEKEND_STRING;
            }
        };
        LOGGER.info("Result: " + string);
    }

}