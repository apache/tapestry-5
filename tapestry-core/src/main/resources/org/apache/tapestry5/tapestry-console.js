// Copyright 2009, 2010 The Apache Software Foundation
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
Tapestry.Logging = {
		
		debug: function(msg)
        {
			if (Tapestry.DEBUG_ENABLED)
				Tapestry.Logging.updateConsole("t-debug", msg);
        },
        
        warn: function(msg)
        {
			Tapestry.Logging.updateConsole("t-warn", msg);
        },
        
        error: function(msg)
        {
    		Tapestry.Logging.updateConsole("t-err", msg);
        },
        
        /** Formats a message and updates the console. The console is virtual
         *  when FireBug is not present, the messages float in the upper-left corner
         *  of the page and fade out after a short period.  The background color identifies
         *  the severity of the message (red for error, yellow for warnings, grey for debug).
         *  Messages can be clicked, which removes the immediately.
         *
         * When FireBug is present, the error(), warn() and debug() methods do not invoke
         * this; instead those functions are rewritten to write entries into the FireBug console.
         *
         * @param className to use for the div element in the console
         * @param message message template
         */
        updateConsole : function (className, message)
        {

            if (Tapestry.Logging.console == undefined)
            	Tapestry.Logging.console = Tapestry.Logging.createConsole("t-console");

            Tapestry.Logging.writeToConsole(Tapestry.Logging.console, className, message);
        },

        createConsole : function(className)
        {
            var body = $$("BODY").first();

            var console = new Element("div", { 'class': className });

            body.insert({ top: console });

            return console;
        },

        writeToConsole : function(console, className, message, slideDown)
        {
            var div = new Element("div", { 'class': className }).update(message).hide();

            console.insert({ top: div });

            new Effect.Appear(div, { duration: .25 });

            var effect = new Effect.Fade(div, { delay: Tapestry.CONSOLE_DURATION,
                afterFinish: function()
                {
            		Tapestry.remove(div);
                }});

            div.observe("click", function()
            {
                effect.cancel();
        		Tapestry.remove(div);
            });
        }
                
}